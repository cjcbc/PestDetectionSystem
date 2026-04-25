package com.gzy.pestdetectionsystem.vo.article;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSummaryVO {

    private String id;

    private String userId;

    private String author;

    private String title;

    private String category;

    private String summary;

    private String content;

    private String coverImage;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    private Boolean isLiked;

    private Long createdTime;

    private Long updatedTime;
}
