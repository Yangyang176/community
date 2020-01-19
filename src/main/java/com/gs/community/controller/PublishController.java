package com.gs.community.controller;

import com.gs.community.cache.TagCache;
import com.gs.community.dto.QuestionDTO;
import com.gs.community.model.Question;
import com.gs.community.model.User;
import com.gs.community.service.QuestionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class PublishController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/publish/{id}")
    public String edit(@PathVariable(name = "id") Integer id,
                       Model model) {
        QuestionDTO questionDTO = questionService.getById(id);
        model.addAttribute("title", questionDTO.getTitle());
        model.addAttribute("description", questionDTO.getDescription());
        model.addAttribute("tag", questionDTO.getTag());
        model.addAttribute("id", questionDTO.getId());
        model.addAttribute("tagDTOs",TagCache.get());
        return "publish";
    }
    @GetMapping("/publish")
    public String publish(Model model) {
        model.addAttribute("tagDTOs",TagCache.get());
        return "publish";
    }
    @PostMapping("/publish")
    public String doPublish(
            @RequestParam("id") Integer id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("tag") String tag,
            HttpServletRequest request,
            Model model){

        model.addAttribute("title",title);
        model.addAttribute("description",description);
        model.addAttribute("tag",tag);
        model.addAttribute("tagDTOs",TagCache.get());
        if(StringUtils.isBlank(title)){
            model.addAttribute("error","标题不能为空！");
            return "publish";
        }
        if(StringUtils.isBlank(description)){
            model.addAttribute("error","问题补充不能为空！");
            return "publish";
        }
        if(StringUtils.isBlank(tag)){
            model.addAttribute("error","标签不能为空！");
            return "publish";
        }
        String invalid = TagCache.filterInvalid(tag);
        if (!StringUtils.isBlank(invalid)){
            model.addAttribute("error","输入非法标签" + invalid);
            return "publish";
        }
        User user = (User) request.getSession().getAttribute("user");
        if (user == null){
            model.addAttribute("error","用户未登录");
            return "publish";
        }
        Question question = new Question();
        question.setId(id);
        question.setTitle(title);
        question.setDescription(description);
        question.setTag(tag);
        question.setCreator(user.getId());
        questionService.createOrUpdate(question);
        return "redirect:/";
    }
}
