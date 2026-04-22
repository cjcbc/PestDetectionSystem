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
@TableName("article_comment")
public class ArticleComment {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long articleId;

    private Long userId;

    private Long parentId;

    private String content;

    private Integer likeCount;

    private Integer replyCount;

    private Integer status;

    private Long createdTime;

    private Long updatedTime;
}
