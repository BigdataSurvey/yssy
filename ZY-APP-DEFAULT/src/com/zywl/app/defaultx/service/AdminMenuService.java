package com.zywl.app.defaultx.service;

import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

@Service
public class AdminMenuService extends DaoService {
    private static final Log logger = LogFactory.getLog(AdminService.class);

    public AdminMenuService(){
        super("AdminMenuMapper");
    }


}
