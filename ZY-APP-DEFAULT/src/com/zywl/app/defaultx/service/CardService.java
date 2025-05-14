package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.card.Card;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardService extends DaoService{

	public CardService( ) {
		super("CardMapper");
	}


	
	public List<Card> findAllCard(){
		return findAll();
	}
}
