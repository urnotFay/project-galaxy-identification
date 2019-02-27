package edu.unimelb.deeplearning4j.constellation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.CvType;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter.ToMat;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.layers.OutputLayer;
import org.deeplearning4j.nn.layers.objdetect.DetectedObject;
import org.deeplearning4j.nn.layers.objdetect.Yolo2OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.workspace.LayerWorkspaceMgr;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.opencv.core.Core;

import com.csvreader.CsvReader;

import edu.unimelb.utils.yolo.NonMaxSuppression;

public class CNNModel {
	public static int reluIndex = 1;
	public static int paddingIndex = 1;
	private static MultiLayerNetwork NETWORK;
	private static final String[] CLASSES = { "Canis Major", "Ursa Major" };
	private static final String dataPath = "src/main/resources/data/hygdata_v3.csv";

	
	public CNNModel() {
		try {
			NETWORK = CNNTrainer.train();
//			computationGraph = CSVTrainer.train();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void recogConsterllation(Mat image, String path, double detectionthreshold) throws Exception {

		NativeImageLoader loader = new NativeImageLoader(300, 300, 3, false);// , new

		INDArray ds = null;
		ds = loader.asMatrix(image);
		ImagePreProcessingScaler scaler = new ImagePreProcessingScaler(0, 1);
		scaler.transform(ds);
//		INDArray results = NETWORK.output(ds);
		int[] predict = NETWORK.predict(ds);
		String predictClass = CLASSES[predict[0]];
//		Map<String, List<Constellation>> constellationData = getConstellationData(image, predictClass);
		
		
		

	}

	private Map<String, List<Constellation>> getConstellationData(Mat image, String predictClass) throws Exception {
        CsvReader csvReader = new CsvReader(dataPath);
     
        Map<String, List<Constellation>> dataMap = new HashMap<String, List<Constellation>>();
        
        csvReader.readHeaders();
        while (csvReader.readRecord()){
            List<Constellation> dataList = new ArrayList<Constellation>();
            
            String name = csvReader.get("Name");
            if(name.equals(predictClass)) {
            	Constellation con = new Constellation();
            	String ra = csvReader.get("RA");
                String dec = csvReader.get("DEC");
                con.setName(name);
                con.setRa(ra);
                con.setDec(dec);
                dataList.add(con);
                dataMap.put(predictClass, dataList);

            }
            
        }
        return dataMap;        

	}

	public void draw(Mat image) {
//		img_gray = cv2.cvtColor(self.image, cv2.COLOR_RGB2GRAY)
//		        self.del_img = self.image.copy()
//		        self.first_delete = True
//		        self.tmp_stars = []
		
		
		


	
	}
	/**
	 * 
	  * @Title: drawLine
	  * @Description: TODO    
	  * @return void    
	  * @throws
	 */
	public void drawLine() {
		


	}
	

	public MultiLayerNetwork getNETWORK() {
		return NETWORK;
	}

}
