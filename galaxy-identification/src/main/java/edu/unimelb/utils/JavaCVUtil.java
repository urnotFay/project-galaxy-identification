package edu.unimelb.utils;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JFrame;
 
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter.ToMat;

/**
 * 
  * @ClassName: JavaCVUtil
  * @Description: TODO
  * @author Xiaorong Yang
  * @date Jan 22, 2019 8:35:28 PM
  *
 */
public class JavaCVUtil {
	   
	  /**
	   * 
	   * 功能说明:显示图像
	   * @param mat
	   * 要显示的mat类型图像
	   * @param title
	   * 窗口标题
	   * @time:2016年3月31日下午1:28:01
	   * @author:linghushaoxia
	   * @exception:
	   *
	   */
	  public static void imShow(Mat mat,String title) {
	    ToMat converter = new OpenCVFrameConverter.ToMat();
	    CanvasFrame canvas = new CanvasFrame(title, 1);
	    canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    canvas.showImage(converter.convert(mat));
	    
	  }
	  /***
	   * 
	   * 功能说明:显示图像
	   * @param filePath
	   * 图像路径(可以包含中文)
	   * @param title 
	   * 窗口标题(可以包含中文)
	   * @time:2016年3月31日下午1:26:05
	   * @author:linghushaoxia
	   * @exception:
	   *
	   */
	  public static void show(String filePath,String title) {
	      Mat mat= imRead(filePath);
	      imShow(mat, title);
	   }
	 
	  
	  /**
	   * 
	   * 功能说明:读取图像
	   * @param filePath
	   * 文件路径,可以包含中文
	   * @return Mat
	   * @time:2016年3月31日下午1:26:51
	   * @author:linghushaoxia
	   * @exception:
	   *
	   */
	  public static Mat imRead(String filePath){
	      Mat mat = null;
	      try {
		  //使用java2D读取图像
		  Image image = ImageIO.read(new File(filePath));
		  /**
		   * 转为mat
		   * 1、由Java2D的image转为javacv自定义的Frame
		   * 2、由Frame转为Mat
		   */
		  Java2DFrameConverter java2dFrameConverter = new Java2DFrameConverter();
		  Frame frame = java2dFrameConverter.convert((BufferedImage) image);
		  ToMat converter = new OpenCVFrameConverter.ToMat();
		  mat = converter.convert(frame);
		  
	    } catch (Exception e) {
		System.out.println("读取图像出现异常：filePath="+filePath);
		e.printStackTrace();
	    }
	      return mat;
	  }
	  /**
	   * 
	   * 功能说明:保存mat到指定路径
	   * @param mat
	   * 要保存的Mat
	   * @param filePath 
	   * 保存路径
	   * @time:2016年3月31日下午8:39:50
	   * @author:linghushaoxia
	   * @exception:
	   *
	   */
	  public static boolean imWrite(Mat mat,String filePath){
	     try {
		 /**
		  * 将mat转为java的BufferedImage
		  */
		  ToMat convert= new ToMat();
		  Frame frame= convert.convert(mat);
		  Java2DFrameConverter java2dFrameConverter = new Java2DFrameConverter();
		  BufferedImage bufferedImage= java2dFrameConverter.convert(frame);
		  ImageIO.write(bufferedImage, "JPG", new File(filePath));
		  bufferedImage.flush();
		  
		  return true;
	    } catch (Exception e) {
		System.out.println("There is an error:"+filePath);
		e.printStackTrace();
	    }
	  return false;
	  }
	  
	  public static void saveCut(InputStream input, OutputStream out, int x,  
      int y, int width, int height) throws Exception {
		  
		  ImageInputStream imageStream = null;  
		  Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("jpg");  
          ImageReader reader = readers.next();  
          imageStream = ImageIO.createImageInputStream(input);  
          reader.setInput(imageStream, true);  
          ImageReadParam param = reader.getDefaultReadParam();  
            
          System.out.println(reader.getWidth(0));  
          System.out.println(reader.getHeight(0));  
          Rectangle rect = new Rectangle(x, y, width, height);  
          param.setSourceRegion(rect);  
          BufferedImage bi = reader.read(0, param);  
          ImageIO.write(bi, "jpg", out);  
		  
	  }
}