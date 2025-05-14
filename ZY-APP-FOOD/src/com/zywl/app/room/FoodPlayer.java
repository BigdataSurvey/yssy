package com.zywl.app.room;

import java.math.BigDecimal;
import java.util.ArrayList;

public class FoodPlayer {
	private String m_userid = "";
	private String m_userNo = "";
	private Integer m_chairid = -1;
	private String m_nickname = "";
	private String m_headurl = "";
	private BigDecimal m_betscore = BigDecimal.ZERO;
	private Integer m_winscore = 0;
	private ArrayList<Integer> m_cardsArr = new ArrayList<Integer>();
	
	public void setUserID(String uid) {
		m_userid = uid;
	}

	public String getUserID() {
		return m_userid;
	}

	public void setChairID(Integer chairid) {
		m_chairid = chairid;
	}

	public Integer getChairID() {
		return m_chairid;
	}

	public void setNickName(String nickname) {
		m_nickname = nickname;
	}

	public String getNickName() {
		return m_nickname;
	}

	public void setHeadUrl(String url) {
		m_headurl = url;
	}

	public String getHeadUrl() {
		return m_headurl;
	}

	public void addBetScore(BigDecimal score) {
		m_betscore = m_betscore.add(score);
	}

	public BigDecimal getBetScore() {
		return m_betscore;
	}

	public void addCard(Integer card) {
		m_cardsArr.add(card);
	}

	public void clearData() {
		m_cardsArr.clear();
		m_betscore = BigDecimal.ZERO;
	}

	public ArrayList<Integer> getAllCard () {
		return m_cardsArr;
	}

	public ArrayList<Integer> getDisplayCard () {
		if(m_cardsArr.size() == 0) {
			return  m_cardsArr;
		}

		ArrayList<Integer> cards = new ArrayList<>();
		for (int i = 0; i < m_cardsArr.size(); i++) {
			cards.add( i<2 ? -1 : m_cardsArr.get(i));
		}
		return cards;
	}

	public String getUserNo() {
		return m_userNo;
	}

	public void setUserNo(String m_userNo) {
		this.m_userNo = m_userNo;
	}

	public Integer getWinScore() {
		return m_winscore;
	}

	public void setWinScore(Integer winscore) {
		this.m_winscore = winscore;
	}
}
