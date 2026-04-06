# YSSY 后端项目开发与排障总览（重整理版 issues.md）

> 适用对象：接手 `yssy.zip` 后端项目、需要继续做主线开发 / 小游戏开发 / 配置表驱动玩法开发 / 问题排查的开发人员。
>
> 本文档基于当前源码、原 `issues.md`、`YSSY后端核心模块架构与功能详解.pdf`、`YSSY项目分析.md`、`addReward逻辑.md`、`yssy 小游戏（独立玩法）模块总览.md`、`全局可配置清单(数值).md` 重新整理。
>
> 本文档强调：**一切开发与排障必须以下钻真实代码为准，不用“猜的流程”。**

---

# 目录

1. 项目整体定位与部署方式
2. 核心模块职责
3. 登录 / 注册 / 自动登录总流程
4. WebSocket 接入与首页初始化流程
5. 静态表缓存、登录同步、运行期热更新
6. 奖励统一入口：`PlayGameService.addReward`
7. 资产、背包、配置中心的常用写法
8. 农场 / 种地 / 种子合成 / 种子兑换的配置驱动模式
9. VIP 卡 / 转赠 / 常见接口说明
10. 小游戏（`ZY-APP-DTS` / `ZY-APP-DTS2` / `ZY-APP-DTS3`）总览
11. 新增一个小游戏的标准落地清单
12. 常用类 / Service / 枚举速查表
13. 开发规范与协作约定

---

# 1. 项目整体定位与部署方式

YSSY 当前后端属于**多模块、分层、通过 WebSocket 串联的分布式老项目**。整体上是：

- `ZY-APP-SERVER`：客户端接入层 / 网关层
- `ZY-APP-MANAGER`：主逻辑服 / 核心业务服
- `ZY-APP-DEFAULT`：DAO + 缓存 + 基础服务层
- `ZY-APP-BASE`：实体、常量、工具类层
- `ZY-APP-WS`：自研 WebSocket 通讯与命令路由框架
- `ZY-APP-LOG`：日志/行为/资产变动记录辅助模块
- `ZY-KEYFACTORY`：独立 key 工具服务
- `ZY-APP-DTS / DTS2 / DTS3`：独立玩法服 / 小游戏服

## 1.1 当前推荐理解方式

不要把它理解成“一个单体 Java 项目”。
正确理解是：

- 客户端大多数业务走 `SERVER`
- `SERVER` 再通过内部 WS 转发给 `MANAGER`
- `MANAGER` 负责真实业务处理、资产、背包、静态表、配置热更新
- 独立玩法服（DTS/DTS2/DTS3）也通过 WS 与 `SERVER` 和 `MANAGER` 协作

## 1.2 当前最小可运行部署

你当前实际使用的最小可运行组合是：

- `ZY-APP-MANAGER`
- `ZY-APP-SERVER`
- `ZY-APP-LOG`
- `ZY-KEYFACTORY`

其中：

- `BASE / DEFAULT / WS` 通常作为依赖 jar 被上层 WAR 引用
- DTS / DTS2 / DTS3 作为可选玩法服，没启动不会影响登录、首页、背包、商城等核心能力

---

# 2. 核心模块职责

## 2.1 `ZY-APP-BASE`

定位：**基础实体 + 常量 + 通用工具**。

主要内容：

- 所有核心实体 Bean：`User`、`Backpack`、`UserCapital`、`Config`、`Item`、`DicFarm`、`UserFarmLand`、`WsidBean` 等
- 通用工具：`BeanUtils`、`DateUtil`、`LockUtil` 等
- 基础常量：配置 key、表名、错误信息、上下文常量
- `BaseServlet` / `BaseCommandServlet` 等父类

使用原则：

- `BASE` 主要提供“结构”和“工具”，不承担重业务逻辑
- 任何跨模块共用的实体、枚举、常量优先沉淀在这里或 DEFAULT 的枚举层

## 2.2 `ZY-APP-DEFAULT`

定位：**数据访问层 + Redis 缓存层 + 基础业务服务层**。

主要内容：

- MyBatis Mapper / XML
- 各表的 `*Service`
- Redis / 本地缓存服务
- 通用注解：`@ServiceClass`、`@ServiceMethod`

典型类：

- `UserService`
- `ConfigService`
- `UserCapitalService`
- `BackpackService`
- `UserVipService`
- `TVipGiftRecordService`
- `UserCacheService`
- `UserCapitalCacheService`
- `WsidCaCheService`
- `WSService`

结论：

- `DEFAULT` 是“读写 DB + 维护缓存 + 提供基础操作”的中台层
- `MANAGER` 做业务编排时大量依赖 `DEFAULT`

## 2.3 `ZY-APP-WS`

定位：**全项目自研 WebSocket 框架与命令总线**。

关键类：

- `Command`
- `Executer`
- `Push`
- `PushCode`
- `TargetSocketType`
- `BaseSocket`
- `BaseServerSocket`
- `BaseClientSocket`

作用：

- 封装内部服务间的长连接通信
- 用“命令码 + ServiceClass/ServiceMethod”做请求路由
- 用 `PushCode` 做全服 / 定向推送

可以把它理解成：

> “项目自己的轻量 RPC + 推送总线”

## 2.4 `ZY-APP-SERVER`

定位：**客户端网关 / 外部 WebSocket 接入层**。

主要职责：

- 提供 `AppSocket` 给客户端建立长连接
- 校验 `wsid / wsPrivateKey`
- 将客户端业务请求转发到 `MANAGER`
- 将 `MANAGER` 或独立玩法服的推送转推给玩家
- 按 `gameId` 路由到具体玩法服

核心类：

- `com.zywl.app.server.socket.AppSocket`
- `GameBaseService`
- `GameFarmService`
- `ServerUserVipService`
- `ServerLotteryGameService`
- `ServerManagerService`
- `ServerContext`

理解方式：

- `SERVER` 自己不做绝大部分核心业务
- `SERVER` 的主要工作是“拿到当前连接用户信息 → 转发 → 回包 / 推送”

