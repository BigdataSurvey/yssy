package com.live.app.ws.util;

import com.live.app.ws.bean.NoticeCommand;
import com.live.app.ws.enums.PushCode;

public class NoticeCommandBuilder {

	private NoticeCommand noticeCommand;

	private NoticeCommandBuilder() {
		noticeCommand = new NoticeCommand();
	}

	public NoticeCommandBuilder(String noticeCommandId) {
		this();
		this.noticeCommand.setId(noticeCommandId);
	}

	
	public NoticeCommandBuilder(NoticeCommand noticeCommand) {
		this.noticeCommand = noticeCommand;
	}

	public synchronized static NoticeCommandBuilder builder() {
		return new NoticeCommandBuilder();
	}

	public synchronized static NoticeCommandBuilder builder(String noticeCommandId) {
		return new NoticeCommandBuilder(noticeCommandId);
	}

	public synchronized static NoticeCommandBuilder builder(NoticeCommand noticeCommand) {
		return new NoticeCommandBuilder(noticeCommand);
	}

	public NoticeCommandBuilder notice(PushCode code, String title, String message) {
		return notice(code.toString(), title, message);
	}

	public NoticeCommandBuilder notice(PushCode code, String title, String message, Object data) {
		return notice(code.toString(), title, message, data);
	}
	
	public NoticeCommandBuilder notice(String code, String title, String message){
		return notice(code, title, message, null);
	}
	
	public NoticeCommandBuilder notice(String code, String title, String message, Object data) {
		this.noticeCommand.setCode(code);
		this.noticeCommand.setTitle(title);
		this.noticeCommand.setMessage(message);
		this.noticeCommand.setData(data);
		return this;
	}

	public NoticeCommand build() {
		return this.noticeCommand;
	}
}