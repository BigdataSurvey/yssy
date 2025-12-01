这个后端项目采用分模块的分布式架构,主要划分为不同的模块,各自职责明确;具体不同模块之间的说明在我上传的"《修仙》后端项目架构与代码分析.pdf"中可以看到,这个PDF是同框架的另一个游戏的说明文档,你可以参考,请你深度分析该PDF;

我简单说一下,
其中ZY-APP-BASE、ZY-APP-DEFAULT、ZY-APP-WS、ZY-APP-SERVER、ZY-APP-MANAGER、ZY-APP-LOG、ZY-KEYFACTORY为核心模块;
ZY-APP-BASE：目录下存放项目的基础类和实体类,所有的 DB 实体类和一些基础的业务逻辑封装在这个模块中。
ZY-APP-DEFAULT：主要负责 DB 层（数据访问层）和缓存管理，MyBatis 和 Spring 配合使用，实现了大部分的数据库操作逻辑。
ZY-APP-WS：WebSocket 通信框架模块，定义了 Command、Executer、BaseServerSocket 等，该模块为所有模块提供了基于 WebSocket 的消息分发和处理机制。
ZY-APP-SERVER：主要提供外部服务的接口（例如登录、用户信息、背包、商城等），通过 WebSocket 和 Manager 进行通信。
ZY-APP-MANAGER：核心服务模块，处理游戏的核心业务逻辑，提供如 getLoginInfo、getUserInfo、getBackPackInfo 等业务接口。
ZY-APP-LOG：用于日志记录，跟踪用户的资产变动、操作记录等。
ZY-KEYFACTORY：用于生成和管理 wsid（WebSocket ID）等密钥的模块。

我目前重新升级了tomcat为tomcat-9.0.105版本,并且只配置了ZY-APP-MANAGER、 ZY-APP-SERVER、ZY-KEYFACTORY 这三个项目,这三个启动后项目就算是跑起来了;


该项目的登录流程概述：
1.yssy 登录流程概述：
1.1.客户端向 ZY-APP-MANAGER 发送登录请求：客户端首先向 ZY-APP-MANAGER 发送一个 HTTP 请求，请求路径为 /getServer，用于获取服务器信息、版本号和登录 URL。
1.2.服务器响应并返回登录地址：ZY-APP-MANAGER 响应包含版本号和 loginUrl（登录地址），然后客户端根据 loginUrl 继续访问 微信登录接口。
1.3.客户端调用 /wxLoginOauth 完成用户登录：客户端向 ZY-APP-MANAGER 的 /wxLoginOauth 接口发起登录请求，携带如 openId、gameToken 等参数，用于身份验证和注册/登录。
1.4.服务器根据用户信息生成 wsid：服务器验证用户信息后，会生成一个 wsid（WebSocket ID），并将其作为响应返回给客户端。
1.5.WebSocket 连接：客户端使用获取到的 wsid 和 wsPrivateKey 通过 WebSocket 与 ZY-APP-SERVER 建立连接。连接建立后，服务器可以根据 wsid 获取用户信息，并推送用户数据。

2.详细登录流程：
2.1 客户端调用 /getServer 接口
	客户端向 ZY-APP-MANAGER 发起 GET 请求：
		GET http://localhost:8080/ZY-APP-MANAGER/getServer
	请求参数：versionCode（版本号），deviceId（设备号），clientType（客户端类型，Web/App）
	返回内容（JSON）：
		{
			"version": "1.0.0",
			"loginUrl": "http://localhost:8080/ZY-APP-MANAGER/wxLoginOauth",
			"wsid": "some_generated_wsid"
		}
	version: 当前服务版本。
	loginUrl: 后续登录的 HTTP 路径，客户端需要通过该 URL 完成微信登录。
	wsid: WebSocket 会话 ID，客户端需要在后续与服务器建立 WebSocket 连接时使用。

