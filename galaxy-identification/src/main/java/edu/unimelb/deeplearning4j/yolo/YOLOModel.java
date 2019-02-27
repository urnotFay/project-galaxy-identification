package edu.unimelb.deeplearning4j.yolo;

import static org.bytedeco.javacpp.opencv_imgproc.putText;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.objdetect.ObjectDetectionRecordReader;
import org.datavec.image.recordreader.objdetect.impl.SvhnLabelProvider;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.fetchers.DataSetType;
import org.deeplearning4j.datasets.fetchers.SvhnDataFetcher;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.layers.objdetect.DetectedObject;
import org.deeplearning4j.nn.layers.objdetect.Yolo2OutputLayer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.MultiDataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.exception.ND4JException;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.nd4j.linalg.api.concurrency.AffinityManager;

import edu.unimelb.utils.yolo.NonMaxSuppression;
import edu.unimelb.utils.yolo.YoloUtil;

public class YOLOModel {

    private static final Logger log = LoggerFactory.getLogger(YOLOModel.class);

	private static final String[] CLASSES = { "Cassiopeia", "Ursa Major", "Orion", "Southern Cross", "Lyra", "NGC 1300",
			"NGC 1309", "NGC 1232", "NGC 4102", "Messier 81", "Messier 82", "NGC 3982", "Messier 66", "NGC 3521",
			"Messier 95", "Messier 96", "Messier 94" };

	private final int IMAGE_INPUT_W = 416;
	private final int IMAGE_INPUT_H = 416;
	private final int CHANNELS = 3;
	private final int GRID_W = 13;
	private final int GRID_H = 13;
	private static ComputationGraph NETWORK;
	private Iterator<File> fileIterator;

	public YOLOModel() {

		try {
			NETWORK = YoloUtil.trainAndWriteYolo();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private List<String> drawBoxes(Mat image, List<DetectedObject> objects) {
		String prediction = "";
		List<String> predictionList = new ArrayList<String>();
		for (DetectedObject obj : objects) {
			double[] xy1 = obj.getTopLeftXY();
			double[] xy2 = obj.getBottomRightXY();
			int predictedClass = obj.getPredictedClass();
			System.out.println("Predicted class " + CLASSES[predictedClass]);
			int x1 = (int) Math.round(IMAGE_INPUT_W * xy1[0] / GRID_W);
			int y1 = (int) Math.round(IMAGE_INPUT_H * xy1[1] / GRID_H);
			int x2 = (int) Math.round(IMAGE_INPUT_W * xy2[0] / GRID_W);
			int y2 = (int) Math.round(IMAGE_INPUT_H * xy2[1] / GRID_H);
			prediction = CLASSES[predictedClass];
			rectangle(image, new opencv_core.Point(x1, y1), new opencv_core.Point(x2, y2), opencv_core.Scalar.RED);
			putText(image, CLASSES[predictedClass], new opencv_core.Point(x1 + 2, y2 - 2), 1, .8,
					opencv_core.Scalar.RED);
			predictionList.add(prediction);

		}
		return predictionList;
	}

  
	public List<String> detectGalaxy(Mat image, double detectionthreshold) {
		List<String> list = new ArrayList<String>();

		Yolo2OutputLayer yout = (Yolo2OutputLayer) NETWORK.getOutputLayer(0);
		NativeImageLoader loader = new NativeImageLoader(IMAGE_INPUT_W, IMAGE_INPUT_H, CHANNELS);// , new
																									// ColorConversionTransform(COLOR_BGR2RGB)
		INDArray ds = null;
		try {
			ds = loader.asMatrix(image);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		ImagePreProcessingScaler scaler = new ImagePreProcessingScaler(0, 1);
		scaler.transform(ds);
		INDArray results = NETWORK.outputSingle(ds);
		List<DetectedObject> objs = yout.getPredictedObjects(results, detectionthreshold);
		List<DetectedObject> objects = NonMaxSuppression.getObjects(objs);

		list = drawBoxes(image, objects);// use objs to see the use of the NonMax Suppression algorithm

		return list;
	}

	public ComputationGraph getNETWORK() {
		return NETWORK;
	}
   
}
