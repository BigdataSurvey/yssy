package com.zywl.app.service;

import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import org.springframework.stereotype.Service;

@Service
public class ServerStateService {
	
	private static boolean service;
	
	public static void stopService(){
		service = false;
		Push.push(PushCode.syncIsService, null, service);
	}
	
	public static void startService(){
		service = true;
		Push.push(PushCode.syncIsService, null, service);
	}

	public static boolean isService() {
		return service;
	}
}
