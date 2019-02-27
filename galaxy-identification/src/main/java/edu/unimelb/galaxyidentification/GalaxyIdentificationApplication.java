package edu.unimelb.galaxyidentification;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("edu.unimelb.galaxyidentification.mapper")

public class GalaxyIdentificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(GalaxyIdentificationApplication.class, args);
	}

}