## 2.5 `ZY-APP-MANAGER`

定位：**主逻辑服 / 核心业务中枢**。

主要职责：

- 登录、注册、自动登录
- 静态表初始化
- 首页聚合回包
- 资产 / 背包 / 道具 / 农场 / 商城 / VIP / 活动等核心逻辑
- 配置中心和热更新
- 向在线玩家推送数据变化

核心类：

- `AuthService`
- `LoginService`
- `PlayGameService`
- `ManagerGameBaseService`
- `ManagerConfigService`
- `ManagerGameFarmService`
- `ManagerUserVipService`

结论：

- 后续主线业务开发，优先找 `MANAGER`
- 真正影响玩家数据的核心逻辑，大多落在 `MANAGER`

## 2.6 `ZY-APP-LOG`

定位：**日志与行为记录辅助模块**。

主要职责：

- 接收行为日志
- 记录资产变动日志 / 背包变动日志
- 配合核心服完成审计与追踪

## 2.7 `ZY-KEYFACTORY`

定位：**独立 key 工具服务**。

说明：

- 当前源码主登录链里，正式 `wsid/wsPrivateKey` 的生成发生在 `AuthService.createWsid(...)`
- `ZY-KEYFACTORY` 仍作为独立的 key 工具服务保留
- 不应把它理解成当前唯一的 wsid 生成中心

## 2.8 `ZY-APP-DTS / ZY-APP-DTS2 / ZY-APP-DTS3`

定位：**小游戏 / 独立玩法服务器模块**。

共同特点：

- 各自开放 `@ServerEndpoint`
- 各自维护玩法状态和房间状态
- 涉及用户主数据、资产结算时通过 `ManagerSocket` 回主逻辑服处理

---

# 3. 登录 / 注册 / 自动登录总流程

这一部分必须以真实代码理解，不按旧文档的概括版本理解。

## 3.1 HTTP 阶段：`/afdagfwae`

入口：

- `ZY-APP-MANAGER/src/com/zywl/app/manager/servlet/AuthServlet.java`
- 路径：`/afdagfwae`

调用：

- `AuthService.checkVersionAndCreateWsid()`

当前源码真实行为：

- 返回版本信息
- 返回 `loginUrl`
- **当前源码里这个接口主要是“版本检查 + 提供登录地址”**
- 正式 `wsid` 不是在这里生成并返回给客户端的

## 3.2 登录入口：`/wxLoginOauth`

入口：

- `LoginOauthServlet`
- `WXLoginOauthServlet`

支持的登录方式：

- `gameToken` 自动登录
- 微信 openId 登录 / 注册
- TabTab 登录 / 注册
- 其他平台登录入口按 Servlet 分开处理

## 3.3 自动登录逻辑

后端逻辑：

- 登录成功后会给用户写 `game_token` 和 `token_time`
- 有效期默认 7 天
- 客户端下次启动时如果本地缓存了 `gameToken`，应再次调用 `/wxLoginOauth` 并携带 `gameToken`
- 后端走 `LoginService.loginByGameToken(...)`

注意：

- 自动登录的前提是客户端必须把 `gameToken` 缓存并在启动时传回
- 后端不会“自己识别自动登录”，而是取决于客户端有没有发 `gameToken`

## 3.4 新用户注册逻辑

核心方法：

- `LoginService.register(...)`
- `UserService.insertUserInfo(...)`
- `LoginService.initUserInfo(...)`

流程：

1. 生成新的 `gameToken`
2. 创建 `User`
3. 初始化配置、资产等基础数据
4. 调 `AuthService.createWsid(...)` 生成正式 `wsInfo`
5. 返回：
   - `userInfo`
   - `wsInfo`

## 3.5 正式 `wsid/wsPrivateKey` 生成逻辑

真实生成位置：

- `AuthService.createWsid(Long userId, String oldWsid, String versionId)`

主要行为：

- 选一个合适的 `SERVER` 节点
- 构造新的 `WsidBean`
- 设置：
   - `wsid`
   - `wsPrivateKey`
   - `authServerAddress`
   - `authServerHost`
   - `versionId`
   - `userId`
- 写入 `WSService` / `WsidCaCheService`

## 3.6 客户端长连接入口

入口：

- `ZY-APP-SERVER/src/com/zywl/app/server/socket/AppSocket.java`
- `@ServerEndpoint(value = "/APPServer" + ...)`

注意：

- 当前真实客户端长连接入口是 `/APPServer`
- 不是 `LogServer`

## 3.7 握手校验逻辑

`AppSocket.onConnect(...)` 的关键校验：

- 根据 URL 中的标识取到 `wsidBean`
- 将握手体里的 `wsPrivateKey` 与 `wsidBean.getWsPrivateKey()` 比较
- 校验通过后：
   - 从 `UserCacheService` 取用户
   - 绑定当前 socket 与用户
   - 进入登录态

---

# 4. WebSocket 接入与首页初始化流程

## 4.1 登录后首页初始化的外部入口

客户端在 WS 建连成功后，会调用：

- `GameBaseService` 对应的登录后首页初始化接口

关键类：

- `ZY-APP-SERVER/src/com/zywl/app/server/service/GameBaseService.java`
- `ZY-APP-MANAGER/src/com/zywl/app/manager/service/manager/ManagerGameBaseService.java`

## 4.2 `SERVER` 的职责

`GameBaseService` 做的事通常是：

1. 从 `AppSocket` 拿 `WsidBean`
2. 拿 `userId`
3. 补进请求参数
4. 用 `Executer.request(TargetSocketType.manager, ...)` 转发给 `MANAGER`

即：

> `SERVER` 做接入和转发，不做首页主逻辑

## 4.3 `MANAGER` 的真实首页聚合方法

核心方法：

- `ManagerGameBaseService.getInfo(...)`

它会组装：

- `userInfo`
- `userCapitals`
- `vip` / `isVip`
- `tableInfo`
- `backpackInfo`
- `farmInfo`
- 最近聊天 / 公告 / 开关等

