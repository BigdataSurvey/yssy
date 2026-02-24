/*
package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.PushBean;
import com.live.app.ws.interfacex.PushHandler;
import com.live.app.ws.socket.BaseSocket;
import com.zywl.app.base.Base;
import com.zywl.app.server.socket.AppSocket;

*/
/**
 * updatePbxInfo 推送过滤器：
 * - 常规包：按 condition=gameId 精确匹配（保持原行为）
 * - 定向包（payload含 targetUserId）：只推给该 userId 对应的 AppSocket
 *//*

public class PbxInfoPushHandler implements PushHandler {

    @Override
    public void onRegist(BaseSocket baseSocket, PushBean pushBean) {}

    @Override
    public void onUnregist(BaseSocket baseSocket, String clientCondition) {}

    @Override
    public boolean checkedPush(BaseSocket baseSocket, String condition, String clientCondition, Object pushData) {
        // 1) 同一 gameId 房间订阅
        if (!Base.eq(clientCondition, condition == null ? "" : condition)) {
            return false;
        }

        if (!(pushData instanceof JSONObject)) {
            return true;
        }
        JSONObject obj = (JSONObject) pushData;

        int status = 0;
        try {
            Object st = obj.get("status");
            status = (st == null ? 0 : Integer.parseInt(String.valueOf(st)));
        } catch (Exception ignore) {}

        // 非3：房间全员收
        if (status != 3) return true;

        // ✅公共开奖展示包：全员放行
        try {
            int isPublic = obj.getIntValue("isPublic");
            if (isPublic == 1) {
                return true;
            }
        } catch (Exception ignore) {}

        // status==3（非公共）：必须定向到个人，否则丢弃
        if (!(baseSocket instanceof AppSocket)) return false;
        long uid = ((AppSocket) baseSocket).getWsidBean().getUserId();
        String uidStr = String.valueOf(uid);

        // targetUserId 定向
        String targetUserId = obj.getString("targetUserId");
        if (targetUserId != null && targetUserId.length() > 0) {
            return targetUserId.equals(uidStr);
        }

        // userIds 定向
        try {
            JSONArray userIds = obj.getJSONArray("userIds");
            if (userIds != null && userIds.size() > 0) {
                for (Object o : userIds) {
                    if (uidStr.equals(String.valueOf(o))) return true;
                }
                return false;
            }
        } catch (Exception ignore) {}

        return false;
    }
}*/
