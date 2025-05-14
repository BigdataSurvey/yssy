package com.zywl.app.manager.service.manager;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Push;
import com.zywl.app.base.service.BaseService;


@Service
public class ManagerLotteryGameService extends BaseService{
	
	
	@PostConstruct
	public void _construct(){
		Push.addPushSuport(PushCode.updateRoomDate, new DefaultPushHandler());
		Push.addPushSuport(PushCode.updateGameDiyData, new DefaultPushHandler());
	}
	

}
