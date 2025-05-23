package com.example.chat.service;

import com.example.chat.model.Message;
import io.netty.channel.Channel; // 添加这个导入
import com.example.chat.protocol.MessageType;
import com.example.chat.protocol.StatusCode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import com.example.chat.service.sql;

public class ChatRoomService {
    private final static Map<String, CopyOnWriteArraySet<String>> rooms = new ConcurrentHashMap<>();//rooms是一个线程安全的map,用来维护聊天室数据

    public void joinRoom(String userId, String roomId) throws SQLException {
        rooms.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(userId);
        sql sql = new sql();
        sql.updateUserRoomAndActivate(userId, roomId);
        CopyOnWriteArraySet<String> userlist = rooms.get(roomId);
        System.out.println("用户列表:"+userlist);
        Message userMessage = new Message();
        userMessage.setSender("System");
        userMessage.setType(MessageType.INFORMATION);
        userMessage.setRoomId(roomId);
        List<String> userIdList = new ArrayList<>(userlist);
        userMessage.setUsers(userIdList);
        Channel channel = UserService.getOnlineUsers().get(userId);
        channel.writeAndFlush(userMessage);


    }

    public void leaveRoom(String userId, String roomId) {
        if (rooms.containsKey(roomId)) {
            rooms.get(roomId).remove(userId);
        }
    }

    public void broadcastMessage(Message message) {
        String roomId = message.getRoomId();
        System.out.println("[广播消息] 房间ID: " + roomId + "，消息内容: " + message.getContent());

        if (!rooms.containsKey(roomId)) {
            System.out.println("[广播消息] 房间 " + roomId + " 不存在，跳过广播");
            return;
        }

        for (String userId : rooms.get(roomId)) {
            System.out.println("[<UNK>] <UNK>ID: " + rooms + "<UNK>: ");
            System.out.println("尝试向用户 " + userId + " 发送消息");

            Channel channel = UserService.getOnlineUsers().get(userId);

            if (channel == null) {
                System.out.println("用户 " + userId + " 没有找到 Channel（可能不在线）");
            } else if (!channel.isActive()) {
                System.out.println("用户 " + userId + " 的 Channel 已断开");
            } else {
                System.out.println("向用户 " + userId + " 发送消息中...");
                channel.writeAndFlush(message);
            }
        }
    }

}