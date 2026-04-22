package com.gzy.pestdetectionsystem.service;

import com.gzy.pestdetectionsystem.dto.CreateArticleDTO;
import com.gzy.pestdetectionsystem.dto.CreateCommentDTO;
import com.gzy.pestdetectionsystem.vo.*;

public interface ArticleService {

    PageVO<ArticleSummaryVO> getArticles(Long userId, Integer page, Integer pageSize,
                                         String category, String keyword, String sortBy);

    ArticleSummaryVO getArticleDetail(Long userId, Long articleId);

    ArticleSummaryVO createArticle(Long userId, CreateArticleDTO dto);

    LikeToggleVO toggleArticleLike(Long userId, Long articleId);

    PageVO<ArticleCommentVO> getArticleComments(Long userId, Long articleId,
                                                Integer page, Integer pageSize, String sortBy);

    ArticleCommentVO createComment(Long userId, Long articleId, CreateCommentDTO dto);

    CommentLikeToggleVO toggleCommentLike(Long userId, Long commentId);

    void deleteComment(Long operatorId, Integer roleId, Long commentId);
}
