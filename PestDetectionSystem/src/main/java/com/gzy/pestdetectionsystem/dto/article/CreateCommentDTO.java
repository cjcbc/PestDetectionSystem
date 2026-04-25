package com.gzy.pestdetectionsystem.dto.article;

import lombok.Data;

@Data
public class CreateCommentDTO {

    private String content;

    private String parentId;
}
