package com.zywl.app.manager.servlet;

import com.zywl.app.base.bean.TsgPayOrder;
import com.zywl.app.base.util.MD5Util;
import com.zywl.app.defaultx.service.TsgPayOrderService;
import com.zywl.app.defaultx.service.UserGiftService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.manager.ManagerGameBaseService;
import com.zywl.app.manager.service.manager.ManagerUserVipService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.TreeMap;

@SuppressWarnings("serial")
@WebServlet(name = "TsgPayServlet", urlPatterns = "/tsgPayNotify")
public class TsgPayServlet extends HttpServlet {

	private static final Log logger = LogFactory.getLog(TsgPayServlet.class);

	private TsgPayOrderService tsgPayOrderService;

	private UserGiftService userGiftService;

	private ManagerUserVipService managerUserVipService;

	public TsgPayServlet() {
		tsgPayOrderService = SpringUtil.getService(TsgPayOrderService.class);
		userGiftService = SpringUtil.getService(UserGiftService.class);
		managerUserVipService = SpringUtil.getService(ManagerUserVipService.class);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doProcess(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doProcess(request, response);
	}

	public void doProcess(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("========================================");
		System.out.println(request.getParameterMap());
		String morderid = request.getParameter("morderid");
		String orderid = request.getParameter("orderid");
		String amt = request.getParameter("amt");
		String status = request.getParameter("status");
		String requesttime = request.getParameter("requesttime");
		String sign = request.getParameter("sign");
		TreeMap<String,String> treeMap = new TreeMap<>();
		treeMap.put("morderid",morderid);
		treeMap.put("orderid",orderid);
		treeMap.put("amt",amt);
		treeMap.put("status",status);
		treeMap.put("requesttime",requesttime);
		StringBuffer stringBuffer = new StringBuffer();
		treeMap.forEach((key, value) -> stringBuffer.append(key).append("=").append(value).append("&"));
		stringBuffer.deleteCharAt(stringBuffer.length()-1);
		stringBuffer.append("=================");
		String signMd5 = MD5Util.md5(stringBuffer.toString());
		if (signMd5.equals(sign)){
			TsgPayOrder tsgPayOrder = tsgPayOrderService.findByOrderNo(morderid);
			if (tsgPayOrder!=null){
				Long userId = tsgPayOrder.getUserId();
				int productId = Math.toIntExact(tsgPayOrder.getProductId());
				userGiftService.addUserGiftNumber(userId,productId);
				managerUserVipService.addExper(userId,new BigDecimal(amt));
			}else {
				logger.error("未查询到回调订单号，订单号："+morderid);
			}
		}
	}
}
