package com.zywl.app.server.servlet;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.MzBuyRecord;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.HTTPUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.service.MzBuyRecordService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.server.service.VerifyCodeService;
import org.apache.commons.lang3.RandomStringUtils;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("serial")
@WebServlet(name = "CreateBotServlet", urlPatterns = "/createBot")
public class CreateBotServlet extends HttpServlet {

	private UserService userService;

	private Set<String> userNos = new HashSet<>();

	private MzBuyRecordService mzBuyRecordService;

	private UserCacheService userCacheService;

	private GameCacheService gameCacheService;
	
	@Override
	public void init() throws ServletException {
		super.init();
		userService = SpringUtil.getService(UserService.class);
		mzBuyRecordService = SpringUtil.getService(MzBuyRecordService.class);
		userCacheService = SpringUtil.getService(UserCacheService.class);
		gameCacheService = SpringUtil.getService(GameCacheService.class);
		Set<String> allUserNo = userService.findAllUserNo();
		userNos.addAll(allUserNo);
	}
	public String getNo() {


		String userNo = RandomStringUtils.randomNumeric(8);
		if (userNos.contains(userNo)) {
			return getNo();
		}
		userNos.add(userNo);
		return userNo;
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		/*Map<String,String> map1 = new HashMap<>();
		map1.put("Authorization","Bearer app-cc0omi2vi8Stzi1iO9O87QJZ");
		String aa = "{\"inputs\":{\"query\":\"\"},\"response _mode\":\"blocking\",\"user\":\"lzx\"}";
		for (int i = 0; i < 1000; i++) {
			String post = HTTPUtil.postJSON("http://42.96.173.84:8099/v1/completion-messages", aa, map1);
			if (post==null){
				continue;
			}
			JSONObject jsonObject = JSONObject.parseObject(post);
			jsonObject.put("nickname",jsonObject.getString("answer"));
			Random random = new Random();
			int k = random.nextInt(300000) + 600000;
			userService.insertUserInfoText((long) k,"",OrderUtil.getOrder5Number(),"",getNo(),jsonObject,"", "",OrderUtil.getOrder5Number(),"");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			System.out.println("已插入"+(i+1)+"条数据");
		}*/

		List<MzBuyRecord> allRecord = mzBuyRecordService.findAllRecord2();
		for (MzBuyRecord record : allRecord) {
			Long userId = record.getUserId();
			User user = userCacheService.getUserInfoById(userId);
			double score = 10;
			Date dateTimeByString = DateUtil.getDateTimeByString("2025-07-17");
			if (user.getRegistTime().getTime()>dateTimeByString.getTime()){
				//新用户
				score = 15;
			}
			System.out.println("检测到"+userId+",扣除积分"+score);
			gameCacheService.addPointMySelf(userId,-score);
		}
	}
}
