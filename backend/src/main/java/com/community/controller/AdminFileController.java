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
 * 管理员文件上传控制器
 */
@RestController
@RequestMapping("/admin/file")
@RequiredArgsConstructor
public class AdminFileController {

    private final FileUtil fileUtil;

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
