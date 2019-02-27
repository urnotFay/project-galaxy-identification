package edu.unimelb.deeplearning4j.constellation;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

import edu.unimelb.utils.JavaCVUtil;

public class ConsterllationRecognition {
	
    private static final NativeImageLoader LOADER = new NativeImageLoader(96, 96, 3);



	public static void main(String[]args) {
		CNNModel cnn = new CNNModel();
		
		try {
			String filePath = "src/main/resources/data/Sample/0.jpg";
			Mat mat = JavaCVUtil.imRead(filePath);
			cnn.recogConsterllation(mat,filePath, 0.4);
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static INDArray readImage(String imagePath) throws IOException{
		 Mat mat = JavaCVUtil.imRead(imagePath);
		 INDArray indArray = LOADER.asMatrix(mat);
		 return transpose(indArray);
		
		
	}
	 
    private static INDArray transpose(INDArray indArray1) {
        INDArray one = Nd4j.create(new int[]{1, 96, 96});
        one.assign(indArray1.get(NDArrayIndex.point(0), NDArrayIndex.point(2)));
        INDArray two = Nd4j.create(new int[]{1, 96, 96});
        two.assign(indArray1.get(NDArrayIndex.point(0), NDArrayIndex.point(1)));
        INDArray three = Nd4j.create(new int[]{1, 96, 96});
        three.assign(indArray1.get(NDArrayIndex.point(0), NDArrayIndex.point(0)));
        return Nd4j.concat(0, one, two, three).reshape(new int[]{1, 3, 96, 96});
    }


}
