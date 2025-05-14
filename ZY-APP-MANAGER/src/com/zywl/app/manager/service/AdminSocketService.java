package com.zywl.app.manager.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.defaultx.ServiceRunable;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.socket.manager.SocketManager;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.Admin;
import com.zywl.app.base.bean.CashTotalInfo;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.AdminService;
import com.zywl.app.defaultx.service.CashRecordService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.manager.service.manager.ManagerSocketService;
import com.zywl.app.manager.service.manager.ManagerUserService;
import com.zywl.app.manager.socket.AdminSocketServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 控制台页面服务
 * @author DOE
 *
 */
@Service
@ServiceClass(code = "002")
public class AdminSocketService extends BaseService {

	private static volatile boolean GET = false;
	
	@Autowired
	private AdminService adminService;

	@Autowired
	private ManagerUserService managerUserService;

	@Autowired
	private UserCapitalService userCapitalService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserCacheService userCacheService;
	
	@Autowired
	private ManagerSocketService managerSocketService;

	@Autowired
	private CashRecordService cashRecordService;

	public static Map<String,BigDecimal> allBalance = new ConcurrentHashMap<>();

	public static Map<String,String> keepAlive = new ConcurrentHashMap<>();

	public static Map<String,String> invest = new ConcurrentHashMap<>();

	public void initAllBalance(){
		long time = System.currentTimeMillis();
		BigDecimal sumBalance = userCapitalService.findSumBalance(UserCapitalTypeEnum.currency_2.getValue());
		allBalance.put("lingshi",sumBalance);

		/*sumBalance = userCapitalService.findSumBalance(UserCapitalTypeEnum.currency_3.getValue());
		allBalance.put("xianjing",sumBalance);*/

		sumBalance = userCapitalService.findSumBalance(UserCapitalTypeEnum.rmb.getValue());
		allBalance.put("rmb",sumBalance);

		sumBalance = userCapitalService.findSumBalance2(UserCapitalTypeEnum.currency_2.getValue());
		allBalance.put("canUseC2",sumBalance);

		sumBalance = userCapitalService.findSumBalance2(UserCapitalTypeEnum.rmb.getValue());
		allBalance.put("canUseRmb",sumBalance);

		List<CashTotalInfo> info = cashRecordService.findWaitTotalInfo();
		if(info.size() > 0) {
			Integer count = info.get(0).getOrderCount();
			allBalance.put("waitCashCount", new BigDecimal( String.valueOf(count)));
			allBalance.put("waitCashAmount", info.get(0).getAmount());
		}
		info = cashRecordService.findCashTotalInfo();
		if(info.size() > 0) {
			Integer count = info.get(0).getOrderCount();
			allBalance.put("cashCount", new BigDecimal( String.valueOf(count)));
			allBalance.put("cashAmount", info.get(0).getAmount());
		}
		logger.info("查询总资产用时："+(System.currentTimeMillis()-time));
		managerUserService.pushAddUser();
	}


	public void initKeepAlive(){
		keepAlive.put("one", userService.getKeepAlive(2,1));
		keepAlive.put("three", userService.getKeepAlive(3,2));
		keepAlive.put("seven", userService.getKeepAlive(7,6));
		keepAlive.put("month", userService.getKeepAlive(30,29));
	}

	//初始化投资相关数据


	@ServiceMethod(code = "001", description = "登录")
	public JSONObject login(AdminSocketServer adminSocketServer, JSONObject data){
		checkNull(data);
		checkNull(data.get("username"), data.get("password"));
		Admin admin = adminService.getAdminByUsername(data.getString("username"));
		if(admin == null || !eq(admin.getPassword(), data.getString("password"))){
			throwExp("用户名或密码不正确");
		}else if(admin.getState() == Admin.DISABLE_STATE){
			throwExp("该账户已禁用");
		}else{
			admin.setPassword(null);
			adminSocketServer.setAdmin(admin);
			adminSocketServer.getHttpSession().setAttribute("Admin", admin);
		}
		return adminSocketServer.getLoginData();
	}
	
