package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
@Service
public class DicZytService extends DaoService {


    public DicZytService() {
        super("DicZytMapper");
    }

    public List<Map<String, JSONArray>> findDicZytList(){
        List<Map<String,JSONArray>> findAll = findList("findAll", null);
        return  findAll;
    }
}
