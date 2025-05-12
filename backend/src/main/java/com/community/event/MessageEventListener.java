package com.community.event;

import com.community.entity.Comment;
import com.community.entity.Post;
import com.community.entity.User;
import com.community.service.CommentService;
import com.community.service.NotificationService;
import com.community.service.PostService;
import com.community.service.UserService;
import com.community.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 消息事件监听器
 */
@Component
@RequiredArgsConstructor
public class MessageEventListener {

    private final NotificationService notificationService;
    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;

    /**
     * 处理点赞事件
     */
    @Async
    @EventListener
    public void handleLikeEvent(LikeEvent event) {
        // 获取被点赞的用户ID
        Long targetUserId = null;
        String content = null;
        
        if (event.getType() == 1) {
            // 帖子点赞
            Post post = postService.getById(event.getTargetId());
            if (post != null) {
                targetUserId = post.getUserId();
                content = "用户 " + getUserNickname(event.getUserId()) + " 点赞了你的帖子";
            }
        } else if (event.getType() == 2) {
            // 评论点赞
            Comment comment = commentService.getById(event.getTargetId());
            if (comment != null) {
                targetUserId = comment.getUserId();
                content = "用户 " + getUserNickname(event.getUserId()) + " 点赞了你的评论";
            }
        }
        
        if (targetUserId != null && !targetUserId.equals(event.getUserId())) {
            // 创建通知
            Long notificationId = notificationService.create(targetUserId, 1, event.getId(), content);
            
            // 发送WebSocket通知
            WebSocketServer.sendNotification(targetUserId, content);
        }
    }

    /**
     * 处理评论事件
     */
    @Async
    @EventListener
    public void handleCommentEvent(CommentEvent event) {
        Comment comment = event.getComment();
        if (comment == null) {
            return;
        }
        
        // 获取被评论的用户ID
        Long targetUserId = null;
        String content = null;
        
        if (comment.getParentId() == 0) {
            // 帖子评论
            Post post = postService.getById(comment.getPostId());
            if (post != null) {
                targetUserId = post.getUserId();
                content = "用户 " + getUserNickname(comment.getUserId()) + " 评论了你的帖子";
            }
        } else {
            // 评论回复
            Comment parentComment = commentService.getById(comment.getParentId());
            if (parentComment != null) {
                targetUserId = parentComment.getUserId();
                content = "用户 " + getUserNickname(comment.getUserId()) + " 回复了你的评论";
            }
        }
        
        if (targetUserId != null && !targetUserId.equals(comment.getUserId())) {
            // 创建通知
            Integer type = comment.getParentId() == 0 ? 2 : 3;
            Long notificationId = notificationService.create(targetUserId, type, comment.getId(), content);
            
            // 发送WebSocket通知
            WebSocketServer.sendNotification(targetUserId, content);
        }
    }

    /**
     * 获取用户昵称
     */
    private String getUserNickname(Long userId) {
        User user = userService.getById(userId);
        return user != null ? user.getNickname() : "未知用户";
    }
}
