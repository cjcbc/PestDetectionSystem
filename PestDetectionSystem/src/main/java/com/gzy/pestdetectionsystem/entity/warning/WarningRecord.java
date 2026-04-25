package com.gzy.pestdetectionsystem.entity.warning;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("warning")
public class WarningRecord {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String title;

    private String content;

    private String region;

    private String pestName;

    private Integer severity;

    private Integer status;

    private Long publishTime;

    private Long publisherId;

    private Integer viewCount;

    private Long createdTime;

    private Long updatedTime;

    private Integer deleted;
}