	@ServiceMethod(code = "002", description = "登出")
	public void loginOut(AdminSocketServer adminSocketServer){
		adminSocketServer.setAdmin(null);
		adminSocketServer.getHttpSession().removeAttribute("Admin");
	}

	public synchronized void syncMonitor() {
		if(!GET) {
			GET = true;
			Executer.executeService(new ServiceRunable(logger) {
				public void service() {
					try {
						Thread.sleep(500);
						massWeb(CommandBuilder.builder().push(PushCode.syncMonitor, getMonitorData()).build());
					} catch (InterruptedException e) {
					} finally {
						GET = false;
					}
				}
			});
		}
	}
	
	public JSONObject getMonitorData() {
		JSONObject data = new JSONObject();
		data.put("totalUser", LoginService.userNos.size());
		data.put("todayRegister",userCacheService.getTodayRegister());
		data.put("todayLogin", userCacheService.getTodayLogin());
		
		data.put("totalCur2", allBalance.getOrDefault("lingshi",BigDecimal.ZERO));
		data.put("totalCur3", allBalance.getOrDefault("xianjing",BigDecimal.ZERO));
		data.put("totalRMB", allBalance.getOrDefault("rmb",BigDecimal.ZERO));
		data.put("totalCanUseCur2", allBalance.getOrDefault("canUseC2",BigDecimal.ZERO));
		data.put("totalCanUseRMB", allBalance.getOrDefault("canUseRmb",BigDecimal.ZERO));
		data.put("totalCashOrder", "还没写");

		data.put("waitCashCount", allBalance.getOrDefault("waitCashCount",BigDecimal.ZERO));
		data.put("waitCashAmount", allBalance.getOrDefault("waitCashAmount",BigDecimal.ZERO));
		data.put("cashCount", allBalance.getOrDefault("cashCount",BigDecimal.ZERO));
		data.put("cashAmount", allBalance.getOrDefault("cashAmount",BigDecimal.ZERO));

		data.put("oneKeepAlive",keepAlive.getOrDefault("one","未查询到结果"));
		data.put("threeKeepAlive",keepAlive.getOrDefault("three","未查询到结果"));
		data.put("sevenKeepAlive",keepAlive.getOrDefault("seven","未查询到结果"));
		data.put("monthKeepAlive",keepAlive.getOrDefault("month","未查询到结果"));

		data.put("vipInfo",invest.getOrDefault("vipInfo","未查询到结果"));
		data.put("petUserInfo",invest.getOrDefault("petUserInfo","未查询到结果"));
		data.put("againInfo",invest.getOrDefault("againInfo","未查询到结果"));
		data.put("todayAdNum",userCacheService.getPlatformAdvertLookNum());
		return data;
	}
	
	/**
	 * 推送Manager实时变更信息到已登录的web控制台
	 * @author DOE
	 * @param command
	 */
	public static void massWeb(Command command){
		Map<String, AdminSocketServer> servers = SocketManager.getClients(TargetSocketType.adminWeb);
		if(servers != null){
			for (AdminSocketServer adminSocketServer : servers.values()) {
				if(adminSocketServer.isLogin()){
					Push.push(adminSocketServer, command);
				}
			}
		}
	}
	
	/**
	 * 推送Manager实时变更信息到指定角色的web控制台
	 * @author DOE
	 * @param command
	 */
	public static void massRole(Command command, Set<String> roleIds){
		Map<String, AdminSocketServer> servers = SocketManager.getClients(TargetSocketType.adminWeb);
		if(servers != null){
			for (AdminSocketServer adminSocketServer : servers.values()) {
				if(adminSocketServer.isLogin() && roleIds.contains(adminSocketServer.getAdmin().getRoleId())){
					Push.push(adminSocketServer, command);
				}
			}
		}
	}
	
	private static final Log logger = LogFactory.getLog(AdminSocketService.class);
	@Override
	protected Log logger() {
		return logger;
	}
}
