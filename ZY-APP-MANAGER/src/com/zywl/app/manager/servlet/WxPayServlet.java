package com.zywl.app.manager.servlet;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import com.alibaba.fastjson2.JSONObject;
import com.ijpay.core.kit.HttpKit;
import com.ijpay.core.kit.PayKit;
import com.ijpay.core.kit.WxPayKit;
import com.zywl.app.base.bean.RechargeOrder;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.service.RechargeOrderService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.service.manager.ManagerSocketService;
import com.zywl.app.manager.service.manager.ManagerUserService;
import com.zywl.app.manager.service.pay.WechatPayService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
@WebServlet(name = "WxPayServlet", urlPatterns = "/payNotify")
public class WxPayServlet extends HttpServlet {

	private static final Log logger = LogFactory.getLog(WxPayServlet.class);

	private WechatPayService wechatPayService;

	private RechargeOrderService rechargeOrderService;

	private ManagerUserService managerUserService;


	private ManagerSocketService managerSocketService;

	private UserCacheService userCacheService;

	private PlayGameService gameService;
	public WxPayServlet() {
		wechatPayService = SpringUtil.getService(WechatPayService.class);
		rechargeOrderService = SpringUtil.getService(RechargeOrderService.class);
		managerUserService = SpringUtil.getService(ManagerUserService.class);
		userCacheService = SpringUtil.getService(UserCacheService.class);
		gameService = SpringUtil.getService(PlayGameService.class);
		managerSocketService = SpringUtil.getService(ManagerSocketService.class);
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
		Map<String, String> map = new HashMap<>(12);
		try (InputStream inputStream = PayKit.getCertFileInputStream(wechatPayService.getBean().getPlatformCertPath())){
			String timestamp = request.getHeader("Wechatpay-Timestamp");
			String nonce = request.getHeader("Wechatpay-Nonce");
			String serialNo = request.getHeader("Wechatpay-Serial");
			String signature = request.getHeader("Wechatpay-Signature");
			String result = HttpKit.readData(request);
			// 需要通过证书序列号查找对应的证书，verifyNotify 中有验证证书的序列号
			String plainText = WxPayKit.verifyNotify(serialNo, result, signature, nonce, timestamp,
					wechatPayService.getBean().getApiKey3(), inputStream);
			if (StrUtil.isNotEmpty(plainText)) {
				JSONObject text = JSONObject.parseObject(plainText);
				String outTradeNo = text.getString("out_trade_no");
				String remark = text.getString("attach");
				String payer = text.getString("payer");
				RechargeOrder rechargeOrder = rechargeOrderService.findByOrderNo(outTradeNo);
				rechargeOrderService.rechargeSuccess(outTradeNo,remark,payer);
				//更改会员等级
				managerUserService.updateUserVipLv(rechargeOrder.getUserId(),rechargeOrder.getProductId());
				//更改技能释放次数

				map.put("code", "SUCCESS");
				map.put("message", "SUCCESS");
			} else {
				response.setStatus(500);
				map.put("code", "ERROR");
				map.put("message", "签名错误");
			}
			response.setHeader("Content-type", ContentType.JSON.toString());
			response.getOutputStream().write(JSONObject.toJSONString(map).getBytes(StandardCharsets.UTF_8));
			response.flushBuffer();
		} catch (Exception e) {
			logger.error("网络波动", e);
		}
	}
}
