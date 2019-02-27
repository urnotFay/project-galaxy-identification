package edu.unimelb.galaxyidentification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.unimelb.galaxyidentification.entity.MatchedObject;
import edu.unimelb.galaxyidentification.entity.SkyObject;
import edu.unimelb.galaxyidentification.mapper.MatchedObjectMapper;
import edu.unimelb.galaxyidentification.mapper.SkyObjectMapper;

@Service
public class SkyObjectService {
	@Autowired
    SkyObjectMapper skyObjectMapper;
	
    public SkyObject queryByName(String name) {
        return skyObjectMapper.queryByName(name);
    }


}
