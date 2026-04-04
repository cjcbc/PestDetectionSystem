-- 病虫害检测系统 - 新增业务表
-- 执行前需先执行 user_table.sql 和 chat_tables.sql

USE `pest_detection_system`;

-- ----------------------------
-- 1. 预警信息表
-- ----------------------------
DROP TABLE IF EXISTS `warning`;
CREATE TABLE `warning` (
  `id` bigint unsigned NOT NULL COMMENT '预警ID，雪花算法生成',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预警标题',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预警内容',
  `region` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '受影响地区',
  `pest_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联病虫害名称',
  `severity` tinyint NOT NULL DEFAULT '1' COMMENT '严重程度：1低 2中 3高',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0下架 1上架',
  `publish_time` bigint unsigned NOT NULL COMMENT '发布时间戳（毫秒）',
  `publisher_id` bigint unsigned NOT NULL COMMENT '发布人ID',
  `view_count` int unsigned NOT NULL DEFAULT '0' COMMENT '浏览量',
  `created_time` bigint unsigned NOT NULL COMMENT '创建时间戳（毫秒）',
  `updated_time` bigint unsigned NOT NULL COMMENT '更新时间戳（毫秒）',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_warning_publisher_id` (`publisher_id`),
  KEY `idx_warning_status` (`status`),
  KEY `idx_warning_publish_time` (`publish_time`),
  CONSTRAINT `fk_warning_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='病虫害预警信息表';

-- ----------------------------
-- 2. 农业资讯表
-- ----------------------------
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article` (
  `id` bigint unsigned NOT NULL COMMENT '资讯ID，雪花算法生成',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资讯标题',
  `summary` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '资讯摘要',
  `content` longtext COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资讯正文（HTML/富文本）',
  `cover_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面图片URL',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类：如 种植技术、病虫害防治、市场动态',
  `tags` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签，逗号分隔',
  `author_id` bigint unsigned NOT NULL COMMENT '作者ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0待审核 1已发布 2下架',
  `audit_comment` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核备注',
  `audit_time` bigint unsigned DEFAULT NULL COMMENT '审核时间戳（毫秒）',
  `auditor_id` bigint unsigned DEFAULT NULL COMMENT '审核人ID',
  `publish_time` bigint unsigned DEFAULT NULL COMMENT '发布时间戳（毫秒）',
  `view_count` int unsigned NOT NULL DEFAULT '0' COMMENT '浏览量',
  `like_count` int unsigned NOT NULL DEFAULT '0' COMMENT '点赞数',
  `created_time` bigint unsigned NOT NULL COMMENT '创建时间戳（毫秒）',
  `updated_time` bigint unsigned NOT NULL COMMENT '更新时间戳（毫秒）',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_article_author_id` (`author_id`),
  KEY `idx_article_status` (`status`),
  KEY `idx_article_category` (`category`),
  KEY `idx_article_publish_time` (`publish_time`),
  CONSTRAINT `fk_article_author` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='农业资讯表';

-- ----------------------------
-- 3. 留言反馈表
-- ----------------------------
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback` (
  `id` bigint unsigned NOT NULL COMMENT '留言ID，雪花算法生成',
  `user_id` bigint unsigned NOT NULL COMMENT '留言用户ID',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '类型：1意见反馈 2问题咨询 3功能建议',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '留言标题',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '留言内容',
  `images` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图片URLs，逗号分隔',
  `contact` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系方式',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0待处理 1已回复 2已关闭',
  `reply_content` text COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '管理员回复内容',
  `reply_time` bigint unsigned DEFAULT NULL COMMENT '回复时间戳（毫秒）',
  `reply_admin_id` bigint unsigned DEFAULT NULL COMMENT '回复管理员ID',
  `created_time` bigint unsigned NOT NULL COMMENT '创建时间戳（毫秒）',
  `updated_time` bigint unsigned NOT NULL COMMENT '更新时间戳（毫秒）',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_feedback_user_id` (`user_id`),
  KEY `idx_feedback_status` (`status`),
  KEY `idx_feedback_type` (`type`),
  KEY `idx_feedback_created_time` (`created_time`),
  CONSTRAINT `fk_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户留言反馈表';

-- ----------------------------
-- 4. 识别记录表（补充字段，参考 chat_tables.sql 中已定义）
-- ----------------------------
-- detection_record 表已在 chat_tables.sql 中定义，此处无需重复创建
