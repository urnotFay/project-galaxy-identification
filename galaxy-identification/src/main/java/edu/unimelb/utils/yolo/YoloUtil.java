package edu.unimelb.utils.yolo;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

import org.datavec.api.io.filters.RandomPathFilter;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.objdetect.ObjectDetectionRecordReader;
import org.datavec.image.recordreader.objdetect.impl.VocLabelProvider;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ConvolutionMode;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.objdetect.Yolo2OutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.transferlearning.FineTuneConfiguration;
import org.deeplearning4j.nn.transferlearning.TransferLearning;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.deeplearning4j.zoo.model.TinyYOLO;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.learning.config.RmsProp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @ClassName: YoloTrainer
 * @Description: TODO
 * @author Xiaorong Yang
 * @date Jan 17, 2019 3:56:48 PM
 *
 */
public class YoloUtil {

	private static final int INPUT_WIDTH = 416;
	private static final int INPUT_HEIGHT = 416;
	private static final int CHANNELS = 3;

	private static final int GRID_WIDTH = 13;
	private static final int GRID_HEIGHT = 13;
	private static final int CLASSES_NUMBER = 17 ;
	private static final int BOXES_NUMBER = 5;
    private static final double[][] PRIOR_BOXES = {{2, 5}, {2.5, 6}, {3, 7}, {3.5, 8}, {4, 9}};

    private static final int BATCH_SIZE = 10;
	private static final int EPOCHS = 25 ;

	private static final double LEARNIGN_RATE = 0.0001;
	private static final int SEED = 7854;
	private static final double LAMDBA_COORD = 1.0;
	private static final double LAMDBA_NO_OBJECT = 0.5;
	
	private static final String DATA_DIR = "src/main/resources/data";
	private static final String MODEL_FILE_NAME = "model.data";
	
    private static final Logger log = LoggerFactory.getLogger(YoloUtil.class);


	public static ComputationGraph trainAndWriteYolo() throws IOException {
		ComputationGraph model;

		File modelFile = new File(MODEL_FILE_NAME);
		if (modelFile.exists()) {

			model = ModelSerializer.restoreComputationGraph(MODEL_FILE_NAME);

		} else {
			Random random = new Random(SEED);
			//Initialize the user interface backend, it is just as tensorboard.
	        //it starts at http://localhost:9000
	        UIServer uiServer = UIServer.getInstance();

	        //Configure where the network information (gradients, score vs. time etc) is to be stored. Here: store in memory.
	        StatsStorage statsStorage = new InMemoryStatsStorage();


	        //Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
	        uiServer.attach(statsStorage);

	        File imageDir = new File(DATA_DIR, "Images");



			RandomPathFilter pathFilter = new RandomPathFilter(random) {
				@Override
				protected boolean accept(String name) {
					name = name.replace("/Images/", "/Annotations/").replace(".jpg", ".xml");
					try {
						return new File(new URI(name)).exists();
					} catch (URISyntaxException ex) {
						throw new RuntimeException(ex);
					}
				}
			};

			InputSplit[] data = new FileSplit(imageDir, NativeImageLoader.ALLOWED_FORMATS, random).sample(pathFilter,
					0.8, 0.2);
			InputSplit trainData = data[0];
			InputSplit testData = data[1];
			ObjectDetectionRecordReader recordReaderTrain = new ObjectDetectionRecordReader(INPUT_HEIGHT, INPUT_WIDTH,
					CHANNELS, GRID_HEIGHT, GRID_WIDTH, new VocLabelProvider(DATA_DIR));
			recordReaderTrain.initialize(trainData);

			ObjectDetectionRecordReader recordReaderTest = new ObjectDetectionRecordReader(INPUT_HEIGHT, INPUT_WIDTH,
					CHANNELS, GRID_HEIGHT, GRID_WIDTH, new VocLabelProvider(DATA_DIR));
			recordReaderTest.initialize(testData);

			RecordReaderDataSetIterator train = new RecordReaderDataSetIterator(recordReaderTrain, BATCH_SIZE, 1, 1,
					true);
			train.setPreProcessor(new ImagePreProcessingScaler(0, 1));

			RecordReaderDataSetIterator test = new RecordReaderDataSetIterator(recordReaderTest, 1, 1, 1, true);
			test.setPreProcessor(new ImagePreProcessingScaler(0, 1));

            ComputationGraph pretrained = (ComputationGraph)TinyYOLO.builder().build().initPretrained();
            INDArray priors = Nd4j.create(PRIOR_BOXES);
    		FineTuneConfiguration fineTuneConf = new FineTuneConfiguration.Builder().seed(SEED)
    				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
    				.gradientNormalization(GradientNormalization.RenormalizeL2PerLayer).gradientNormalizationThreshold(1.0)
    				.updater(new RmsProp(LEARNIGN_RATE)).activation(Activation.IDENTITY).miniBatch(true)
    				.trainingWorkspaceMode(WorkspaceMode.ENABLED).build();

    		 model = new TransferLearning.GraphBuilder(pretrained).fineTuneConfiguration(fineTuneConf)
    				.setInputTypes(InputType.convolutional(INPUT_HEIGHT, INPUT_WIDTH, CHANNELS))
    				.removeVertexKeepConnections("conv2d_9")
    				.addLayer("convolution2d_9",
    						new ConvolutionLayer.Builder(1, 1).nIn(1024).nOut(BOXES_NUMBER * (5 + CLASSES_NUMBER))
    								.stride(1, 1).convolutionMode(ConvolutionMode.Same).weightInit(WeightInit.UNIFORM)
    								.hasBias(false).activation(Activation.IDENTITY).build(),
    						"leaky_re_lu_8")
    				.addLayer("outputs", new Yolo2OutputLayer.Builder().lambbaNoObj(LAMDBA_NO_OBJECT)
    						.lambdaCoord(LAMDBA_COORD).boundingBoxPriors(priors).build(), "convolution2d_9")
    				.setOutputs("outputs").build();
            System.out.println(model.summary(InputType.convolutional(INPUT_HEIGHT, INPUT_WIDTH, CHANNELS)));
	        log.info("Train model...");
//	        model.setListeners(new ScoreIterationListener(1));//print score after each iteration on stout 
	        model.setListeners(new StatsListener(statsStorage));
	       


	        for (int i = 0; i <EPOCHS; i++) {
	            train.reset();
	            while (train.hasNext()) {
	                model.fit(train.next());
	              
	                
	            }
	            log.info("*** Completed epoch {} ***", i);
		       

	        }
	        
	        log.info("*** Saving Model ***");
	        ModelSerializer.writeModel(model, "model.data", true);
	        log.info("*** Training Done ***");
	        
		
		}

		return model;
	}

}
