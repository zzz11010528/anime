package com.community.event;

import com.community.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论事件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentEvent {

    /**
     * 评论
     */
    private Comment comment;
}
