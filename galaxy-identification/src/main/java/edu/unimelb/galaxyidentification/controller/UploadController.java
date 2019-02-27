package edu.unimelb.galaxyidentification.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.unimelb.utils.FileUtil;
import edu.unimelb.utils.LireUtil;
import scala.util.parsing.json.JSONObject;

/**
 * 
  * @ClassName: UploadController
  * @Description: TODO
  * @author Xiaorong Yang
  * @date Jan 22, 2019 8:35:51 PM
  *
 */
@Controller
public class UploadController {
	
	private static String SERVER_PATH = "http://localhost:8081/upload?location=upload";

	@RequestMapping(value="/saveFiles", method = RequestMethod.POST)
	
	public void uploadImg(@RequestParam("file") MultipartFile file,
            HttpServletRequest request,HttpServletResponse response) {
		
        String fileName = file.getOriginalFilename();

        
       //search images for matching
        
        try {
            String uploadFile = FileUtil.uploadFile(file.getBytes(),fileName);

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(SERVER_PATH);//
    		CloseableHttpResponse resp = null;
    		String result = "";

			MultipartEntityBuilder mEntityBuilder = MultipartEntityBuilder.create();
			mEntityBuilder.addBinaryBody("uploadfile", file.getBytes());
			
			httpPost.setEntity(mEntityBuilder.build());
			resp = httpClient.execute(httpPost);
			int statusCode = resp.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity resEntity = resp.getEntity();
				result =EntityUtils.toString(resEntity);
				// 消耗掉response
				EntityUtils.consume(resEntity);
				
				FileUtil.delAllFile(uploadFile);
				
				HttpSession session = request.getSession();
	            session.setAttribute("originalName", fileName);
	            session.setAttribute("path", SERVER_PATH+fileName);
	            session.setAttribute("tempPath", uploadFile);
	            response.sendRedirect("/identification");
				
			}else {
				result = "Error";
	            response.sendRedirect("/index?result="+result);

			}

            
            
            
        } catch (Exception e) {

        	e.printStackTrace();
        }
        		
	}
	
}
