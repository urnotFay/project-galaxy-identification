package edu.unimelb.galaxyidentification.controller;

import static org.bytedeco.javacpp.opencv_imgproc.putText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.unimelb.deeplearning4j.yolo.YOLOModel;
import edu.unimelb.galaxyidentification.entity.MatchedObject;
import edu.unimelb.galaxyidentification.entity.SkyObject;
import edu.unimelb.galaxyidentification.service.SkyObjectService;
import edu.unimelb.utils.FileUtil;
import edu.unimelb.utils.ImagesUtil;
import edu.unimelb.utils.JavaCVUtil;
import edu.unimelb.utils.LireUtil;

@Controller
public class IdentificationController {

	@Autowired
	private SkyObjectService skyObjectService;

	@RequestMapping(value = "/identification")
	public String identification(HttpServletRequest request, HttpServletResponse response, Model model)
			throws Exception {

		String tempResultPath = "src/main/resources/temp/result/";
		Object attribute = request.getSession().getAttribute("tempPath");
		String originalName = request.getSession().getAttribute("originalName").toString();
		List<MatchedObject> list = new ArrayList<MatchedObject>();

		String path = "";
		List<String> detectGalaxyList = new ArrayList<String>();
		if (attribute != null) {
			path = attribute.toString();
			List<String> searchList = LireUtil.search(path);
			int size = searchList.size();

			List<String> imageList = new ArrayList<String>();
			imageList.add(path);
			for (int i = 0; i < size; i++) {
				imageList.add(searchList.get(i));
			}
			YOLOModel detector = new YOLOModel();
			for (String searchPath : imageList) {
				Mat mat = JavaCVUtil.imRead(searchPath);
				MatchedObject match = new MatchedObject();
				detectGalaxyList = detector.detectGalaxy(mat, 0.4);
				if (detectGalaxyList.size() != 0) {
					putText(mat, "Result", new opencv_core.Point(10, 25), 2, .9, opencv_core.Scalar.YELLOW);
					String savePath = "http://localhost:8081/upload?location=result";
					String nginxPath = "http://localhost:8089/result/";
					String name = UUID.randomUUID().toString() + ".jpg";
					String tempPath = tempResultPath + name;
					boolean imWrite = JavaCVUtil.imWrite(mat, tempPath);
					if (imWrite) {
						CloseableHttpClient httpClient = HttpClients.createDefault();
						HttpPost httpPost = new HttpPost(savePath);//
						CloseableHttpResponse resp = null;

						MultipartEntityBuilder mEntityBuilder = MultipartEntityBuilder.create();
						mEntityBuilder.addBinaryBody("uploadfile", new File(tempPath));

						List<NameValuePair> nvps = new ArrayList<NameValuePair>();
						nvps.add(new BasicNameValuePair("location", "result"));

						httpPost.setEntity(mEntityBuilder.build());
						resp = httpClient.execute(httpPost);
						int statusCode = resp.getStatusLine().getStatusCode();
						if (statusCode == HttpStatus.SC_OK) {
							HttpEntity resEntity = resp.getEntity();
							EntityUtils.consume(resEntity);
							SkyObject skyObject = skyObjectService.queryByName(match.getName());

							if (skyObject != null) {
								match.setDescription(skyObject.getDescription());
						
								

							} else {
								match.setDescription("");
								

							}

							
							match.setName(detectGalaxyList.get(0));
							String id = UUID.randomUUID().toString();
							match.setId(id);
							match.setPath(File.separator + originalName);
							match.setSearchPath(nginxPath + name);
							//read csv 
				            FileReader filereader = new FileReader("src/main/resources/data/evaluation.csv");
				            BufferedReader reader  = new BufferedReader(filereader);
				            reader .readLine();
				           
				            String line = null;    
				            while((line=reader.readLine())!=null){    
				                String item[] = line.split(",");
				         

				                String objectName = item[0];
				               if(objectName.equals(detectGalaxyList.get(0))) {
				            	   match.setSimilarity(Float.valueOf(item[1])*100+"%");
				               } 
				            }    
			
				            

							list.add(match);


						}
					}
				}

			}

		}

		Collections.sort(list, new Comparator<MatchedObject>() {

			@Override
			public int compare(MatchedObject o1, MatchedObject o2) {
				// TODO Auto-generated method stub
				NumberFormat nf = NumberFormat.getPercentInstance();
				int result = -1;
				try {
					Number m1 = nf.parse(o1.getSimilarity());
					Number m2 = nf.parse(o2.getSimilarity());
					Float result1 = new Float(m1.floatValue());
					Float result2 = new Float(m2.floatValue());

					int compare = result1.compareTo(result2);
					if (compare == 0) {
						result = 0;
					} else if (compare == 1) {
						result = -1;
					} else {
						result = 1;
					}

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return result;
			};
		});
		model.addAttribute("matchList", list);
		return "result";

	}


}
