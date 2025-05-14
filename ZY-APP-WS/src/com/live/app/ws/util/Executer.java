package com.live.app.ws.util;

import com.alibaba.fastjson2.JSON;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.interfacex.ServiceController;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.socket.BaseServerSocket;
import com.live.app.ws.socket.manager.SocketManager;
import com.zywl.app.base.exp.AppException;
import com.zywl.app.base.util.AppDefaultThreadFactory;
import com.zywl.app.base.util.Async;
import com.zywl.app.base.util.PropertiesUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Executer {

    private static final Log logger = LogFactory.getLog(Executer.class);

    private static boolean syncingTaskNum;

    private static ExecutorService requestExecutor;

    private static ExecutorService responseExecutor;

    private static ExecutorService disposeRequestExecutor;

    private static ExecutorService disposeResponseExecutor;

    //各个业务阶段耗时操作可提供给此线程池
    private static ExecutorService serviceExecutor;

    private static ExecutorService syncTaskExecutor;

    private static Map<String, Listener> listenerPool = new ConcurrentHashMap<String, Listener>();

    private static Map<String, Task> taskPool = new ConcurrentHashMap<String, Task>();

    private static AtomicInteger QPS = new AtomicInteger(0);

    public static ServiceController controller;

    public static Timer timeoutTimer;

    static {
        PropertiesUtil propertiesUtil = new PropertiesUtil("thread.properties");
        requestExecutor = Executors.newFixedThreadPool(propertiesUtil.getInteger("thread.request.pool"), new AppDefaultThreadFactory("Request"));
        responseExecutor = Executors.newFixedThreadPool(propertiesUtil.getInteger("thread.response.pool"), new AppDefaultThreadFactory("Response"));

        disposeRequestExecutor = Executors.newFixedThreadPool(propertiesUtil.getInteger("thread.disposeRequest.pool"), new AppDefaultThreadFactory("DoRequest"));
        disposeResponseExecutor = Executors.newFixedThreadPool(propertiesUtil.getInteger("thread.disposeResponse.pool"), new AppDefaultThreadFactory("DoResponse"));

        serviceExecutor = Executors.newFixedThreadPool(propertiesUtil.getInteger("thread.service.pool"), new AppDefaultThreadFactory("ServiceExecutor"));

        syncTaskExecutor = Executors.newSingleThreadExecutor(new AppDefaultThreadFactory("SyncTaskExecutor"));

        timeoutTimer = new Timer("Task Timeout Timer");
        timeoutTimer.schedule(new TimerTask() {
            public void run() {
                try {
                    for (String id : taskPool.keySet()) {
                        Task task = taskPool.get(id);
                        if (task != null) {
                            if (System.currentTimeMillis() - task.getCreateTime() >= task.getTimeout()) {
                                try {
                                    task.getDoThread().interrupt();
                                } catch (Exception e) {
                                }
                                response(CommandBuilder.builder(task.getCommandId()).error("请求超时").build());
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("超时检测异常:" + e, e);
                }
            }
        }, 3 * 1000, 3 * 1000);
    }

    public static void request(TargetSocketType socketType, Command command) {
        request(socketType, command, null);
    }

    /**
     * 请求服务端
     *
     * @param socketType
     * @param command
     * @param listener
     * @author DOE
     */
    public static void request(final TargetSocketType socketType, final Command command, final Listener listener) {
        Set<BaseClientSocket> clients = SocketManager.getServers(socketType);
        if (clients != null && !clients.isEmpty()) {
            for (final BaseClientSocket baseClientSocket : clients) {
                requestExecutor.execute(new Runnable() {
                    public void run() {
                        if (listener != null) {
                            listenerPool.put(command.getId(), listener);
                        }
                        if (!baseClientSocket.sendCommand(command)) {
                            //发送失败删除任务
                            listenerPool.remove(command.getId());
                        }
                    }
                });
            }
        } else {
            logger.error("命令未发送，服务端已离线[" + JSON.toJSONString(command) + "]");
        }
    }

    /**
     * 响应客户端
     *
     * @param baseServerSocket
     * @param command
     * @author DOE
     */
    public static void response(final Command command) {
        responseExecutor.execute(new Runnable() {
            public void run() {
                Task task = removeTask(command);
                if (task != null) {
                    //还原原始数据
                    command.setCode(task.getCode());
                    command.setRequestTime(task.getRequestTime());

                    long time = System.currentTimeMillis() - task.getCreateTime();
                    if (time > 2000) {
                        if (!task.getCode().equals("700200") && !task.getCode().equals("007002") && !task.getCode().equals("999999")) {
                            logger.info("[" + task.getCode() + "]任务执行结束，总耗时：" + (time) + " ms：" + JSON.toJSONString(command));
                        }

                    } else {
                        if (!task.getCode().equals("700200") && !task.getCode().equals("007002") && !task.getCode().equals("999999")) {
                            logger.info("[" + task.getCode() + "]任务执行结束，总耗时：" + (time) + " ms");
                        }

                    }
                    task.getBaseServerSocket().sendCommand(command);
                } else {
                    logger.info("任务已超时，停止响应客户端：" + JSON.toJSONString(command));
                }
            }
        });
    }

    /**
     * 服务端处理客户端请求
     *
     * @param baseServerSocket
     * @param command
     * @author DOE
     */
    public static void disposeRequest(final BaseServerSocket baseServerSocket, final Command command) {
        disposeRequestExecutor.execute(new Thread() {
            public void run() {
                try {
                    QPS.addAndGet(1);
                    Task task = addTask(command.getId(), command.getCode(), command.getRequestTime(), this, baseServerSocket, command);
                    if (controller == null) {
                        throw new AppException("控制器未初始化");
                    }
                    Object result = controller.exec(baseServerSocket, command);
                    if (result instanceof Async) {
                        task.setTimeout(((Async) result).getTimeout());
                    } else {
                        response(CommandBuilder.builder(command).success(result).build());
                    }
                } catch (AppException e) {
                    logger.warn("执行异常[" + command.getCode() + "]：" + e);
                    response(CommandBuilder.builder(command).error(e.getMessage()).build());
                } catch (Exception e) {
                    logger.error("未知执行异常：" + e, e);
                    response(CommandBuilder.builder(command).error("ERROR").build());
                }
            }
        });
    }

    /**
     * 客户端收到响应后处理
     *
     * @param baseClientSocket
     * @param command
     * @author DOE
     */
    public static void disposeResponse(final BaseClientSocket baseClientSocket, final Command command) {
        disposeResponseExecutor.execute(new Runnable() {
            public void run() {
                try {
                    Listener listener = listenerPool.remove(command.getId());
                    if (listener != null) {
                        listener.handle(baseClientSocket, command);
                    }
                } catch (AppException e) {
                    logger.warn("执行异常[" + command.getId() + "]：" + e);
                } catch (Exception e) {
                    logger.error("未知执行异常：" + e, e);
                }
            }
        });
    }

    public static void executeService(Runnable runnable) {
        serviceExecutor.execute(runnable);
    }

    public static Future<?> submitService(Runnable runnable) {
        return serviceExecutor.submit(runnable);
    }

    private static Task addTask(String commandId, String code, String requestTime, Thread thread, BaseServerSocket baseServerSocket, Command command) {
        Task task = new Task(commandId, code, requestTime);
        task.setCreateTime(System.currentTimeMillis());
        task.setTimeout(30 * 1000);
        task.setBaseServerSocket(baseServerSocket);
        task.setDoThread(thread);
        taskPool.put(command.getId(), task);
        syncServerTaskNum();
        return task;
    }

    private static Task removeTask(Command command) {
        Task task = taskPool.remove(command.getId());
        syncServerTaskNum();
        return task;
    }

    private synchronized static void syncServerTaskNum() {
        if (!syncingTaskNum) {
            syncingTaskNum = true;
            syncTaskExecutor.execute(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(1000);
                        Push.push(PushCode.syncTaskNum, null, Executer.size() + "," + Executer.QPS());
                    } catch (Exception e) {
                        logger.error("同步任务数异常：" + e, e);
                    } finally {
                        syncingTaskNum = false;
                        QPS.set(0);
                    }
                }
            });
        }
    }

    public static int size() {
        return taskPool.size();
    }

    public static int QPS() {
        return QPS.get();
    }
}

class Task {

    private String commandId;

    private String code;

    private String requestTime;

    private long createTime;

    private long timeout;

    private BaseServerSocket baseServerSocket;

    private Thread doThread;

    public Task(String commandId, String code, String requestTime) {
        super();
        this.commandId = commandId;
        this.code = code;
        this.requestTime = requestTime;
    }

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public BaseServerSocket getBaseServerSocket() {
        return baseServerSocket;
    }

    public void setBaseServerSocket(BaseServerSocket baseServerSocket) {
        this.baseServerSocket = baseServerSocket;
    }

    public Thread getDoThread() {
        return doThread;
    }

    public void setDoThread(Thread doThread) {
        this.doThread = doThread;
    }

}
