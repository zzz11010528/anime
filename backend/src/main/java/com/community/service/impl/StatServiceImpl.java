package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.entity.*;
import com.community.mapper.*;
import com.community.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务实现类
 */
@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {

    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final LikeMapper likeMapper;
    private final CollectionMapper collectionMapper;

    @Override
    public Map<String, Object> getProductSalesStat(Long productId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();

        // 查询订单项
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        if (productId != null) {
            wrapper.eq(OrderItem::getProductId, productId);
        }
        List<OrderItem> orderItems = orderItemMapper.selectList(wrapper);

        // 查询订单
        List<Long> orderIds = orderItems.stream().map(OrderItem::getOrderId).collect(Collectors.toList());
        if (orderIds.isEmpty()) {
            result.put("totalSales", 0);
            result.put("totalAmount", BigDecimal.ZERO);
            result.put("dailySales", Collections.emptyList());
            return result;
        }

        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.in(Order::getId, orderIds);
        orderWrapper.ge(Order::getStatus, 1); // 已支付的订单
        if (startDate != null) {
            orderWrapper.ge(Order::getCreatedAt, LocalDateTime.of(startDate, LocalTime.MIN));
        }
        if (endDate != null) {
            orderWrapper.le(Order::getCreatedAt, LocalDateTime.of(endDate, LocalTime.MAX));
        }
        List<Order> orders = orderMapper.selectList(orderWrapper);

        // 统计销量和销售额
        Map<Long, Order> orderMap = orders.stream().collect(Collectors.toMap(Order::getId, order -> order));
        int totalSales = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<LocalDate, Integer> dailySalesMap = new HashMap<>();
        Map<LocalDate, BigDecimal> dailyAmountMap = new HashMap<>();

        for (OrderItem item : orderItems) {
            Order order = orderMap.get(item.getOrderId());
            if (order != null) {
                totalSales += item.getQuantity();
                totalAmount = totalAmount.add(item.getTotalPrice());

                LocalDate orderDate = order.getCreatedAt().toLocalDate();
                dailySalesMap.put(orderDate, dailySalesMap.getOrDefault(orderDate, 0) + item.getQuantity());
                dailyAmountMap.put(orderDate, dailyAmountMap.getOrDefault(orderDate, BigDecimal.ZERO).add(item.getTotalPrice()));
            }
        }

        // 构建日期范围内的每日销量
        List<Map<String, Object>> dailySales = new ArrayList<>();
        if (startDate != null && endDate != null) {
            LocalDate date = startDate;
            while (!date.isAfter(endDate)) {
                Map<String, Object> dailyData = new HashMap<>();
                dailyData.put("date", date);
                dailyData.put("sales", dailySalesMap.getOrDefault(date, 0));
                dailyData.put("amount", dailyAmountMap.getOrDefault(date, BigDecimal.ZERO));
                dailySales.add(dailyData);
                date = date.plusDays(1);
            }
        }

        result.put("totalSales", totalSales);
        result.put("totalAmount", totalAmount);
        result.put("dailySales", dailySales);

        return result;
    }

    @Override
    public Map<String, Object> getHotProducts(LocalDate startDate, LocalDate endDate, Integer limit) {
        Map<String, Object> result = new HashMap<>();

        // 查询所有商品，明确指定需要的列
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.select(Product::getId, Product::getName, Product::getMainImage,
                             Product::getPrice, Product::getUserId);
        List<Product> products = productMapper.selectList(productWrapper);

        if (products.isEmpty()) {
            result.put("hotProducts", Collections.emptyList());
            return result;
        }

        // 查询订单项
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(OrderItem::getOrderId, OrderItem::getProductId,
                      OrderItem::getQuantity, OrderItem::getTotalPrice);
        List<OrderItem> orderItems = orderItemMapper.selectList(wrapper);

        // 查询订单
        List<Long> orderIds = orderItems.stream().map(OrderItem::getOrderId).collect(Collectors.toList());
        if (orderIds.isEmpty()) {
            result.put("hotProducts", Collections.emptyList());
            return result;
        }

        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.in(Order::getId, orderIds);
        orderWrapper.ge(Order::getStatus, 1); // 已支付的订单
        if (startDate != null) {
            orderWrapper.ge(Order::getCreatedAt, LocalDateTime.of(startDate, LocalTime.MIN));
        }
        if (endDate != null) {
            orderWrapper.le(Order::getCreatedAt, LocalDateTime.of(endDate, LocalTime.MAX));
        }
        orderWrapper.select(Order::getId, Order::getStatus, Order::getCreatedAt);
        List<Order> orders = orderMapper.selectList(orderWrapper);

        // 统计每个商品的销量
        Map<Long, Order> orderMap = orders.stream().collect(Collectors.toMap(Order::getId, order -> order));
        Map<Long, Integer> productSalesMap = new HashMap<>();
        Map<Long, BigDecimal> productAmountMap = new HashMap<>();

        for (OrderItem item : orderItems) {
            Order order = orderMap.get(item.getOrderId());
            if (order != null) {
                productSalesMap.put(item.getProductId(), productSalesMap.getOrDefault(item.getProductId(), 0) + item.getQuantity());
                productAmountMap.put(item.getProductId(), productAmountMap.getOrDefault(item.getProductId(), BigDecimal.ZERO).add(item.getTotalPrice()));
            }
        }

        // 按销量排序
        List<Map<String, Object>> hotProducts = new ArrayList<>();
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, product -> product));

        productSalesMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit != null ? limit : 10)
                .forEach(entry -> {
                    Long productId = entry.getKey();
                    Product product = productMap.get(productId);
                    if (product != null) {
                        Map<String, Object> productData = new HashMap<>();
                        productData.put("productId", productId);
                        productData.put("productName", product.getName());
                        productData.put("productImage", product.getMainImage());
                        productData.put("sales", entry.getValue());
                        productData.put("amount", productAmountMap.getOrDefault(productId, BigDecimal.ZERO));
                        productData.put("price", product.getPrice());
                        productData.put("userId", product.getUserId());
                        hotProducts.add(productData);
                    }
                });

        result.put("hotProducts", hotProducts);

        return result;
    }

    @Override
    public Map<String, Object> getPostStat(Long postId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();

        // 查询帖子
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (postId != null) {
            wrapper.eq(Post::getId, postId);
        }
        if (startDate != null) {
            wrapper.ge(Post::getCreatedAt, LocalDateTime.of(startDate, LocalTime.MIN));
        }
        if (endDate != null) {
            wrapper.le(Post::getCreatedAt, LocalDateTime.of(endDate, LocalTime.MAX));
        }
        List<Post> posts = postMapper.selectList(wrapper);

        if (posts.isEmpty()) {
            result.put("totalPosts", 0);
            result.put("totalViews", 0);
            result.put("totalLikes", 0);
            result.put("totalComments", 0);
            result.put("dailyPosts", Collections.emptyList());
            return result;
        }

        // 统计帖子数量、浏览量、点赞数、评论数
        int totalPosts = posts.size();
        int totalViews = posts.stream().mapToInt(Post::getViewCount).sum();
        int totalLikes = posts.stream().mapToInt(Post::getLikeCount).sum();
        int totalComments = posts.stream().mapToInt(Post::getCommentCount).sum();

        // 按日期统计帖子数量
        Map<LocalDate, Integer> dailyPostsMap = new HashMap<>();
        for (Post post : posts) {
            LocalDate postDate = post.getCreatedAt().toLocalDate();
            dailyPostsMap.put(postDate, dailyPostsMap.getOrDefault(postDate, 0) + 1);
        }

        // 构建日期范围内的每日帖子数量
        List<Map<String, Object>> dailyPosts = new ArrayList<>();
        if (startDate != null && endDate != null) {
            LocalDate date = startDate;
            while (!date.isAfter(endDate)) {
                Map<String, Object> dailyData = new HashMap<>();
                dailyData.put("date", date);
                dailyData.put("posts", dailyPostsMap.getOrDefault(date, 0));
                dailyPosts.add(dailyData);
                date = date.plusDays(1);
            }
        }

        result.put("totalPosts", totalPosts);
        result.put("totalViews", totalViews);
        result.put("totalLikes", totalLikes);
        result.put("totalComments", totalComments);
        result.put("dailyPosts", dailyPosts);

        return result;
    }

    @Override
    public Map<String, Object> getHotPosts(LocalDate startDate, LocalDate endDate, Integer limit) {
        Map<String, Object> result = new HashMap<>();

        // 查询帖子
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1); // 正常状态的帖子
        if (startDate != null) {
            wrapper.ge(Post::getCreatedAt, LocalDateTime.of(startDate, LocalTime.MIN));
        }
        if (endDate != null) {
            wrapper.le(Post::getCreatedAt, LocalDateTime.of(endDate, LocalTime.MAX));
        }

        // 添加查询字段，明确指定需要的列，避免使用 collect_count
        wrapper.select(Post::getId, Post::getTitle, Post::getViewCount,
                      Post::getLikeCount, Post::getCommentCount, Post::getCreatedAt,
                      Post::getUserId);

        List<Post> posts = postMapper.selectList(wrapper);

        if (posts.isEmpty()) {
            result.put("hotPosts", Collections.emptyList());
            return result;
        }

        // 按热度排序（浏览量 + 点赞数 * 2 + 评论数 * 3）
        List<Map<String, Object>> hotPosts = new ArrayList<>();

        posts.stream()
                .sorted((p1, p2) -> {
                    int heat1 = p1.getViewCount() + p1.getLikeCount() * 2 + p1.getCommentCount() * 3;
                    int heat2 = p2.getViewCount() + p2.getLikeCount() * 2 + p2.getCommentCount() * 3;
                    return Integer.compare(heat2, heat1);
                })
                .limit(limit != null ? limit : 10)
                .forEach(post -> {
                    Map<String, Object> postData = new HashMap<>();
                    postData.put("postId", post.getId());
                    postData.put("title", post.getTitle());
                    postData.put("viewCount", post.getViewCount());
                    postData.put("likeCount", post.getLikeCount());
                    postData.put("commentCount", post.getCommentCount());
                    postData.put("createdAt", post.getCreatedAt());
                    postData.put("userId", post.getUserId());
                    hotPosts.add(postData);
                });

        result.put("hotPosts", hotPosts);

        return result;
    }

    @Override
    public Map<String, Object> getUserBehaviorStat(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();

        // 查询用户
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            userWrapper.eq(User::getId, userId);
        }
        List<User> users = userMapper.selectList(userWrapper);

        if (users.isEmpty()) {
            result.put("totalUsers", 0);
            result.put("dailyUsers", Collections.emptyList());
            return result;
        }

        // 查询帖子
        LambdaQueryWrapper<Post> postWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            postWrapper.eq(Post::getUserId, userId);
        }
        if (startDate != null) {
            postWrapper.ge(Post::getCreatedAt, LocalDateTime.of(startDate, LocalTime.MIN));
        }
        if (endDate != null) {
            postWrapper.le(Post::getCreatedAt, LocalDateTime.of(endDate, LocalTime.MAX));
        }
        List<Post> posts = postMapper.selectList(postWrapper);

        // 查询评论
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            commentWrapper.eq(Comment::getUserId, userId);
        }
        if (startDate != null) {
            commentWrapper.ge(Comment::getCreatedAt, LocalDateTime.of(startDate, LocalTime.MIN));
        }
        if (endDate != null) {
            commentWrapper.le(Comment::getCreatedAt, LocalDateTime.of(endDate, LocalTime.MAX));
        }
        List<Comment> comments = commentMapper.selectList(commentWrapper);

        // 查询订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            orderWrapper.eq(Order::getUserId, userId);
        }
        if (startDate != null) {
            orderWrapper.ge(Order::getCreatedAt, LocalDateTime.of(startDate, LocalTime.MIN));
        }
        if (endDate != null) {
            orderWrapper.le(Order::getCreatedAt, LocalDateTime.of(endDate, LocalTime.MAX));
        }
        List<Order> orders = orderMapper.selectList(orderWrapper);

        // 统计用户行为
        int totalUsers = users.size();
        int totalPosts = posts.size();
        int totalComments = comments.size();
        int totalOrders = orders.size();
        BigDecimal totalOrderAmount = orders.stream()
                .filter(order -> order.getStatus() >= 1) // 已支付的订单
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 按日期统计用户注册数量
        Map<LocalDate, Integer> dailyUsersMap = new HashMap<>();
        for (User user : users) {
            LocalDate registerDate = user.getCreatedAt().toLocalDate();
            dailyUsersMap.put(registerDate, dailyUsersMap.getOrDefault(registerDate, 0) + 1);
        }

        // 构建日期范围内的每日用户注册数量
        List<Map<String, Object>> dailyUsers = new ArrayList<>();
        if (startDate != null && endDate != null) {
            LocalDate date = startDate;
            while (!date.isAfter(endDate)) {
                Map<String, Object> dailyData = new HashMap<>();
                dailyData.put("date", date);
                dailyData.put("users", dailyUsersMap.getOrDefault(date, 0));
                dailyUsers.add(dailyData);
                date = date.plusDays(1);
            }
        }

        result.put("totalUsers", totalUsers);
        result.put("totalPosts", totalPosts);
        result.put("totalComments", totalComments);
        result.put("totalOrders", totalOrders);
        result.put("totalOrderAmount", totalOrderAmount);
        result.put("dailyUsers", dailyUsers);

        return result;
    }

    @Override
    public Map<String, Object> getActiveUsers(LocalDate startDate, LocalDate endDate, Integer limit) {
        Map<String, Object> result = new HashMap<>();

        // 查询用户，明确指定需要的列
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.select(User::getId, User::getUsername, User::getNickname, User::getAvatar);
        List<User> users = userMapper.selectList(userWrapper);

        if (users.isEmpty()) {
            result.put("activeUsers", Collections.emptyList());
            return result;
        }

        // 查询帖子
        LambdaQueryWrapper<Post> postWrapper = new LambdaQueryWrapper<>();
        if (startDate != null) {
            postWrapper.ge(Post::getCreatedAt, LocalDateTime.of(startDate, LocalTime.MIN));
        }
        if (endDate != null) {
            postWrapper.le(Post::getCreatedAt, LocalDateTime.of(endDate, LocalTime.MAX));
        }
        postWrapper.select(Post::getUserId, Post::getCreatedAt);
        List<Post> posts = postMapper.selectList(postWrapper);

        // 查询评论
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        if (startDate != null) {
            commentWrapper.ge(Comment::getCreatedAt, LocalDateTime.of(startDate, LocalTime.MIN));
        }
        if (endDate != null) {
            commentWrapper.le(Comment::getCreatedAt, LocalDateTime.of(endDate, LocalTime.MAX));
        }
        commentWrapper.select(Comment::getUserId, Comment::getCreatedAt);
        List<Comment> comments = commentMapper.selectList(commentWrapper);

        // 查询点赞
        LambdaQueryWrapper<Like> likeWrapper = new LambdaQueryWrapper<>();
        if (startDate != null) {
            likeWrapper.ge(Like::getCreatedAt, LocalDateTime.of(startDate, LocalTime.MIN));
        }
        if (endDate != null) {
            likeWrapper.le(Like::getCreatedAt, LocalDateTime.of(endDate, LocalTime.MAX));
        }
        likeWrapper.select(Like::getUserId, Like::getCreatedAt);
        List<Like> likes = likeMapper.selectList(likeWrapper);

        // 统计用户活跃度
        Map<Long, Integer> userActivityMap = new HashMap<>();
        Map<Long, LocalDateTime> lastActiveTimeMap = new HashMap<>();

        // 发帖得5分
        for (Post post : posts) {
            userActivityMap.put(post.getUserId(), userActivityMap.getOrDefault(post.getUserId(), 0) + 5);
            updateLastActiveTime(lastActiveTimeMap, post.getUserId(), post.getCreatedAt());
        }

        // 评论得3分
        for (Comment comment : comments) {
            userActivityMap.put(comment.getUserId(), userActivityMap.getOrDefault(comment.getUserId(), 0) + 3);
            updateLastActiveTime(lastActiveTimeMap, comment.getUserId(), comment.getCreatedAt());
        }

        // 点赞得1分
        for (Like like : likes) {
            userActivityMap.put(like.getUserId(), userActivityMap.getOrDefault(like.getUserId(), 0) + 1);
            updateLastActiveTime(lastActiveTimeMap, like.getUserId(), like.getCreatedAt());
        }

        // 按活跃度排序
        List<Map<String, Object>> activeUsers = new ArrayList<>();
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));

        userActivityMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit != null ? limit : 10)
                .forEach(entry -> {
                    Long userId = entry.getKey();
                    User user = userMap.get(userId);
                    if (user != null) {
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("userId", userId);
                        userData.put("username", user.getUsername());
                        userData.put("nickname", user.getNickname());
                        userData.put("avatar", user.getAvatar());
                        userData.put("activityCount", entry.getValue());
                        userData.put("lastActiveTime", lastActiveTimeMap.get(userId));
                        activeUsers.add(userData);
                    }
                });

        result.put("activeUsers", activeUsers);

        return result;
    }

    /**
     * 更新用户最后活动时间
     */
    private void updateLastActiveTime(Map<Long, LocalDateTime> lastActiveTimeMap, Long userId, LocalDateTime activityTime) {
        LocalDateTime lastActiveTime = lastActiveTimeMap.get(userId);
        if (lastActiveTime == null || activityTime.isAfter(lastActiveTime)) {
            lastActiveTimeMap.put(userId, activityTime);
        }
    }

    @Override
    public Map<String, Object> getSystemOverview() {
        Map<String, Object> result = new HashMap<>();

        // 统计用户数量
        Long userCount = userMapper.selectCount(null);

        // 统计商品数量
        Long productCount = productMapper.selectCount(null);

        // 统计帖子数量
        Long postCount = postMapper.selectCount(null);

        // 统计评论数量
        Long commentCount = commentMapper.selectCount(null);

        // 统计订单数量和金额
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.ge(Order::getStatus, 1); // 已支付的订单
        List<Order> orders = orderMapper.selectList(orderWrapper);
        int orderCount = orders.size();
        BigDecimal orderAmount = orders.stream()
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 统计今日数据
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(today, LocalTime.MAX);

        // 今日新增用户
        LambdaQueryWrapper<User> todayUserWrapper = new LambdaQueryWrapper<>();
        todayUserWrapper.between(User::getCreatedAt, todayStart, todayEnd);
        Long todayUserCount = userMapper.selectCount(todayUserWrapper);

        // 今日新增帖子
        LambdaQueryWrapper<Post> todayPostWrapper = new LambdaQueryWrapper<>();
        todayPostWrapper.between(Post::getCreatedAt, todayStart, todayEnd);
        Long todayPostCount = postMapper.selectCount(todayPostWrapper);

        // 今日新增订单
        LambdaQueryWrapper<Order> todayOrderWrapper = new LambdaQueryWrapper<>();
        todayOrderWrapper.between(Order::getCreatedAt, todayStart, todayEnd);
        todayOrderWrapper.ge(Order::getStatus, 1); // 已支付的订单
        List<Order> todayOrders = orderMapper.selectList(todayOrderWrapper);
        int todayOrderCount = todayOrders.size();
        BigDecimal todayOrderAmount = todayOrders.stream()
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        result.put("userCount", userCount);
        result.put("productCount", productCount);
        result.put("postCount", postCount);
        result.put("commentCount", commentCount);
        result.put("orderCount", orderCount);
        result.put("orderAmount", orderAmount);
        result.put("todayUserCount", todayUserCount);
        result.put("todayPostCount", todayPostCount);
        result.put("todayOrderCount", todayOrderCount);
        result.put("todayOrderAmount", todayOrderAmount);

        return result;
    }
}
