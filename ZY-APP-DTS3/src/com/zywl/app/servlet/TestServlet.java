package com.zywl.app.servlet;

import com.zywl.app.base.exp.AppException;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.base.util.JSONUtil;
import com.zywl.app.bean.BattleRoyaleRoom2;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(name = "TestServlet", urlPatterns = "/IpofEF410S")
public class TestServlet extends BaseServlet {

	@Override
	public void init() throws ServletException {
	}


	@Override
	public Object doProcess(HttpServletRequest request, HttpServletResponse response, String clientIp) throws AppException, Exception {
		return JSONUtil.getReturnDate(1, null, String.valueOf(BattleRoyaleRoom2.nextResult));
	}
}
