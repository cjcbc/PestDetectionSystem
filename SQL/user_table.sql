USE pest_detection_system;
CREATE TABLE USER(
id BIGINT UNSIGNED PRIMARY KEY COMMENT '雪花算法ID',
username VARCHAR(50) NOT NULL COMMENT '用户名',
password VARCHAR(255) NOT NULL COMMENT '哈希值',
salt VARCHAR(64) NOT NULL COMMENT '密码盐值',
email VARCHAR(100) UNIQUE COMMENT '邮箱',
phone VARCHAR(20) UNIQUE COMMENT '手机号',
created_time BIGINT UNSIGNED NOT NULL COMMENT '创建时间戳（毫秒）',
INDEX idx_created_time (created_time),
INDEX idx_user_name (username)
)ENGINE=InnoDB  
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci  
COMMENT='用户信息表'
