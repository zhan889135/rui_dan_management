package com.talent.interview.utils;

import com.talent.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;

@Slf4j
public class FileOperateUtil {

    /**
     * 保存文件到本地并写入数据库
     */
    public static String saveFile(String uploadPath, String path, MultipartFile picture) {
        if (picture == null) return null;
        File folder = new File(uploadPath + File.separator + path);
        if (!folder.exists()) folder.mkdirs();

        String originalFileName = picture.getOriginalFilename();
        String filePath = folder.getAbsolutePath() + File.separator + originalFileName;

        try {
            // 删除已有文件
            File existingFile = new File(filePath);
            if (existingFile.exists() && !existingFile.delete()) {
                log.warn("文件删除失败，无法删除原有文件: " + filePath);
            }

            // 保存文件
            picture.transferTo(new File(filePath));
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }

        log.info("文件保存成功：" + "pictureFolder" + path + File.separator + originalFileName);

        return path + File.separator + originalFileName;
    }

    /**
     * 根据Path获取文件
     */
    public static void getFile(String path, HttpServletResponse response) {
        if(StringUtils.isNotEmpty(path)){
            path = path.replace("/", File.separator);
            path = path.replace("\\", File.separator);
        }
        File file = new File(path);

        if (!file.exists()) {
            log.error("fullPath: {}", path);
            return;
        }

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileName = file.getName();
            String mimeType = Files.probeContentType(file.toPath());

            // 设置响应头，指示文件类型和文件名
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.setContentType(mimeType != null ? mimeType : "application/octet-stream");
            response.setStatus(HttpStatus.OK.value());

            // 将文件流写入响应
            IOUtils.copy(fileInputStream, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            log.error("文件读取失败", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * 获取音频文件时长
     */
    public static double getWavDurationInSeconds(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            log.warn("音频文件不存在: {}", filePath);
            return 0.0;
        }

        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(file);
            AudioFormat format = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            return (frames + 0.0) / format.getFrameRate();
        } catch (Exception e) {
            log.error("获取 WAV 音频时长失败: {}", filePath, e);
            return 0.0;
        } finally {
            if (audioInputStream != null) {
                try {
                    audioInputStream.close();
                } catch (Exception e) {
                    log.warn("关闭音频输入流失败: {}", filePath, e);
                }
            }
        }
    }

}