这就是玩家登录后首页的“大包回包”。

---

# 5. 静态表缓存、登录同步、运行期热更新

这是 YSSY 当前最重要的基础机制之一。

## 5.1 `PlayGameService`：服务端静态表缓存中心

关键方法：

- `_InitGameInfoService()`
- `initItem()`
- `initMine()`
- `initRole()`
- `initShop()`
- `initDicVip()`
- `initDicHandBook()`
- `initDicHandBookReward()`
- `initFarm()`

典型静态 Map：

- `itemMap`
- `DIC_MINE`
- `DIC_ROLE`
- `DIC_SHOP_LIST`
- `DIC_SHOP_MAP`
- `DIC_VIP_MAP`
- `DIC_HAND_BOOK_MAP`
- `DIC_HAND_BOOK_REWARD_MAP`
- `DIC_FARM`
- `DIC_PET`

作用：

- 服务启动时一次性从 DB 把静态表加载到 JVM 内存
- 后续大多数逻辑直接从这些 Map 取数据
- 避免频繁查库

## 5.2 `ManagerConfigService`：配置中心内存缓存

关键方法：

- `_construct()`
- `setConfigCache(...)`
- `getString(...)`
- `getInteger(...)`
- `updateConfigData(...)`
- `updateGameKey(...)`

核心 Map：

- `CONFIG`

作用：

- 启动时从 `Config` 表加载所有配置项
- 登录同步时提供“当前表版本号”
- 运行时作为热更新的统一控制中心

## 5.3 登录时的静态表同步：`syncTableInfo`

关键方法：

- `ManagerGameBaseService.syncTableInfo(JSONObject params)`

核心逻辑：

1. 从 `CONFIG` 读取服务端版本号，例如：
   - `ITEM_VERSION`
   - `MINE_VERSION`
   - `ROLE_VERSION`
   - `FARM_TABLE_VERSION`
   - `PET_TABLE_VERSION`
   - `VIP_TABLE_VERSION`
2. 从客户端请求参数里拿本地 `tableInfo`
3. 对比客户端版本与服务端版本
4. 如果某张表版本不一致，就从 `PlayGameService` 静态 Map 中取出整表数据
5. 按 `{version,data}` 格式回给客户端

注意：

- 这里不查 DB
- 只读两块内存：
   - 版本号：`CONFIG`
   - 表数据：`PlayGameService` 静态 Map

## 5.4 运行期热更新：`updateConfigData + updateGameKey`

后台修改配置的入口：

- `ManagerConfigService.updateConfigData(...)`

热更新真正联动逻辑：

- `ManagerConfigService.updateGameKey(String key, String value)`

常见动作：

### 5.4.1 清缓存类

- `REFRESH_USER_ITEM` → 清 `PlayGameService.playerItems`
- `REFRESH_USER_CAPITAL` → 清 `UserCapitalCacheService.userCapitals`
- `REFRESH_USER_COIN` → 清 `PlayGameService.playercoins`

### 5.4.2 重载静态表类

- `ITEM_VERSION` → `initItem()` + 手册/奖池联动 + 推送新 `itemTable`
- `SHOP_VERSION` → `initShop()`
- `MINE_VERSION` → `initMine()` + 推送新 `mineTable`
- `ROLE_VERSION` → `initRole()` + 推送新 `roleTable`
- `FARM_TABLE_VERSION` → `initFarm()` + 推送新 `farmTable`
- `PET_TABLE_VERSION` → `initPet()` + 推送新 `petTable`

### 5.4.3 全服通知类

- `SERVICE_STATUS` → 维护 / 强制下线
- `APP_VERSION` → 版本缓存刷新
- 公告类 key → 直接推通知给在线玩家

结论：

- `@PostConstruct` 只负责服务启动时第一次初始化
- 服务运行中要刷新配置、重载表、推送在线玩家，走 `updateGameKey`

---

# 6. 奖励统一入口：`PlayGameService.addReward`

这是后续新需求开发时最重要的公共能力之一。

## 6.1 定位

`addReward` 是：

> “全项目统一的奖励发放入口 / 清算中心 / 总闸门”

任何玩法、任务、活动、邮件、农场、抽奖、礼包等给玩家发奖励，优先都应走它。

## 6.2 方法签名

核心方法：

- `PlayGameService.addReward(Long userId, JSONArray array, LogCapitalTypeEnum capitalSource, LogUserBackpackTypeEnum backpackSource)`

## 6.3 标准奖励 JSON 结构

统一约定：

```json
[
  {"type":1,"id":"2101","number":10},
  {"type":1,"id":"1001","number":100}
]
```

字段含义：

- `type`：当前统一按 `1` 使用
- `id`：奖励目标 ID（本质上是 `dic_item.id / itemId`）
- `number`：数量
- `channel` / `fromUserId`：可选，用于来源记录

## 6.4 资产还是背包，怎么判断？

不是调用方写死，而是 `addReward` 内部通过静态道具表判断：

- 先用 `id` 查 `PlayGameService.itemMap`
- 看对应 `Item.type`

当前约定：

- `type = 4`：资产货币 → 走资产入口
- 其他：
   - `1` 果实 / 材料
   - `2` 种子 / 基础道具
   - `3` 功能道具
   - `5` 礼包（预留）
     → 全部走背包入口

## 6.5 `addReward` 内部做了什么

### 6.5.1 资产型奖励

- 调 `UserCapitalService` 增加资产
- 写资产日志
- 更新资产缓存
- 调 `ManagerGameBaseService.pushCapitalUpdate(...)` 推给客户端

### 6.5.2 道具型奖励

- 调 `updateUserBackpack(...)` 更新背包
- 写背包日志
- 获取最新背包快照
- 推送背包更新给客户端

## 6.6 业务约束

后续新功能开发应尽量遵守：

> 以上对资产和道具的添加 / 扣除，优先走统一模式，不在各玩法里散落写“直接改资产 / 直接推送”的重复逻辑。

