package com.gs.community.controller;

import com.gs.community.dto.NotificationDTO;
import com.gs.community.enums.NotificationTypeEnum;
import com.gs.community.exception.CustomizeErrorCode;
import com.gs.community.exception.CustomizeException;
import com.gs.community.model.User;
import com.gs.community.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/notification/{id}")
    public String profile(HttpServletRequest request,
                          @PathVariable(name = "id") Integer id) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            throw new CustomizeException(CustomizeErrorCode.NO_LOGIN);
        }
        NotificationDTO notificationDTO = notificationService.read(id, user);
        if (NotificationTypeEnum.REPLY_COMMENT.getType() == notificationDTO.getType()
                || NotificationTypeEnum.REPLY_QUESTION.getType() == notificationDTO.getType()
                || NotificationTypeEnum.LIKE_COMMENT.getType() == notificationDTO.getType()
                || NotificationTypeEnum.LIKE_QUESTION.getType() == notificationDTO.getType()) {
            return "redirect:/question/" + notificationDTO.getOuterId();
        } else {
            return "redirect:/";
        }
    }
    @PostMapping("/notification/readAll")
    @ResponseBody
    public Map<String,Object> readAllNotifications(HttpServletRequest request,
                                                   @RequestParam(name = "all",defaultValue = "0") Integer all) {

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            throw new CustomizeException(CustomizeErrorCode.NO_LOGIN);
        }
        Map<String,Object> map  = new HashMap<>();
        if(all==null||all!=1) {
            map.put("code",500);
            map.put("msg","妈呀，出问题啦！");
        }
        else if(all==1){
            int c = notificationService.readAllByUserId(user.getId());
            if(c==0){
                map.put("msg","哎呀，没有您未读的消息呀！");
                map.put("code",500);
            }
            else{
                map.put("msg","恭喜您，成功已读"+c+"条消息！");
                map.put("code",200);
            }
        }
        return map;

    }
    @PostMapping("/notification/removeAll")
    @ResponseBody
    public Map<String,Object> removeAllNotifications(HttpServletRequest request,
                                                     @RequestParam(name = "all",defaultValue = "0") Integer all) {

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            throw new CustomizeException(CustomizeErrorCode.NO_LOGIN);
        }
        Map<String,Object> map  = new HashMap<>();
        if(all==null||all!=1) {
            map.put("code",500);
            map.put("msg","妈呀，出问题啦！");
        }
        else if(all==1){
            int c = notificationService.removeAllByUserId(user.getId());
            if(c==0) {
                map.put("code",500);
                map.put("msg","哎呀，没有需要删除的消息呀！");
            }
            else {
                map.put("code",200);
                map.put("msg","恭喜您，成功删除"+c+"条消息！");
            }
        }
        return map;
    }
    @PostMapping("/notification/remove/id")
    @ResponseBody
    public Map<String,Object> removeNotificationById(HttpServletRequest request,
                                                     @RequestParam(name = "id",defaultValue = "0") Integer id) {

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            throw new CustomizeException(CustomizeErrorCode.NO_LOGIN);
        }
        Map<String,Object> map  = new HashMap<>();
        if(id==null||id==0) {
            map.put("code",500);
            map.put("msg","妈呀，出问题啦！");
        }
        else if(id>0){
            int c = notificationService.removeById(id);
            if(c==0) {
                map.put("code",500);
                map.put("msg","哎呀，该消息已经被删除过了呀！");
            }
            else {
                map.put("code",200);
                map.put("msg","恭喜您，成功删除"+c+"条消息！");
            }
        }
        return map;
    }
}
