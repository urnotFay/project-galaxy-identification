package edu.unimelb.galaxyidentification.mapper;


import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import edu.unimelb.galaxyidentification.entity.MatchedObject;

@Repository
public interface MatchedObjectMapper {
	
	@Select("SELECT * FROM t_matched_object")
    MatchedObject queryMatchedObject(String name);
	
	

}
