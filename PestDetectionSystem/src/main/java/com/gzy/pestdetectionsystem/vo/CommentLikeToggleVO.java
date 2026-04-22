package com.gzy.pestdetectionsystem.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentLikeToggleVO {

    private String commentId;

    private Boolean liked;

    private Integer likeCount;
}
