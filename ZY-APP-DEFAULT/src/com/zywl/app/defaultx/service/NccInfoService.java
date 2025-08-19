package com.zywl.app.defaultx.service;

import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

@Service
public class NccInfoService extends DaoService {

    public NccInfoService() {
        super("NccInfoMapper");
    }
}
