package com.community.websocket;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.community.service.MessageService;
import com.community.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务
 */
@Slf4j
@Component
@ServerEndpoint("/websocket/{token}")
public class WebSocketServer {

    /**
     * 用户ID和WebSocket的映射关系
     */
    private static final Map<Long, WebSocketServer> CLIENTS = new ConcurrentHashMap<>();

    /**
     * 会话
     */
    private Session session;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 消息服务
     */
    private static MessageService messageService;

    /**
     * 通知服务
     */
    private static NotificationService notificationService;

    /**
     * 注入消息服务
     */
    @Autowired
    public void setMessageService(MessageService messageService) {
        WebSocketServer.messageService = messageService;
    }

    /**
     * 注入通知服务
     */
    @Autowired
    public void setNotificationService(NotificationService notificationService) {
        WebSocketServer.notificationService = notificationService;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        try {
            // 验证token
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId == null) {
                session.close();
                return;
            }

            this.session = session;
            this.userId = Long.parseLong(loginId.toString());

            // 添加到映射关系
            CLIENTS.put(userId, this);
            log.info("有新的连接，用户ID：{}，当前在线人数：{}", userId, CLIENTS.size());
        } catch (Exception e) {
            log.error("WebSocket连接异常", e);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if (userId != null) {
            // 移除映射关系
            CLIENTS.remove(userId);
            log.info("有连接关闭，用户ID：{}，当前在线人数：{}", userId, CLIENTS.size());
        }
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message) {
        log.info("收到用户{}的消息：{}", userId, message);
        try {
            // 解析消息
            JSONObject jsonObject = JSONUtil.parseObj(message);
            String type = jsonObject.getStr("type");

            if ("heartbeat".equals(type)) {
                // 心跳消息
                sendMessage("pong");
            } else if ("message".equals(type)) {
                // 私信消息
                Long toUserId = jsonObject.getLong("toUserId");
                String content = jsonObject.getStr("content");

                // 发送私信
                Long messageId = messageService.send(userId, toUserId, content);

                // 推送给接收者
                sendToUser(toUserId, JSONUtil.createObj()
                        .set("type", "message")
                        .set("fromUserId", userId)
                        .set("messageId", messageId)
                        .set("content", content)
                        .toString());
            }
        } catch (Exception e) {
            log.error("处理消息异常", e);
        }
    }

    /**
     * 发生错误时调用的方法
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误", error);
    }

    /**
     * 发送消息
     */
    public void sendMessage(String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("发送消息异常", e);
        }
    }

    /**
     * 发送消息给指定用户
     */
    public static void sendToUser(Long userId, String message) {
        WebSocketServer client = CLIENTS.get(userId);
        if (client != null) {
            client.sendMessage(message);
        }
    }

    /**
     * 发送通知
     */
    public static void sendNotification(Long userId, String message) {
        sendToUser(userId, JSONUtil.createObj()
                .set("type", "notification")
                .set("content", message)
                .toString());
    }
}
