package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.entity.Message;
import com.community.vo.MessageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 私信Mapper接口
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 分页查询私信列表
     * @param page 分页参数
     * @param userId 用户ID
     * @param targetUserId 目标用户ID
     * @return 私信列表
     */
    Page<MessageVO> selectMessagePage(Page<MessageVO> page,
                                     @Param("userId") Long userId,
                                     @Param("targetUserId") Long targetUserId);

    /**
     * 获取用户的私信会话列表
     * @param userId 用户ID
     * @return 私信会话列表
     */
    List<MessageVO> selectConversationList(@Param("userId") Long userId);

    /**
     * 获取未读私信数量
     * @param userId 用户ID
     * @return 未读私信数量
     */
    int selectUnreadCount(@Param("userId") Long userId);
}
