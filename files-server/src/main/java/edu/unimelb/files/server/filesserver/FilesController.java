package edu.unimelb.files.server.filesserver;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@EnableAutoConfiguration
public class FilesController {

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	@ResponseBody
	public String upload(HttpServletRequest request,@RequestParam("location") String location) {
		Part part;
		try {
			part = request.getPart("uploadfile");
			

			InputStream input = part.getInputStream();

			OutputStream output = new FileOutputStream(
					"E:\\urnotFay\\materials\\UniMelbourne\\2019\\SummerProject\\FilesServer\\" + location + "\\"
							+ part.getSubmittedFileName());
			IOUtils.copy(input, output);

			output.close();
			input.close();
			return "OK";
		} catch (Exception e) {
			
			e.printStackTrace();
			return "FAILED";
			
		}

	}

}
