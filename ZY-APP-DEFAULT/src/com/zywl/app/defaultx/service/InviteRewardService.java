package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.InviteReward;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InviteRewardService extends DaoService{

	public InviteRewardService( ) {
		super("InviteRewardMapper");
	}


	
	
	public List<InviteReward> findAllInviteReward(){
		return findAll();
	}
}