2.2 客户端调用 /wxLoginOauth 完成登录
客户端使用从 /getServer 获取到的 loginUrl，发起 POST 请求到 ZY-APP-MANAGER 的 /wxLoginOauth：
POST http://localhost:8080/ZY-APP-MANAGER/wxLoginOauth

请求参数（示例）：
{
  "openId": "user_openid",
  "gameToken": "game_token", 
  "deviceId": "device123",
  "versionId": "1.0.0",
  "inviteCode": "invite_code"
}
返回内容（JSON）：
{
  "wsid": "generated_wsid",
  "userInfo": {
    "userId": "user123",
    "nickname": "Player123",
    "avatar": "http://avatar.com/image.jpg"
  }
}
wsid: 用户会话 ID（WebSocket ID），用来与服务器建立 WebSocket 连接。
userInfo: 返回用户基本信息，包括 userId、nickname 和 avatar 等

2.3服务器生成 wsid 并返回
服务器接收到客户端的登录请求后，会根据 openId 和 gameToken 来验证用户：
	如果是新用户，进行 注册，并生成 wsid。
	如果是老用户，直接验证其身份，生成 wsid。
服务器会根据客户端传来的 deviceId、versionId 等信息，生成一个 唯一的会话 ID (wsid)。

2.4客户端建立 WebSocket 连接
客户端收到 wsid 后，会使用该 wsid 和 wsPrivateKey（私钥）与 ZY-APP-SERVER 建立 WebSocket 连接。
客户端连接的 WebSocket 地址为：
ws://127.0.0.1:8083/ZY-APP-SERVER/LogServer?wsid=some_generated_wsid&pk=private_key
客户端通过该 WebSocket 连接，服务器会根据 wsid 验证用户身份，并返回用户相关数据。

2.5 服务器处理 WebSocket 连接
服务器收到 WebSocket 连接请求后，根据 wsid 查找对应的用户信息。
通过 wsid，服务器可以将 用户信息 和 会话 绑定，保证后续通信的正常进行。

3. 整体流程图（登录流程简化版）
客户端 → 发送 GET /getServer  →  服务器（ZY-APP-MANAGER）  
   ↓                                 ↓  
  获取版本与登录 URL  ← 返回版本、登录 URL、wsid  
   ↓                                 ↑  
客户端 → 发送 POST /wxLoginOauth →  服务器（ZY-APP-MANAGER）  
   ↓                                 ↓  
  发送 openId 和 gameToken        返回 wsid 和 userInfo  
   ↓                                 ↑  
客户端 → 使用 wsid 与服务器 WebSocket 连接  
   ↓                                 ↓  
连接建立成功   ← 通过 WebSocket 获取用户数据  


有一个很重要的概念：
PlayGameService是系统初始化缓存配置：
系统启动通过@PostConstruct配置的_InitGameInfoService方法一次性把各种表加载进静态 Map；把DB 里的各种“静态配置表”（道具、矿、角色、商城、成就、VIP 等）加载到JVM内存里，所有后端逻辑直接从这些 Map 取数据，避免频繁查库。

ManagerGameBaseService.syncTableInfo是登录时“客户端静态表同步器”：
这个方法从managerConfigService里拿当前服务器的表版本号（Config 表里的 ITEM_VERSION/MINE_VERSION/ROLE_VERSION后续还有其他的）
从 params 里拿客户端带上来的 tableInfo 版本号；
之后根据前面的两个值对每一张参与同步的表做判断；
也就是登录时的“冷启动拉取机制”:客户端带自己本地的表版本号 → 服务端对比配置中心版本 → 有差异就通过 tableInfo 把整张表推过去，让客户端更新本地缓存。这样就不用频繁请求后端。
注意这里有两个“数据源”：一方面的版本号来源是Config表加载到内存的Map;一方面的表内容来源是从PlayGameService里的静态Map;
也就是说，syncTableInfo 是站在“游戏服对玩家”的角度，读这两块内存数据，给玩家做表同步。

