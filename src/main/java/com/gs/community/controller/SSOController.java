package com.gs.community.controller;

import com.gs.community.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class SSOController {
    @Value("${vaptcha.vid}")
    private String vaptcha_vid;

    @RequestMapping("/sso/{action}")
    public String auth(HttpServletRequest request, HttpServletResponse response,
                       @PathVariable(name = "action") String action, Model model) {
        User user = (User) request.getSession().getAttribute("user");
        if (user != null) {
            return "redirect:/";
        }
        model.addAttribute("vaptcha_vid", vaptcha_vid);
        if ("login".equals(action)) {
            model.addAttribute("section", "login");
            model.addAttribute("sectionName", "登录");
        } else if ("register".equals(action)) {
            model.addAttribute("section", "register");
            model.addAttribute("sectionName", "注册");
        } else {
            return "redirect:/";
        }
        return "sso";
    }
}
