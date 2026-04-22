CREATE DATABASE  IF NOT EXISTS `pest_detection_system` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `pest_detection_system`;
-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: pest_detection_system
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `article`
--

DROP TABLE IF EXISTS `article`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article` (
  `id` bigint unsigned NOT NULL COMMENT '资讯ID，雪花算法生成',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资讯标题',
  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '资讯摘要',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资讯正文（HTML/富文本）',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面图片URL',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类：如 种植技术、病虫害防治、市场动态',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签，逗号分隔',
  `author_id` bigint unsigned NOT NULL COMMENT '作者ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0待审核 1已发布 2下架',
  `audit_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核备注',
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `article_comment`
--

DROP TABLE IF EXISTS `article_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article_comment` (
  `id` bigint unsigned NOT NULL COMMENT '评论ID，雪花算法生成',
  `article_id` bigint unsigned NOT NULL COMMENT '文章ID',
  `user_id` bigint unsigned NOT NULL COMMENT '评论用户ID',
  `parent_id` bigint unsigned DEFAULT NULL COMMENT '父评论ID（用于评论回复，NULL表示顶级评论）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `like_count` int unsigned NOT NULL DEFAULT '0' COMMENT '评论点赞数',
  `reply_count` int unsigned NOT NULL DEFAULT '0' COMMENT '回复数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0待审核 1已发布 2已删除',
  `created_time` bigint unsigned NOT NULL COMMENT '创建时间戳（毫秒）',
  `updated_time` bigint unsigned NOT NULL COMMENT '更新时间戳（毫秒）',
  PRIMARY KEY (`id`),
  KEY `idx_article_comment_article_id` (`article_id`),
  KEY `idx_article_comment_user_id` (`user_id`),
  KEY `idx_article_comment_parent_id` (`parent_id`),
  KEY `idx_article_comment_created_time` (`created_time`),
  CONSTRAINT `fk_article_comment_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_article_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `article_comment` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_article_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `article_like`
--

DROP TABLE IF EXISTS `article_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article_like` (
  `id` bigint unsigned NOT NULL COMMENT '点赞记录ID，雪花算法生成',
  `article_id` bigint unsigned NOT NULL COMMENT '文章ID',
  `user_id` bigint unsigned NOT NULL COMMENT '点赞用户ID',
  `created_time` bigint unsigned NOT NULL COMMENT '点赞时间戳（毫秒）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_user` (`article_id`,`user_id`) COMMENT '同一用户只能点赞一次',
  KEY `idx_article_like_user_id` (`user_id`),
  KEY `idx_article_like_article_id` (`article_id`),
  CONSTRAINT `fk_article_like_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_article_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章点赞记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chat_message`
--

DROP TABLE IF EXISTS `chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_message` (
  `id` bigint unsigned NOT NULL COMMENT '消息ID，雪花算法生成',
  `session_id` bigint unsigned NOT NULL COMMENT '所属会话ID',
  `user_id` bigint unsigned NOT NULL COMMENT '所属用户ID',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息角色：system、user、assistant',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '本次调用使用的模型名称',
  `prompt_tokens` int unsigned NOT NULL DEFAULT '0' COMMENT '输入token数',
  `completion_tokens` int unsigned NOT NULL DEFAULT '0' COMMENT '输出token数',
  `total_tokens` int unsigned NOT NULL DEFAULT '0' COMMENT '总token数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '消息状态：0失败，1成功',
  `created_time` bigint unsigned NOT NULL COMMENT '创建时间戳（毫秒）',
  `detection_id` bigint DEFAULT NULL COMMENT '关联的检测记录ID',
  PRIMARY KEY (`id`),
  KEY `idx_chat_message_session_id` (`session_id`),
  KEY `idx_chat_message_user_id` (`user_id`),
  KEY `idx_chat_message_created_time` (`created_time`),
  KEY `idx_chat_message_detection` (`detection_id`),
  CONSTRAINT `fk_chat_message_session` FOREIGN KEY (`session_id`) REFERENCES `chat_session` (`id`),
  CONSTRAINT `fk_chat_message_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM对话消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chat_quota_daily`
--

DROP TABLE IF EXISTS `chat_quota_daily`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_quota_daily` (
  `id` bigint unsigned NOT NULL COMMENT '额度记录ID，雪花算法生成',
  `user_id` bigint unsigned NOT NULL COMMENT '所属用户ID',
  `quota_date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '额度日期，如 2026-03-29',
  `request_count` int unsigned NOT NULL DEFAULT '0' COMMENT '当日请求次数',
  `input_tokens` int unsigned NOT NULL DEFAULT '0' COMMENT '当日输入token累计',
  `output_tokens` int unsigned NOT NULL DEFAULT '0' COMMENT '当日输出token累计',
  `created_time` bigint unsigned NOT NULL COMMENT '创建时间戳（毫秒）',
  `updated_time` bigint unsigned NOT NULL COMMENT '更新时间戳（毫秒）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chat_quota_user_date` (`user_id`,`quota_date`),
  KEY `idx_chat_quota_updated_time` (`updated_time`),
  CONSTRAINT `fk_chat_quota_daily_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM对话每日额度表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chat_session`
--

DROP TABLE IF EXISTS `chat_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_session` (
  `id` bigint unsigned NOT NULL COMMENT '会话ID，雪花算法生成',
  `user_id` bigint unsigned NOT NULL COMMENT '所属用户ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话标题',
  `scene` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'expert' COMMENT '会话场景，如 expert、diagnosis',
  `detection_id` bigint unsigned DEFAULT NULL COMMENT '关联的识别记录ID，仅识别后咨询场景使用',
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '历史摘要，供长对话压缩上下文使用',
  `message_count` int unsigned NOT NULL DEFAULT '0' COMMENT '消息总数',
  `last_message_at` bigint unsigned NOT NULL COMMENT '最近一条消息时间戳（毫秒）',
  `created_time` bigint unsigned NOT NULL COMMENT '创建时间戳（毫秒）',
  `updated_time` bigint unsigned NOT NULL COMMENT '更新时间戳（毫秒）',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_chat_session_user_id` (`user_id`),
  KEY `idx_chat_session_detection_id` (`detection_id`),
  KEY `idx_chat_session_last_message_at` (`last_message_at`),
  CONSTRAINT `fk_chat_session_detection` FOREIGN KEY (`detection_id`) REFERENCES `detection_record` (`id`),
  CONSTRAINT `fk_chat_session_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM对话会话表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `comment_like`
--

DROP TABLE IF EXISTS `comment_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment_like` (
  `id` bigint unsigned NOT NULL COMMENT '点赞记录ID，雪花算法生成',
  `comment_id` bigint unsigned NOT NULL COMMENT '评论ID',
  `user_id` bigint unsigned NOT NULL COMMENT '点赞用户ID',
  `created_time` bigint unsigned NOT NULL COMMENT '点赞时间戳（毫秒）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`,`user_id`) COMMENT '同一用户只能点赞一次',
  KEY `idx_comment_like_user_id` (`user_id`),
  KEY `idx_comment_like_comment_id` (`comment_id`),
  CONSTRAINT `fk_comment_like_comment` FOREIGN KEY (`comment_id`) REFERENCES `article_comment` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `detection_record`
--

DROP TABLE IF EXISTS `detection_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detection_record` (
  `id` bigint unsigned NOT NULL COMMENT '识别记录ID，雪花算法生成',
  `user_id` bigint unsigned NOT NULL COMMENT '所属用户ID',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始图片访问地址',
  `original_file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '原始文件名',
  `result_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型完整识别结果JSON',
  `top_label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最高置信度标签',
  `confidence` decimal(10,4) DEFAULT NULL COMMENT '最高置信度',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '记录状态：0失败，1成功',
  `created_time` bigint unsigned NOT NULL COMMENT '创建时间戳（毫秒）',
  `updated_time` bigint unsigned NOT NULL COMMENT '更新时间戳（毫秒）',
  PRIMARY KEY (`id`),
  KEY `idx_detection_record_user_id` (`user_id`),
  KEY `idx_detection_record_created_time` (`created_time`),
  CONSTRAINT `fk_detection_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='病虫害识别记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `feedback`
--

DROP TABLE IF EXISTS `feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `feedback` (
  `id` bigint unsigned NOT NULL COMMENT '留言ID，雪花算法生成',
  `user_id` bigint unsigned NOT NULL COMMENT '留言用户ID',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '类型：1意见反馈 2问题咨询 3功能建议',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '留言标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '留言内容',
  `images` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图片URLs，逗号分隔',
  `contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系方式',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0待处理 1已回复 2已关闭',
  `reply_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '管理员回复内容',
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint unsigned NOT NULL COMMENT '雪花算法ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '哈希值',
  `salt` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码盐值',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `created_time` bigint unsigned NOT NULL COMMENT '创建时间戳（毫秒）',
  `role` tinyint NOT NULL DEFAULT '1' COMMENT '用户角色: 0管理员, 1普通用户',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  `sex` tinyint unsigned DEFAULT NULL COMMENT '0 男\r\n            1 女\r\n            2 未知',
  `is_identity_authentication` tinyint(1) DEFAULT NULL COMMENT '是否身份认证',
  `status` tinyint unsigned DEFAULT NULL COMMENT '0正常\r\n            1锁定',
  `flag` tinyint unsigned DEFAULT NULL COMMENT '0 普通用户\r\n            1 自媒体人\r\n            2 大V',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `phone` (`phone`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_user_name` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_fan`
--

DROP TABLE IF EXISTS `user_fan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_fan` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int unsigned DEFAULT NULL COMMENT '用户ID',
  `fans_id` int unsigned DEFAULT NULL COMMENT '粉丝ID',
  `fans_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '粉丝昵称',
  `level` tinyint unsigned DEFAULT NULL COMMENT '粉丝忠实度\r\n            0 正常\r\n            1 潜力股\r\n            2 勇士\r\n            3 铁杆\r\n            4 老铁',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `is_display` tinyint unsigned DEFAULT NULL COMMENT '是否可见我动态',
  `is_shield_letter` tinyint unsigned DEFAULT NULL COMMENT '是否屏蔽私信',
  `is_shield_comment` tinyint unsigned DEFAULT NULL COMMENT '是否屏蔽评论',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='APP用户粉丝信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_follow`
--

DROP TABLE IF EXISTS `user_follow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_follow` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int unsigned DEFAULT NULL COMMENT '用户ID',
  `follow_id` int unsigned DEFAULT NULL COMMENT '关注作者ID',
  `follow_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '粉丝昵称',
  `level` tinyint unsigned DEFAULT NULL COMMENT '关注度\r\n            0 偶尔感兴趣\r\n            1 一般\r\n            2 经常\r\n            3 高度',
  `is_notice` tinyint unsigned DEFAULT NULL COMMENT '是否动态通知',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='APP用户关注信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `warning`
--

DROP TABLE IF EXISTS `warning`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `warning` (
  `id` bigint unsigned NOT NULL COMMENT '预警ID，雪花算法生成',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预警标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预警内容',
  `region` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '受影响地区',
  `pest_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联病虫害名称',
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `warning_read`
--

DROP TABLE IF EXISTS `warning_read`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `warning_read` (
  `id` bigint unsigned NOT NULL COMMENT '已读记录ID，雪花算法生成',
  `warning_id` bigint unsigned NOT NULL COMMENT '预警ID',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `read_time` bigint unsigned NOT NULL COMMENT '阅读时间戳（毫秒）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warning_user` (`warning_id`,`user_id`) COMMENT '同一用户对同一预警只记录一次',
  KEY `idx_warning_read_user_id` (`user_id`),
  KEY `idx_warning_read_warning_id` (`warning_id`),
  KEY `idx_warning_read_time` (`read_time`),
  CONSTRAINT `fk_warning_read_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_warning_read_warning` FOREIGN KEY (`warning_id`) REFERENCES `warning` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户预警已读记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'pest_detection_system'
--

--
-- Dumping routines for database 'pest_detection_system'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-21 20:05:57
