
-- library_db.users definition

CREATE TABLE `users` (
                         `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
                         `username` varchar(50) NOT NULL COMMENT '用户名（学号/工号）',
                         `name` varchar(50) NOT NULL COMMENT '姓名',
                         `password` varchar(255) NOT NULL COMMENT '密码（加密存储）',
                         `email` varchar(100) NOT NULL COMMENT '电子邮箱',
                         `role` varchar(20) NOT NULL DEFAULT 'user' COMMENT '角色：user/admin',
                         `user_type` varchar(20) NOT NULL COMMENT '用户类型：student/teacher/admin',
                         `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `username` (`username`),
                         KEY `idx_username` (`username`),
                         KEY `idx_role` (`role`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';


CREATE TABLE `books` (
                         `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
                         `title` varchar(200) NOT NULL COMMENT '书名',
                         `author` varchar(100) NOT NULL COMMENT '作者',
                         `publisher` varchar(100) NOT NULL COMMENT '出版社',
                         `isbn` varchar(20) NOT NULL COMMENT 'ISBN 编号',
                         `description` text COMMENT '简介',
                         `cover_url` varchar(500) DEFAULT NULL COMMENT '封面图片 URL',
                         `category` varchar(50) DEFAULT NULL COMMENT '图书分类',
                         `total_copies` int NOT NULL DEFAULT '1' COMMENT '总复本数',
                         `available_copies` int NOT NULL DEFAULT '1' COMMENT '可借复本数',
                         `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
                         PRIMARY KEY (`id`),
                         KEY `idx_title` (`title`),
                         KEY `idx_author` (`author`),
                         KEY `idx_isbn` (`isbn`),
                         KEY `idx_category` (`category`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='图书表';

-- library_db.borrow_records definition

CREATE TABLE `borrow_records` (
                                  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
                                  `user_id` int NOT NULL COMMENT '用户 ID',
                                  `book_id` int NOT NULL COMMENT '图书 ID',
                                  `borrow_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '借阅日期',
                                  `due_date` datetime NOT NULL COMMENT '应还日期',
                                  `return_date` datetime DEFAULT NULL COMMENT '实际归还日期',
                                  `status` varchar(20) NOT NULL DEFAULT 'borrowing' COMMENT '状态：borrowing/returned',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_user_id` (`user_id`),
                                  KEY `idx_book_id` (`book_id`),
                                  KEY `idx_status` (`status`),
                                  KEY `idx_due_date` (`due_date`),
                                  CONSTRAINT `borrow_records_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                  CONSTRAINT `borrow_records_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='借阅记录表';


INSERT INTO `users` (`id`, `username`, `name`, `password`, `email`, `role`, `user_type`, `created_at`) VALUES (1, 'admin', '系统管理员', 'e10adc3949ba59abbe56e057f20f883e', 'admin@library.com', 'admin', 'admin', '2026-04-07 09:32:43');
INSERT INTO `users` (`id`, `username`, `name`, `password`, `email`, `role`, `user_type`, `created_at`) VALUES (2, '20220101048', '大哥', '4f24a9fa587fc899aee1c3f783655f7c', 'chende@wtbu.edu.cn', 'user', 'student', '2026-04-07 09:42:27');
INSERT INTO `users` (`id`, `username`, `name`, `password`, `email`, `role`, `user_type`, `created_at`) VALUES (4, '25204221', '寥寥', '0276ca8dd9fbdd38989af241b4920ddf', '1231234123@qq.com', 'user', 'student', '2026-04-07 09:45:02');
INSERT INTO `users` (`id`, `username`, `name`, `password`, `email`, `role`, `user_type`, `created_at`) VALUES (5, '123456', '张三', '65f4c88dd45490f3860bde00a7533d11', '111@qq.com', 'user', 'teacher', '2026-04-07 09:51:24');

INSERT INTO `books` (`id`, `title`, `author`, `publisher`, `isbn`, `description`, `cover_url`, `category`, `total_copies`, `available_copies`, `created_at`) VALUES (1, 'Java 编程思想（第 4 版）', 'Bruce Eckel', '机械工业出版社', '978-7-111-21382-6', '本书介绍了\r\n  Java 编程的基本思想和核心技术，包括面向对象编程、泛\r\n  型、并发编程等内容。', NULL, '计算机/互联网', 5, 4, '2026-04-07 09:36:32');
INSERT INTO `books` (`id`, `title`, `author`, `publisher`, `isbn`, `description`, `cover_url`, `category`, `total_copies`, `available_copies`, `created_at`) VALUES (2, '深入理解 Java 虚拟机（第 3 版）', '周志明', '机械工业出版社', '978-7-111-63037-9', '全面讲解\r\n  JVM 的工作原理、内存管理、类加载机制、字节码执行引\r\n  擎等核心内容。', NULL, '计算机/互联网', 4, 2, '2026-04-07 09:36:32');
INSERT INTO `books` (`id`, `title`, `author`, `publisher`, `isbn`, `description`, `cover_url`, `category`, `total_copies`, `available_copies`, `created_at`) VALUES (3, '算法（第 4 版）', 'Robert Sedgewick', '人民邮电出版社', '978-7-115-29380-0', '算法领域的经典教材，全面介绍排序、查找、图、字符串\r\n                                          等常用算法。', NULL, '计算机/互联网', 3, 3, '2026-04-07 09:36:32');
INSERT INTO `books` (`id`, `title`, `author`, `publisher`, `isbn`, `description`, `cover_url`, `category`, `total_copies`, `available_copies`, `created_at`) VALUES (4, '红楼梦', '曹雪芹，高鹗', '人民文学出版社', '978-7-02-000226-4', '中国古代章回体长篇小说，中国\r\n  古典四大名著之一，被誉为中国封建社会的百科全书。', NULL, '文学/小说', 6, 6, '2026-04-07 09:36:32');
INSERT INTO `books` (`id`, `title`, `author`, `publisher`, `isbn`, `description`, `cover_url`, `category`, `total_copies`, `available_copies`, `created_at`) VALUES (5, '活着', '余华', '作家出版社', '978-7-5063-4567-8', '讲述了一个人一生的故事，展现了中国人民在苦难中顽\r\n                                         强生存的精神。', NULL, '文学/小说', 4, 4, '2026-04-07 09:36:32');
INSERT INTO `books` (`id`, `title`, `author`, `publisher`, `isbn`, `description`, `cover_url`, `category`, `total_copies`, `available_copies`, `created_at`) VALUES (6, '百年孤独', '加西亚·马尔克斯', '南海出版公司', '978-7-5442-5399-4', '哥伦比亚作家加西亚·马尔克斯的\r\n  代表作，魔幻现实主义文学的巅峰之作。', NULL, '文学/小说', 3, 3, '2026-04-07 09:36:32');
INSERT INTO `books` (`id`, `title`, `author`, `publisher`, `isbn`, `description`, `cover_url`, `category`, `total_copies`, `available_copies`, `created_at`) VALUES (7, '中国近代史', '蒋廷黻', '武汉大学出版社', '978-7-307-12345-6', '讲述从鸦片战争到抗日战争的中\r\n  国近代历史，分析中国近代化进程的曲折历程。', NULL, '历史/地理', 4, 4, '2026-04-07 09:36:32');
INSERT INTO `books` (`id`, `title`, `author`, `publisher`, `isbn`, `description`, `cover_url`, `category`, `total_copies`, `available_copies`, `created_at`) VALUES (8, '明朝那些事儿（第一部）', '当年明月', '中国海关出版社', '978-7-8016-5678-9', '以轻松幽默\r\n  的语言讲述明朝历史，让历史变得生动有趣。', NULL, '历史/地理', 5, 5, '2026-04-07 09:36:32');
INSERT INTO `books` (`id`, `title`, `author`, `publisher`, `isbn`, `description`, `cover_url`, `category`, `total_copies`, `available_copies`, `created_at`) VALUES (9, '经济学原理（第 8 版）', '曼昆', '北京大学出版社', '978-7-301-23456-7', '经济学入门经典教材，通俗易懂\r\n  地介绍微观经济学和宏观经济学的基本原理。', NULL, '经济/管理', 4, 4, '2026-04-07 09:36:32');
INSERT INTO `books` (`id`, `title`, `author`, `publisher`, `isbn`, `description`, `cover_url`, `category`, `total_copies`, `available_copies`, `created_at`) VALUES (10, '管理学（第 13 版）', '斯蒂芬·罗宾斯', '中国人民大学出版社', '978-7-300-34567-8', '全面介\r\n  绍管理学的基本理论、方法和实践，是管理学经典教材。', NULL, '经济/管理', 3, 3, '2026-04-07 09:36:32');

INSERT INTO `borrow_records` (`id`, `user_id`, `book_id`, `borrow_date`, `due_date`, `return_date`, `status`) VALUES (1, 2, 1, '2026-04-07 09:42:39', '2026-05-07 09:42:39', '2026-04-07 09:47:15', 'returned');
INSERT INTO `borrow_records` (`id`, `user_id`, `book_id`, `borrow_date`, `due_date`, `return_date`, `status`) VALUES (3, 5, 1, '2026-04-07 09:51:47', '2026-05-07 09:51:47', NULL, 'borrowing');
INSERT INTO `borrow_records` (`id`, `user_id`, `book_id`, `borrow_date`, `due_date`, `return_date`, `status`) VALUES (4, 5, 2, '2026-04-07 09:51:49', '2026-05-07 09:51:49', NULL, 'borrowing');
INSERT INTO `borrow_records` (`id`, `user_id`, `book_id`, `borrow_date`, `due_date`, `return_date`, `status`) VALUES (5, 2, 2, '2026-04-07 10:05:23', '2026-05-07 10:05:23', NULL, 'borrowing');
