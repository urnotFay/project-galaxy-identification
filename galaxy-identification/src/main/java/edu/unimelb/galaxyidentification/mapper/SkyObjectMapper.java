package edu.unimelb.galaxyidentification.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import edu.unimelb.galaxyidentification.entity.SkyObject;

@Mapper
public interface SkyObjectMapper {

	 
	@Select("select id, object_name, object_description from t_sky_object where object_name=#{name}")  
    @Results({  
       @Result(property="name",column="object_name"),
       @Result(property="description", column = "object_description"),
       @Result(property="id", column = "id")

    })  
	SkyObject queryByName(String name);
	


}
