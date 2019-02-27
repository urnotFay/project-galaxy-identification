package edu.unimelb.deeplearning4j.yolo;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter.ToMat;

import static org.bytedeco.javacpp.opencv_imgproc.putText;

import org.bytedeco.javacpp.opencv_core;
import org.nd4j.linalg.factory.Nd4j;

import edu.unimelb.utils.JavaCVUtil;

public class GalaxyDetection {

	public static void main(String[] args) {

//		Nd4j.ENFORCE_NUMERICAL_STABILITY = true;

		YOLOModel detector = new YOLOModel();

		ToMat converter = new OpenCVFrameConverter.ToMat();

		CanvasFrame mainframe = new CanvasFrame("Real-time Rubik's Cube Detector - Emaraic",
				CanvasFrame.getDefaultGamma() / 2.2);
		mainframe.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		mainframe.setCanvasSize(1000, 1000);
		mainframe.setLocationRelativeTo(null);
		mainframe.setVisible(true);

		String filePath = "src/main/resources/data/Sample/Galaxy_0000.jpg";
		Mat mat = JavaCVUtil.imRead(filePath);

		detector.detectGalaxy(mat, 0.4);

		putText(mat, "Detection", new opencv_core.Point(10, 25), 2, .9, opencv_core.Scalar.YELLOW);

		mainframe.showImage(converter.convert(mat));

	}

}
