package com.zywl.app.room;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.bean.FoodGameConfig;
import com.zywl.app.service.GameFoodService;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {
    private Map<Integer,FoodRoom> rooms = new ConcurrentHashMap<>();

    private Vector<Integer> startRoomId = new Vector<>();//已开始的房间ID
    private Vector<Integer> waitRoomId = new Vector<>();//等待中的房间ID
    private Vector<Integer> idleRoomId = new Vector<>();//空闲的房间ID

    private int typeID = 0;
    private int roomCount = 1;
    private int maxRoom = 500;
    private FoodGameConfig cfg;
    private GameFoodService foodservice;

    public void Init(int typeID, FoodGameConfig cfg, GameFoodService foodservice) {
        this.typeID = typeID;
        this.cfg = cfg;
        this.foodservice = foodservice;
    }

    public FoodRoom creatRoom() {
        if(rooms.size() > maxRoom) {
            return null;
        }
        FoodRoom room = new FoodRoom();
        int roomId = typeID * 1000 + roomCount;
        room.init(roomId, cfg,this);
        rooms.put(roomId,room);
        roomCount++;
        setRoomIdle(roomId);
        return room;
    }

    public void exitRoom(String uid) {
        for(Map.Entry<Integer,FoodRoom> entry: rooms.entrySet()) {
            FoodRoom room = entry.getValue();
            if(room.isInRoom(uid)) {
                room.exitRoom(uid);
                break;
            }
        }
    }

    public void removePlayerByUserID(String userId) {
        for(Map.Entry<Integer,FoodRoom> entry: rooms.entrySet()) {
            FoodRoom room = entry.getValue();
            if(room.isInRoom(userId)) {
                room.removeWatchPlayer(userId);
                break;
            }
        }
    }

    public FoodRoom getRoomById(int roomId) {
        return rooms.get(roomId);
    }

    public FoodRoom getRoomForEnter(String userId) {
        FoodRoom room = null;
        for (Integer roomId : startRoomId) {
            room = rooms.get(roomId);
            if(room.isInRoom(userId)) {
                return room;
            }
        }

        for (Integer roomId : waitRoomId) {
            room = rooms.get(roomId);
            if(room.isInRoom(userId)) {
                return room;
            }
        }
        //此时说明是玩家没在游戏房间，优先推等待开始的房间
        if(waitRoomId.size() > 0) {
            int idx = (int) (Math.random() * waitRoomId.size());
            room = rooms.get(waitRoomId.get(idx));
            if(room != null) {
                return room;
            }
        }

        if(startRoomId.size() > 0) {
            int idx = (int) (Math.random() * startRoomId.size());
            room = rooms.get(startRoomId.get(idx));
            if(room != null) {
                return room;
            }
        }

        for (Integer roomId : idleRoomId) {
            room = rooms.get(roomId);
            if(room != null) {
                return room;
            }
        }
        return creatRoom();
    }

    public synchronized FoodRoom getRoomForJoin() {
        FoodRoom room = null;
        if(waitRoomId.size() > 0) {
            for (Integer roomId : waitRoomId) {
                room = rooms.get(roomId);
                if (room.isCanJoin()) {
                    return room;
                }
            }
        }

        if(idleRoomId.size() > 0) {
            for (Integer roomId : idleRoomId) {
                room = rooms.get(roomId);
                if (room != null) {
                    return room;
                }
            }
        }

        return creatRoom();
    }

    public void setRoomStart(Integer roomId) {
        if(waitRoomId.contains(roomId)) {
            waitRoomId.removeElement(roomId);
        }
        if(idleRoomId.contains(roomId)) {
            idleRoomId.removeElement(roomId);
        }
        if(!startRoomId.contains(roomId)) {
            startRoomId.add(roomId);
        }
    }

    public void setRoomWait(Integer roomId) {
        if(idleRoomId.contains(roomId)) {
            idleRoomId.removeElement(roomId);
        }
        if (!waitRoomId.contains(roomId)) {
            waitRoomId.add(roomId);
        }
    }

    public void setRoomIdle(Integer roomId) {
        if (startRoomId.contains(roomId)) {
            startRoomId.removeElement(roomId);
        }
        if (!idleRoomId.contains(roomId)) {
            idleRoomId.add(roomId);
        }
    }

    public GameFoodService getFoodservice() {
        return foodservice;
    }

    public FoodGameConfig getCfg() {
        return cfg;
    }

    public Object getRoomCount() {
        JSONObject obj = new JSONObject();
        obj.put("allCount", roomCount);
        obj.put("startRoomCount", startRoomId.size());
        obj.put("idleRoomCount", idleRoomId.size());
        obj.put("waitRoomCount", waitRoomId.size());
        return obj;
    }

    public int getMaxRoom() {
        return maxRoom;
    }

    public void setMaxRoom(int maxRoom) {
        this.maxRoom = maxRoom;
    }

    public void clearAll() {
        if(waitRoomId.size() <= 0) {
            return;
        }

        for (Integer roomId : waitRoomId) {
            rooms.get(roomId).clearAll();
        }
    }
}
