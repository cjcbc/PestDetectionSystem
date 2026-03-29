CREATE DATABASE IF NOT EXISTS `pest_detection_system` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `pest_detection_system`;

-- LLM 专家对话系统核心表

DROP TABLE IF EXISTS `chat_quota_daily`;
DROP TABLE IF EXISTS `chat_message`;
DROP TABLE IF EXISTS `chat_session`;
DROP TABLE IF EXISTS `detection_record`;

CREATE TABLE `chat_session` (
  `id` bigint unsigned NOT NULL COMMENT '会话ID，雪花算法生成',
  `user_id` bigint unsigned NOT NULL COMMENT '所属用户ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话标题',
  `scene` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'expert' COMMENT '会话场景，如 expert、diagnosis',
  `detection_id` bigint unsigned DEFAULT NULL COMMENT '关联的识别记录ID，仅识别后咨询场景使用',
  `summary` text COLLATE utf8mb4_unicode_ci COMMENT '历史摘要，供长对话压缩上下文使用',
  `message_count` int unsigned NOT NULL DEFAULT '0' COMMENT '消息总数',
  `last_message_at` bigint unsigned NOT NULL COMMENT '最近一条消息时间戳（毫秒）',
  `created_time` bigint unsigned NOT NULL COMMENT '创建时间戳（毫秒）',
  `updated_time` bigint unsigned NOT NULL COMMENT '更新时间戳（毫秒）',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_chat_session_user_id` (`user_id`),
  KEY `idx_chat_session_detection_id` (`detection_id`),
  KEY `idx_chat_session_last_message_at` (`last_message_at`),
  CONSTRAINT `fk_chat_session_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM对话会话表';

CREATE TABLE `detection_record` (
  `id` bigint unsigned NOT NULL COMMENT '识别记录ID，雪花算法生成',
  `user_id` bigint unsigned NOT NULL COMMENT '所属用户ID',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始图片访问地址',
  `original_file_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '原始文件名',
  `result_json` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型完整识别结果JSON',
  `top_label` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最高置信度标签',
  `confidence` decimal(10,4) DEFAULT NULL COMMENT '最高置信度',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '记录状态：0失败，1成功',
  `created_time` bigint unsigned NOT NULL COMMENT '创建时间戳（毫秒）',
  `updated_time` bigint unsigned NOT NULL COMMENT '更新时间戳（毫秒）',
  PRIMARY KEY (`id`),
  KEY `idx_detection_record_user_id` (`user_id`),
  KEY `idx_detection_record_created_time` (`created_time`),
  CONSTRAINT `fk_detection_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='病虫害识别记录表';

CREATE TABLE `chat_message` (
  `id` bigint unsigned NOT NULL COMMENT '消息ID，雪花算法生成',
  `session_id` bigint unsigned NOT NULL COMMENT '所属会话ID',
  `user_id` bigint unsigned NOT NULL COMMENT '所属用户ID',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息角色：system、user、assistant',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `model` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '本次调用使用的模型名称',
  `prompt_tokens` int unsigned NOT NULL DEFAULT '0' COMMENT '输入token数',
  `completion_tokens` int unsigned NOT NULL DEFAULT '0' COMMENT '输出token数',
  `total_tokens` int unsigned NOT NULL DEFAULT '0' COMMENT '总token数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '消息状态：0失败，1成功',
  `created_time` bigint unsigned NOT NULL COMMENT '创建时间戳（毫秒）',
  PRIMARY KEY (`id`),
  KEY `idx_chat_message_session_id` (`session_id`),
  KEY `idx_chat_message_user_id` (`user_id`),
  KEY `idx_chat_message_created_time` (`created_time`),
  CONSTRAINT `fk_chat_message_session` FOREIGN KEY (`session_id`) REFERENCES `chat_session` (`id`),
  CONSTRAINT `fk_chat_message_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM对话消息表';

CREATE TABLE `chat_quota_daily` (
  `id` bigint unsigned NOT NULL COMMENT '额度记录ID，雪花算法生成',
  `user_id` bigint unsigned NOT NULL COMMENT '所属用户ID',
  `quota_date` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '额度日期，如 2026-03-29',
  `request_count` int unsigned NOT NULL DEFAULT '0' COMMENT '当日请求次数',
  `input_tokens` int unsigned NOT NULL DEFAULT '0' COMMENT '当日输入token累计',
  `output_tokens` int unsigned NOT NULL DEFAULT '0' COMMENT '当日输出token累计',
  `created_time` bigint unsigned NOT NULL COMMENT '创建时间戳（毫秒）',
  `updated_time` bigint unsigned NOT NULL COMMENT '更新时间戳（毫秒）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chat_quota_user_date` (`user_id`, `quota_date`),
  KEY `idx_chat_quota_updated_time` (`updated_time`),
  CONSTRAINT `fk_chat_quota_daily_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM对话每日额度表';

ALTER TABLE `chat_session`
  ADD CONSTRAINT `fk_chat_session_detection` FOREIGN KEY (`detection_id`) REFERENCES `detection_record` (`id`);
