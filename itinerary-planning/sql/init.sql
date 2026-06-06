-- 用户表
CREATE DATABASE IF NOT EXISTS itinerary_planning DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE itinerary_planning;

-- 用户表
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 目的地表
DROP TABLE IF EXISTS `destinations`;
CREATE TABLE `destinations` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '目的地ID',
    `name` VARCHAR(100) NOT NULL COMMENT '目的地名称',
    `description` TEXT COMMENT '目的地描述',
    `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
    `province` VARCHAR(50) DEFAULT NULL COMMENT '省份',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
    `latitude` DECIMAL(10, 6) DEFAULT NULL COMMENT '纬度',
    `longitude` DECIMAL(10, 6) DEFAULT NULL COMMENT '经度',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '分类（景点、美食、购物等）',
    `rating` DECIMAL(2, 1) DEFAULT NULL COMMENT '评分',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图片URL',
    `open_time` VARCHAR(100) DEFAULT NULL COMMENT '开放时间',
    `ticket_price` DECIMAL(10, 2) DEFAULT NULL COMMENT '门票价格',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_city` (`city`),
    KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='目的地表';

-- 行程表
DROP TABLE IF EXISTS `itineraries`;
CREATE TABLE `itineraries` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '行程ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(100) NOT NULL COMMENT '行程标题',
    `description` TEXT COMMENT '行程描述',
    `start_date` DATE DEFAULT NULL COMMENT '开始日期',
    `end_date` DATE DEFAULT NULL COMMENT '结束日期',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-计划中，1-进行中，2-已完成，3-已取消',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行程表';

-- 行程目的地关联表
DROP TABLE IF EXISTS `itinerary_destinations`;
CREATE TABLE `itinerary_destinations` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `itinerary_id` BIGINT NOT NULL COMMENT '行程ID',
    `destination_id` BIGINT NOT NULL COMMENT '目的地ID',
    `visit_order` INT DEFAULT 0 COMMENT '游览顺序',
    `visit_date` DATE DEFAULT NULL COMMENT '游览日期',
    `note` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_itinerary_id` (`itinerary_id`),
    KEY `idx_destination_id` (`destination_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行程目的地关联表';

-- Seata AT 模式回滚日志表
DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log` (
    `branch_id` BIGINT NOT NULL COMMENT '分支事务ID',
    `xid` VARCHAR(128) NOT NULL COMMENT '全局事务ID',
    `context` VARCHAR(128) NOT NULL COMMENT 'undo_log上下文',
    `rollback_info` LONGBLOB NOT NULL COMMENT '回滚信息',
    `log_status` INT NOT NULL COMMENT '状态：0-正常，1-防悬挂',
    `log_created` DATETIME(6) NOT NULL COMMENT '创建时间',
    `log_modified` DATETIME(6) NOT NULL COMMENT '修改时间',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT事务回滚日志表';

-- 插入示例目的地数据
INSERT INTO `destinations` (`name`, `description`, `city`, `province`, `category`, `rating`, `open_time`, `ticket_price`) VALUES
('西湖', '杭州最著名的自然景观，世界文化遗产', '杭州', '浙江', '景点', 4.8, '全天开放', 0.00),
('灵隐寺', '千年古刹，杭州著名佛教寺院', '杭州', '浙江', '景点', 4.6, '07:00-18:00', 75.00),
('外滩', '上海的标志性景观，万国建筑博览群', '上海', '上海', '景点', 4.7, '全天开放', 0.00),
('南京路步行街', '中华商业第一街，购物天堂', '上海', '上海', '购物', 4.3, '全天开放', 0.00),
('故宫博物院', '世界上最大的古代宫殿建筑群', '北京', '北京', '景点', 4.9, '08:30-17:00', 60.00),
('南锣鼓巷', '北京最古老的街区之一，小吃和文创聚集地', '北京', '北京', '美食', 4.2, '全天开放', 0.00),
('宽窄巷子', '成都历史文化街区，体验巴蜀文化', '成都', '四川', '美食', 4.4, '全天开放', 0.00),
('大熊猫繁育研究基地', '近距离观赏国宝大熊猫', '成都', '四川', '景点', 4.8, '07:30-18:00', 55.00);
