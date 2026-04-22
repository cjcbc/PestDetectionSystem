package com.gzy.pestdetectionsystem.controller;

import com.gzy.pestdetectionsystem.exception.BusinessException;
import com.gzy.pestdetectionsystem.service.ArticleService;
import com.gzy.pestdetectionsystem.utils.Result;
import com.gzy.pestdetectionsystem.utils.TokenAuthHelper;
import com.gzy.pestdetectionsystem.vo.CommentLikeToggleVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final ArticleService articleService;
    private final TokenAuthHelper tokenAuthHelper;

    @PostMapping("/{commentId}/like")
    public Result<CommentLikeToggleVO> toggleLike(@PathVariable String commentId,
                                                  HttpServletRequest request) {
        Long userId = tokenAuthHelper.requireUser(request).getUserId();
        return Result.ok(articleService.toggleCommentLike(userId, parseId(commentId, "commentId")));
    }

    @DeleteMapping("/{commentId}")
    public Result<?> deleteComment(@PathVariable String commentId,
                                   HttpServletRequest request) {
        TokenAuthHelper.AuthUser user = tokenAuthHelper.requireUser(request);
        Long userId = user.getUserId();
        Integer roleId = user.getRoleId();
        articleService.deleteComment(userId, roleId, parseId(commentId, "commentId"));
        return Result.ok("评论已删除");
    }

    private Long parseId(String raw, String fieldName) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            throw new BusinessException(40001, fieldName + " 格式不正确");
        }
    }
}
