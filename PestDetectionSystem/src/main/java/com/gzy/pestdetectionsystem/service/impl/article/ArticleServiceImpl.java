package com.gzy.pestdetectionsystem.service.impl.article;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gzy.pestdetectionsystem.dto.article.CreateArticleDTO;
import com.gzy.pestdetectionsystem.dto.article.CreateCommentDTO;
import com.gzy.pestdetectionsystem.entity.article.Article;
import com.gzy.pestdetectionsystem.entity.article.ArticleComment;
import com.gzy.pestdetectionsystem.entity.article.ArticleLike;
import com.gzy.pestdetectionsystem.entity.article.CommentLike;
import com.gzy.pestdetectionsystem.entity.user.User;
import com.gzy.pestdetectionsystem.exception.BusinessException;
import com.gzy.pestdetectionsystem.mapper.article.ArticleCommentMapper;
import com.gzy.pestdetectionsystem.mapper.article.ArticleLikeMapper;
import com.gzy.pestdetectionsystem.mapper.article.ArticleMapper;
import com.gzy.pestdetectionsystem.mapper.article.CommentLikeMapper;
import com.gzy.pestdetectionsystem.mapper.user.UserMapper;
import com.gzy.pestdetectionsystem.service.article.ArticleService;
import com.gzy.pestdetectionsystem.utils.SnowflakeIdGenerator;
import com.gzy.pestdetectionsystem.vo.article.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private static final int COMMENT_STATUS_PUBLISHED = 1;
    private static final int COMMENT_STATUS_DELETED = 2;

    private final ArticleMapper articleMapper;
    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleCommentMapper articleCommentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final UserMapper userMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public PageVO<ArticleSummaryVO> getArticles(Long userId, Integer page, Integer pageSize,
                                                String category, String keyword, String sortBy) {
        int pageNum = normalizePage(page);
        int size = normalizePageSize(pageSize, 20, 100);

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getDeleted, 0)
                .eq(Article::getStatus, 1)
                .orderByDesc(Article::getPublishTime);

        if (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category)) {
            wrapper.eq(Article::getCategory, category.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Article::getTitle, kw)
                    .or().like(Article::getSummary, kw)
                    .or().like(Article::getContent, kw));
        }

        if ("hottest".equalsIgnoreCase(sortBy)) {
            wrapper.orderByDesc(Article::getLikeCount).orderByDesc(Article::getPublishTime);
        } else {
            wrapper.orderByDesc(Article::getPublishTime);
        }

        Page<Article> articlePage = articleMapper.selectPage(new Page<>(pageNum, size), wrapper);
        List<Article> records = articlePage.getRecords();
        if (records.isEmpty()) {
            return new PageVO<>(articlePage.getTotal(), pageNum, size, pages(articlePage.getTotal(), size), Collections.emptyList());
        }

        Set<Long> articleIds = records.stream().map(Article::getId).collect(Collectors.toSet());
        Set<Long> authorIds = records.stream().map(Article::getAuthorId).collect(Collectors.toSet());

        Map<Long, User> userMap = userMapper.selectBatchIds(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));

        Map<Long, Long> commentCountMap = loadCommentCountMap(articleIds);
        Set<Long> likedArticleIds = loadLikedArticleIds(userId, articleIds);

        List<ArticleSummaryVO> list = records.stream().map(article -> toArticleSummaryVO(
                article,
                userMap.get(article.getAuthorId()),
                commentCountMap.getOrDefault(article.getId(), 0L).intValue(),
                likedArticleIds.contains(article.getId())
        )).collect(Collectors.toList());

        return new PageVO<>(articlePage.getTotal(), pageNum, size, pages(articlePage.getTotal(), size), list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleSummaryVO getArticleDetail(Long userId, Long articleId) {
        Article article = requirePublishedArticle(articleId);

        articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, articleId)
                .setSql("view_count = view_count + 1"));

        article = articleMapper.selectById(articleId);
        User author = userMapper.selectById(article.getAuthorId());

        Long commentCount = articleCommentMapper.selectCount(new LambdaQueryWrapper<ArticleComment>()
                .eq(ArticleComment::getArticleId, articleId)
                .eq(ArticleComment::getStatus, COMMENT_STATUS_PUBLISHED));

        boolean liked = false;
        if (userId != null) {
            liked = articleLikeMapper.selectCount(new LambdaQueryWrapper<ArticleLike>()
                    .eq(ArticleLike::getArticleId, articleId)
                    .eq(ArticleLike::getUserId, userId)) > 0;
        }

        return toArticleSummaryVO(article, author, commentCount.intValue(), liked);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleSummaryVO createArticle(Long userId, CreateArticleDTO dto) {
        if (dto == null || isBlank(dto.getTitle()) || isBlank(dto.getContent())) {
            throw new BusinessException(40080, "标题和内容不能为空");
        }

        long now = System.currentTimeMillis();
        Article article = new Article();
        article.setId(snowflakeIdGenerator.nextId());
        article.setTitle(dto.getTitle().trim());
        article.setSummary(emptyToNull(dto.getSummary()));
        article.setContent(dto.getContent().trim());
        article.setCoverImage(emptyToNull(dto.getCoverImage()));
        article.setCategory(emptyToNull(dto.getCategory()));
        article.setTags(emptyToNull(dto.getTags()));
        article.setAuthorId(userId);
        article.setStatus(1);
        article.setPublishTime(now);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCreatedTime(now);
        article.setUpdatedTime(now);
        article.setDeleted(0);
        articleMapper.insert(article);

        User author = userMapper.selectById(userId);
        return toArticleSummaryVO(article, author, 0, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LikeToggleVO toggleArticleLike(Long userId, Long articleId) {
        Article article = requirePublishedArticle(articleId);

        ArticleLike existing = articleLikeMapper.selectOne(new LambdaQueryWrapper<ArticleLike>()
                .eq(ArticleLike::getArticleId, articleId)
                .eq(ArticleLike::getUserId, userId)
                .last("limit 1"));

        boolean liked;
        if (existing != null) {
            articleLikeMapper.deleteById(existing.getId());
            articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                    .eq(Article::getId, articleId)
                    .setSql("like_count = GREATEST(like_count - 1, 0)"));
            liked = false;
        } else {
            ArticleLike like = new ArticleLike();
            like.setId(snowflakeIdGenerator.nextId());
            like.setArticleId(articleId);
            like.setUserId(userId);
            like.setCreatedTime(System.currentTimeMillis());
            articleLikeMapper.insert(like);
            articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                    .eq(Article::getId, articleId)
                    .setSql("like_count = like_count + 1"));
            liked = true;
        }

        Article latest = articleMapper.selectById(article.getId());
        return new LikeToggleVO(String.valueOf(articleId), liked, safeInt(latest.getLikeCount()));
    }

    @Override
    public PageVO<ArticleCommentVO> getArticleComments(Long userId, Long articleId,
                                                       Integer page, Integer pageSize, String sortBy) {
        requirePublishedArticle(articleId);

        int pageNum = normalizePage(page);
        int size = normalizePageSize(pageSize, 20, 100);

        LambdaQueryWrapper<ArticleComment> topWrapper = new LambdaQueryWrapper<ArticleComment>()
                .eq(ArticleComment::getArticleId, articleId)
                .isNull(ArticleComment::getParentId)
                .eq(ArticleComment::getStatus, COMMENT_STATUS_PUBLISHED);

        if ("hottest".equalsIgnoreCase(sortBy)) {
            topWrapper.orderByDesc(ArticleComment::getLikeCount).orderByDesc(ArticleComment::getCreatedTime);
        } else {
            topWrapper.orderByDesc(ArticleComment::getCreatedTime);
        }

        Page<ArticleComment> commentPage = articleCommentMapper.selectPage(new Page<>(pageNum, size), topWrapper);
        List<ArticleComment> tops = commentPage.getRecords();
        if (tops.isEmpty()) {
            return new PageVO<>(commentPage.getTotal(), pageNum, size, pages(commentPage.getTotal(), size), Collections.emptyList());
        }

        Set<Long> topIds = tops.stream().map(ArticleComment::getId).collect(Collectors.toSet());
        List<ArticleComment> allReplies = articleCommentMapper.selectList(new LambdaQueryWrapper<ArticleComment>()
                .in(ArticleComment::getParentId, topIds)
                .eq(ArticleComment::getStatus, COMMENT_STATUS_PUBLISHED)
                .orderByDesc(ArticleComment::getCreatedTime));

        Map<Long, List<ArticleComment>> replyMap = allReplies.stream()
                .collect(Collectors.groupingBy(ArticleComment::getParentId));

        Set<Long> userIds = new HashSet<>();
        tops.forEach(c -> userIds.add(c.getUserId()));
        allReplies.forEach(c -> userIds.add(c.getUserId()));

        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));

        Set<Long> allCommentIds = new HashSet<>();
        tops.forEach(c -> allCommentIds.add(c.getId()));
        allReplies.forEach(c -> allCommentIds.add(c.getId()));
        Set<Long> likedCommentIds = loadLikedCommentIds(userId, allCommentIds);

        List<ArticleCommentVO> list = tops.stream().map(top -> {
            List<ArticleReplyVO> replies = replyMap.getOrDefault(top.getId(), Collections.emptyList())
                    .stream()
                    .limit(3)
                    .map(reply -> toReplyVO(reply, userMap.get(reply.getUserId()), likedCommentIds.contains(reply.getId())))
                    .collect(Collectors.toList());
            return toCommentVO(top, userMap.get(top.getUserId()), likedCommentIds.contains(top.getId()), replies);
        }).collect(Collectors.toList());

        return new PageVO<>(commentPage.getTotal(), pageNum, size, pages(commentPage.getTotal(), size), list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleCommentVO createComment(Long userId, Long articleId, CreateCommentDTO dto) {
        requirePublishedArticle(articleId);

        if (dto == null || isBlank(dto.getContent())) {
            throw new BusinessException(40081, "评论内容不能为空");
        }
        String content = dto.getContent().trim();
        if (content.length() > 1000) {
            throw new BusinessException(40082, "评论内容不能超过1000字");
        }

        Long parentId = parseOptionalId(dto.getParentId(), "parentId", 40083);
        if (parentId != null) {
            ArticleComment parent = articleCommentMapper.selectById(parentId);
            if (parent == null || !Objects.equals(parent.getArticleId(), articleId)
                    || !Objects.equals(parent.getStatus(), COMMENT_STATUS_PUBLISHED)) {
                throw new BusinessException(40083, "父评论不存在或已删除");
            }
        }

        long now = System.currentTimeMillis();
        ArticleComment comment = new ArticleComment();
        comment.setId(snowflakeIdGenerator.nextId());
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setContent(content);
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        comment.setStatus(COMMENT_STATUS_PUBLISHED);
        comment.setCreatedTime(now);
        comment.setUpdatedTime(now);
        articleCommentMapper.insert(comment);

        if (parentId != null) {
            articleCommentMapper.update(null, new LambdaUpdateWrapper<ArticleComment>()
                    .eq(ArticleComment::getId, parentId)
                    .setSql("reply_count = reply_count + 1"));
        }

        User user = userMapper.selectById(userId);
        return toCommentVO(comment, user, false, Collections.emptyList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentLikeToggleVO toggleCommentLike(Long userId, Long commentId) {
        ArticleComment comment = articleCommentMapper.selectById(commentId);
        if (comment == null || !Objects.equals(comment.getStatus(), COMMENT_STATUS_PUBLISHED)) {
            throw new BusinessException(40084, "评论不存在或已删除");
        }

        CommentLike existing = commentLikeMapper.selectOne(new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, userId)
                .last("limit 1"));

        boolean liked;
        if (existing != null) {
            commentLikeMapper.deleteById(existing.getId());
            articleCommentMapper.update(null, new LambdaUpdateWrapper<ArticleComment>()
                    .eq(ArticleComment::getId, commentId)
                    .setSql("like_count = GREATEST(like_count - 1, 0)"));
            liked = false;
        } else {
            CommentLike like = new CommentLike();
            like.setId(snowflakeIdGenerator.nextId());
            like.setCommentId(commentId);
            like.setUserId(userId);
            like.setCreatedTime(System.currentTimeMillis());
            commentLikeMapper.insert(like);
            articleCommentMapper.update(null, new LambdaUpdateWrapper<ArticleComment>()
                    .eq(ArticleComment::getId, commentId)
                    .setSql("like_count = like_count + 1"));
            liked = true;
        }

        ArticleComment latest = articleCommentMapper.selectById(commentId);
        return new CommentLikeToggleVO(String.valueOf(commentId), liked, safeInt(latest.getLikeCount()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long operatorId, Integer roleId, Long commentId) {
        ArticleComment comment = articleCommentMapper.selectById(commentId);
        if (comment == null || Objects.equals(comment.getStatus(), COMMENT_STATUS_DELETED)) {
            throw new BusinessException(40084, "评论不存在或已删除");
        }

        boolean isOwner = Objects.equals(comment.getUserId(), operatorId);
        boolean isAdmin = Objects.equals(roleId, 0);
        if (!isOwner && !isAdmin) {
            throw new BusinessException(403, "只有评论作者或管理员可删除");
        }

        Long parentId = comment.getParentId();

        // 先删子评论和点赞，再删本评论，避免保留脏计数。
        deleteCommentCascade(commentId);

        if (parentId != null) {
            articleCommentMapper.update(null, new LambdaUpdateWrapper<ArticleComment>()
                    .eq(ArticleComment::getId, parentId)
                    .setSql("reply_count = GREATEST(reply_count - 1, 0)"));
        }
    }

    private void deleteCommentCascade(Long commentId) {
        List<ArticleComment> children = articleCommentMapper.selectList(new LambdaQueryWrapper<ArticleComment>()
                .eq(ArticleComment::getParentId, commentId));
        for (ArticleComment child : children) {
            deleteCommentCascade(child.getId());
        }

        commentLikeMapper.delete(new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getCommentId, commentId));

        articleCommentMapper.deleteById(commentId);
    }

    private Article requirePublishedArticle(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null || !Objects.equals(article.getDeleted(), 0) || !Objects.equals(article.getStatus(), 1)) {
            throw new BusinessException(40085, "文章不存在或不可访问");
        }
        return article;
    }

    private Map<Long, Long> loadCommentCountMap(Set<Long> articleIds) {
        if (articleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ArticleComment> comments = articleCommentMapper.selectList(new LambdaQueryWrapper<ArticleComment>()
                .select(ArticleComment::getArticleId)
                .in(ArticleComment::getArticleId, articleIds)
                .eq(ArticleComment::getStatus, COMMENT_STATUS_PUBLISHED));

        return comments.stream().collect(Collectors.groupingBy(ArticleComment::getArticleId, Collectors.counting()));
    }

    private Set<Long> loadLikedArticleIds(Long userId, Set<Long> articleIds) {
        if (userId == null || articleIds.isEmpty()) {
            return Collections.emptySet();
        }
        return articleLikeMapper.selectList(new LambdaQueryWrapper<ArticleLike>()
                        .eq(ArticleLike::getUserId, userId)
                        .in(ArticleLike::getArticleId, articleIds))
                .stream().map(ArticleLike::getArticleId).collect(Collectors.toSet());
    }

    private Set<Long> loadLikedCommentIds(Long userId, Set<Long> commentIds) {
        if (userId == null || commentIds.isEmpty()) {
            return Collections.emptySet();
        }
        return commentLikeMapper.selectList(new LambdaQueryWrapper<CommentLike>()
                        .eq(CommentLike::getUserId, userId)
                        .in(CommentLike::getCommentId, commentIds))
                .stream().map(CommentLike::getCommentId).collect(Collectors.toSet());
    }

    private ArticleSummaryVO toArticleSummaryVO(Article article, User user, Integer commentCount, boolean liked) {
        String summary = article.getSummary();
        if (isBlank(summary) && !isBlank(article.getContent())) {
            String plain = article.getContent().replaceAll("<[^>]*>", "").trim();
            summary = plain.length() > 120 ? plain.substring(0, 120) + "..." : plain;
        }

        return new ArticleSummaryVO(
            String.valueOf(article.getId()),
            String.valueOf(article.getAuthorId()),
                user == null ? "未知用户" : user.getUsername(),
                article.getTitle(),
                article.getCategory(),
                summary,
                article.getContent(),
                article.getCoverImage(),
                safeInt(article.getViewCount()),
                safeInt(article.getLikeCount()),
                safeInt(commentCount),
                liked,
                article.getCreatedTime(),
                article.getUpdatedTime()
        );
    }

    private ArticleCommentVO toCommentVO(ArticleComment comment, User user, boolean isLiked, List<ArticleReplyVO> replies) {
        return new ArticleCommentVO(
            String.valueOf(comment.getId()),
            String.valueOf(comment.getUserId()),
                user == null ? "未知用户" : user.getUsername(),
                user == null ? null : user.getImage(),
                comment.getContent(),
                safeInt(comment.getLikeCount()),
                safeInt(comment.getReplyCount()),
                isLiked,
                comment.getCreatedTime(),
                replies
        );
    }

    private ArticleReplyVO toReplyVO(ArticleComment comment, User user, boolean isLiked) {
        return new ArticleReplyVO(
            String.valueOf(comment.getId()),
            String.valueOf(comment.getUserId()),
                user == null ? "未知用户" : user.getUsername(),
                user == null ? null : user.getImage(),
                comment.getContent(),
                safeInt(comment.getLikeCount()),
                isLiked,
                comment.getCreatedTime()
        );
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int normalizePageSize(Integer pageSize, int defaultSize, int maxSize) {
        int size = pageSize == null || pageSize < 1 ? defaultSize : pageSize;
        return Math.min(size, maxSize);
    }

    private int pages(long total, int pageSize) {
        return (int) ((total + pageSize - 1) / pageSize);
    }

    private Integer safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String emptyToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private Long parseOptionalId(String rawId, String fieldName, int errorCode) {
        if (isBlank(rawId)) {
            return null;
        }

        try {
            return Long.parseLong(rawId.trim());
        } catch (NumberFormatException ex) {
            throw new BusinessException(errorCode, fieldName + " 格式不正确");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
