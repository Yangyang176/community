package com.gs.community.controller;

import com.gs.community.dto.ResultDTO;
import com.gs.community.exception.CustomizeErrorCode;
import com.gs.community.exception.CustomizeException;
import com.gs.community.model.User;
import com.gs.community.service.UserService;
import com.gs.community.util.JavaMailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
public class MailController {
    @Autowired
    private UserService userService;

    @GetMapping("/profile/regmail")
    public String hello(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            throw new CustomizeException(CustomizeErrorCode.NO_LOGIN);
        }
        return "user/regmail";
    }

    @ResponseBody//@ResponseBody返回json格式的数据
    @RequestMapping(value = "/mail/getMailCode", method = RequestMethod.GET)
    public Object getMailCode(@RequestParam("username") String username,
                              @RequestParam("mail") String mail){
        //  System.out.println("mail:"+mail);
        // TODO 自动生成的方法存根
        try {
            JavaMailUtils.setUserName(username);
            JavaMailUtils.setReceiveMailAccount(mail);
            JavaMailUtils.send();
            return ResultDTO.okOf(JavaMailUtils.code);
        } catch (Exception e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
            System.out.println(e.getMessage());
            return ResultDTO.errorOf(CustomizeErrorCode.SEND_MAIL_FAILED);
        }

    }

    @ResponseBody//@ResponseBody返回json格式的数据
    @RequestMapping(value = "/mail/submitMail", method = RequestMethod.GET)
    public Object submitMail(@RequestParam("id") String id,
                             @RequestParam("mail") String mail){
        //  System.out.println("mail:"+mail);
        // TODO 自动生成的方法存根
        return userService.updateUserMailById(id,mail);
    }

    @ResponseBody//@ResponseBody返回json格式的数据
    @RequestMapping(value = "/mail/registerOrLoginWithMail", method = RequestMethod.GET)
    public Object registerOrLoginWithMail(
            @RequestParam("mail") String mail,
            HttpServletResponse response){
        //  System.out.println("mail:"+mail);
        String token = UUID.randomUUID().toString();
        //  Cookie cookie = new Cookie("token", token);
        // cookie.setMaxAge(60 * 60 * 24 * 30 * 6);
        //  response.addCookie(cookie);
        // TODO 自动生成的方法存根
        return userService.registerOrLoginWithMail(mail,token);
    }

    @RequestMapping(value = "/registerorLoginWithMailisOk", method = RequestMethod.GET)
    public String returnToken(Model model,
                              @RequestParam(name = "token")String token,
                              HttpServletResponse response){
        //  System.out.println("mail:"+mail);
        // TODO 自动生成的方法存根
        model.addAttribute("rsTitle", "成功啦！！！");
        model.addAttribute("rsMessage", "您已成功注册/登陆本站！");
        Cookie cookie = new Cookie("token", token);
        cookie.setMaxAge(60 * 60 * 24 * 30 * 6);
        response.addCookie(cookie);
        return "result";
    }
}
