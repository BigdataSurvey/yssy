package com.zywl.app.servlet;

import com.zywl.app.base.exp.AppException;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.room.FoodRoom;
import com.zywl.app.room.RoomManager;
import com.zywl.app.service.GameFoodService;

import javax.annotation.PostConstruct;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "GameManagerServlet", urlPatterns = "/manager")
public class GameManagerServlet extends BaseServlet {
    private GameFoodService gameFoodService;

    @PostConstruct
    public void _Construct() {
        gameFoodService = SpringUtil.getService(GameFoodService.class);
    }

    @Override
    public Object doProcess(HttpServletRequest request, HttpServletResponse response, String clientIp) throws AppException, Exception {
        String action = request.getParameter("action");
        String strType = request.getParameter("type");
        String strRoomId = request.getParameter("roomId");
        switch (action) {
            case "getAllInfo" : {
                return gameFoodService.getAllInfo();
            }
            case "getRoomInfo": {
                if(strType == null || strRoomId == null || strType.isEmpty() || strRoomId.isEmpty()) {
                    return null;
                }
                int type = Integer.parseInt(strType);
                int roomId = Integer.parseInt(strRoomId);
                return getRoomInfo(type, roomId);
            }
            case "checkStart": {
                if(strType == null || strRoomId == null || strType.isEmpty() || strRoomId.isEmpty()) {
                    return null;
                }
                int type = Integer.parseInt(strType);
                int roomId = Integer.parseInt(strRoomId);
                return checkStart(type, roomId);
            }
            case "setMaxRoom": {
                if(strType == null || strType.isEmpty()) {
                    return null;
                }
                String maxRoom = request.getParameter("maxRoom");
                if(maxRoom == null || maxRoom.isEmpty()) {
                    return null;
                }
                int type = Integer.parseInt(strType);
                RoomManager manager = gameFoodService.getRoomManager(type);
                if(manager == null) {
                    return null;
                }
                manager.setMaxRoom(Integer.parseInt(maxRoom));
                return "success";
            }

            case "clear": {
                gameFoodService.clearAll();
                return "success";
            }
        }

        return null;
    }

    public Object getRoomInfo(int type, int roomId) {
        RoomManager manager = gameFoodService.getRoomManager(type);
        if(manager == null) {
            return null;
        }
        FoodRoom foodRoom = manager.getRoomById(roomId);
        if(foodRoom != null) {
            return foodRoom.getRoomInfo("");
        }
        return null;
    }

    public Object checkStart(int type, int roomId) {
        RoomManager manager = gameFoodService.getRoomManager(type);
        if(manager == null) {
            return "op fail";
        }
        FoodRoom foodRoom = manager.getRoomById(roomId);
        if(foodRoom != null) {
            foodRoom.checkStart();
            return "op success";
        }
        return "op fail";
    }
}
