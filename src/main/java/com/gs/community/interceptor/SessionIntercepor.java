package com.gs.community.interceptor;

import com.gs.community.enums.AdPosEnum;
import com.gs.community.mapper.UserAccountMapper;
import com.gs.community.mapper.UserInfoMapper;
import com.gs.community.mapper.UserMapper;
import com.gs.community.model.*;
import com.gs.community.service.AdService;
import com.gs.community.service.NavService;
import com.gs.community.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

@Service
public class SessionIntercepor implements HandlerInterceptor {
    @Autowired(required = false)
    private UserMapper userMapper;
    @Autowired
    private UserAccountMapper userAccountMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private NavService navService;
    @Autowired
    private AdService adService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        设置context级别的属性 设置广告
        for (AdPosEnum adPos : AdPosEnum.values()) {
            request.getServletContext().setAttribute(adPos.name(), adService.list(adPos.name()));
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length != 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token")) {
                    String token = cookie.getValue();
                    UserExample userExample = new UserExample();
                    userExample.createCriteria().andTokenEqualTo(token);
                    List<User> users = userMapper.selectByExample(userExample);
                    if (users.size() != 0) {
                        HttpSession session = request.getSession();
                        User user = users.get(0);
                        UserAccountExample userAccountExample = new UserAccountExample();
                        userAccountExample.createCriteria().andUserIdEqualTo(user.getId());
                        List<UserAccount> userAccounts = userAccountMapper.selectByExample(userAccountExample);
                        UserAccount userAccount = userAccounts.get(0);
                        UserInfoExample userInfoExample = new UserInfoExample();
                        userInfoExample.createCriteria().andUserIdEqualTo(user.getId());
                        List<UserInfo> userInfos = userInfoMapper.selectByExample(userInfoExample);
                        UserInfo userInfo = userInfos.get(0);
                        session.setAttribute("user", user);
                        session.setAttribute("userAccount",userAccount);
                        session.setAttribute("userInfo",userInfo);
                        Integer unreadCount = notificationService.unreadCount(user.getId());
                        session.setAttribute("unreadCount", unreadCount);
                        UserExample example = new UserExample();
                        user.setGmtModified(System.currentTimeMillis());
                        example.createCriteria()
                                .andIdEqualTo(user.getId());
                        userMapper.updateByExampleSelective(user, example);
                    }
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