推荐开发模式：

1. 配表中写 `reward`
2. 业务里读取 `reward`
3. 组装成 `JSONArray`
4. 调 `addReward(...)`

---

# 7. 资产、背包、配置中心的常用写法

这一部分是日常开发最常用的模板。

## 7.1 用户校验

```java
Map<Long, User> users = userCacheService.loadUsers(userId);
```

或者在业务中统一封装：

```java
User user = loadAndCheckUser(userId);
```

## 7.2 查 VIP

```java
userVipService.findUserVipByUserId(userId);
```

注意：

- 该方法当前实现可在无记录时自动创建默认记录

## 7.3 取配置

```java
managerConfigService.getInteger(Config.IP_LOGIN_RISK);
managerConfigService.getString(Config.ITEM_VERSION);
```

## 7.4 取背包

```java
gameService.getReturnPack(userId);
```

说明：

- `gameService` 在多数场景下就是 `PlayGameService`
- `getReturnPack` 是“获取玩家当前背包快照”的标准方法

## 7.5 校验道具是否足够

```java
gameService.checkUserItemNumber(userId, itemId, number);
```

## 7.6 更新背包

```java
gameService.updateUserBackpack(userId, itemId, number, backpackSource);
```

## 7.7 推送背包更新

```java
managerGameBaseService.pushBackpackUpdate(userId, itemId, number, 1);
```

或：

```java
Push.push(PushCode.updateUserBackpack, managerSocketService.getServerIdByUserId(userId), pushData);
```

## 7.8 查资产

```java
userCapitalService.findUserCapitalByUserIdAndCapitalType(userId, capitalTypeId);
```

## 7.9 扣资产

```java
userCapitalService.subUserBalance(...)
```

建议：

- 独立业务尽量在 `UserCapitalService` 中补一个语义清晰的方法
- 例如：
   - `subUserBalanceBySendMail`
   - `subUserBalanceByBuyLion`
   - `subUserBalanceByVipTransfer`

不要把复杂扣费逻辑全塞在上层业务里。

## 7.10 增资产

```java
userCapitalService.addUserBalance(...)
userCapitalService.addUserBalanceByAddReward(...)
```

## 7.11 清理资产缓存

```java
userCapitalCacheService.deltedUserCapitalCache(userId, capitalTypeId);
```

## 7.12 更新配置

```java
managerConfigService.updateConfigData(key, value);
```

---

# 8. 农场 / 种地 / 种子合成 / 种子兑换的配置驱动模式

这部分结合当前“种地需求”与新增文档统一整理。

## 8.1 农场相关核心文件

- `ZY-APP-BASE/src/com/zywl/app/base/bean/card/DicFarm.java`
- `ZY-APP-DEFAULT/src/com/zywl/app/defaultx/service/card/DicFarmService.java`
- `ZY-APP-DEFAULT/src/com/zywl/app/defaultx/mapper/DicFarmMapper.xml`
- `ZY-APP-BASE/src/com/zywl/app/base/bean/UserFarmLand.java`
- `ZY-APP-DEFAULT/src/com/zywl/app/defaultx/service/UserFarmLandService.java`
- `ZY-APP-DEFAULT/src/com/zywl/app/defaultx/mapper/UserFarmLandMapper.xml`
- `ZY-APP-SERVER/src/com/zywl/app/server/service/GameFarmService.java`
- `ZY-APP-MANAGER/src/com/zywl/app/manager/service/manager/ManagerGameFarmService.java`

## 8.2 农场需求标准链路

### 8.2.1 启动加载静态表

- `PlayGameService.initFarm()`
- 静态缓存：`DIC_FARM`

### 8.2.2 登录同步静态表

- `ManagerGameBaseService.syncTableInfo(...)`
- 下发：`farmTable`
- 版本号来源：`CONFIG`
- 数据来源：`DIC_FARM`

### 8.2.3 运行期热更新

- `updateGameKey(FARM_TABLE_VERSION)`
- 重载 `DIC_FARM`
- 推送在线玩家新 `farmTable`

### 8.2.4 收割奖励

- `dic_farm.reward` 配 `JSONArray`
- 收割时读取该 `reward`
- 最终调用：`addReward(...)`

## 8.3 `dic_farm` 的设计理解

每一行代表：

- 某个种子 `seed_item_id`
- 生长时间 `grow_seconds`
- 成熟总产出 `reward`

当前业务语义：

- 播种时扣 1 个种子
- 作物进入计时
- 收割时按时间比例累计产出
- 产出最终走 `addReward`

## 8.4 一阶种子兑换

走：

- `ManagerGameFarmService.exchangeSeed(code="005")`

配置：

- `t_config.SEED_EXCHANGE_CONFIG`

核心语义：

- 用资产购买基础种子
- 扣费后仍然通过 `addReward([{type:1,id:seedItemId,number:number}])` 发种子

## 8.5 种子合成

当前规则：

- 三合一
- 支持明概率 + 暗概率
- 失败时主种子保底
- 奖池走 `SEED_SYN_POOL`

相关配置：

- `dic_item.can_syn`
- `dic_item.syn_use`
- `dic_item.syn_rate`
- `dic_item.price`
- `SEED_SYN_DARK_SWITCH`
- `SEED_SYN_DARK_RATE_LV2~LV5`
- `SEED_SYN_FAIL_POOL_RATE`
- `SEED_SYN_POOL`

理解方式：

- 合成公式由 `dic_item` 定义
- 成功率由静态表 + 配置中心共同决定
- 结算结果仍应统一回到背包 / 资产 / `addReward` 体系

---

# 9. VIP 卡 / 转赠 / 常见接口说明

## 9.1 常见道具与资产约定

当前常用枚举：

### `ItemIdEnum`

- `CORE_POINT = 1001` 核心积分
- `GAME_CONSUME_COIN = 1002` 游戏消耗货币
- `SECOND_POINT = 1003` 二级积分
- `SEED_*` / `MATERIAL_*`
- `VIP1_CARD = 4001`
- `VIP2_CARD = 4002`

