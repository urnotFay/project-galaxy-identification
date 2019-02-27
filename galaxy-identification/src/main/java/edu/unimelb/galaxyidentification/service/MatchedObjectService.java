package edu.unimelb.galaxyidentification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.unimelb.galaxyidentification.entity.MatchedObject;
import edu.unimelb.galaxyidentification.mapper.MatchedObjectMapper;

@Service
public class MatchedObjectService {
	@Autowired
    MatchedObjectMapper matchedObjectMapper;
    public MatchedObject queryMatchedObject(String name) {
        return matchedObjectMapper.queryMatchedObject(name);
    }


}
