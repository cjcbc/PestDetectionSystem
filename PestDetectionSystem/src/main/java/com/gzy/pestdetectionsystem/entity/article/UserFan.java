package com.gzy.pestdetectionsystem.entity.article;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * APP用户粉丝信息表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "user_fan")
@Schema(description = "用户粉丝实体")
public class UserFan {
    
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
     * 粉丝ID
     */
    @TableField(value = "fans_id")
    @Schema(description = "粉丝ID")
    private Long fansId;
    
    /**
     * 粉丝昵称
     */
    @TableField(value = "fans_name")
    @Schema(description = "粉丝昵称")
    private String fansName;
    
    /**
     * 粉丝忠实度
     * 0 正常
     * 1 潜力股
     * 2 勇士
     * 3 铁杆
     * 4 老铁
     */
    @TableField(value = "level")
    @Schema(description = "粉丝忠实度(0:正常, 1:潜力股, 2:勇士, 3:铁杆, 4:老铁)")
    private Integer level;
    
    /**
     * 创建时间
     */
    @TableField(value = "created_time")
    @Schema(description = "创建时间")
    private Long createdTime;
    
    /**
     * 是否可见我动态
     */
    @TableField(value = "is_display")
    @Schema(description = "是否可见我动态")
    private Integer isDisplay;
    
    /**
     * 是否屏蔽私信
     */
    @TableField(value = "is_shield_letter")
    @Schema(description = "是否屏蔽私信")
    private Integer isShieldLetter;
    
    /**
     * 是否屏蔽评论
     */
    @TableField(value = "is_shield_comment")
    @Schema(description = "是否屏蔽评论")
    private Integer isShieldComment;
}
