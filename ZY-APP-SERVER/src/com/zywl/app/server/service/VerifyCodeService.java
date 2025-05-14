package com.zywl.app.server.service;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;

@Service
public class VerifyCodeService {

	// 图片宽度
	private static final int IMG_WIDTH = 100;
	// 图片高度
	private static final int IMG_HEIGHT = 30;
	// 验证码长度
	private static final int CODE_LEN = 4;
	
	private static final long TIMEOUT = 3 * 60 * 1000;
	
	public static final String SESSION_KEY = "__verifyCode";
	
	public BufferedImage create(HttpSession session){
		// 用于绘制图片，设置图片的长宽和图片类型（RGB)
		BufferedImage bi = new BufferedImage(IMG_WIDTH, IMG_HEIGHT,
				BufferedImage.TYPE_INT_RGB);
		// 获取绘图工具
		Graphics graphics = bi.getGraphics();
		graphics.setColor(new Color(100, 230, 200)); // 使用RGB设置背景颜色
		graphics.fillRect(0, 0, 100, 30); // 填充矩形区域

		// 验证码中所使用到的字符
		char[] codeChar = "0123456789".toCharArray();
		//char[] codeChar = "卢卡斯大家六十九杰佛为妇女物价偏高比二我爱刷国风范耶夫嗯而无法个案访谈发".toCharArray();
		String captcha = ""; // 存放生成的验证码
		Random random = new Random();
		for (int i = 0; i < CODE_LEN; i++) { // 循环将每个验证码字符绘制到图片上
			int index = random.nextInt(codeChar.length);
			// 随机生成验证码颜色
			graphics.setColor(new Color(random.nextInt(150), random.nextInt(200), random.nextInt(255)));
			// 将一个字符绘制到图片上，并制定位置（设置x,y坐标）
			graphics.drawString(codeChar[index] + "", (i * 25) + 10, 20);
			captcha += codeChar[index];
		}
		// 将生成的验证码code放入sessoin中
		JSONObject data = new JSONObject();
		data.put("captcha", captcha);
		data.put("createTime", System.currentTimeMillis());
		session.setAttribute(SESSION_KEY, data);
		return bi;
	}
	
	public String get(HttpSession session){
		try{
			JSONObject data = (JSONObject) session.getAttribute(SESSION_KEY);
			if(data != null){
				long createTime = data.getLongValue("createTime");
				if(System.currentTimeMillis() - createTime < TIMEOUT){
					return data.getString("captcha");
				}
			}
			return null;
		}finally{
			session.removeAttribute(SESSION_KEY);
		}
	}
}
