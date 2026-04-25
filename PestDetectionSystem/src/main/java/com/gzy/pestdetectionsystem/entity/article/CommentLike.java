package com.gzy.pestdetectionsystem.entity.article;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("comment_like")
public class CommentLike {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long commentId;

    private Long userId;

    private Long createdTime;
}
