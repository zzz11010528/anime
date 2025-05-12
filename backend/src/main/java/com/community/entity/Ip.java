package com.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IP实体类
 */
@Data
@TableName("ip")
public class Ip implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * IP ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * IP名称
     */
    private String name;

    /**
     * IP描述
     */
    private String description;

    /**
     * IP LOGO URL
     */
    private String logo;

    /**
     * 状态：0-禁用，1-正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
