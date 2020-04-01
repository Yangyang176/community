package com.gs.community.controller;

import com.gs.community.dto.FileDTO;
import com.gs.community.model.User;
import com.gs.community.provider.UCloudProvider;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class FileController {
    @Autowired
    private UCloudProvider uCloudProvider;

    @ResponseBody
    @RequestMapping("/file/upload")
    public FileDTO upload(HttpServletRequest request) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFile("editormd-image-file");
        try {
            String fileName = uCloudProvider.upload(file.getInputStream(), file.getContentType(), file.getOriginalFilename());
            FileDTO fileDTO = new FileDTO();
            fileDTO.setSuccess(1);
            fileDTO.setUrl(fileName);
            return fileDTO;
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileDTO fileDTO = new FileDTO();
        fileDTO.setSuccess(1);
        fileDTO.setUrl("/images/wechat.png");
        return fileDTO;
    }

    //人脸头像上传接口
    @PostMapping("/file/avatar")
    @ResponseBody
    public Map<String, Object> uploadAvatar(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        Map<String, Object> map = new HashMap<>();
        try {
            User user = (User) request.getSession().getAttribute("user");
            if (user == null) {
                map.put("status", 500);
                map.put("msg", "请登录后再上传图片哦");
                map.put("url", null);
                return map;
            }
            InputStream inputStream;
            String contentType;
            String fileName;
            inputStream = file.getInputStream();
            contentType = file.getContentType();
            fileName = getFileName(contentType);
//            Long contentLength = file.getSize();
            //System.out.println("原始名字"+file.getOriginalFilename()+"类型："+file.getContentType()+"名字："+"upload/user/"+user.getId()+"/"+filename);
            String url = uCloudProvider.uploadAvatar(inputStream, contentType, fileName);
//            String url = uCloudProvider.uploadAvatar2(inputStream,contentType,user,fileName,contentLength);

            map.put("status", 0);
            map.put("msg", "");
            map.put("url", url);
            // System.out.println(map);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", 500);
            map.put("msg", "上传失败");
            map.put("url", null);
            return map;
        }
    }

    public String getFileName(String contentType) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStr = sdf.format(new Date());
        String[] ss = contentType.split("/");
        String str = RandomStringUtils.random(5,
                "abcdefghijklmnopqrstuvwxyz1234567890");
        String name = timeStr + "_" + str + "." + ss[1];
        return name;
    }
}
