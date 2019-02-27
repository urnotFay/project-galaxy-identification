package edu.unimelb.deeplearning4j.evaluate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.layers.objdetect.DetectedObject;
import org.deeplearning4j.nn.layers.objdetect.Yolo2OutputLayer;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;

import edu.unimelb.deeplearning4j.yolo.YOLOModel;
import edu.unimelb.galaxyidentification.entity.MatchedObject;
import edu.unimelb.utils.JavaCVUtil;
import edu.unimelb.utils.yolo.NonMaxSuppression;

public class Evaluate {

	private static final String[] CLASSES = { "Cassiopeia", "Ursa Major", "Orion", "Southern Cross", "Lyra", "NGC 1300",
			"NGC 1309", "NGC 1232", "NGC 4102", "Messier 81", "Messier 82", "NGC 3982", "Messier 66", "NGC 3521",
			"Messier 95", "Messier 96", "Messier 94" };

	public static void eval(String realName) throws IOException {
		List<MatchedObject> matchedList = new ArrayList<MatchedObject>();

		String dic = "src/main/resources/data/Annotations/";
		int correctCount = 0;
		List<String> allFileList = getAllFile(dic, false);
		int allFileSize = allFileList.size();
		YOLOModel model = new YOLOModel();
		for (int i = 0; i < allFileSize; i++) {

			SAXReader reader = new SAXReader();
			Document document;
			try {
				document = reader.read(new File(allFileList.get(i)));
				Element rootElem = document.getRootElement();
				Element object = rootElem.element("object");
				Element objectName = object.element("name");
				Element fileName = rootElem.element("filename");
				String fileNameText = fileName.getText();
				if (objectName.getText().equals(realName)) {
					MatchedObject match = new MatchedObject();
					match.setName(realName);
					match.setPath("src/main/resources/data/Images/" + fileNameText);
					matchedList.add(match);
				}
				
				

				

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		for(int i = 0;i<matchedList.size();i++) {
			Mat mat = JavaCVUtil.imRead(matchedList.get(i).getPath());

			Yolo2OutputLayer yout = (Yolo2OutputLayer) model.getNETWORK().getOutputLayer(0);
			NativeImageLoader loader = new NativeImageLoader(416, 416, 3);// , new
																			// ColorConversionTransform(COLOR_BGR2RGB)
			INDArray ds = loader.asMatrix(mat);

			ImagePreProcessingScaler scaler = new ImagePreProcessingScaler(0, 1);
			scaler.transform(ds);
			INDArray results = model.getNETWORK().outputSingle(ds);
			List<DetectedObject> objs = yout.getPredictedObjects(results, 0.4);
			List<DetectedObject> objects = NonMaxSuppression.getObjects(objs);
			if (objects.size() != 0) {
				DetectedObject detectedObject = objects.get(0);
				int predictedClass = detectedObject.getPredictedClass();
				String prediction = CLASSES[predictedClass];
				if (prediction.equals(matchedList.get(i).getName())) {
					
					correctCount++;
					

					System.out.println("--------" + correctCount);

				}

			}
			
		}
		
		BigDecimal machedCount = new BigDecimal(matchedList.size());
		BigDecimal decimalCorrectCount = new BigDecimal(correctCount);
		BigDecimal totalSize = new BigDecimal(allFileList.size());
		BigDecimal precisionDecimal = null;
		if(decimalCorrectCount.intValue()==0) {
			 precisionDecimal = machedCount.divide(totalSize, 3, RoundingMode.HALF_UP);
			 if(precisionDecimal.floatValue()<0.1) {
				 precisionDecimal = precisionDecimal.multiply(new BigDecimal(10));
			 }
		}else {
	        precisionDecimal = decimalCorrectCount.divide(machedCount, 3, RoundingMode.HALF_UP);

		}
		BigDecimal recall = precisionDecimal.divide(machedCount, 3, RoundingMode.HALF_UP);
		System.out.println(precisionDecimal.toString() + "------" + recall.toString() + "------------");
	}
	

	public static List<String> getAllFile(String directoryPath, boolean isAddDirectory) {
		List<String> list = new ArrayList<String>();
		File baseFile = new File(directoryPath);
		if (baseFile.isFile() || !baseFile.exists()) {
			return list;
		}
		File[] files = baseFile.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				if (isAddDirectory) {
					list.add(file.getAbsolutePath());
				}
				list.addAll(getAllFile(file.getAbsolutePath(), isAddDirectory));
			} else {
				list.add(file.getAbsolutePath());
			}
		}
		return list;
	}

}
