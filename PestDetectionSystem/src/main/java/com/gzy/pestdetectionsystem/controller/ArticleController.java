package com.gzy.pestdetectionsystem.controller;

import com.gzy.pestdetectionsystem.dto.CreateArticleDTO;
import com.gzy.pestdetectionsystem.dto.CreateCommentDTO;
import com.gzy.pestdetectionsystem.exception.BusinessException;
import com.gzy.pestdetectionsystem.service.ArticleService;
import com.gzy.pestdetectionsystem.utils.Result;
import com.gzy.pestdetectionsystem.utils.TokenAuthHelper;
import com.gzy.pestdetectionsystem.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final TokenAuthHelper tokenAuthHelper;

    @GetMapping
    public Result<PageVO<ArticleSummaryVO>> getArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "newest") String sortBy,
            HttpServletRequest request) {
        TokenAuthHelper.AuthUser authUser = tokenAuthHelper.optionalUser(request);
        Long userId = authUser == null ? null : authUser.getUserId();
        return Result.ok(articleService.getArticles(userId, page, pageSize, category, keyword, sortBy));
    }

    @GetMapping("/{articleId}")
    public Result<ArticleSummaryVO> getArticleDetail(@PathVariable String articleId,
                                                     HttpServletRequest request) {
        TokenAuthHelper.AuthUser authUser = tokenAuthHelper.optionalUser(request);
        Long userId = authUser == null ? null : authUser.getUserId();
        return Result.ok(articleService.getArticleDetail(userId, parseId(articleId, "articleId")));
    }

    @PostMapping
    public Result<ArticleSummaryVO> createArticle(@RequestBody CreateArticleDTO dto,
                                                  HttpServletRequest request) {
        Long userId = tokenAuthHelper.requireUser(request).getUserId();
        return Result.ok(articleService.createArticle(userId, dto));
    }

    @PostMapping("/{articleId}/like")
    public Result<LikeToggleVO> toggleLike(@PathVariable String articleId,
                                           HttpServletRequest request) {
        Long userId = tokenAuthHelper.requireUser(request).getUserId();
        return Result.ok(articleService.toggleArticleLike(userId, parseId(articleId, "articleId")));
    }

    @GetMapping("/{articleId}/comments")
    public Result<PageVO<ArticleCommentVO>> getComments(@PathVariable String articleId,
                                                         @RequestParam(defaultValue = "1") Integer page,
                                                         @RequestParam(defaultValue = "20") Integer pageSize,
                                                         @RequestParam(defaultValue = "newest") String sortBy,
                                                         HttpServletRequest request) {
        TokenAuthHelper.AuthUser authUser = tokenAuthHelper.optionalUser(request);
        Long userId = authUser == null ? null : authUser.getUserId();
        return Result.ok(articleService.getArticleComments(userId, parseId(articleId, "articleId"), page, pageSize, sortBy));
    }

    @PostMapping("/{articleId}/comments")
    public Result<ArticleCommentVO> createComment(@PathVariable String articleId,
                                                  @RequestBody CreateCommentDTO dto,
                                                  HttpServletRequest request) {
        Long userId = tokenAuthHelper.requireUser(request).getUserId();
        return Result.ok(articleService.createComment(userId, parseId(articleId, "articleId"), dto));
    }

    private Long parseId(String raw, String fieldName) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            throw new BusinessException(40001, fieldName + " 格式不正确");
        }
    }
}
