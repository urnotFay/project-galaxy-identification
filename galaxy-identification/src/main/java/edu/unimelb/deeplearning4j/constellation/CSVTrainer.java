package edu.unimelb.deeplearning4j.constellation;

import static java.lang.Math.toIntExact;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.api.util.ClassPathResource;
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
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.primitives.Pair;
import org.nd4j.linalg.schedule.MapSchedule;
import org.nd4j.linalg.schedule.ScheduleType;
import org.nd4j.linalg.schedule.StepSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVTrainer {

	protected static final Logger log = LoggerFactory.getLogger(CSVTrainer.class);

	private static final String MODEL_FILE_NAME = "Constellation.data";
	private static final String DATA_DIR = "src/main/resources/data/Constellation.csv";
	private static  int labelIndex = 2; // 5 values in each row of the iris.txt CSV: 4 input features followed by an
						// integer label (class) index. Labels are the 5th value (index 4) in each row
	private static int numClasses = 3; // 3 classes (types of iris flowers) in the iris data set. Classes have integer
						// values 0, 1 or 2
	private static int batchSize = 150;

	public static MultiLayerNetwork train() throws Exception {

		MultiLayerNetwork model;
		File modelFile = new File(MODEL_FILE_NAME);

		if (modelFile.exists()) {

			model = ModelSerializer.restoreMultiLayerNetwork(MODEL_FILE_NAME);
		} else {
//			
	    	 RecordReader recordReader = readCSVDataset(DATA_DIR,batchSize,labelIndex,numClasses);
	    	 DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader,batchSize,labelIndex,numClasses);
	         DataSet allData = iterator.next();
	         allData.shuffle();
	         SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.65);  //Use 65% of data for training

	         DataSet trainingData = testAndTrain.getTrain();
	         DataSet testData = testAndTrain.getTest();

	         //We need to normalize our data. We'll use NormalizeStandardize (which gives us mean 0, unit variance):
	         DataNormalization normalizer = new NormalizerStandardize();
	         normalizer.fit(trainingData);           //Collect the statistics (mean/stdev) from the training data. This does not modify the input data
	         normalizer.transform(trainingData);     //Apply normalization to the training data
	         normalizer.transform(testData); 

	    	 model = buildNetwork(trainingData);

			
	         ModelSerializer.writeModel(model, MODEL_FILE_NAME, true);
	        

		}

		return model;
	}

	private static MultiLayerNetwork buildNetwork(DataSet trainingData) throws Exception {
		final int numInputs = 2;
        int outputNum = 3;
        long seed = 6;
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .activation(Activation.TANH)
                .weightInit(WeightInit.XAVIER)
                .updater(new Sgd(0.1))
                .l2(1e-4)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(3)
                    .build())
                .layer(1, new DenseLayer.Builder().nIn(3).nOut(3)
                    .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                    .activation(Activation.SOFTMAX)
                    .nIn(3).nOut(outputNum).build())
                .backprop(true).pretrain(false)
                .setInputType(InputType.convolutionalFlat(20, 20, 3)) // InputType.convolutional for normal image
                .build();

            //run the model
            MultiLayerNetwork model = new MultiLayerNetwork(conf);
            model.init();
            model.setListeners(new ScoreIterationListener(100));

            for(int i=0; i<1000; i++ ) {
                model.fit(trainingData);
            }


		return model;

	}


	private static RecordReader readCSVDataset(String csvFileClasspath, int batchSize, int labelIndex, int numClasses)
			throws IOException, InterruptedException {

		RecordReader rr = new CSVRecordReader();
		rr.initialize(new FileSplit(new File(csvFileClasspath)));

		return rr;

	}

}
