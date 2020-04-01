package com.gs.community.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gs.community.dto.CommentDTO;
import com.gs.community.dto.QuestionDTO;
import com.gs.community.enums.CommentTypeEnum;
import com.gs.community.exception.CustomizeErrorCode;
import com.gs.community.exception.CustomizeException;
import com.gs.community.model.Question;
import com.gs.community.model.User;
import com.gs.community.model.UserAccount;
import com.gs.community.service.CommentService;
import com.gs.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class QuestionController {

    @Autowired
    private QuestionService questionService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private Environment env;

    @GetMapping("/question/{id}")
    public String question(@PathVariable(name = "id") Integer id, HttpServletRequest request,
                           Model model) {
        User user = (User) request.getSession().getAttribute("user");
        Integer userId;
        if (user == null) userId = 0;
        else userId = user.getId();
        QuestionDTO questionDTO = questionService.getById(id, userId);
        UserAccount userAccount = (UserAccount) request.getSession().getAttribute("userAccount");
        if (userAccount == null) {
            if (questionDTO.getPermission() != 0) {
                model.addAttribute("rsTitle", "您无权访问！");
                model.addAttribute("rsMessage", "该帖设置了权限，游客不可见，快去登陆吧");
                return "result";
            }
        } else if (questionDTO.getPermission() > userAccount.getGroupId() && questionDTO.getCreator() != userAccount.getUserId()){
            model.addAttribute("rsTitle", "您无权访问！");
            model.addAttribute("rsMessage", "该贴仅作者和"+env.getProperty("user.group.r"+questionDTO.getPermission())+"及以上的用户可以访问，快去多多发帖提升等级或者开通VIP畅享全站！");
            return "result";
        }
        List<QuestionDTO> relatedQuestions = questionService.selectRelated(questionDTO);
        List<CommentDTO> comments = commentService.listByTargetId(id, CommentTypeEnum.QUESTION);
        //增加浏览数
        questionService.incView(id);
        model.addAttribute("question", questionDTO);
        model.addAttribute("comments", comments);
        model.addAttribute("relatedQuestions", relatedQuestions);
        return "question";
    }
    @PostMapping("/question/del/id")
    @ResponseBody
    public Map<String,Object> delQuestionById(HttpServletRequest request,
                                              @RequestParam(name = "id",defaultValue = "0") Integer id) {

        User user = (User) request.getSession().getAttribute("user");
        UserAccount userAccount = (UserAccount) request.getSession().getAttribute("userAccount");
        if (user == null||userAccount==null) {
            throw new CustomizeException(CustomizeErrorCode.NO_LOGIN);
        }
        Map<String,Object> map  = new HashMap<>();
        if(id==null||id==0) {
            map.put("code",500);
            map.put("msg","妈呀，出问题啦！");
        }
        else{

            int c = questionService.delQuestionById(user.getId(),userAccount.getGroupId(),id);
            if(c==0) {
                map.put("code",500);
                map.put("msg","哎呀，该贴已删除或您无权删除！");
            }
            else {
                map.put("code",200);
                map.put("msg","恭喜您，成功删除"+c+"条帖子！");
            }
        }
        return map;

    }
    @PostMapping("/question/set/id")
    @ResponseBody
    public Map<String,Object> setQuestionById(HttpServletRequest request,
                                              @RequestParam(name = "id",defaultValue = "0") Integer id
            ,@RequestParam(name = "rank",required = false) Integer rank
            ,@RequestParam(name = "field",required = false) String field
            ,@RequestParam(name = "json",required = false) String json) {

        User user = (User) request.getSession().getAttribute("user");
        UserAccount userAccount = (UserAccount) request.getSession().getAttribute("userAccount");
        if (user == null||userAccount==null) {
            throw new CustomizeException(CustomizeErrorCode.NO_LOGIN);
        }
        Map<String,Object> map  = new HashMap<>();
        if(id==null||id==0||field==null) {
            map.put("code",500);
            map.put("msg","妈呀，出问题啦！");
            return map;
        }
        Question question = questionService.getQuestionById(id);
        if(!(question.getCreator()==user.getId()||userAccount.getGroupId()>=18)){
            map.put("code",500);
            map.put("msg","您无权进行此操作！");
            return map;
        }
        if("stick".equals(field)){
            if(rank==1) question.setStatus(question.getStatus()|2);
            if(rank==0) question.setStatus(question.getStatus()&253);
        }
        if("essence".equals(field)){
            if(rank==1) question.setStatus(question.getStatus()|1);
            if(rank==0) question.setStatus(question.getStatus()&254);
        }
        if("promote".equals(field)){
            question.setGmtLatestComment(System.currentTimeMillis());
        }
        if("admin".equals(field)){
            JSONObject obj= JSON.parseObject(json);
            question.setColumn2(obj.getInteger("column2"));
            question.setPermission(obj.getInteger("permission"));
        }
        if(questionService.updateQuestion(question)==1){
            map.put("code",200);
            map.put("msg","恭喜您，设置成功！");
        }
        return map;

    }
}
