package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.hongbao.RedEnvelope;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.service.RecordSheetService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 红包雨
 */
@Service
@ServiceClass(code = MessageCodeContext.RECORD)
public class ManagerRecordSheetService extends BaseService {


    @Autowired
    private RecordSheetService recordSheetService;






}