### `UserCapitalTypeEnum`

当前仍保留旧资产类型：

- `currency_1 = 1` 文币（已弃用）
- `currency_2 = 2` 通宝（已弃用）
- `yyb = 3` 游园券（已弃用）
- `rmb = 4` 游园币（已弃用）
- `score = 5` 积分（已弃用）

当前主要使用：

- `hxjf = 1001` 核心积分(小丑币)
- `xxxhhb = 1002` 游戏消耗货币(弹珠)
- `ejjf = 1003` 二级积分

开发时必须注意：

- 旧枚举值仍在代码中存在，但很多已废弃
- 新需求优先使用 `1001 / 1002 / 1003`

## 9.2 VIP 卡转赠相关接口

`SERVER` 侧：

- `ServerUserVipService`

典型接口：

- `001`：VIP 面板信息
- `005`：VIP 卡确认激活
- `078007`：VIP 转赠记录列表（由 `SERVER` 转发到 `MANAGER` 的 `9008007`）

`MANAGER` 侧：

- `ManagerUserVipService`

基础数据服务：

- `TVipGiftRecordService`

排查说明：

- 转赠记录列表本质是“按条件分页查列表”
- 是否只看自己，取决于请求参数（如 `direction`）和 `MANAGER` 侧构造的 query 条件
- 排查时不要直接怀疑 Mapper，要先看 `SERVER` 传了什么、`MANAGER` 组了什么查询条件

---

# 10. 小游戏（`ZY-APP-DTS` / `ZY-APP-DTS2` / `ZY-APP-DTS3`）总览

这部分是后续小游戏开发与接入的共识骨架。

## 10.1 运行时角色分工

### `ZY-APP-SERVER`

作用：

1. 玩家只连接它
2. 按 `gameId` 路由到目标玩法服
3. 把玩家请求封装成 WS 指令发给玩法服
4. 做 push 的二跳转发

### `ZY-APP-DTS / DTS2 / DTS3`

作用：

1. 提供 `@ServerEndpoint` 给接口服连接
2. 通过 `@ServiceClass + @ServiceMethod` 提供玩法指令处理
3. 维护房间 / 状态 / 结算 / 状态机
4. 通过 `ManagerSocket` 回连主逻辑服处理用户主数据与资产结算

### `ZY-APP-MANAGER`

作用：

- 提供统一用户数据
- 提供资产扣减 / 入账
- 提供结算、日志、风控、任务协同

## 10.2 当前小游戏模块关键类

### `ZY-APP-DTS`

- `BattleRoyaleSocketServer`
- `BattleRoyaleService`
- `BattleRoyaleRequsetMangerService`
- `ManagerSocket`
- `ServerManagerService`
- `ServerStateService`

### `ZY-APP-DTS2`

- `BattleRoyaleSocketServer2`
- `BattleRoyaleService2`
- `PbxService`
- `BattleRoyaleRequsetMangerService2`
- `ManagerSocket2`
- `ServerStateService`

### `ZY-APP-DTS3`

- `BattleRoyaleSocketServer2`
- `BattleRoyaleService2`
- `BattleRoyaleRequsetMangerService2`
- `ManagerSocket2`
- `ServerStateService`

说明：

- `DTS2` 当前还承载了 PBX 示例/玩法
- `DTS3` 架构与 `DTS2` 非常接近，但连接主服的端点和部署目标不同

## 10.3 玩法服端点

当前已知端点：

- `ZY-APP-DTS/src/.../BattleRoyaleSocketServer.java`
   - `/BattleRoyaleServer...`
- `ZY-APP-DTS2/src/.../BattleRoyaleSocketServer2.java`
   - `/BattleRoyale2Server...`
- `ZY-APP-DTS3/src/.../BattleRoyaleSocketServer2.java`
   - `/BattleRoyale2Server...`

## 10.4 `SERVER` 如何路由到玩法服

关键点：

- `TargetSocketType.getServerEnum(gameId)`
- `Executer.request(TargetSocketType.xxx, CommandBuilder.builder().request(...).build())`

当前明确的映射示例：

- `gameId == 12` → `TargetSocketType.dts2`

这说明：

- 游戏玩法的接入，不是硬编码到某个 Servlet
- 而是通过 `gameId -> TargetSocketType -> WS命令码` 三段式路由

## 10.5 PBX（推箱子）在 DTS2 的接入方式

当前约定：

- `PbxService`：`@ServiceClass(code = "102")`
- `102101`：join
- `102103`：op
- `102104`：leave
- `gameId = 12`

标准调试链路：

- `SERVER` 提供 `/pbxDebug`
- `SERVER` 转发 `102101/102103/102104`
- `DTS2` 执行 `PbxService`
- push 再通过 `SERVER` 转回玩家

## 10.6 Push 两跳机制

### 第一跳：玩法服 → `SERVER`

典型 push：

- `PushCode.updatePbxInfo`
- `PushCode.updatePbxStatus`

### 第二跳：`SERVER` → 玩家 `AppSocket`

常见路由条件：

- 全量玩法信息：按 `gameId`
- 定向玩家状态：按 `userId`

结论：

- 小游戏 push 是否“到玩家”，必须分两段看
- 第一段通了，只代表玩法服 push 到了 `SERVER`
- 第二段还要看 `SERVER` 有没有做对 `condition` 绑定和二跳转发

## 10.7 为什么玩法服必须连 `MANAGER`

因为玩法服不应该自己直接改主业务数据库。

涉及以下场景时，应回主服：

- 校验用户主数据
- 扣资产
- 发奖励
- 结算入账
- 日志 / 任务 / 风控联动

这也是小游戏开发时必须坚持的边界：

> 玩法服做状态机与玩法规则，主服做统一数据与统一记账。

---

# 11. 新增一个小游戏的标准落地清单

这是后续小游戏开发的推荐 checklist。

## 11.1 协议层

