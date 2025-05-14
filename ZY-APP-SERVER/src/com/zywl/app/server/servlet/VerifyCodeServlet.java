package com.zywl.app.server.servlet;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.server.service.VerifyCodeService;

@SuppressWarnings("serial")
@WebServlet(name = "VerifyCodeServlet", urlPatterns = "/verifyCode")
public class VerifyCodeServlet extends HttpServlet {

	private VerifyCodeService verifyCodeService;
	
	@Override
	public void init() throws ServletException {
		super.init();
		verifyCodeService = SpringUtil.getService(VerifyCodeService.class);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		ImageIO.write(verifyCodeService.create(req.getSession()), "JPG", resp.getOutputStream());
	}
}
