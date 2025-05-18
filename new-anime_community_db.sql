/*
 Navicat Premium Dump SQL

 Source Server         : anime-community
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : 47.122.118.192:3306
 Source Schema         : anime_community_db

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 18/05/2025 15:54:22
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for cart
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `selected` tinyint NOT NULL DEFAULT 1 COMMENT '是否选中：0-未选中，1-已选中',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_product`(`user_id` ASC, `product_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cart
-- ----------------------------
INSERT INTO `cart` VALUES (12, 9, 1, 1, 0, '2025-05-11 11:10:33', '2025-05-11 11:10:33');

-- ----------------------------
-- Table structure for collection
-- ----------------------------
DROP TABLE IF EXISTS `collection`;
CREATE TABLE `collection`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `type` tinyint NOT NULL COMMENT '类型：1-帖子，2-商品',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_target_type`(`user_id` ASC, `target_id` ASC, `type` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of collection
-- ----------------------------
INSERT INTO `collection` VALUES (1, 4, 2, 1, '2025-05-04 08:57:16');
INSERT INTO `collection` VALUES (2, 4, 3, 1, '2025-05-04 08:57:16');
INSERT INTO `collection` VALUES (3, 4, 8, 1, '2025-05-04 08:57:16');
INSERT INTO `collection` VALUES (5, 5, 4, 1, '2025-05-04 08:57:16');
INSERT INTO `collection` VALUES (6, 5, 6, 1, '2025-05-04 08:57:16');
INSERT INTO `collection` VALUES (7, 6, 3, 1, '2025-05-04 08:57:16');
INSERT INTO `collection` VALUES (8, 6, 9, 1, '2025-05-04 08:57:16');
INSERT INTO `collection` VALUES (9, 6, 10, 1, '2025-05-04 08:57:16');
INSERT INTO `collection` VALUES (10, 4, 2, 2, '2025-05-04 08:57:16');
INSERT INTO `collection` VALUES (11, 4, 5, 2, '2025-05-04 08:57:16');
INSERT INTO `collection` VALUES (12, 4, 7, 2, '2025-05-04 08:57:16');
INSERT INTO `collection` VALUES (13, 5, 1, 2, '2025-05-04 08:57:16');
INSERT INTO `collection` VALUES (14, 5, 3, 2, '2025-05-04 08:57:16');
INSERT INTO `collection` VALUES (15, 5, 8, 2, '2025-05-04 08:57:16');
INSERT INTO `collection` VALUES (16, 6, 4, 2, '2025-05-04 08:57:16');
INSERT INTO `collection` VALUES (17, 6, 6, 2, '2025-05-04 08:57:16');
INSERT INTO `collection` VALUES (18, 6, 10, 2, '2025-05-04 08:57:16');
INSERT INTO `collection` VALUES (19, 9, 4, 2, '2025-05-07 18:10:54');
INSERT INTO `collection` VALUES (21, 9, 11, 2, '2025-05-07 19:41:04');
INSERT INTO `collection` VALUES (22, 9, 11, 1, '2025-05-07 19:53:51');
INSERT INTO `collection` VALUES (23, 9, 1, 1, '2025-05-07 22:42:26');
INSERT INTO `collection` VALUES (25, 6, 11, 1, '2025-05-08 13:25:55');
INSERT INTO `collection` VALUES (27, 9, 4, 1, '2025-05-08 22:38:39');
INSERT INTO `collection` VALUES (28, 4, 11, 1, '2025-05-09 21:34:24');
INSERT INTO `collection` VALUES (29, 9, 12, 1, '2025-05-12 01:55:20');

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父评论ID，0表示一级评论',
  `reply_user_id` bigint NULL DEFAULT NULL COMMENT '回复用户ID',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-删除，1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (1, 5, 1, 0, NULL, '这个路飞手办真不错，做工很精细！', 15, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `comment` VALUES (2, 6, 1, 0, NULL, '请问在哪里买的？价格是多少？', 8, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `comment` VALUES (3, 4, 1, 2, 6, '在XX店买的，299元，性价比很高', 5, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `comment` VALUES (4, 6, 2, 0, NULL, '感谢分享，对我很有帮助', 11, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `comment` VALUES (5, 4, 3, 0, NULL, '画得真好，很有原作的感觉', 20, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `comment` VALUES (6, 5, 3, 0, NULL, '太厉害了，请问用的什么工具？', 12, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `comment` VALUES (7, 6, 3, 6, 5, '用的是XX牌数位板，谢谢喜欢', 7, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `comment` VALUES (8, 5, 4, 0, NULL, '最终季真的很震撼，剧情转折很惊人', 18, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `comment` VALUES (9, 4, 5, 0, NULL, '推荐XX店的柯南手表，做工很精细', 9, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `comment` VALUES (10, 6, 6, 0, NULL, '收藏很全面，羡慕啊！', 14, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `comment` VALUES (11, 9, 1, 1, NULL, '我也认同', 0, 0, '2025-05-08 11:43:49', '2025-05-08 11:43:49');
INSERT INTO `comment` VALUES (12, 9, 1, 0, NULL, '真不错', 0, 0, '2025-05-08 11:44:05', '2025-05-08 11:44:05');
INSERT INTO `comment` VALUES (13, 9, 1, 1, NULL, '的', 0, 0, '2025-05-08 11:44:24', '2025-05-08 11:44:24');
INSERT INTO `comment` VALUES (14, 9, 1, 0, NULL, '等我', 0, 0, '2025-05-08 11:44:38', '2025-05-08 11:44:38');
INSERT INTO `comment` VALUES (15, 9, 1, 1, NULL, '21312321', 0, 0, '2025-05-08 12:09:18', '2025-05-08 12:09:18');
INSERT INTO `comment` VALUES (16, 9, 1, 1, 5, '3', 2, 1, '2025-05-08 12:16:13', '2025-05-08 12:16:13');
INSERT INTO `comment` VALUES (17, 9, 1, 16, 9, '34423', 0, 1, '2025-05-08 12:40:05', '2025-05-08 12:40:05');
INSERT INTO `comment` VALUES (18, 9, 1, 1, 9, 'dawdw', 0, 0, '2025-05-08 12:46:53', '2025-05-08 12:46:53');
INSERT INTO `comment` VALUES (19, 9, 1, 1, 9, 'adwwd', 0, 0, '2025-05-08 12:47:00', '2025-05-08 12:47:00');
INSERT INTO `comment` VALUES (20, 9, 1, 1, 9, '3432', 0, 0, '2025-05-08 13:11:48', '2025-05-08 13:11:48');
INSERT INTO `comment` VALUES (21, 4, 1, 1, 9, 'huhuhu', 0, 1, '2025-05-08 13:23:24', '2025-05-08 13:23:24');
INSERT INTO `comment` VALUES (22, 6, 11, 0, NULL, '可以', 0, 1, '2025-05-08 13:26:07', '2025-05-08 13:26:07');
INSERT INTO `comment` VALUES (23, 6, 1, 1, 9, '等我', 0, 1, '2025-05-08 13:26:40', '2025-05-08 13:26:40');
INSERT INTO `comment` VALUES (24, 4, 11, 0, NULL, 'dwadwa', 0, 1, '2025-05-09 21:34:31', '2025-05-09 21:34:31');
INSERT INTO `comment` VALUES (25, 4, 11, 22, 6, 'dawdwa ', 0, 1, '2025-05-09 21:34:38', '2025-05-09 21:34:38');

-- ----------------------------
-- Table structure for community_category
-- ----------------------------
DROP TABLE IF EXISTS `community_category`;
CREATE TABLE `community_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类描述',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类图标',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '社区分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of community_category
-- ----------------------------
INSERT INTO `community_category` VALUES (1, '作品分享', '分享你的动漫周边收藏', NULL, 1, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `community_category` VALUES (2, '购物心得', '分享购物体验和产品评价', NULL, 2, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `community_category` VALUES (3, '同人创作', '分享你的同人作品', NULL, 3, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `community_category` VALUES (4, '活动讨论', '讨论各种动漫相关活动', NULL, 4, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `community_category` VALUES (5, '求助问答', '寻求帮助和解答问题', NULL, 5, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');

-- ----------------------------
-- Table structure for group_buying
-- ----------------------------
DROP TABLE IF EXISTS `group_buying`;
CREATE TABLE `group_buying`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '拼团ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `group_price` decimal(10, 2) NOT NULL COMMENT '拼团价格',
  `min_group_size` int NOT NULL DEFAULT 2 COMMENT '最小成团人数',
  `max_group_size` int NOT NULL COMMENT '最大成团人数',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-已结束，1-进行中，2-未开始',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拼团活动表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of group_buying
-- ----------------------------
INSERT INTO `group_buying` VALUES (1, 11, 24.00, 2, 7, '2025-05-06 21:32:28', '2025-05-07 00:00:00', 0, '2025-05-06 19:32:59', '2025-05-06 19:33:14');
INSERT INTO `group_buying` VALUES (2, 4, 103.20, 2, 5, '2025-05-09 00:00:00', '2025-05-10 00:00:00', 0, '2025-05-08 23:44:09', '2025-05-08 23:44:09');
INSERT INTO `group_buying` VALUES (3, 1, 239.20, 2, 5, '2025-05-09 17:14:10', '2025-05-12 00:00:00', 0, '2025-05-09 17:13:34', '2025-05-09 17:13:34');
INSERT INTO `group_buying` VALUES (4, 1, 239.20, 2, 5, '2025-05-13 20:54:00', '2025-05-14 00:00:00', 0, '2025-05-13 20:53:23', '2025-05-13 20:53:23');

-- ----------------------------
-- Table structure for group_order
-- ----------------------------
DROP TABLE IF EXISTS `group_order`;
CREATE TABLE `group_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '拼团订单ID',
  `group_buying_id` bigint NOT NULL COMMENT '拼团活动ID',
  `leader_user_id` bigint NOT NULL COMMENT '团长用户ID',
  `current_size` int NOT NULL DEFAULT 1 COMMENT '当前人数',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-组团中，1-已成团，2-已解散',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_group_buying_id`(`group_buying_id` ASC) USING BTREE,
  INDEX `idx_leader_user_id`(`leader_user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拼团订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of group_order
-- ----------------------------
INSERT INTO `group_order` VALUES (5, 2, 4, 2, 1, '2025-05-10 16:50:35', '2025-05-09 16:50:35', '2025-05-09 16:50:35');
INSERT INTO `group_order` VALUES (6, 2, 4, 2, 1, '2025-05-10 17:00:09', '2025-05-09 17:00:09', '2025-05-09 17:00:09');
INSERT INTO `group_order` VALUES (7, 2, 9, 2, 1, '2025-05-10 17:09:20', '2025-05-09 17:09:20', '2025-05-09 17:09:20');
INSERT INTO `group_order` VALUES (8, 3, 9, 2, 1, '2025-05-10 17:15:20', '2025-05-09 17:15:20', '2025-05-09 17:15:20');
INSERT INTO `group_order` VALUES (9, 4, 9, 1, 2, '2025-05-14 20:54:37', '2025-05-13 20:54:37', '2025-05-13 20:54:37');
INSERT INTO `group_order` VALUES (10, 4, 9, 2, 1, '2025-05-14 20:55:39', '2025-05-13 20:55:39', '2025-05-13 20:55:39');

-- ----------------------------
-- Table structure for ip
-- ----------------------------
DROP TABLE IF EXISTS `ip`;
CREATE TABLE `ip`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'IP ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'IP名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'IP描述',
  `logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP LOGO URL',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'IP表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ip
-- ----------------------------
INSERT INTO `ip` VALUES (1, '海贼王', '日本漫画家尾田荣一郎作品', '/upload/ip/2025/05/07/9e4eed1019424dbbbcf6c3b0021c0866.jpg', 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `ip` VALUES (2, '火影忍者', '日本漫画家岸本齐史作品', '/upload/ip/2025/05/07/479c8d931717485a9e2678025c00bc7e.jpg', 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `ip` VALUES (3, '鬼灭之刃', '日本漫画家吾峠呼世晴作品', '/upload/ip/2025/05/07/28f04516cdbd4e5880736dcb948ea9db.jpeg', 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `ip` VALUES (4, '进击的巨人', '日本漫画家谏山创作品', '/upload/ip/2025/05/07/c434509696194f91a6d65ca7dab32703.jpeg', 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `ip` VALUES (5, '名侦探柯南', '日本漫画家青山刚昌作品', '/upload/ip/2025/05/07/fe05afae4e5e4f37bba24f6ceea1b0e6.jpeg', 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `ip` VALUES (6, '龙珠', '日本漫画家鸟山明作品', '/upload/ip/2025/05/07/079a58af0c674b4c898fc46c6e53da03.jpg', 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `ip` VALUES (7, '哆啦A梦', '日本漫画家藤子·F·不二雄作品', '/upload/ip/2025/05/07/27e1f01e27024dcc8c2d82056884a82f.jpg', 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `ip` VALUES (8, '一拳超人', '日本漫画家ONE原作、村田雄介作画的漫画', '/upload/ip/2025/05/07/a3ac2e8feb244582853a1df5eb59aa00.jpg', 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `ip` VALUES (9, '刀剑神域', '日本轻小说家川原砾作品', '/upload/ip/2025/05/07/864b57987d214ece8d0a2106229a615f.jpeg', 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `ip` VALUES (10, '我的英雄学院', '日本漫画家堀越耕平作品', '/upload/ip/2025/05/07/32d54a0f84844795a26b64a54c322324.jpeg', 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');

-- ----------------------------
-- Table structure for like
-- ----------------------------
DROP TABLE IF EXISTS `like`;
CREATE TABLE `like`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `type` tinyint NOT NULL COMMENT '类型：1-帖子，2-评论',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_target_type`(`user_id` ASC, `target_id` ASC, `type` ASC) USING BTREE,
  INDEX `idx_target_id_type`(`target_id` ASC, `type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '点赞表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of like
-- ----------------------------
INSERT INTO `like` VALUES (1, 4, 2, 1, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (2, 4, 3, 1, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (3, 4, 5, 1, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (4, 4, 8, 1, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (5, 4, 9, 1, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (6, 5, 1, 1, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (7, 5, 3, 1, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (8, 5, 4, 1, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (9, 5, 6, 1, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (10, 5, 10, 1, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (11, 6, 1, 1, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (12, 6, 2, 1, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (13, 6, 4, 1, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (14, 6, 7, 1, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (15, 6, 8, 1, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (16, 4, 1, 2, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (17, 4, 5, 2, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (18, 4, 8, 2, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (19, 5, 2, 2, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (20, 5, 4, 2, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (21, 5, 9, 2, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (22, 6, 3, 2, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (23, 6, 6, 2, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (24, 6, 10, 2, '2025-05-04 08:57:16');
INSERT INTO `like` VALUES (25, 9, 4, 2, '2025-05-07 18:10:57');
INSERT INTO `like` VALUES (26, 9, 11, 1, '2025-05-07 19:53:50');
INSERT INTO `like` VALUES (27, 9, 1, 1, '2025-05-07 22:42:25');
INSERT INTO `like` VALUES (29, 4, 16, 2, '2025-05-08 13:23:16');
INSERT INTO `like` VALUES (30, 6, 11, 1, '2025-05-08 13:25:49');
INSERT INTO `like` VALUES (31, 6, 16, 2, '2025-05-08 13:26:36');
INSERT INTO `like` VALUES (32, 4, 11, 1, '2025-05-09 21:34:27');

-- ----------------------------
-- Table structure for merchant_certification
-- ----------------------------
DROP TABLE IF EXISTS `merchant_certification`;
CREATE TABLE `merchant_certification`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '认证ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `company_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公司名称',
  `business_license` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '营业执照URL',
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系人电话',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人邮箱',
  `certification_status` tinyint NOT NULL DEFAULT 0 COMMENT '认证状态：0-审核中，1-已认证，2-未通过',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商家认证表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of merchant_certification
-- ----------------------------
INSERT INTO `merchant_certification` VALUES (1, 2, '动漫周边专卖店', 'https://example.com/license1.jpg', '张三', '13800000001', 'merchant1@example.com', 1, NULL, '2025-05-04 08:57:16', '2025-05-04 09:26:17');
INSERT INTO `merchant_certification` VALUES (2, 3, '二次元精品店', 'https://example.com/license2.jpg', '李四', '13800000002', 'merchant2@example.com', 1, NULL, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `merchant_certification` VALUES (3, 8, '对哇对哇', '/upload/post/2025/05/05/b20c3e7f71bc4424b9c3a8902160a773.jpg', '我的', '15139222222', '2@2.com', 1, NULL, '2025-05-05 10:24:43', '2025-05-05 11:06:24');
INSERT INTO `merchant_certification` VALUES (4, 10, '5556', '/upload/post/2025/05/11/4c935a29e1eb44c29c0d047b4521400e.png', '45454777\\', '15336312208', '22@qq.com', 2, '111', '2025-05-11 22:12:56', '2025-05-11 22:12:56');
INSERT INTO `merchant_certification` VALUES (11, 12, 'dddd', '/upload/post/2025/05/17/44120d51f4064dfa9b0c0648b6976aa3.jpg', 'ffvfv', '15336312208', '444@qq.com', 1, '11', '2025-05-17 21:10:24', '2025-05-17 21:10:24');

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `from_user_id` bigint NOT NULL COMMENT '发送者ID',
  `to_user_id` bigint NOT NULL COMMENT '接收者ID',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_from_to_user`(`from_user_id` ASC, `to_user_id` ASC) USING BTREE,
  INDEX `idx_to_user_id`(`to_user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '私信表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of message
-- ----------------------------
INSERT INTO `message` VALUES (2, 9, 5, '123', 1, '2025-05-08 14:29:50');
INSERT INTO `message` VALUES (3, 9, 6, '大王大王大王', 0, '2025-05-08 15:36:36');
INSERT INTO `message` VALUES (5, 9, 6, '带娃', 0, '2025-05-08 17:06:46');
INSERT INTO `message` VALUES (7, 9, 3, '阿瓦达', 1, '2025-05-08 17:07:14');
INSERT INTO `message` VALUES (8, 3, 9, '你好', 1, '2025-05-08 17:31:44');
INSERT INTO `message` VALUES (9, 3, 9, '213', 1, '2025-05-08 17:53:40');
INSERT INTO `message` VALUES (10, 9, 3, '哈哈哈', 1, '2025-05-08 20:18:22');
INSERT INTO `message` VALUES (11, 3, 9, '带娃', 1, '2025-05-08 20:21:04');
INSERT INTO `message` VALUES (12, 9, 3, '的', 1, '2025-05-08 20:21:27');
INSERT INTO `message` VALUES (13, 3, 9, '懂得都懂', 1, '2025-05-08 20:23:49');
INSERT INTO `message` VALUES (14, 3, 9, '你好', 1, '2025-05-08 20:39:44');
INSERT INTO `message` VALUES (15, 9, 3, '低洼地', 1, '2025-05-08 20:39:51');
INSERT INTO `message` VALUES (16, 3, 9, '123', 1, '2025-05-08 21:05:26');
INSERT INTO `message` VALUES (17, 9, 3, '123', 1, '2025-05-08 21:05:32');
INSERT INTO `message` VALUES (18, 9, 3, '说话\n', 1, '2025-05-08 21:05:44');
INSERT INTO `message` VALUES (19, 3, 9, '低洼地', 1, '2025-05-08 21:06:06');
INSERT INTO `message` VALUES (20, 9, 3, '对哇对哇', 1, '2025-05-08 21:12:02');
INSERT INTO `message` VALUES (21, 9, 8, '達瓦達瓦達瓦達瓦', 1, '2025-05-09 21:33:14');
INSERT INTO `message` VALUES (22, 8, 9, '達瓦達瓦達瓦', 1, '2025-05-09 21:33:27');
INSERT INTO `message` VALUES (23, 9, 8, '達瓦達瓦達瓦', 1, '2025-05-09 21:33:32');
INSERT INTO `message` VALUES (24, 4, 9, '123', 1, '2025-05-16 22:11:48');

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` tinyint NOT NULL COMMENT '类型：1-点赞，2-评论，3-回复，4-系统通知',
  `target_id` bigint NULL DEFAULT NULL COMMENT '目标ID',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知内容',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notification
-- ----------------------------
INSERT INTO `notification` VALUES (1, 6, 1, 25, '用户 测试 点赞了你的评论', 0, '2025-05-07 18:10:57');
INSERT INTO `notification` VALUES (2, 4, 1, 27, '用户 测试 点赞了你的帖子', 1, '2025-05-07 22:42:25');
INSERT INTO `notification` VALUES (3, 5, 1, 28, '用户 测试 点赞了你的帖子', 1, '2025-05-08 11:43:03');
INSERT INTO `notification` VALUES (4, 5, 3, 11, '用户 测试 回复了你的评论', 0, '2025-05-08 11:43:49');
INSERT INTO `notification` VALUES (5, 4, 2, 12, '用户 测试 评论了你的帖子', 1, '2025-05-08 11:44:05');
INSERT INTO `notification` VALUES (6, 5, 3, 13, '用户 测试 回复了你的评论', 0, '2025-05-08 11:44:24');
INSERT INTO `notification` VALUES (7, 4, 2, 14, '用户 测试 评论了你的帖子', 1, '2025-05-08 11:44:38');
INSERT INTO `notification` VALUES (8, 5, 3, 15, '用户 测试 回复了你的评论', 0, '2025-05-08 12:09:18');
INSERT INTO `notification` VALUES (9, 5, 3, 16, '用户 测试 回复了你的评论', 0, '2025-05-08 12:16:13');
INSERT INTO `notification` VALUES (10, 5, 3, 18, '用户 测试 回复了你的评论', 0, '2025-05-08 12:46:53');
INSERT INTO `notification` VALUES (11, 5, 3, 19, '用户 测试 回复了你的评论', 0, '2025-05-08 12:47:00');
INSERT INTO `notification` VALUES (12, 5, 3, 20, '用户 测试 回复了你的评论', 0, '2025-05-08 13:11:48');
INSERT INTO `notification` VALUES (13, 9, 1, 29, '用户 动漫迷小王 点赞了你的评论', 1, '2025-05-08 13:23:16');
INSERT INTO `notification` VALUES (14, 5, 3, 21, '用户 动漫迷小王 回复了你的评论', 1, '2025-05-08 13:23:24');
INSERT INTO `notification` VALUES (15, 9, 1, 30, '用户 漫画控 点赞了你的帖子', 1, '2025-05-08 13:25:49');
INSERT INTO `notification` VALUES (16, 9, 2, 22, '用户 漫画控 评论了你的帖子', 1, '2025-05-08 13:26:07');
INSERT INTO `notification` VALUES (17, 9, 1, 31, '用户 漫画控 点赞了你的评论', 1, '2025-05-08 13:26:36');
INSERT INTO `notification` VALUES (18, 5, 3, 23, '用户 漫画控 回复了你的评论', 1, '2025-05-08 13:26:41');
INSERT INTO `notification` VALUES (19, 9, 1, 32, '用户 动漫迷小王 点赞了你的帖子', 1, '2025-05-09 21:34:27');
INSERT INTO `notification` VALUES (20, 9, 2, 24, '用户 动漫迷小王 评论了你的帖子', 1, '2025-05-09 21:34:32');
INSERT INTO `notification` VALUES (21, 6, 3, 25, '用户 动漫迷小王 回复了你的评论', 0, '2025-05-09 21:34:39');

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `seller_id` bigint NOT NULL COMMENT '卖家ID',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
  `pay_amount` decimal(10, 2) NOT NULL COMMENT '实付金额',
  `pay_type` tinyint NULL DEFAULT NULL COMMENT '支付方式：1-支付宝',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态：0-待付款，1-待发货，2-待收货，3-已完成，4-已取消，5-申请退款，6-已退款',
  `shipping_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货人姓名',
  `shipping_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货人电话',
  `shipping_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货地址',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `shipping_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime NULL DEFAULT NULL COMMENT '收货时间',
  `close_time` datetime NULL DEFAULT NULL COMMENT '关闭时间',
  `group_order_id` bigint NULL DEFAULT NULL COMMENT '拼团订单ID，非拼团为null',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_seller_id`(`seller_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_group_order_id`(`group_order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order
-- ----------------------------
INSERT INTO `order` VALUES (1, 'ORD20230501001', 4, 2, 299.00, 299.00, 1, 3, '王小明', '13900000001', '北京市海淀区XX街XX号', '2025-05-01 10:30:00', '2025-05-02 14:20:00', '2025-05-05 18:10:00', NULL, NULL, '2025-05-04 08:57:16', '2025-05-04 09:01:46');
INSERT INTO `order` VALUES (2, 'ORD20230502001', 5, 2, 99.00, 99.00, 1, 3, '李小红', '13900000002', '上海市浦东新区XX路XX号', '2025-05-02 11:25:00', '2025-05-03 09:15:00', '2025-05-06 16:40:00', NULL, NULL, '2025-05-04 08:57:16', '2025-05-04 09:01:52');
INSERT INTO `order` VALUES (3, 'ORD20230503001', 6, 2, 359.00, 359.00, 1, 3, '张小华', '13900000003', '广州市天河区XX大道XX号', '2025-05-03 14:50:00', '2025-05-04 11:30:00', '2025-05-07 10:20:00', NULL, NULL, '2025-05-04 08:57:16', '2025-05-04 09:01:56');
INSERT INTO `order` VALUES (4, 'ORD20230504001', 4, 3, 129.00, 129.00, 1, 3, '王小明', '13900000001', '北京市海淀区XX街XX号', '2025-05-04 16:40:00', '2025-05-05 10:10:00', '2025-05-08 14:30:00', NULL, NULL, '2025-05-04 08:57:16', '2025-05-04 09:02:00');
INSERT INTO `order` VALUES (5, 'ORD20230505001', 5, 3, 199.00, 199.00, 1, 3, '李小红', '13900000002', '上海市浦东新区XX路XX号', '2025-05-05 09:20:00', '2025-05-06 15:40:00', '2025-05-09 11:50:00', NULL, NULL, '2025-05-04 08:57:16', '2025-05-04 09:02:05');
INSERT INTO `order` VALUES (6, 'ORD20230506001', 6, 3, 89.00, 89.00, 1, 2, '张小华', '13900000003', '广州市天河区XX大道XX号', '2025-05-06 13:10:00', '2025-05-07 09:30:00', NULL, NULL, NULL, '2025-05-04 08:57:16', '2025-05-04 09:01:41');
INSERT INTO `order` VALUES (7, 'ORD20230507001', 4, 2, 59.00, 59.00, 1, 3, '王小明', '13900000001', '北京市海淀区XX街XX号', '2025-05-07 15:30:00', '2025-05-13 20:47:22', '2025-05-14 00:24:42', NULL, NULL, '2025-05-04 08:57:16', '2025-05-04 09:01:11');
INSERT INTO `order` VALUES (8, 'ORD20230508001', 5, 2, 79.00, 79.00, 1, 0, '李小红', '13900000002', '上海市浦东新区XX路XX号', NULL, NULL, NULL, NULL, NULL, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `order` VALUES (9, 'ORD20230509001', 6, 3, 29.00, 29.00, 1, 0, '张小华', '13900000003', '广州市天河区XX大道XX号', NULL, NULL, NULL, NULL, NULL, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `order` VALUES (10, 'ORD20230510001', 4, 3, 39.00, 39.00, 1, 0, '王小明', '13900000001', '北京市海淀区XX街XX号', NULL, NULL, NULL, NULL, NULL, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `order` VALUES (11, '202505091154270b005a', 9, 2, 99.00, 99.00, 1, 4, '我的', '15136363636', '北京市 北京市 东城区 123123', NULL, NULL, NULL, '2025-05-09 12:36:08', NULL, '2025-05-09 11:54:27', '2025-05-09 11:54:27');
INSERT INTO `order` VALUES (12, '202505091200582dedd2', 9, 2, 99.00, 99.00, NULL, 4, '我的', '15136363636', '北京市 北京市 东城区 123123', NULL, NULL, NULL, '2025-05-09 12:17:06', NULL, '2025-05-09 12:00:59', '2025-05-09 12:00:59');
INSERT INTO `order` VALUES (14, '20250509124404c1f8f2', 9, 3, 359.00, 359.00, 1, 4, '我的', '15136363636', '北京市 北京市 东城区 123123', NULL, NULL, NULL, '2025-05-09 12:49:03', NULL, '2025-05-09 12:44:04', '2025-05-09 12:44:04');
INSERT INTO `order` VALUES (15, '20250509124913455b36', 9, 3, 359.00, 359.00, 1, 4, '我的', '15136363636', '北京市 北京市 东城区 123123', NULL, NULL, NULL, '2025-05-09 13:07:53', NULL, '2025-05-09 12:49:14', '2025-05-09 12:49:14');
INSERT INTO `order` VALUES (16, '20250509125944506914', 9, 2, 359.00, 359.00, 1, 4, '我的', '15136363636', '北京市 北京市 东城区 123123', NULL, NULL, NULL, '2025-05-09 13:07:50', NULL, '2025-05-09 12:59:45', '2025-05-09 12:59:45');
INSERT INTO `order` VALUES (17, '202505091305198b144f', 9, 2, 359.00, 359.00, 1, 4, '我的', '15136363636', '北京市 北京市 东城区 123123', NULL, NULL, NULL, '2025-05-09 13:15:58', NULL, '2025-05-09 13:05:20', '2025-05-09 13:05:20');
INSERT INTO `order` VALUES (18, '202505091316230bbbb3', 9, 3, 129.00, 129.00, 1, 6, '无法', '15136363636', '北京市 北京市 西城区 12312', '2025-05-09 13:16:54', NULL, NULL, NULL, NULL, '2025-05-09 13:16:24', '2025-05-09 13:16:24');
INSERT INTO `order` VALUES (19, '20250509133714016ad9', 9, 2, 359.00, 359.00, 1, 3, '无法', '15136363636', '北京市 北京市 西城区 12312', '2025-05-09 13:38:05', '2025-05-09 14:39:36', '2025-05-09 15:33:53', NULL, NULL, '2025-05-09 13:37:15', '2025-05-09 13:37:15');
INSERT INTO `order` VALUES (20, '202505091419276287a9', 9, 3, 129.00, 129.00, 1, 6, '无法', '15136363636', '北京市 北京市 西城区 12312', '2025-05-09 14:20:01', NULL, NULL, NULL, NULL, '2025-05-09 14:19:27', '2025-05-09 14:19:27');
INSERT INTO `order` VALUES (21, '20250509165616507246', 4, 3, 103.20, 103.20, 1, 1, NULL, NULL, NULL, '2025-05-09 16:56:56', NULL, NULL, NULL, 5, '2025-05-09 16:56:17', '2025-05-09 16:56:17');
INSERT INTO `order` VALUES (22, '20250509170056006747', 5, 3, 103.20, 103.20, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 6, '2025-05-09 17:00:57', '2025-05-09 17:00:57');
INSERT INTO `order` VALUES (23, '20250509170948324335', 4, 3, 103.20, 103.20, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 7, '2025-05-09 17:09:49', '2025-05-09 17:09:49');
INSERT INTO `order` VALUES (24, '20250509171519189911', 9, 2, 239.20, 239.20, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 8, '2025-05-09 17:15:20', '2025-05-09 17:15:20');
INSERT INTO `order` VALUES (25, '20250509171552817711', 4, 2, 239.20, 239.20, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 8, '2025-05-09 17:15:52', '2025-05-09 17:15:52');
INSERT INTO `order` VALUES (27, '20250513205437856409', 9, 2, 239.20, 239.20, NULL, 4, NULL, NULL, NULL, NULL, NULL, NULL, '2025-05-14 20:55:00', 9, '2025-05-13 20:54:37', '2025-05-13 20:54:37');
INSERT INTO `order` VALUES (28, '20250513205538315263', 9, 2, 239.20, 239.20, 1, 1, NULL, NULL, NULL, '2025-05-13 21:04:14', NULL, NULL, NULL, 10, '2025-05-13 20:55:39', '2025-05-13 20:55:39');
INSERT INTO `order` VALUES (29, '20250513205713130171', 5, 2, 239.20, 239.20, 1, 1, NULL, NULL, NULL, '2025-05-13 20:58:45', NULL, NULL, NULL, 10, '2025-05-13 20:57:13', '2025-05-13 20:57:13');
INSERT INTO `order` VALUES (30, '2025051400305806c024', 11, 2, 359.00, 359.00, 1, 1, '123', '15136363693', '上海市 上海市 黄浦区 213', '2025-05-14 00:31:34', NULL, NULL, NULL, NULL, '2025-05-14 00:30:58', '2025-05-14 00:30:58');
INSERT INTO `order` VALUES (31, '202505162212084e1b76', 4, 9, 0.01, 0.01, 1, 3, '213', '15136363636', '北京市 北京市 东城区 21312312', '2025-05-16 22:12:59', '2025-05-16 22:13:43', '2025-05-16 22:14:20', NULL, NULL, '2025-05-16 22:12:09', '2025-05-16 22:12:09');
INSERT INTO `order` VALUES (32, '20250517210059cf7900', 4, 9, 10.00, 10.00, 1, 3, '213', '15136363636', '北京市 北京市 东城区 21312312', '2025-05-17 21:01:40', '2025-05-17 21:15:56', '2025-05-17 21:16:39', NULL, NULL, '2025-05-17 21:01:00', '2025-05-17 21:01:00');
INSERT INTO `order` VALUES (33, '20250517210524e9d9f8', 9, 9, 10.00, 10.00, NULL, 4, '213', '15136363636', '北京市 北京市 东城区 21312312', NULL, NULL, NULL, '2025-05-17 21:05:37', NULL, '2025-05-17 21:05:25', '2025-05-17 21:05:25');
INSERT INTO `order` VALUES (34, '202505172108010dfda4', 9, 8, 30.00, 30.00, 1, 3, '213', '15136363636', '北京市 北京市 东城区 21312312', '2025-05-17 21:08:39', '2025-05-17 21:09:08', '2025-05-17 21:09:24', NULL, NULL, '2025-05-17 21:08:01', '2025-05-17 21:08:01');
INSERT INTO `order` VALUES (35, '20250517212144218829', 4, 9, 10.00, 10.00, 1, 3, '11', '15339318888', '上海市 上海市 黄浦区 111', '2025-05-17 21:22:28', '2025-05-17 21:22:44', '2025-05-17 21:23:58', NULL, NULL, '2025-05-17 21:21:44', '2025-05-17 21:21:44');

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `product_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品图片',
  `price` decimal(10, 2) NOT NULL COMMENT '商品单价',
  `quantity` int NOT NULL COMMENT '购买数量',
  `total_price` decimal(10, 2) NOT NULL COMMENT '总价',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单项表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_item
-- ----------------------------
INSERT INTO `order_item` VALUES (1, 1, 1, '路飞手办', 'https://example.com/product1.jpg', 299.00, 1, 299.00, '2025-05-04 08:57:16');
INSERT INTO `order_item` VALUES (2, 2, 2, 'Q版鸣人', 'https://example.com/product2.jpg', 99.00, 1, 99.00, '2025-05-04 08:57:16');
INSERT INTO `order_item` VALUES (3, 3, 3, '灶门炭治郎手办', 'https://example.com/product3.jpg', 359.00, 1, 359.00, '2025-05-04 08:57:16');
INSERT INTO `order_item` VALUES (4, 4, 4, '进击的巨人T恤', 'https://example.com/product4.jpg', 129.00, 1, 129.00, '2025-05-04 08:57:16');
INSERT INTO `order_item` VALUES (5, 5, 5, '柯南卫衣', 'https://example.com/product5.jpg', 199.00, 1, 199.00, '2025-05-04 08:57:16');
INSERT INTO `order_item` VALUES (6, 6, 6, '龙珠抱枕', 'https://example.com/product6.jpg', 89.00, 1, 89.00, '2025-05-04 08:57:16');
INSERT INTO `order_item` VALUES (7, 7, 7, '哆啦A梦杯子', 'https://example.com/product7.jpg', 59.00, 1, 59.00, '2025-05-04 08:57:16');
INSERT INTO `order_item` VALUES (8, 8, 8, '一拳超人挂画', 'https://example.com/product8.jpg', 79.00, 1, 79.00, '2025-05-04 08:57:16');
INSERT INTO `order_item` VALUES (9, 9, 9, '刀剑神域文件夹', 'https://example.com/product9.jpg', 29.00, 1, 29.00, '2025-05-04 08:57:16');
INSERT INTO `order_item` VALUES (10, 10, 10, '英雄学院笔记本', 'https://example.com/product10.jpg', 39.00, 1, 39.00, '2025-05-04 08:57:16');
INSERT INTO `order_item` VALUES (11, 11, 2, 'Q版鸣人', '/upload/product/2025/05/07/4eff07416ef14774b7cebc27e77833b7.jpg', 99.00, 1, 99.00, '2025-05-09 11:54:27');
INSERT INTO `order_item` VALUES (12, 12, 2, 'Q版鸣人', '/upload/product/2025/05/07/4eff07416ef14774b7cebc27e77833b7.jpg', 99.00, 1, 99.00, '2025-05-09 12:00:59');
INSERT INTO `order_item` VALUES (13, 13, 4, '进击的巨人T恤', '/upload/product/2025/05/07/843f1ab7e06442329a20748bdd8788fa.jpg', 129.00, 1, 129.00, '2025-05-09 12:36:46');
INSERT INTO `order_item` VALUES (14, 14, 3, '灶门炭治郎手办', '/upload/product/2025/05/07/e52595f758ee4cb4bca249c2bf40f587.jpeg', 359.00, 1, 359.00, '2025-05-09 12:44:04');
INSERT INTO `order_item` VALUES (15, 15, 3, '灶门炭治郎手办', '/upload/product/2025/05/07/e52595f758ee4cb4bca249c2bf40f587.jpeg', 359.00, 1, 359.00, '2025-05-09 12:49:14');
INSERT INTO `order_item` VALUES (16, 16, 3, '灶门炭治郎手办', '/upload/product/2025/05/07/e52595f758ee4cb4bca249c2bf40f587.jpeg', 359.00, 1, 359.00, '2025-05-09 12:59:45');
INSERT INTO `order_item` VALUES (17, 17, 3, '灶门炭治郎手办', '/upload/product/2025/05/07/e52595f758ee4cb4bca249c2bf40f587.jpeg', 359.00, 1, 359.00, '2025-05-09 13:05:20');
INSERT INTO `order_item` VALUES (18, 18, 4, '进击的巨人T恤', '/upload/product/2025/05/07/843f1ab7e06442329a20748bdd8788fa.jpg', 129.00, 1, 129.00, '2025-05-09 13:16:24');
INSERT INTO `order_item` VALUES (19, 19, 3, '灶门炭治郎手办', '/upload/product/2025/05/07/e52595f758ee4cb4bca249c2bf40f587.jpeg', 359.00, 1, 359.00, '2025-05-09 13:37:15');
INSERT INTO `order_item` VALUES (20, 20, 4, '进击的巨人T恤', '/upload/product/2025/05/07/843f1ab7e06442329a20748bdd8788fa.jpg', 129.00, 1, 129.00, '2025-05-09 14:19:27');
INSERT INTO `order_item` VALUES (21, 21, 4, '进击的巨人T恤', '/upload/product/2025/05/07/843f1ab7e06442329a20748bdd8788fa.jpg', 103.20, 1, 103.20, '2025-05-09 16:56:17');
INSERT INTO `order_item` VALUES (22, 22, 4, '进击的巨人T恤', '/upload/product/2025/05/07/843f1ab7e06442329a20748bdd8788fa.jpg', 103.20, 1, 103.20, '2025-05-09 17:00:57');
INSERT INTO `order_item` VALUES (23, 23, 4, '进击的巨人T恤', '/upload/product/2025/05/07/843f1ab7e06442329a20748bdd8788fa.jpg', 103.20, 1, 103.20, '2025-05-09 17:09:49');
INSERT INTO `order_item` VALUES (24, 24, 1, '路飞手办', '/upload/product/2025/05/07/98fa00b688fc4f2785293d9f879d62e7.jpg', 239.20, 1, 239.20, '2025-05-09 17:15:20');
INSERT INTO `order_item` VALUES (25, 25, 1, '路飞手办', '/upload/product/2025/05/07/98fa00b688fc4f2785293d9f879d62e7.jpg', 239.20, 1, 239.20, '2025-05-09 17:15:52');
INSERT INTO `order_item` VALUES (26, 26, 7, '哆啦A梦杯子', '/upload/product/2025/05/07/184ecb04e36b49249f4dc72ac41034a7.jpg', 59.00, 1, 59.00, '2025-05-13 20:42:56');
INSERT INTO `order_item` VALUES (27, 27, 1, '路飞手办', '/upload/product/2025/05/07/98fa00b688fc4f2785293d9f879d62e7.jpg', 239.20, 1, 239.20, '2025-05-13 20:54:37');
INSERT INTO `order_item` VALUES (28, 28, 1, '路飞手办', '/upload/product/2025/05/07/98fa00b688fc4f2785293d9f879d62e7.jpg', 239.20, 1, 239.20, '2025-05-13 20:55:39');
INSERT INTO `order_item` VALUES (29, 29, 1, '路飞手办', '/upload/product/2025/05/07/98fa00b688fc4f2785293d9f879d62e7.jpg', 239.20, 1, 239.20, '2025-05-13 20:57:13');
INSERT INTO `order_item` VALUES (30, 30, 3, '灶门炭治郎手办', '/upload/product/2025/05/07/e52595f758ee4cb4bca249c2bf40f587.jpeg', 359.00, 1, 359.00, '2025-05-14 00:30:58');
INSERT INTO `order_item` VALUES (31, 31, 12, '123123123123123', '/upload/product/2025/05/16/5436383dc1d644fbb4a2cab39b6c0a7d.jpg', 0.01, 1, 0.01, '2025-05-16 22:12:09');
INSERT INTO `order_item` VALUES (32, 32, 13, '摩卡', '/upload/product/2025/05/17/302fce037c4045a7bb078fd887075e3c.png', 10.00, 1, 10.00, '2025-05-17 21:01:00');
INSERT INTO `order_item` VALUES (33, 33, 13, '摩卡', '/upload/product/2025/05/17/302fce037c4045a7bb078fd887075e3c.png', 10.00, 1, 10.00, '2025-05-17 21:05:25');
INSERT INTO `order_item` VALUES (34, 34, 11, '流麻', '/upload/product/2025/05/05/a3febfe471fc482f97426e9998ed8025.jpg', 30.00, 1, 30.00, '2025-05-17 21:08:01');
INSERT INTO `order_item` VALUES (35, 35, 13, '摩卡', '/upload/product/2025/05/17/302fce037c4045a7bb078fd887075e3c.png', 10.00, 1, 10.00, '2025-05-17 21:21:44');

-- ----------------------------
-- Table structure for payment
-- ----------------------------
DROP TABLE IF EXISTS `payment`;
CREATE TABLE `payment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `payment_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付流水号',
  `payment_platform` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付平台：alipay-支付宝',
  `amount` decimal(10, 2) NOT NULL COMMENT '支付金额',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态：0-未支付，1-已支付，2-支付失败，3-已退款',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  UNIQUE INDEX `idx_payment_no`(`payment_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of payment
-- ----------------------------
INSERT INTO `payment` VALUES (1, 11, NULL, 'alipay', 99.00, 0, NULL, '2025-05-09 12:29:23', '2025-05-09 12:29:23');
INSERT INTO `payment` VALUES (3, 13, NULL, 'alipay', 129.00, 0, NULL, '2025-05-09 12:36:55', '2025-05-09 12:36:55');
INSERT INTO `payment` VALUES (6, 14, NULL, 'alipay', 359.00, 0, NULL, '2025-05-09 12:44:07', '2025-05-09 12:44:07');
INSERT INTO `payment` VALUES (7, 15, NULL, 'alipay', 359.00, 0, NULL, '2025-05-09 12:49:16', '2025-05-09 12:49:16');
INSERT INTO `payment` VALUES (8, 16, NULL, 'alipay', 359.00, 0, NULL, '2025-05-09 12:59:49', '2025-05-09 12:59:49');
INSERT INTO `payment` VALUES (9, 17, NULL, 'alipay', 359.00, 0, NULL, '2025-05-09 13:05:21', '2025-05-09 13:05:21');
INSERT INTO `payment` VALUES (11, 18, '2025050922001436290506180057', 'alipay', 129.00, 1, '2025-05-09 13:16:54', '2025-05-09 13:16:25', '2025-05-09 13:16:25');
INSERT INTO `payment` VALUES (12, 19, '2025050922001436290506182684', 'alipay', 359.00, 1, '2025-05-09 13:38:05', '2025-05-09 13:37:16', '2025-05-09 13:37:16');
INSERT INTO `payment` VALUES (13, 20, '2025050922001436290506180058', 'alipay', 129.00, 3, '2025-05-09 14:20:01', '2025-05-09 14:19:29', '2025-05-09 14:19:29');
INSERT INTO `payment` VALUES (14, 21, '2025050922001436290506184052', 'alipay', 103.20, 1, '2025-05-09 16:56:56', '2025-05-09 16:56:29', '2025-05-09 16:56:29');
INSERT INTO `payment` VALUES (15, 24, NULL, 'alipay', 239.20, 0, NULL, '2025-05-13 20:41:04', '2025-05-13 20:41:04');
INSERT INTO `payment` VALUES (16, 26, '2025051322001417150505678878', 'alipay', 59.00, 1, '2025-05-13 20:44:36', '2025-05-13 20:43:18', '2025-05-13 20:43:18');
INSERT INTO `payment` VALUES (17, 29, '2025051322001417150505678879', 'alipay', 239.20, 1, '2025-05-13 20:58:45', '2025-05-13 20:57:59', '2025-05-13 20:57:59');
INSERT INTO `payment` VALUES (18, 28, '2025051322001417150505681306', 'alipay', 239.20, 1, '2025-05-13 21:04:14', '2025-05-13 21:03:35', '2025-05-13 21:03:35');
INSERT INTO `payment` VALUES (19, 30, '2025051422001417150505677061', 'alipay', 359.00, 1, '2025-05-14 00:31:35', '2025-05-14 00:31:03', '2025-05-14 00:31:03');
INSERT INTO `payment` VALUES (20, 31, '2025051622001417150505715232', 'alipay', 0.01, 1, '2025-05-16 22:12:59', '2025-05-16 22:12:23', '2025-05-16 22:12:23');
INSERT INTO `payment` VALUES (21, 32, '2025051722001417150505722749', 'alipay', 10.00, 1, '2025-05-17 21:01:40', '2025-05-17 21:01:05', '2025-05-17 21:01:05');
INSERT INTO `payment` VALUES (22, 34, '2025051722001417150505727844', 'alipay', 30.00, 1, '2025-05-17 21:08:40', '2025-05-17 21:08:07', '2025-05-17 21:08:07');
INSERT INTO `payment` VALUES (23, 35, '2025051722001417150505729581', 'alipay', 10.00, 1, '2025-05-17 21:22:28', '2025-05-17 21:21:51', '2025-05-17 21:21:51');

-- ----------------------------
-- Table structure for post
-- ----------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `ip_id` bigint NULL DEFAULT NULL COMMENT 'IP ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '浏览量',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` int NOT NULL DEFAULT 0 COMMENT '评论数',
  `collect_count` int NOT NULL DEFAULT 0 COMMENT '收藏数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-删除，1-正常，2-审核中',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_ip_id`(`ip_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of post
-- ----------------------------
INSERT INTO `post` VALUES (1, 4, 1, 1, '我的路飞手办开箱', '<p>今天收到了路飞的手办，质量非常好，分享一下开箱体验...</p>', 887, 121, 34, 50, 1, '2025-05-04 08:57:16', '2025-05-04 11:11:32');
INSERT INTO `post` VALUES (2, 5, 2, 2, '火影忍者周边购买攻略', '<p>分享一下我购买火影忍者周边的经验和推荐的店铺...</p>', 593, 100, 25, 45, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `post` VALUES (3, 6, 3, 3, '自制鬼灭之刃同人漫画', '<p>最近画了一些鬼灭之刃的同人漫画，希望大家喜欢...</p>', 760, 200, 40, 80, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `post` VALUES (4, 4, 4, 4, '进击的巨人最终季讨论', '<p>关于进击的巨人最终季的一些看法和讨论...</p>', 844, 250, 60, 101, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `post` VALUES (5, 5, 5, 5, '求推荐柯南周边', '<p>最近想收集一些柯南的周边，有什么好的推荐吗？</p>', 385, 50, 40, 20, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `post` VALUES (6, 6, 1, 6, '龙珠系列手办收藏展示', '<p>展示一下我收藏的龙珠系列手办，包括悟空、贝吉塔等...</p>', 431, 90, 20, 40, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `post` VALUES (7, 4, 2, 7, '哆啦A梦周边性价比分析', '<p>分析一下市面上哆啦A梦周边的性价比，哪些值得购买...</p>', 383, 70, 15, 30, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `post` VALUES (8, 5, 3, 8, '一拳超人同人画作', '<p>分享我画的一拳超人同人画，包括埼玉、杰诺斯等角色...</p>', 484, 110, 25, 55, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `post` VALUES (9, 6, 4, 9, '刀剑神域新作品讨论', '<p>关于刀剑神域最新作品的讨论和看法...</p>', 581, 130, 35, 60, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `post` VALUES (10, 4, 5, 10, '求推荐英雄学院周边', '<p>想买一些英雄学院的周边，有什么好的推荐吗？</p>', 304, 40, 30, 15, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `post` VALUES (11, 9, 2, 6, '423423432432423432', '234234234324234234234234324😗🤨带娃大王大王对哇对哇的🤪', 128, 3, 3, 3, 1, '2025-05-07 19:40:42', '2025-05-08 16:31:13');
INSERT INTO `post` VALUES (12, 9, 3, 8, '22222', '11525555', 4, 0, 0, 1, 1, '2025-05-11 11:08:04', '2025-05-11 11:08:04');

-- ----------------------------
-- Table structure for post_image
-- ----------------------------
DROP TABLE IF EXISTS `post_image`;
CREATE TABLE `post_image`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子图片表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of post_image
-- ----------------------------
INSERT INTO `post_image` VALUES (1, 1, 'https://example.com/post1_1.jpg', 1, '2025-05-04 08:57:16');
INSERT INTO `post_image` VALUES (2, 1, 'https://example.com/post1_2.jpg', 2, '2025-05-04 08:57:16');
INSERT INTO `post_image` VALUES (3, 2, 'https://example.com/post2_1.jpg', 1, '2025-05-04 08:57:16');
INSERT INTO `post_image` VALUES (4, 3, 'https://example.com/post3_1.jpg', 1, '2025-05-04 08:57:16');
INSERT INTO `post_image` VALUES (5, 3, 'https://example.com/post3_2.jpg', 2, '2025-05-04 08:57:16');
INSERT INTO `post_image` VALUES (6, 3, 'https://example.com/post3_3.jpg', 3, '2025-05-04 08:57:16');
INSERT INTO `post_image` VALUES (7, 6, 'https://example.com/post6_1.jpg', 1, '2025-05-04 08:57:16');
INSERT INTO `post_image` VALUES (8, 6, 'https://example.com/post6_2.jpg', 2, '2025-05-04 08:57:16');
INSERT INTO `post_image` VALUES (9, 8, 'https://example.com/post8_1.jpg', 1, '2025-05-04 08:57:16');
INSERT INTO `post_image` VALUES (10, 8, 'https://example.com/post8_2.jpg', 2, '2025-05-04 08:57:16');
INSERT INTO `post_image` VALUES (14, 11, '/upload/post/2025/05/07/1f10e3594f0d448595ab30016681d6cb.jpeg', 1, '2025-05-08 16:31:13');
INSERT INTO `post_image` VALUES (15, 11, '/upload/post/2025/05/07/c085d33311644ceca8f0f46a4b99f516.jpg', 2, '2025-05-08 16:31:13');
INSERT INTO `post_image` VALUES (16, 11, '/upload/post/2025/05/07/cf671d6518e1476a8d0dad4e6ee055da.jpg', 3, '2025-05-08 16:31:13');
INSERT INTO `post_image` VALUES (17, 12, '/upload/post/2025/05/11/1987f5d549de41ecb9fe623763d38fb5.jpg', 1, '2025-05-11 11:08:04');

-- ----------------------------
-- Table structure for post_topic
-- ----------------------------
DROP TABLE IF EXISTS `post_topic`;
CREATE TABLE `post_topic`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `topic_id` bigint NOT NULL COMMENT '话题ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_post_topic`(`post_id` ASC, `topic_id` ASC) USING BTREE,
  INDEX `idx_topic_id`(`topic_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子话题关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of post_topic
-- ----------------------------
INSERT INTO `post_topic` VALUES (1, 1, 1, '2025-05-04 08:57:16');
INSERT INTO `post_topic` VALUES (2, 6, 1, '2025-05-04 08:57:16');
INSERT INTO `post_topic` VALUES (3, 2, 2, '2025-05-04 08:57:16');
INSERT INTO `post_topic` VALUES (4, 7, 2, '2025-05-04 08:57:16');
INSERT INTO `post_topic` VALUES (5, 5, 2, '2025-05-04 08:57:16');
INSERT INTO `post_topic` VALUES (6, 3, 3, '2025-05-04 08:57:16');
INSERT INTO `post_topic` VALUES (7, 8, 3, '2025-05-04 08:57:16');
INSERT INTO `post_topic` VALUES (8, 4, 4, '2025-05-04 08:57:16');
INSERT INTO `post_topic` VALUES (9, 9, 4, '2025-05-04 08:57:16');
INSERT INTO `post_topic` VALUES (13, 11, 1, '2025-05-08 16:31:13');
INSERT INTO `post_topic` VALUES (14, 11, 2, '2025-05-08 16:31:13');
INSERT INTO `post_topic` VALUES (15, 11, 3, '2025-05-08 16:31:13');
INSERT INTO `post_topic` VALUES (16, 12, 4, '2025-05-11 11:08:04');
INSERT INTO `post_topic` VALUES (17, 12, 1, '2025-05-11 11:08:04');

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `user_id` bigint NOT NULL COMMENT '发布者用户ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `ip_id` bigint NULL DEFAULT NULL COMMENT 'IP ID',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品描述',
  `price` decimal(10, 2) NOT NULL COMMENT '商品价格',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存',
  `sales` int NOT NULL DEFAULT 0 COMMENT '销量',
  `main_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主图URL',
  `is_certified` tinyint NOT NULL DEFAULT 0 COMMENT '是否认证商家发布：0-否，1-是',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架，2-审核中',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_ip_id`(`ip_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product
-- ----------------------------
INSERT INTO `product` VALUES (1, 2, 6, 1, '路飞手办', '海贼王路飞PVC手办，高约25cm', 299.00, 101, 49, '/upload/product/2025/05/07/98fa00b688fc4f2785293d9f879d62e7.jpg', 1, 1, '2025-05-04 08:57:16', '2025-05-07 16:25:53');
INSERT INTO `product` VALUES (2, 2, 7, 2, 'Q版鸣人', '火影忍者鸣人Q版手办，高约10cm', 99.00, 200, 120, '/upload/product/2025/05/07/4eff07416ef14774b7cebc27e77833b7.jpg', 1, 1, '2025-05-04 08:57:16', '2025-05-07 16:27:37');
INSERT INTO `product` VALUES (3, 2, 6, 3, '灶门炭治郎手办', '鬼灭之刃主角炭治郎精致手办', 359.00, 78, 32, '/upload/product/2025/05/07/e52595f758ee4cb4bca249c2bf40f587.jpeg', 1, 1, '2025-05-04 08:57:16', '2025-05-07 16:28:43');
INSERT INTO `product` VALUES (4, 3, 9, 4, '进击的巨人T恤', '进击的巨人主题T恤，多种尺码', 129.00, 299, 201, '/upload/product/2025/05/07/843f1ab7e06442329a20748bdd8788fa.jpg', 1, 1, '2025-05-04 08:57:16', '2025-05-07 16:44:38');
INSERT INTO `product` VALUES (5, 3, 10, 5, '柯南卫衣', '名侦探柯南主题卫衣，舒适保暖', 199.00, 150, 80, '/upload/product/2025/05/07/7aa29ffed4344a3d97f871c2b092f41e.jpg', 1, 1, '2025-05-04 08:57:16', '2025-05-07 16:45:23');
INSERT INTO `product` VALUES (6, 3, 15, 6, '龙珠抱枕', '龙珠主题抱枕，柔软舒适', 89.00, 200, 100, '/upload/product/2025/05/07/ff9f1857e2454597907d6a34ed0d0b89.png', 1, 1, '2025-05-04 08:57:16', '2025-05-07 16:46:33');
INSERT INTO `product` VALUES (7, 2, 16, 7, '哆啦A梦杯子', '哆啦A梦造型陶瓷杯', 59.00, 299, 151, '/upload/product/2025/05/07/184ecb04e36b49249f4dc72ac41034a7.jpg', 1, 1, '2025-05-04 08:57:16', '2025-05-07 16:29:35');
INSERT INTO `product` VALUES (8, 2, 17, 8, '一拳超人挂画', '一拳超人主题装饰挂画', 79.00, 100, 40, 'https://example.com/product8.jpg', 1, -1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `product` VALUES (9, 3, 12, 9, '刀剑神域文件夹', '刀剑神域主题文件收纳夹', 29.00, 500, 300, 'https://example.com/product9.jpg', 1, -1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `product` VALUES (10, 3, 13, 10, '英雄学院笔记本', '我的英雄学院主题笔记本', 39.00, 400, 250, 'https://example.com/product10.jpg', 1, -1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `product` VALUES (11, 8, 10, 1, '流麻', '123123', 30.00, 29, 1, '/upload/product/2025/05/05/a3febfe471fc482f97426e9998ed8025.jpg', 1, 1, '2025-05-05 23:21:49', '2025-05-15 10:26:33');
INSERT INTO `product` VALUES (12, 9, 3, 2, '火影卡片', '123', 50.00, 1, 1, '/upload/product/2025/05/16/5436383dc1d644fbb4a2cab39b6c0a7d.jpg', 0, 1, '2025-05-16 22:11:02', '2025-05-17 22:32:28');
INSERT INTO `product` VALUES (13, 9, 15, 4, '摩卡', '个人闲置', 10.00, 8, 2, '/upload/product/2025/05/17/302fce037c4045a7bb078fd887075e3c.png', 0, 1, '2025-05-17 20:59:36', '2025-05-17 22:32:48');

-- ----------------------------
-- Table structure for product_category
-- ----------------------------
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父分类ID，0表示一级分类',
  `level` tinyint NOT NULL DEFAULT 1 COMMENT '分类级别：1-一级，2-二级，3-三级',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product_category
-- ----------------------------
INSERT INTO `product_category` VALUES (1, '手办', 0, 1, 1, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `product_category` VALUES (2, '服装', 0, 1, 2, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `product_category` VALUES (3, '文具', 0, 1, 3, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `product_category` VALUES (4, '家居', 0, 1, 4, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `product_category` VALUES (5, '其他', 0, 1, 5, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `product_category` VALUES (6, 'PVC手办', 1, 2, 1, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `product_category` VALUES (7, 'Q版手办', 1, 2, 2, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `product_category` VALUES (8, '可动手办', 1, 2, 3, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `product_category` VALUES (9, 'T恤', 2, 2, 1, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `product_category` VALUES (10, '卫衣', 2, 2, 2, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `product_category` VALUES (11, '帽子', 2, 2, 3, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `product_category` VALUES (12, '文件夹', 3, 2, 1, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `product_category` VALUES (13, '笔记本', 3, 2, 2, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `product_category` VALUES (14, '钢笔', 3, 2, 3, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `product_category` VALUES (15, '抱枕', 4, 2, 1, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `product_category` VALUES (16, '杯子', 4, 2, 2, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `product_category` VALUES (17, '挂画', 4, 2, 3, 1, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `product_category` VALUES (18, '3424', 2, 2, 1, 0, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `product_category` VALUES (19, '234234324', 0, 1, 1, 0, '2025-05-03 23:20:26', '2025-05-03 23:20:26');
INSERT INTO `product_category` VALUES (20, '23423432423424', 1, 2, 1, 0, '2025-05-03 23:20:26', '2025-05-03 23:20:26');

-- ----------------------------
-- Table structure for product_detail
-- ----------------------------
DROP TABLE IF EXISTS `product_detail`;
CREATE TABLE `product_detail`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '详情ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详情内容（HTML格式）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品详情表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product_detail
-- ----------------------------
INSERT INTO `product_detail` VALUES (1, 1, '<p>海贼王路飞PVC手办，高约25cm，采用优质PVC材料制作，造型精致，细节丰富。</p><p>适合海贼王粉丝收藏。</p>', '2025-05-04 08:57:16', '2025-05-07 16:25:53');
INSERT INTO `product_detail` VALUES (2, 2, '<p>火影忍者鸣人Q版手办，高约10cm，Q萌可爱，适合桌面摆放。</p>', '2025-05-04 08:57:16', '2025-05-07 16:27:37');
INSERT INTO `product_detail` VALUES (3, 3, '<p>鬼灭之刃主角炭治郎精致手办，高约20cm，还原动漫场景，细节精美。</p>', '2025-05-04 08:57:16', '2025-05-07 16:28:43');
INSERT INTO `product_detail` VALUES (4, 4, '<p>进击的巨人主题T恤，100%纯棉材质，多种尺码可选，舒适透气。</p>', '2025-05-04 08:57:16', '2025-05-07 16:44:38');
INSERT INTO `product_detail` VALUES (5, 5, '<p>名侦探柯南主题卫衣，柔软保暖，适合秋冬季节穿着。</p>', '2025-05-04 08:57:16', '2025-05-07 16:45:23');
INSERT INTO `product_detail` VALUES (6, 6, '<p>龙珠主题抱枕，柔软舒适，填充优质PP棉，可拆洗。</p>', '2025-05-04 08:57:16', '2025-05-07 16:46:33');
INSERT INTO `product_detail` VALUES (7, 7, '<p>哆啦A梦造型陶瓷杯，容量350ml，食品级材质，安全健康。</p>', '2025-05-04 08:57:16', '2025-05-07 16:29:35');
INSERT INTO `product_detail` VALUES (8, 8, '<p>一拳超人主题装饰挂画，尺寸50x70cm，高清印刷，色彩鲜艳。</p>', '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `product_detail` VALUES (9, 9, '<p>刀剑神域主题文件收纳夹，A4大小，容量30页，防水耐用。</p>', '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `product_detail` VALUES (10, 10, '<p>我的英雄学院主题笔记本，100页，内页优质纸张，封面精美。</p>', '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `product_detail` VALUES (11, 11, '123123213123', '2025-05-05 23:21:49', '2025-05-15 10:26:33');
INSERT INTO `product_detail` VALUES (12, 12, '123', '2025-05-16 22:11:02', '2025-05-17 22:32:28');
INSERT INTO `product_detail` VALUES (13, 13, '全新', '2025-05-17 20:59:36', '2025-05-17 22:32:48');

-- ----------------------------
-- Table structure for product_image
-- ----------------------------
DROP TABLE IF EXISTS `product_image`;
CREATE TABLE `product_image`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品图片表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product_image
-- ----------------------------
INSERT INTO `product_image` VALUES (11, 8, 'https://example.com/product8_1.jpg', 1, '2025-05-04 08:57:16');
INSERT INTO `product_image` VALUES (12, 9, 'https://example.com/product9_1.jpg', 1, '2025-05-04 08:57:16');
INSERT INTO `product_image` VALUES (13, 10, 'https://example.com/product10_1.jpg', 1, '2025-05-04 08:57:16');
INSERT INTO `product_image` VALUES (15, 1, '/upload/product/2025/05/07/b2a6f279656a42a4a9eec1203e741be5.jpg', 1, '2025-05-07 16:25:53');
INSERT INTO `product_image` VALUES (16, 1, '/upload/product/2025/05/07/267156632187429da4b249eabb528ba0.jpg', 2, '2025-05-07 16:25:53');
INSERT INTO `product_image` VALUES (17, 2, '/upload/product/2025/05/07/8f53f7f7a4684f0da3e890d30fa176a9.jpg', 1, '2025-05-07 16:27:37');
INSERT INTO `product_image` VALUES (18, 2, '/upload/product/2025/05/07/5b7604e1308b432d927a34e01fd67cd2.jpg', 2, '2025-05-07 16:27:37');
INSERT INTO `product_image` VALUES (19, 3, '/upload/product/2025/05/07/c1bc048f2ddb4f3f8404e35780931736.jpeg', 1, '2025-05-07 16:28:43');
INSERT INTO `product_image` VALUES (20, 3, '/upload/product/2025/05/07/cf3185d268c9405982bbf5e0bbbd25a1.jpg', 2, '2025-05-07 16:28:43');
INSERT INTO `product_image` VALUES (21, 7, '/upload/product/2025/05/07/28174edba9da4dbfb9941c364c64f261.jpg', 1, '2025-05-07 16:29:35');
INSERT INTO `product_image` VALUES (22, 7, '/upload/product/2025/05/07/640785e2e98f4708a20207477603a249.jpg', 2, '2025-05-07 16:29:35');
INSERT INTO `product_image` VALUES (27, 4, '/upload/product/2025/05/07/8358f3c9363b4d49be1ba57cb86a374d.jpg', 1, '2025-05-07 16:44:38');
INSERT INTO `product_image` VALUES (28, 4, '/upload/product/2025/05/07/f8b6d9a5069445ecb4c1d788757923ef.jpg', 2, '2025-05-07 16:44:38');
INSERT INTO `product_image` VALUES (29, 5, '/upload/product/2025/05/07/5ccdd51032d54ecf92c8847fc5842e10.jpg', 1, '2025-05-07 16:45:23');
INSERT INTO `product_image` VALUES (30, 5, '/upload/product/2025/05/07/60725b7699184f44a144ef7d6d2f8e1e.jpg', 2, '2025-05-07 16:45:23');
INSERT INTO `product_image` VALUES (31, 6, '/upload/product/2025/05/07/6659c81810384e02a7134f1af7f231e5.jpg', 1, '2025-05-07 16:46:33');
INSERT INTO `product_image` VALUES (32, 6, '/upload/product/2025/05/07/71df8db8b3cc444eb75917a8b8b7b5e5.jpg', 2, '2025-05-07 16:46:33');
INSERT INTO `product_image` VALUES (33, 6, '/upload/product/2025/05/07/6108cf5ed7e24d73a7e37913303c8e58.jpg', 3, '2025-05-07 16:46:33');
INSERT INTO `product_image` VALUES (34, 11, '/upload/product/2025/05/05/c7f67ee18f1642faaff485fb4617e68e.jpg', 1, '2025-05-15 10:26:33');

-- ----------------------------
-- Table structure for product_review
-- ----------------------------
DROP TABLE IF EXISTS `product_review`;
CREATE TABLE `product_review`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_item_id` bigint NOT NULL COMMENT '订单项ID',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评价内容',
  `rating` tinyint NOT NULL DEFAULT 5 COMMENT '评分：1-5',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-删除，1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_order_item_id`(`order_item_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品评价表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product_review
-- ----------------------------
INSERT INTO `product_review` VALUES (4, 9, 3, 19, 19, '123', 5, 1, '2025-05-09 16:33:28', '2025-05-09 16:33:28');
INSERT INTO `product_review` VALUES (5, 4, 12, 31, 31, '425432453453', 5, 1, '2025-05-16 22:14:31', '2025-05-16 22:14:31');
INSERT INTO `product_review` VALUES (6, 9, 11, 34, 34, 'hh', 5, 1, '2025-05-17 21:25:14', '2025-05-17 21:25:14');

-- ----------------------------
-- Table structure for product_review_image
-- ----------------------------
DROP TABLE IF EXISTS `product_review_image`;
CREATE TABLE `product_review_image`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `review_id` bigint NOT NULL COMMENT '评价ID',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_review_id`(`review_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品评价图片表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product_review_image
-- ----------------------------
INSERT INTO `product_review_image` VALUES (3, 4, '/upload/review/2025/05/09/11e6509f615c43b9a97cd63d45c8b71b.jpg', 0, '2025-05-09 16:33:28');

-- ----------------------------
-- Table structure for refund
-- ----------------------------
DROP TABLE IF EXISTS `refund`;
CREATE TABLE `refund`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '退款ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `refund_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '退款编号',
  `refund_amount` decimal(10, 2) NOT NULL COMMENT '退款金额',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '退款原因',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '退款状态：0-申请中，1-已退款，2-已拒绝',
  `refund_time` datetime NULL DEFAULT NULL COMMENT '退款时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_refund_no`(`refund_no` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '退款信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of refund
-- ----------------------------
INSERT INTO `refund` VALUES (1, 18, '1d6047997b03438d8cea0c448a3c55c6', 129.00, '32131', 0, NULL, '2025-05-09 13:17:41', '2025-05-09 13:17:41');
INSERT INTO `refund` VALUES (2, 18, '21f6e0184ea8435786dfb05619f90f4b', 129.00, '213', 0, NULL, '2025-05-09 14:08:42', '2025-05-09 14:08:42');
INSERT INTO `refund` VALUES (3, 20, '4073c51de923499f93f14b0dd6d9fa08', 129.00, '2', 1, '2025-05-09 14:37:20', '2025-05-09 14:20:11', '2025-05-09 14:20:11');

-- ----------------------------
-- Table structure for topic
-- ----------------------------
DROP TABLE IF EXISTS `topic`;
CREATE TABLE `topic`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '话题ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '话题名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '话题描述',
  `post_count` int NOT NULL DEFAULT 0 COMMENT '帖子数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '话题表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of topic
-- ----------------------------
INSERT INTO `topic` VALUES (1, '手办收藏', '分享各种动漫手办的收藏心得', 5, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `topic` VALUES (2, '周边购买', '讨论动漫周边的购买经验', 4, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `topic` VALUES (3, '同人创作', '展示动漫同人作品', 3, 1, '2025-05-04 08:57:16', '2025-05-04 08:57:16');
INSERT INTO `topic` VALUES (4, '动漫讨论', '讨论各种动漫作品', 3, 1, '2025-05-04 08:57:16', '2025-05-04 10:53:14');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `gender` tinyint NULL DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `bio` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人简介',
  `role` tinyint NOT NULL DEFAULT 0 COMMENT '角色：0-普通用户，1-商家，2-管理员',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `idx_email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `idx_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '$2a$10$kiYBO0ZChcdzvUM1AEVwo.yZoPZGNPUM1gyoTiZlC7.kinJKDlSS.', '系统管理员', '/upload/avatar/2025/05/04/2b4ef0130f934e85b8005153019432c8.jpg', 'admin@admin.com', '', 0, NULL, '', 2, 1, '2025-05-03 23:20:26', '2025-05-04 12:17:13');
INSERT INTO `user` VALUES (2, 'merchant1', '$2a$10$QOliZoMZcycxZBHapYs4lu6OD.YzW2BVVF4YplyWSqyNRpyaKC11q', '商家一号', '/upload/avatar/2025/05/06/6caa5814182d41fba2b73ee112ab3f0c.jpg', 'merchant1@example.com', '13800000001', 1, NULL, '专业动漫周边销售商', 1, 1, '2025-05-04 08:57:16', '2025-05-06 22:11:56');
INSERT INTO `user` VALUES (3, 'merchant2', '$2a$10$QOliZoMZcycxZBHapYs4lu6OD.YzW2BVVF4YplyWSqyNRpyaKC11q', '二号店铺', '/upload/avatar/2025/05/08/99464d06eb654995a5c59c194b81369b.jpeg', 'merchant2@example.com', '13800000002', 2, NULL, '专注高品质手办', 1, 1, '2025-05-04 08:57:16', '2025-05-08 11:45:50');
INSERT INTO `user` VALUES (4, 'user1', '$2a$10$QOliZoMZcycxZBHapYs4lu6OD.YzW2BVVF4YplyWSqyNRpyaKC11q', '动漫迷小王', '/upload/avatar/2025/05/08/c91753e693a5449aa0de00fdfec4a0e5.jpg', 'user1@example.com', '13800000003', 1, NULL, '热爱动漫的普通用户', 0, 1, '2025-05-04 08:57:16', '2025-05-08 13:21:27');
INSERT INTO `user` VALUES (5, 'user2', '$2a$10$QOliZoMZcycxZBHapYs4lu6OD.YzW2BVVF4YplyWSqyNRpyaKC11q', '二次元少女', '/upload/avatar/2025/05/08/777367c608384f45b3d4c31752d0019e.jpg', 'user2@example.com', '13800000004', 2, NULL, 'COSPLAY爱好者', 0, 1, '2025-05-04 08:57:16', '2025-05-08 13:24:28');
INSERT INTO `user` VALUES (6, 'user3', '$2a$10$HeJ657/s8b0aHJFdbP.gmeqBCqgR6Aufhcb5EL0Ujbi2Wa9I60Qoq', '漫画控', '/upload/avatar/2025/05/08/38badf87cc9e4d1eb01fe4214793acb6.jpg', 'user3@example.com', '13800000005', 1, NULL, '收集各种漫画周边', 0, 1, '2025-05-04 08:57:16', '2025-05-08 13:25:29');
INSERT INTO `user` VALUES (8, 'bus1', '$2a$10$QOliZoMZcycxZBHapYs4lu6OD.YzW2BVVF4YplyWSqyNRpyaKC11q', '带娃', '/upload/avatar/2025/05/05/b2b353689a2f4b1089e7719758c95478.jpg', '2@2.com', '15136363636', 2, NULL, '34234', 1, 1, '2025-05-04 17:48:38', '2025-05-06 18:57:11');
INSERT INTO `user` VALUES (9, 'testU', '$2a$10$mzyDTVU.5BQnrn52R9JuPu4LXFYp7Wknbl8Fd9u0jMDCNyCbS0tM.', '测试', '/upload/avatar/2025/05/07/cdaafb185de344a18e16dfd296b3e21a.jpg', '2222@2.com', '15135353535', 1, NULL, '123123555', 0, 1, '2025-05-06 21:18:16', '2025-05-07 22:13:21');
INSERT INTO `user` VALUES (10, 'bus2', '$2a$10$iA9IRid93aW7C0RZ2Y8Z5enirs43Mc5F4UZ6u2D4cf5ZfNZf/zbHu', '11', NULL, '22@qq.com', '15336312208', 0, NULL, '', 0, 1, '2025-05-11 22:12:24', '2025-05-11 22:12:24');
INSERT INTO `user` VALUES (11, '扣扣索索', '$2a$10$qFO8OI.dhqb215AkzJtF0uTG9nsqvkuZJZ2dLsxh5d4LdNwq.X3kq', 'koukou', '', 'ssss@aq.com', '15233333333', 0, NULL, '', 0, 1, '2025-05-14 00:29:30', '2025-05-14 00:29:30');
INSERT INTO `user` VALUES (12, 'bus3', '$2a$10$EhDKXt7Noe2cL/y/GwudeOn/3M8q8oi68QjoU3wXoywMdGgP/YKGa', '22', NULL, '444@qq.com', '15544855159', 0, NULL, '', 1, 1, '2025-05-15 10:24:33', '2025-05-15 10:24:33');
INSERT INTO `user` VALUES (13, 'uu4', '$2a$10$h6.9/B8/uLr6rblbRVx70uo4.TwaqzAEPShAXJ0SGSUG.DCSN9Say', '番茄炒鸡蛋', '', 'w@qq.com', '15233318888', 0, NULL, '', 0, 1, '2025-05-17 21:30:16', '2025-05-17 21:30:16');

SET FOREIGN_KEY_CHECKS = 1;
