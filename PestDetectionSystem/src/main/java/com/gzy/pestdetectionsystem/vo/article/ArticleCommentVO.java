package com.gzy.pestdetectionsystem.vo.article;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleCommentVO {

    private String id;

    private String userId;

    private String username;

    private String userImage;

    private String content;

    private Integer likeCount;

    private Integer replyCount;

    private Boolean isLiked;

    private Long createdTime;

    private List<ArticleReplyVO> replies;
}
