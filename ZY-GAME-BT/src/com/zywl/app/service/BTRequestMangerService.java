package com.zywl.app.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import org.springframework.stereotype.Service;

@Service
public class BTRequestMangerService {

	
	public void requestManagerGetGoodServer(JSONObject data, Listener listener) {
		Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("9004001", data).build(), listener);
	}
}
