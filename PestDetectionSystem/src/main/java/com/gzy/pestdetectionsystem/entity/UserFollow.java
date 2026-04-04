package com.gzy.pestdetectionsystem.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * APP用户关注信息表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "user_follow")
@Schema(description = "用户关注实体")
public class UserFollow {
    
    /**
     * 主键
     */
    @TableField(value = "id")
    @Schema(description = "主键")
    private Long id;
    
    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    @Schema(description = "用户ID")
    private Long userId;
    
    /**
     * 关注作者ID
     */
    @TableField(value = "follow_id")
    @Schema(description = "关注作者ID")
    private Long followId;
    
    /**
     * 关注作者昵称
     */
    @TableField(value = "follow_name")
    @Schema(description = "关注作者昵称")
    private String followName;
    
    /**
     * 关注度
     * 0 偶尔感兴趣
     * 1 一般
     * 2 经常
     * 3 高度
     */
    @TableField(value = "level")
    @Schema(description = "关注度(0:偶尔感兴趣, 1:一般, 2:经常, 3:高度)")
    private Integer level;
    
    /**
     * 是否动态通知
     */
    @TableField(value = "is_notice")
    @Schema(description = "是否动态通知")
    private Integer isNotice;
    
    /**
     * 创建时间
     */
    @TableField(value = "created_time")
    @Schema(description = "创建时间")
    private Long createdTime;
}