1. 分配新的 `gameId`
2. 在 `TargetSocketType` 中确认映射
3. 规划指令空间：
   - 例如 `@ServiceClass(code = "103")`
   - `103101` join
   - `103103` op
   - `103104` leave
4. 规划 pushCode

## 11.2 `SERVER` 侧

1. 启动时建立到玩法服的连接
2. 补 `gameId -> TargetSocketType` 路由
3. 在 `ServerLotteryGameService` 或对应转发服务中补转发逻辑
4. 需要时增加 Debug Servlet
5. 做 push 注册和 condition 绑定

## 11.3 玩法服侧

1. 新建 `@ServerEndpoint`
2. 新建 `@ServiceClass / @ServiceMethod`
3. 维护玩法状态机
4. 新建 `ManagerSocket`
5. 涉及扣费 / 发奖 / 结算时回 `MANAGER`
6. 注册玩法相关的 `PushCode`

## 11.4 测试建议

### 无前端时

- 先验证：`SERVER ↔ 玩法服`
- 再验证：玩法服 push → `SERVER`
- 最后如需验证“玩家是否真收到 push”，补一个最小 WS 模拟器

### 典型调试方式

- 通过 Debug Servlet 发 join / op / leave
- 看 `SERVER` 日志是否收发成功
- 看玩法服日志是否执行成功
- 看 push 是否到达 `SERVER`

---

# 12. 常用类 / Service / 枚举速查表

## 12.1 核心业务类

- `AuthService`：版本检查、创建 `wsid`
- `LoginService`：登录 / 注册 / 自动登录
- `PlayGameService`：静态表、背包、奖励、部分业务缓存核心
- `ManagerGameBaseService`：首页、表同步、背包/资产推送
- `ManagerConfigService`：配置中心、热更新
- `ManagerGameFarmService`：农场/种地业务
- `ManagerUserVipService`：VIP 业务

## 12.2 常用缓存 / 基础服务

- `UserCacheService`
- `UserCapitalCacheService`
- `UserCapitalService`
- `UserVipService`
- `WsidCaCheService`
- `WSService`
- `TVipGiftRecordService`

## 12.3 常用枚举

- `ItemIdEnum`
- `UserCapitalTypeEnum`
- `PushCode`
- `TargetSocketType`

## 12.4 常用 PushCode

当前常见：

- `updateTableVersion`
- `updateUserCapital`
- `updateUserBackpack`
- `syncAppOnline`
- `fcAppLoginOut`
- `updatePbxInfo`
- `updatePbxStatus`

---

# 13. 开发规范与协作约定

这是后续分析、排障、设计、开发、联调时必须严格遵守的共识。该规范优先级高于一般性的“经验做法”，后续所有讨论都应回到真实源码、真实表结构、真实调用链。

## 13.1 总原则：源码驱动分析，拒绝臆测

请始终以“资深软件架构师 + 代码审计专家”的工作方式分析本项目，所有结论必须建立在以下基础之上：

### 13.1.1 源码驱动分析

所有结论、观点、建议，必须直接基于：

- 项目中的真实源码
- 我提供的数据库文件 / 表结构 / SQL 文件
- 已上传的项目分析文档 / 架构文档 / 业务说明文档

分析时必须尽量落到以下维度：

- 具体文件路径
- 类名
- 方法名
- 关键代码片段
- 表名 / 字段名
- 命令码 / Servlet 路径 / WebSocket 端点
- 调用链上下游关系

不允许只讲概念，不允许只讲“通常会怎样”，必须回答“当前项目实际上是怎样”。

### 13.1.2 拒绝臆测

严禁基于假设、通用经验、行业套路做空泛分析。

如果信息不足，必须明确指出：

- 还缺哪个源码文件
- 还缺哪个模块
- 还缺哪张表的表结构
- 还缺哪段 SQL / Mapper / Service / Servlet / Socket 逻辑
- 还缺哪个请求参数 / 响应包 / 日志 / 异常栈

不清楚就明确说明“不足以得出结论”，然后指出需要继续查看的真实文件。

### 13.1.3 Bug 修复的输出结构

分析或解决 Bug 时，回复必须尽量按以下结构组织：

1. 问题 / 现象
2. 源码定位（文件 / 类 / 方法 / 关键片段）
3. 深度逻辑分析（调用链、触发条件、根因）
4. 结论 / 修复方案
5. 影响范围
6. 回归测试建议

必须通过真实源码下钻，定位到具体根因代码行或根因逻辑分支；必须解释 Bug 的技术触发原理，不能只给表面修复建议。

### 13.1.4 新功能开发的输出要求

当讨论新功能开发时，必须：

- 先结合现有架构风格与接口约定
- 再给出可直接嵌入当前代码库的实现思路
- 优先复用现有 Service / 枚举 / 推送 / 缓存 / 静态表 / Config 模式
- 尽量避免新功能绕开既有主线，单独发明一套平行机制

---

## 13.2 新功能实现的标准规范

### 13.2.1 静态表类需求必须补齐“三段式”

如果一个新功能引入了需要前端缓存或登录时同步的静态表，则必须同时补齐以下三段：

1. **启动加载**
   - 在 `PlayGameService.java` 中增加对应静态 `Map/List`
   - 在 `@PostConstruct` 初始化阶段增加 `initXxx()` 加载逻辑
   - 从数据库读取完整配置，缓存到 JVM 静态内存

2. **登录下发**
   - 在 `ManagerGameBaseService.syncTableInfo(...)` 中增加版本判断与表数据下发
   - 从 `ManagerConfigService.CONFIG` 读取版本号
   - 从 `PlayGameService` 静态 Map 读取表数据
   - 返回给客户端的 `tableInfo`

3. **运行时热更新**
   - 在 `ManagerConfigService.updateGameKey(...)` 中增加热更新分支
   - 变更配置版本后，重新加载该表对应内存
   - 通过 `Push.updateTableVersion` 或等效推送通知在线玩家更新

凡是新增一张前端需要缓存的静态表，必须把这三段补齐，否则该功能不算完整接入。

### 13.2.2 涉及奖励的配置表，必须有 `reward` 字段

