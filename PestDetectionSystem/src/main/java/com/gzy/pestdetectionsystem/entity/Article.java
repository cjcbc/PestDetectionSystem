package com.gzy.pestdetectionsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("article")
public class Article {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String title;

    private String summary;

    private String content;

    private String coverImage;

    private String category;

    private String tags;

    private Long authorId;

    private Integer status;

    private String auditComment;

    private Long auditTime;

    private Long auditorId;

    private Long publishTime;

    private Integer viewCount;

    private Integer likeCount;

    private Long createdTime;

    private Long updatedTime;

    private Integer deleted;
}
