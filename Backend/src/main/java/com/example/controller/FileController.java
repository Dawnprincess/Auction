package com.example.controller;

import cn.hutool.core.io.FileUtil;
import com.example.common.Result;
import com.example.exception.CustomException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/files")
public class FileController {

    private static final String filePath = System.getProperty("user.dir") + "/files/";
    
    @PostMapping("/upload")
    public Result upload(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "type", required = false, defaultValue = "other") String type
    ) {
        if (file == null || file.isEmpty()) {
            throw new CustomException("400", "上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty() || FileUtil.isDirectory(originalFilename)) {
            throw new CustomException("400", "无效的文件名");
        }

        //给文件名添加时间戳，避免重复
        String fileName = System.currentTimeMillis() + "_" + originalFilename;
        
        String subDir = type.equals("avatar") ? "avatar/" : 
                       type.equals("goods") ? "goods/" : 
                       "other/";
        
        String dirPath = filePath + subDir;
        
        FileUtil.mkdir(dirPath);
        
        String realPath = dirPath + fileName;
        try{
            FileUtil.writeBytes(file.getBytes(), realPath);
        }catch(IOException e) {
            e.printStackTrace();
            throw new CustomException("500", "文件上传失败");
        }
        
        String url = "http://localhost:8080/files/download/" + subDir + fileName;
        return Result.success(url);
    }

    @GetMapping("/download/{path}/{filename}")
    public void download(@PathVariable String path, @PathVariable String filename, HttpServletResponse response) throws IOException {
        try{
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            response.setContentType("application/octet-stream");
            OutputStream os = response.getOutputStream();
            String realPath = filePath + path + "/" + filename;
            byte[] bytes = FileUtil.readBytes(realPath);
            os.write(bytes);
            os.flush();
        }catch(Exception e) {
            e.printStackTrace();
            throw new CustomException("500", "文件下载失败");
        }
    }
}