如果需求涉及任务奖励、活动奖励、合成奖励、兑换奖励、邮件奖励、玩法结算奖励等，则推荐在配置表中增加 `reward` 字段，并统一使用奖励 JSON 结构，例如：

```json
[{"type":1,"id":2101,"number":10}]
```

之后在具体逻辑中统一调用：

```java
gameService.addReward(userId, rewardArr, LogCapitalTypeEnum.xx, LogUserBackpackTypeEnum.xx);
```

不要在各处业务逻辑里手工拆开“加资产 / 加背包 / 写日志 / 推送”，优先统一收口到 `PlayGameService.addReward(...)`。

### 13.2.3 所有具体业务逻辑先做用户校验

所有需要玩家态的具体逻辑，在读取玩家数据、扣资产、发奖励、推进玩法状态之前，优先做非空判断与用户加载校验。

常见做法：

- 使用 `loadAndCheckUser()` 风格的方法先校验用户
- 或先通过 `userCacheService.loadUsers(userId)` / `loadUser(userId)` 获取用户并判空
- 对依赖用户上下文的功能，必须先确认用户存在、状态正常、缓存数据可用

### 13.2.4 异常抛出规范

如果异常信息是**偏排障 / 偏服务端日志用途**，并且会返回到前端做调试或联调用途，则建议使用带上下文信息的格式，例如：

```java
throwExp(String.format("xxx 参数错误, xxx原因.{用户ID: %s, 当前xxx类型: %s}", userId, xxx));
```

如果异常信息是**直接给用户看的业务提示**，则应使用简洁明确的业务提示语，不要把 `userId`、内部字段、服务端上下文直接暴露给用户，例如：

```java
throwExp("资产不足");
```

### 13.2.5 资产扣费统一在 `UserCapitalService.java` 中封装

凡是扣费，不要在上层业务中直接东拼西凑地调用多个资产方法。推荐在 `UserCapitalService.java` 中为具体业务新增独立语义的方法，例如：

- `subUserBalanceByOpenVip(...)`
- `subUserBalanceBySendMail(...)`
- `subUserBalanceByBuyLion(...)`

这类方法内部再去调用：

- `getUserCapitalCacheByType(...)`
- `subUserBalance(...)`
- 日志枚举
- 订单号 / 业务流水号
- 必要的缓存清理与推送

这样可以保证每种扣费场景都有独立语义，便于审计、排障、扩展。

---

## 13.3 常用标准写法与推荐模式

### 13.3.1 扣费标准写法

```java
userCapitalService.subUserBalanceByOpenVip(userId, price, capitalTypeId, orderNo, null, LogCapitalTypeEnum.buy_vip);
```

### 13.3.2 清理资产缓存

```java
userCapitalCacheService.deltedUserCapitalCache(userId, capitalTypeId);
```

### 13.3.3 推送资产更新

```java
managerGameBaseService.pushCapitalUpdate(userId, capitalTypeId);
```

### 13.3.4 获取用户最新资产余额

```java
UserCapital capital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalTypeId);
```

### 13.3.5 `addReward` 发奖

```java
gameService.addReward(userId, rewardArr, LogCapitalTypeEnum.xx, LogUserBackpackTypeEnum.xx);
```

### 13.3.6 用户校验 / 用户缓存

```java
Map<Long, User> users = userCacheService.loadUsers(userId);
```

### 13.3.7 查询用户 VIP 信息

```java
userVipService.findUserVipByUserId(userId);
```

### 13.3.8 获取配置项

```java
managerConfigService.getInteger(Config.IP_LOGIN_RISK);
managerConfigService.getString(Config.IP_LOGIN_RISK);
```

### 13.3.9 获取用户背包信息

```java
gameService.getReturnPack(userId);
```

### 13.3.10 推送背包最新状态

```java
managerGameBaseService.pushBackpackUpdate(userId, itemId, number, 1);
```

### 13.3.11 检查背包道具是否充足

```java
PlayGameService.checkUserItemNumber(...);
```

### 13.3.12 检查资产是否充足

```java
UserCapitalService.findUserCapitalByUserIdAndCapitalType(...);
```

### 13.3.13 扣除用户资产

```java
UserCapitalService.subUserBalance(...);
```

说明：直接调用 `subUserBalance(...)` 可以工作，但**更推荐**在 `UserCapitalService` 中新增“语义明确的业务方法”来封装具体扣费场景。

### 13.3.14 增加用户资产

```java
UserCapitalService.addUserBalance(...);
```

### 13.3.15 发奖励时增加资产

```java
userCapitalService.addUserBalanceByAddReward(...);
```

### 13.3.16 更新背包

```java
PlayGameService.updateUserBackpack(...);
```

### 13.3.17 更新配置表

```java
managerConfigService.updateConfigData(...);
```

### 13.3.18 背包推送

```java
Push.push(PushCode.updateUserBackpack, managerSocketService.getServerIdByUserId(userId), pushData);
```

---

## 13.4 常用服务、枚举与核心文件定位

下面这些文件属于后续开发和排障时的高频入口，应优先熟悉并复用。

### 13.4.1 用户资产缓存服务

`yssy\ZY-APP-DEFAULT\src\com\zywl\app\defaultx\cache\UserCapitalCacheService.java`

职责：

- 用户资产缓存读取
- 按用户 + 资产类型获取余额
- 删除 / 刷新资产缓存
- 供资产服务、首页信息、推送使用

### 13.4.2 用户缓存服务

`yssy\ZY-APP-DEFAULT\src\com\zywl\app\defaultx\cache\UserCacheService.java`

职责：

- 用户缓存加载
- 用户基本信息读取
- 多用户批量装载
- 供登录、首页、排行榜、业务逻辑读取玩家对象

### 13.4.3 用户资产类型枚举

`yssy\ZY-APP-DEFAULT\src\com\zywl\app\defaultx\enmus\UserCapitalTypeEnum.java`

说明：

