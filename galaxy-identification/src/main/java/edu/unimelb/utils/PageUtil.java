package edu.unimelb.utils;

import java.util.List;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import edu.unimelb.galaxyidentification.entity.MatchedObject;

public class PageUtil {
	/**
     * 分页查询
     * @param page 当前页数
     * @param pageSize 每页个数
     * @return
     */
    public static PageInfo<MatchedObject> findAll(int page,int pageSize, List<MatchedObject> list) {
        PageHelper.startPage(page, pageSize);//改写语句实现分页查询
        List<MatchedObject> all = list;
        PageInfo<MatchedObject> info = new PageInfo<>(all);
        return info;
    }

}
