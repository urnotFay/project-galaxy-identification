package edu.unimelb.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import edu.unimelb.deeplearning4j.evaluate.Evaluate;

public class UpdateXmlUtil {

	public static void main(String[] args) {
		String dic = "src/main/resources/data/Annotations/";

		updateXML(dic);
	}

	private static void updateXML(String xmlPath) {
		List<String> allFile = Evaluate.getAllFile(xmlPath, false);
		for(int i = 0 ; i<allFile.size();i++) {
			try {
			SAXReader reader = new SAXReader();
			Document document = reader.read(new File(allFile.get(i)));
			Element rootElem = document.getRootElement();
			
		
			Element path = rootElem.element("path");
			String pathText = path.getText();
	
			int indexOf = pathText.indexOf("Galaxy");
			String substring = pathText.substring(indexOf);
			path.setText("src/main/resources/data/Images/"+substring);
			
			
			OutputFormat format=OutputFormat.createPrettyPrint();

			XMLWriter writer;
			
				writer = new XMLWriter(new FileWriter(allFile.get(i)),format);
				writer.write(document);
				writer.flush();
				writer.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		}
	}
	private static void updateXML() {
		
		String prefix = "src/main/resources/data/Annotations/Galaxy_";
		String imagePrefix = "src/main/resources/data/Images/Galaxy_";

		try {
			
			String fileName = "";
			String imagePath = "";
			for (int i = 0; i < 1627; i++) {
				if (i < 10) {
					fileName = prefix+"000" + i+".xml";
					imagePath = imagePrefix + "000"+i+".jpg";
				} else if(i>10 && i<100) {
					fileName = prefix+"00" + i+".xml";
					imagePath = imagePrefix + "00"+i+".jpg";

				}else if(i>100 && i<1000) {
					fileName = prefix+"0" + i+".xml";
					imagePath = imagePrefix + "0"+i+".jpg";

				}else if(i>1000) {
					fileName = prefix + i+".xml";
					imagePath = imagePrefix +i+".jpg";
				}
				
				SAXReader reader = new SAXReader();
				Document document = reader.read(new File(fileName));
				Element rootElem = document.getRootElement();
				Element element = rootElem.element("path");
				element.setText(imagePath);
				
				
				OutputFormat format=OutputFormat.createPrettyPrint();

				XMLWriter writer = new XMLWriter(new FileWriter(fileName),format);
				writer.write(document);
				writer.flush();
				writer.close();


			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