- 既包含当前仍在使用的资产类型
- 也包含历史遗留、已弃用的资产类型
- 注意：**文币、通宝、游园券、游园币、积分** 已弃用，开发时不要误用旧枚举项

### 13.4.4 用户配置服务 / 全局配置热更新入口

`yssy\ZY-APP-MANAGER\src\com\zywl\app\manager\service\manager\ManagerConfigService.java`

职责：

- 启动时加载 `Config` 表到 `CONFIG`
- 提供 `getInteger/getString` 读取配置
- 处理后台配置修改
- 驱动 `updateGameKey(...)` 热更新逻辑

### 13.4.5 VIP 用户服务

`yssy\ZY-APP-DEFAULT\src\com\zywl\app\defaultx\service\UserVipService.java`

职责：

- 查询用户 VIP 信息
- 维护用户 VIP 数据
- 给 VIP 面板、VIP 开通、VIP 转赠等逻辑提供数据支撑

### 13.4.6 道具枚举

`yssy\ZY-APP-DEFAULT\src\com\zywl\app\defaultx\enmus\ItemIdEnum.java`

说明：

- 该枚举与道具表数据存在对应关系
- 可用于常用道具 ID 的语义化引用
- 注意：`MONEY_1`（银币）、`GOLD`（金币）已经弃用，不要在新功能中继续使用旧值

### 13.4.7 资产类型枚举与道具表映射关系

`yssy\ZY-APP-DEFAULT\src\com\zywl\app\defaultx\enmus\UserCapitalTypeEnum.java`

说明：

- 项目中存在“资产型道具”和“资产类型枚举”双重概念
- `PlayGameService.addReward(...)` 会根据道具表中 `item.type` 决定是走“资产入口”还是“背包入口”
- 开发时必须区分“道具 ID”与“资产类型 ID”的语义边界

---

## 13.5 先找真实代码，再决定写法

以下原则必须保持：

- 获取背包信息：先下钻 `getReturnPack(userId)` 再用
- 更新背包：先看 `updateUserBackpack(...)` 的语义、缓存副作用、日志副作用、推送副作用
- 扣资产：优先在 `UserCapitalService` 中封装语义清晰的方法
- 发奖励：优先统一走 `addReward(...)`
- 获取静态表：先看 `PlayGameService` 当前有没有现成 Map
- 做热更新：先看 `ManagerConfigService.updateGameKey(...)` 既有模式
- 做登录返回：先看 `ManagerGameBaseService.getInfo(...)` / `syncTableInfo(...)`
- 做小游戏主服联动：先看现有 DTS / DTS2 / DTS3 的 ManagerSocket 与 push 结构

任何新写法，必须先确认当前项目里有没有现成模式；有现成模式优先复用，没有再扩展。

---

## 13.6 不要在上层业务随意复制资产 / 背包 / push 细节

推荐模式：

1. 玩法或功能先算出结果
2. 组装业务参数 / 奖励 JSON / 扣费参数
3. 发奖励统一走 `addReward(...)`
4. 扣费统一走 `UserCapitalService` 业务化封装
5. 配置变化走 `updateConfigData/updateGameKey`
6. 登录时静态表同步依赖 `syncTableInfo`
7. 背包、资产、push 尽量使用已有服务封装，不在 Controller / Servlet / 顶层 Service 里直接散写细节

不要在上层业务里同时手搓：

- 查用户资产
- 扣资产
- 写日志
- 清缓存
- 发 push
- 改背包
- 发奖励

这类细节应尽量收口到已有 Service 中，避免逻辑散乱、重复代码和副作用遗漏。

---

## 13.7 静态表业务与配置驱动优先

### 13.7.1 静态表优先

优先通过：

- `dic_*` 静态表
- `PlayGameService` 静态 Map
- `syncTableInfo(...)`
- `updateGameKey(...)`

来承载静态数据逻辑。

### 13.7.2 配置驱动优先

优先让以下对象承担业务可配置能力：

- `dic_*` 静态表
- `t_config`
- `reward JSON`

不要把本应可运营配置、可热更新、可同步下发的内容，硬编码散落在多个 Service 内部。

---

## 13.8 主线开发与小游戏开发的共识

无论是主线业务还是小游戏（`ZY-APP-DTS / ZY-APP-DTS2 / ZY-APP-DTS3`），都尽量复用既有模式：

- 登录返回
- 启动加载
- Config 热更新
- 静态表 Map
- reward → `addReward`
- ManagerSocket / ServerSocket 两跳 push
- 常用枚举与用户缓存 / 资产缓存 / 背包缓存服务

小游戏新增逻辑时，不要因为“它是独立玩法服”就完全绕开主系统已有模式；能复用的尽量复用，尤其是：

- 奖励
- 用户信息加载
- 资产扣减
- 道具发放
- 配置表
- 推送格式
- 命令码组织方式

---

## 13.9 我们后续协作时的执行约定

1. 每次新需求，先定位真实代码入口
2. 每次方案，都要能落到真实类 / 方法 / 枚举 / 表字段
3. 每一步改动后，都要检查是否与既有架构一致
4. 每次排障，都要先确认真实请求参数、真实返回包、真实日志、真实 SQL / Mapper
5. 每次涉及静态表、配置、奖励、资产、背包、推送时，都优先复用既有总线和封装
6. 每次需求讨论，都优先给出“能直接嵌入当前代码库”的实现思路，而不是泛化设计图

---

## 13.10 收尾结论

当前 YSSY 后端后续分析与开发的“共识地基”可以概括为：

1. **所有结论必须以真实源码、真实表结构、真实调用链为准。**
2. **静态表要遵守：启动加载 → 登录同步 → 运行期热更新。**
3. **奖励统一收口到 `PlayGameService.addReward(...)`。**
4. **资产扣费优先在 `UserCapitalService` 中按业务语义封装。**
5. **主线与小游戏都尽量复用既有模式，不轻易另起一套平行机制。**
6. **后续协作时，坚持“源码驱动分析、拒绝臆测、定位到真实代码入口”的工作方式。**
