package com.gs.community.controller;

import com.gs.community.cache.HotTagCache;
import com.gs.community.dto.PaginationDTO;
import com.gs.community.dto.QuestionDTO;
import com.gs.community.model.UserAccount;
import com.gs.community.service.QuestionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class IndexController {
    @Autowired(required = false)
    private QuestionService questionService;
    @Autowired
    private HotTagCache hotTagCache;

    @GetMapping("/")
    public String index(HttpServletRequest request,
                        Model model,
                        @RequestParam(name = "page", defaultValue = "1") Integer page,
                        @RequestParam(name = "size", defaultValue = "15") Integer size,
                        @RequestParam(name = "search", required = false) String search,
                        @RequestParam(name = "tag", required = false) String tag,
                        @RequestParam(name = "sort", required = false) String sort) {
        UserAccount userAccount = (UserAccount) request.getSession().getAttribute("userAccount");
        List<QuestionDTO> topQuestions = questionService.listTop(search, tag, sort);
        PaginationDTO pagination = questionService.list(page, size, search, tag, sort,userAccount);
        List<String> hotTags = hotTagCache.getHots();
        model.addAttribute("pagination", pagination);
        model.addAttribute("search", search);
        model.addAttribute("hotTags", hotTags);
        model.addAttribute("tag", tag);
        model.addAttribute("sort", sort);
        model.addAttribute("topQuestions", topQuestions);
        return "index";
    }
}
