package com.live.app.ws.util;

import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.PushCode;
import com.zywl.app.base.bean.SystemLocale;
import com.zywl.app.base.util.DateUtil;

public class CommandBuilder {

	private Command command;

	private CommandBuilder() {
		command = new Command();
	}

	public CommandBuilder(String commandId) {
		this();
		this.command.setId(commandId);
	}

	
	public CommandBuilder(Command command) {
		this.command = command;
	}

	public synchronized static CommandBuilder builder() {
		return new CommandBuilder();
	}

	public synchronized static CommandBuilder builder(String commandId) {
		return new CommandBuilder(commandId);
	}

	public synchronized static CommandBuilder builder(Command command) {
		return new CommandBuilder(command);
	}

	public CommandBuilder setData(Object data) {
		this.command.setData(data);
		return this;
	}

	public CommandBuilder request(String code, Object data) {
		this.command.setCode(code);
		this.command.setData(data);
		return this;
	}
	
	public CommandBuilder request(String code, Object data,String message) {
		this.command.setCode(code);
		this.command.setData(data);
		this.command.setMessage(message);
		return this;
	}
	

	public CommandBuilder success(Object data) {
		return response(true, false, null, data);
	}
	
	public CommandBuilder success(Object data,String message) {
		return response(true, false, message, data);
	}

	public CommandBuilder error(String message) {
		return response(false, false, message, null);
	}
	
	public CommandBuilder error(String message, Object data) {
		return response(false, false, message, data);
	}

	public CommandBuilder push(PushCode pushCode, Object data) {
		this.command.setCode(pushCode.name());
		return response(true, true, null, data);
	}
	
	public CommandBuilder push(String pushCode, Object data) {
		this.command.setCode(pushCode);
		return response(true, true, null, data);
	}

	public CommandBuilder mass(String massCode, Object data) {
		this.command.setCode(massCode);
		return response(true, true, null, data);
	}
	
	public CommandBuilder response(boolean success, boolean push, String message, Object data) {
		this.command.setPush(push);
		this.command.setSuccess(success);
		this.command.setMessage(message);
		this.command.setData(data);
		this.command.setResponseTime(DateUtil.getCurrent0());
		return this;
	}

	public Command build() {
		return build(SystemLocale.DEFAULT_LOCALE);
	}
	
	public Command build(String locale) {
		this.command.setLocale(locale);
		return this.command;
	}
}