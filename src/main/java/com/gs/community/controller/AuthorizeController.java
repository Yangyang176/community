package com.gs.community.controller;

import com.gs.community.dto.AccessTokenDTO;
import com.gs.community.dto.BaiduAccessTokenDTO;
import com.gs.community.dto.BaiduUserDTO;
import com.gs.community.dto.GithubUser;
import com.gs.community.model.User;
import com.gs.community.model.UserInfo;
import com.gs.community.provider.BaiduProvider;
import com.gs.community.provider.GithubProvider;
import com.gs.community.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
public class AuthorizeController {
    @Autowired
    private GithubProvider githubProvider;
    @Value("${github.client.id}")
    private String clientId;
    @Value("${github.client.secret}")
    private String clientSecret;
    @Value("${github.redirect.uri}")
    private String redirectUri;
    @Value("${baidu.client.id}")
    private String baiduClientId;
    @Value("${baidu.client.secret}")
    private String baiduClientSecret;
    @Value("${baidu.redirect.uri}")
    private String baiduRedirectUri;
    @Autowired
    private UserService userService;
    @Autowired
    private BaiduProvider baiduProvider;

    @GetMapping("/callback")
    public String callback(@RequestParam(name = "code") String code,
                           @RequestParam(name = "state") String state,
                           HttpServletRequest request,
                           HttpServletResponse response) {
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
        accessTokenDTO.setClient_id(clientId);
        accessTokenDTO.setClient_secret(clientSecret);
        accessTokenDTO.setCode(code);
        accessTokenDTO.setRedirect_uri(redirectUri);
        accessTokenDTO.setState(state);
        String accessToken = githubProvider.getAccessToken(accessTokenDTO);
        GithubUser githubUser = githubProvider.getUser(accessToken);
        if (githubUser != null && githubUser.getId() != null) {
            //登陆成功 写cookie session
            User user = new User();
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            user.setName(githubUser.getName());
            user.setAccountId(String.valueOf(githubUser.getId()));
            user.setAvatarUrl(githubUser.getAvatar_url());
            userService.createOrUpdate(user);
            Cookie cookie = getCookie(token);
            response.addCookie(cookie);
            return "redirect:/";
        } else {
            //登陆失败 重新登陆
            return "redirect:/";
        }
    }

    @GetMapping("/callbackbaidu")
    public String callbackBaidu(@RequestParam(name = "code") String code,
                                @RequestParam(name = "state") String state,
                                HttpServletResponse response,
                                HttpServletRequest request,
                                Model model) {

        BaiduAccessTokenDTO baiduAccessTokenDTO = new BaiduAccessTokenDTO();
        baiduAccessTokenDTO.setGrant_type("authorization_code");
        baiduAccessTokenDTO.setCode(code);
        baiduAccessTokenDTO.setClient_id(baiduClientId);
        baiduAccessTokenDTO.setClient_secret(baiduClientSecret);
        baiduAccessTokenDTO.setRedirect_uri(baiduRedirectUri);
        // System.out.println("code是"+code+"state是"+state);
        String accessToken = baiduProvider.getAccessToken(baiduAccessTokenDTO);
        BaiduUserDTO baiduUser = baiduProvider.getUser(accessToken);
        // System.out.println(baiduUser.getUsername()+baiduUser.getUserid());
        if (baiduUser != null && baiduUser.getUserid() != null) {
            User user = new User();
            UserInfo userInfo = new UserInfo();
            String token = UUID.randomUUID().toString();
            user.setName(baiduUser.getUsername());
            user.setToken(token);
            user.setBaiduAccountId("" + baiduUser.getUserid());
            user.setAvatarUrl("http://tb.himg.baidu.com/sys/portrait/item/" + baiduUser.getPortrait());

            BeanUtils.copyProperties(baiduUser,userInfo);
            // System.out.println("生日:"+userInfo.getBirthday()+"realname:"+userInfo.getRealname());
            // userInfo.setUserId();
            User loginuser = (User) request.getSession().getAttribute("user");
            int flag = userService.createOrUpdateBaidu(user,loginuser,userInfo);
            if(flag == 1) {//创建百度账号
                model.addAttribute("rsTitle", "成功啦！！！");
                model.addAttribute("rsMessage", "您已使用百度账号成功注册本站！");
                /*model.addAttribute("aouth", "Baidu");
                model.addAttribute("aouthName", "账户中心-绑定百度账号");
                request.getSession().setAttribute("userInfoTemp",userInfo);
                request.getSession().setAttribute("userTemp",user);
                return "oauth";*/
            }
            if (flag==2) {
                model.addAttribute("rsTitle", "成功啦！！！");
                model.addAttribute("rsMessage", "您的账号已成功绑定/更新百度账号！");
            }
            if (flag==3) {
                model.addAttribute("rsTitle", "成功啦！！！");
                model.addAttribute("rsMessage", "您已使用百度账号成功登陆本站！");
            }
            Cookie cookie=getCookie(token);
            response.addCookie(cookie);
            return "result";

        }else {
            // 登录失败，重新登录
            return "redirect:/";
        }
    }

    private Cookie getCookie(String token) {
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);//通过js脚本是无法获取到cookie的信息的。防止XSS攻击。
        cookie.setMaxAge(60 * 60 * 24 * 3 * 1);//缩短为三天
        return cookie;
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response) {
        request.getSession().removeAttribute("user");
        Cookie cookie = new Cookie("token", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/";
    }
}
