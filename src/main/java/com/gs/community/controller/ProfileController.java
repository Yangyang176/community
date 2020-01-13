package com.gs.community.controller;

import com.gs.community.dto.PaginationDTO;
import com.gs.community.model.User;
import com.gs.community.service.NotificationService;
import com.gs.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
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
    @GetMapping("/profile/{action}")
    public String profile(HttpServletRequest request,
                          @PathVariable(name = "action") String action,
                          Model model,
                          @RequestParam(name = "page", defaultValue = "1") Integer page,
                          @RequestParam(name = "size", defaultValue = "10") Integer size) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/";
        }
        if ("questions".equals(action)) {
            model.addAttribute("section", "questions");
            model.addAttribute("sectionName", "我的问题");
            PaginationDTO pagination = questionService.listByUserId(user.getId(), page, size);
            model.addAttribute("pagination", pagination);
        }
        if ("replies".equals(action)) {
            model.addAttribute("section", "replies");
            model.addAttribute("sectionName", "最新回复");
            PaginationDTO pagination = notificationService.listByUserId(user.getId(), page, size);
            model.addAttribute("pagination", pagination);
        }
        return "profile";
    }
}
