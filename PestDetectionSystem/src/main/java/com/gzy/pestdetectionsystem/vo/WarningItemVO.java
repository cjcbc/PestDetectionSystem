package com.gzy.pestdetectionsystem.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarningItemVO {

    private String id;

    private String title;

    private String content;

    private String region;

    private String pestName;

    private Integer severity;

    private Integer status;

    private Long publishTime;

    private String publisherId;

    private String publisherName;

    private Integer viewCount;

    private Boolean isRead;

    private Long readTime;

    private Long createdTime;
}
