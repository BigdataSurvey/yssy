package com.zywl.app.server.service;

import java.math.BigDecimal;

public class Text {
	
	BigDecimal a = new BigDecimal("10.0100");

	@Override
	public String toString() {
		return "Text [a=" + a + "]";
	}

	public BigDecimal getA() {
		return a;
	}

	public void setA(BigDecimal a) {
		this.a = a;
	}

	

	
	

}
