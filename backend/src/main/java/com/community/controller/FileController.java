package com.community.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.community.common.result.R;
import com.community.common.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileUtil fileUtil;

    /**
     * 上传头像
     */
    @PostMapping("/upload/avatar")
    public R<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String url = fileUtil.upload(file, "avatar");
        return R.ok(url, "上传成功");
    }

    /**
     * 上传商品图片
     */
    @PostMapping("/upload/product")
    public R<String> uploadProductImage(@RequestParam("file") MultipartFile file) {
        String url = fileUtil.upload(file, "product");
        return R.ok(url, "上传成功");
    }

    /**
     * 上传帖子图片
     */
    @PostMapping("/upload/post")
    public R<String> uploadPostImage(@RequestParam("file") MultipartFile file) {
        String url = fileUtil.upload(file, "post");
        return R.ok(url, "上传成功");
    }

    /**
     * 上传评价图片
     */
    @PostMapping("/upload/review")
    public R<String> uploadReviewImage(@RequestParam("file") MultipartFile file) {
        String url = fileUtil.upload(file, "review");
        return R.ok(url, "上传成功");
    }

    /**
     * 通用文件上传接口（管理员接口）
     */
    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file, @RequestParam(value = "type", defaultValue = "common") String type) {
        // 校验权限
        StpUtil.checkRole("admin");

        // 上传文件
        String url = fileUtil.upload(file, type);
        return R.ok(url, "上传成功");
    }
}
