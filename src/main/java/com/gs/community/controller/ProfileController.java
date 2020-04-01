package com.gs.community.controller;

import com.gs.community.dto.PaginationDTO;
import com.gs.community.exception.CustomizeErrorCode;
import com.gs.community.exception.CustomizeException;
import com.gs.community.model.User;
import com.gs.community.service.NotificationService;
import com.gs.community.service.QuestionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ProfileController {
    @Autowired(required = false)
    private QuestionService questionService;
    @Autowired
    private NotificationService notificationService;

    @Value("${vaptcha.vid}")
    private String vaptcha_vid;

    @GetMapping("/profile/{action}")
    public String profile(HttpServletRequest request,
                          @PathVariable(name = "action") String action,
                          Model model,
                          @RequestParam(name = "page", defaultValue = "1") Integer page,
                          @RequestParam(name = "size", defaultValue = "10") Integer size) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            throw new CustomizeException(CustomizeErrorCode.NO_LOGIN);
        }
        if ("questions".equals(action)) {
            model.addAttribute("section", "questions");
            model.addAttribute("sectionName", "我的问题");
            PaginationDTO pagination = questionService.listByUserId(user.getId(), page, size);
            model.addAttribute("pagination", pagination);
        }
        if ("notifies".equals(action)) {
            model.addAttribute("section", "notifies");
            model.addAttribute("sectionName", "最新通知");
            PaginationDTO pagination = notificationService.listByUserId(user.getId(), page, size);
            model.addAttribute("pagination", pagination);
        }
        if ("likes".equals(action)) {
            PaginationDTO paginationDTO = questionService.listByThumbExample(user.getId(), page, size, "likes");
            model.addAttribute("section", "likes");
            model.addAttribute("sectionName", "我的收藏");
            model.addAttribute("pagination", paginationDTO);
        }
        return "profile";
    }
    @GetMapping("/user/set/{action}")
    public String getSetPage(HttpServletRequest request,
                             @PathVariable(name = "action") String action,
                             Model model) {
        User user = (User) request.getSession().getAttribute("user");
        if(user == null) {
            throw new CustomizeException(CustomizeErrorCode.NO_LOGIN);
        }
        if("info".equals(action)|| StringUtils.isBlank(action)){
            model.addAttribute("section", "info");
            model.addAttribute("sectionName", "我的资料");
            model.addAttribute("sectionInfo", "绑定邮箱账号后，您可以使用邮箱账号登录本站\n");
            return "user/set";
        }
        if("account".equals(action)){
            model.addAttribute("section", "account");
            model.addAttribute("sectionName", "绑定/更新邮箱账号");
            model.addAttribute("sectionInfo", "绑定邮箱账号后，您可以使用邮箱账号登录本站\n");
            model.addAttribute("vaptcha_vid", vaptcha_vid);
            return "user/account";
        }
        return "user/set";
    }
    @GetMapping("/user/message")
    public String messeage(HttpServletRequest request,
                           Model model,
                           @RequestParam(name = "page",defaultValue = "1")Integer page,
                           @RequestParam(name = "size",defaultValue = "5")Integer size){
        User user = (User)request.getSession().getAttribute("user");

        if(user==null){
            throw new CustomizeException(CustomizeErrorCode.NO_LOGIN);
        }
        PaginationDTO paginationDTO = notificationService.listByUserId(user.getId(), page, size);
        model.addAttribute("section", "message");
        model.addAttribute("pagination", paginationDTO);
        return "user/message";
    }
    @GetMapping("/user/question/{action}")
    public String p(HttpServletRequest request,
                    @PathVariable(name = "action") String action,
                    Model model,
                    @RequestParam(name = "page",defaultValue = "1")Integer page,
                    @RequestParam(name = "size",defaultValue = "10")Integer size){
        User user = (User)request.getSession().getAttribute("user");

        if(user==null){
            throw new CustomizeException(CustomizeErrorCode.NO_LOGIN);
        }
        if("myQuestions".equals(action)){
            model.addAttribute("section", "myQuestions");
            model.addAttribute("sectionName", "我的问题");
            PaginationDTO pagination = questionService.listByUserId(user.getId(), page, size);
            model.addAttribute("pagination",pagination);
        }
        if("likes".equals(action)){
            PaginationDTO paginationDTO = questionService.listByThumbExample(user.getId(), page, size,"likes");
            model.addAttribute("section", "likes");
            model.addAttribute("pagination", paginationDTO);
            model.addAttribute("sectionName", "我的收藏");
        }

        return "user/question";
    }
}
