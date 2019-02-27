package edu.unimelb.galaxyidentification.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UploadPageController {

	@GetMapping("/uploadPage")
	String uploadPage(HttpServletRequest request) {
		return "/uploadPage";
	} 
}
