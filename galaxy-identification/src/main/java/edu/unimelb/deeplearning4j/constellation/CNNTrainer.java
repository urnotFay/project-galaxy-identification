package edu.unimelb.deeplearning4j.constellation;

import static java.lang.Math.toIntExact;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.datavec.image.transform.FlipImageTransform;
import org.datavec.image.transform.ImageTransform;
import org.datavec.image.transform.PipelineImageTransform;
import org.datavec.image.transform.WarpImageTransform;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.distribution.Distribution;
import org.deeplearning4j.nn.conf.distribution.GaussianDistribution;
import org.deeplearning4j.nn.conf.distribution.NormalDistribution;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.LocalResponseNormalization;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.primitives.Pair;
import org.nd4j.linalg.schedule.MapSchedule;
import org.nd4j.linalg.schedule.ScheduleType;
import org.nd4j.linalg.schedule.StepSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CNNTrainer {

	protected static final Logger log = LoggerFactory.getLogger(CNNTrainer.class);

	private static final String MODEL_FILE_NAME = "Constellation.bin";
	private static final String DATA_DIR = "src/main/resources/data/Constellation";
	protected static int height = 300;
	protected static int width = 300;
	protected static int channels = 3;
	protected static int batchSize = 20;

	protected static long seed = 42;
	protected static Random rng = new Random(seed);
	protected static int epochs = 50;
	protected static double splitTrainTest = 0.8;
	protected static boolean save = false;
	protected static int maxPathsPerLabel = 18;

	private static int numLabels;

	public static MultiLayerNetwork train() throws Exception {

		MultiLayerNetwork model;
		File modelFile = new File(MODEL_FILE_NAME);

		if (modelFile.exists()) {

			model = ModelSerializer.restoreMultiLayerNetwork(MODEL_FILE_NAME);
		} else {
//			
			ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
			File mainPath = new File(DATA_DIR);
			FileSplit fileSplit = new FileSplit(mainPath, NativeImageLoader.ALLOWED_FORMATS, rng);
			int numExamples = toIntExact(fileSplit.length());
			numLabels = fileSplit.getRootDir().listFiles(File::isDirectory).length; // This only works if your root is
																					// clean: only label subdirs.
			BalancedPathFilter pathFilter = new BalancedPathFilter(rng, labelMaker, numExamples, numLabels,
					maxPathsPerLabel);

			InputSplit[] inputSplit = fileSplit.sample(pathFilter, splitTrainTest, 1 - splitTrainTest);
			InputSplit trainData = inputSplit[0];

			ImageTransform flipTransform1 = new FlipImageTransform(rng);
			ImageTransform flipTransform2 = new FlipImageTransform(new Random(123));
			ImageTransform warpTransform = new WarpImageTransform(rng, 42);
			boolean shuffle = false;
			List<Pair<ImageTransform, Double>> pipeline = Arrays.asList(new Pair<>(flipTransform1, 0.9),
					new Pair<>(flipTransform2, 0.8), new Pair<>(warpTransform, 0.5));

			ImageTransform transform = new PipelineImageTransform(pipeline, shuffle);
			/**
			 * Data Setup -> normalization - how to normalize images and generate large
			 * dataset to train on
			 **/
			DataNormalization scaler = new ImagePreProcessingScaler(0, 1);

			model = buildNetwork();

			model.init();

			ImageRecordReader recordReader = new ImageRecordReader(height, width, channels, labelMaker);
			DataSetIterator dataIter;

			// Train with transformations
			recordReader.initialize(trainData, transform);
			dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, numLabels);
			scaler.fit(dataIter);
			dataIter.setPreProcessor(scaler);
			model.fit(dataIter, epochs);

			// Example on how to get predict results with trained model. Result for first
			// example in minibatch is printed
			dataIter.reset();
			DataSet testDataSet = dataIter.next();
			List<String> allClassLabels = recordReader.getLabels();
			int labelIndex = testDataSet.getLabels().argMax(1).getInt(0);
			int[] predictedClasses = model.predict(testDataSet.getFeatures());
			String expectedResult = allClassLabels.get(labelIndex);
			String modelPrediction = allClassLabels.get(predictedClasses[0]);
			System.out.print("\nFor a single example that is labeled " + expectedResult + " the model predicted "
					+ modelPrediction + "\n\n");

			ModelSerializer.writeModel(model, MODEL_FILE_NAME, true);

			log.info("****************Model finished********************");

		}

		return model;
	}

	private static MultiLayerNetwork buildNetwork() throws Exception {
		double nonZeroBias = 1;
		double dropOut = 0.5;

		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(seed)
				.weightInit(WeightInit.DISTRIBUTION).dist(new NormalDistribution(0.0, 0.01)).activation(Activation.RELU)
				.updater(new Nesterovs(new StepSchedule(ScheduleType.ITERATION, 1e-2, 0.1, 100000), 0.9))
				.biasUpdater(new Nesterovs(new StepSchedule(ScheduleType.ITERATION, 2e-2, 0.1, 100000), 0.9))
				.gradientNormalization(GradientNormalization.RenormalizeL2PerLayer) // normalize to prevent vanishing or
																					// exploding gradients
				.l2(5 * 1e-4).list()
				.layer(0,
						convInit("cnn1", channels, 96, new int[] { 11, 11 }, new int[] { 4, 4 }, new int[] { 3, 3 }, 0))
				.layer(1, new LocalResponseNormalization.Builder().name("lrn1").build())
				.layer(2, maxPool("maxpool1", new int[] { 3, 3 }))
				.layer(3, conv5x5("cnn2", 256, new int[] { 1, 1 }, new int[] { 2, 2 }, nonZeroBias))
				.layer(4, new LocalResponseNormalization.Builder().name("lrn2").build())
				.layer(5, maxPool("maxpool2", new int[] { 3, 3 })).layer(6, conv3x3("cnn3", 384, 0))
				.layer(7, conv3x3("cnn4", 384, nonZeroBias)).layer(8, conv3x3("cnn5", 256, nonZeroBias))
				.layer(9, maxPool("maxpool3", new int[] { 3, 3 }))
				.layer(10, fullyConnected("ffn1", 4096, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)))
				.layer(11, fullyConnected("ffn2", 4096, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)))
				.layer(12,
						new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).name("output")
								.nOut(numLabels).activation(Activation.SOFTMAX).build())
				.backprop(true).pretrain(false).setInputType(InputType.convolutional(height, width, channels)).build();

		return new MultiLayerNetwork(conf);

	}

	private static ConvolutionLayer convInit(String name, int in, int out, int[] kernel, int[] stride, int[] pad,
			double bias) {
		return new ConvolutionLayer.Builder(kernel, stride, pad).name(name).nIn(in).nOut(out).biasInit(bias).build();
	}

	private static ConvolutionLayer conv3x3(String name, int out, double bias) {
		return new ConvolutionLayer.Builder(new int[] { 3, 3 }, new int[] { 1, 1 }, new int[] { 1, 1 }).name(name)
				.nOut(out).biasInit(bias).build();
	}

	private static ConvolutionLayer conv5x5(String name, int out, int[] stride, int[] pad, double bias) {
		return new ConvolutionLayer.Builder(new int[] { 5, 5 }, stride, pad).name(name).nOut(out).biasInit(bias)
				.build();
	}

	private static SubsamplingLayer maxPool(String name, int[] kernel) {
		return new SubsamplingLayer.Builder(kernel, new int[] { 2, 2 }).name(name).build();
	}

	private static DenseLayer fullyConnected(String name, int out, double bias, double dropOut, Distribution dist) {
		return new DenseLayer.Builder().name(name).nOut(out).biasInit(bias).dropOut(dropOut).dist(dist).build();
	}

}