ManagerConfigService中的updateConfigData是Admin客户端后台(不是玩家游戏客户端)来调用的;
ManagerConfigService.updateGameKey：后台运维触发的“热更新控制中心”:
也就是说updateGameKey是被GM改了之后,根据不同的做不同的事情;比如:ITEM_VERSION 变更：重载物品表 + 推送新表给在线玩家这种;

PlayGameService：维护“后端当前用的静态表数据（Map）也就是服务器当前认为的正确最新的数据”；
syncTableInfo：拿这些 Map + 配置中心的版本号，把“需要同步的那几张表”打包返回给客户端。
ManagerConfigService.updateConfigData 是运行期间（配表/运营配置被修改）,运维在后台页面改了某条 Config（比如 ITEM_VERSION++），后台请求打到ManagerConfigService.updateConfigData(code=002)；、
updateConfigData就去改DB的Config表;更新Config的Map;并且推送updateConfig的消息;
那么紧接着updateGameKey(key, value) 看 key 是什么? 比如是ITEM_VERSION; 那么说明运营已经改了Item、手册表这些;但是现在内存里还是就数据，所以必须调用init重新来初始化;最新的 DB 数据重新加载到这些静态 Map 里；
然后再把新表通过 Push.updateTableVersion 推给所有在线玩家。

换句话说：
@PostConstruct 只负责“服务刚启动时”第一次初始化；
updateGameKey 是“服务已经跑起来以后，当配表/Config 被修改时，主动刷新内存数据”的入口。

那么我们现在来总结一下流程(以道具物品表Item为例):
1:在服务启动阶段
1.1:PlayGameService.@PostConstruct → initItem() 代表从 DB Item 表读全部数据 → 塞进 itemMap。
1.2:ManagerConfigService.@PostConstruct → setConfigCache 代表从DB Config 表读全部配置 → 塞进 CONFIG，包括 ITEM_VERSION = "3"。
那么此时,服务器内存里有一份itemMap(真实数据);和配置中心里有ITEM_VERSION = "3" (当前版本号)
2:玩家登录
2.1:客户端在请求的getInfo时带上了 "tableInfo": { "itemTable": "2" }
2.2:getInfo 中调用了 result.put("tableInfo", syncTableInfo(params));
2.3:syncTableInfo 从CONFIG 里读：itemV = managerConfigService.getString(Config.ITEM_VERSION)，得到 "3"；之后比较客户端的"2"和服务器的"3",不想等就需要同步; 用 new ArrayList<>(PlayGameService.itemMap.values()) 拿到最新 Item 列表,就可以返回：
"tableInfo": {
  "itemTable": {
    "version": "3",
    "data": [ /* 全量 Item 数据 */ ]
  }
}
2.4:客户端收到后把本地的itemTable更新成这份data、把本地的itemTableVersion更新为"3"、下次登录就带"3",如果服务器还是"3",那么就不会再发这张表;
3:运维修改了物品表(比如增加了一个道具)
3.1:运维通过后台管理系统修改Item表数据并把 Config.ITEM_VERSION 改为 "4"; 这个时候就会操作触发 Admin WebSocket 请求，调用 ManagerConfigService.updateConfigData(code=002)。
3.2:updateConfigData(key="ITEM_VERSION", value="4");那么这个方法就会去更新DB中的Config表、CONFIG.put("ITEM_VERSION", "4")、Push.updateConfig 给所有客户端（一般游戏端可以不用理它）;
3.3:updateGameKey("ITEM_VERSION", "4") 这个方法会调用gameService.initItem()，从 DB 把 Item 新表读进 itemMap、同时还会有initDicHandBook/initDicHandBookReward/initPrize 等等联动；之后调 Push.push(PushCode.updateTableVersion, null, tableInfo)，把新表推给所有当前在线玩家；

此时已在线玩家通过 push 收到 "itemTable" 的新表，可立即更新;
之后新登录玩家：通过 syncTableInfo 比对 "4"，决定是否下发表。
