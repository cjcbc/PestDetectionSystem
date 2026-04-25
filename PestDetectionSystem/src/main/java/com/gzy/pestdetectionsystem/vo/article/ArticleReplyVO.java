package com.gzy.pestdetectionsystem.vo.article;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleReplyVO {

    private String id;

    private String userId;

    private String username;

    private String userImage;

    private String content;

    private Integer likeCount;

    private Boolean isLiked;

    private Long createdTime;
}
