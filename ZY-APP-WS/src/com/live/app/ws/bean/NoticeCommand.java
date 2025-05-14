package com.live.app.ws.bean;


public class NoticeCommand extends Command {
	
	private String title;
	
	private Long expire;
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getExpire() {
		return expire == null ? 60000 : expire;
	}

	public void setExpire(Long expire) {
		this.expire = expire;
	}
	
}
