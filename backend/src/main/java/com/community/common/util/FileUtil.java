package com.community.common.util;

import com.community.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传工具类
 */
@Component
public class FileUtil {

    @Value("${community.file-upload-path}")
    private String uploadPath;

    /**
     * 上传文件
     *
     * @param file     文件
     * @param fileType 文件类型（avatar-头像，product-商品图片，post-帖子图片）
     * @return 文件访问路径
     */
    public String upload(MultipartFile file, String fileType) {
        // 校验文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 校验文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.lastIndexOf(".") == -1) {
            throw new BusinessException("文件格式不正确");
        }

        // 获取文件后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (!isAllowedExtension(suffix)) {
            throw new BusinessException("文件格式不正确，只允许上传jpg、jpeg、png、gif格式的图片");
        }

        // 生成文件名
        String fileName = UUID.randomUUID().toString().replaceAll("-", "") + suffix;

        // 按日期生成目录
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String path = uploadPath + fileType + "/" + datePath + "/";
        File directory = new File(path);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new BusinessException("创建目录失败");
            }
        }

        // 保存文件
        try {
            File dest = new File(path + fileName);
            file.transferTo(dest);
            return "/upload/" + fileType + "/" + datePath + "/" + fileName;
        } catch (IOException e) {
            throw new BusinessException("上传文件失败", e);
        }
    }

    /**
     * 校验文件后缀
     */
    private boolean isAllowedExtension(String extension) {
        String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".gif"};
        for (String allowedExtension : allowedExtensions) {
            if (allowedExtension.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
}
