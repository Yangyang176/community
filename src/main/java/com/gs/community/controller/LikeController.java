package com.gs.community.controller;

import com.gs.community.dto.ResultDTO;
import com.gs.community.dto.ThumbCreateDTO;
import com.gs.community.exception.CustomizeErrorCode;
import com.gs.community.exception.CustomizeException;
import com.gs.community.model.Thumb;
import com.gs.community.model.User;
import com.gs.community.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController {

    @Autowired
    private LikeService likeService;

    @ResponseBody //返回json格式数据
    @RequestMapping(value = "/like", method = RequestMethod.POST)
    public Object like(@RequestBody ThumbCreateDTO thumbCreateDTO,//@RequestBody接受json格式的数据
                       HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return ResultDTO.errorOf(CustomizeErrorCode.NO_LOGIN);
        }
        if (thumbCreateDTO == null) {
            return ResultDTO.errorOf(CustomizeErrorCode.CAN_NOT_LIKE);
        }
        Thumb thumb = new Thumb();
        thumb.setTargetId(thumbCreateDTO.getTargetId());
        thumb.setType(thumbCreateDTO.getType());
        thumb.setGmtModified(System.currentTimeMillis());
        thumb.setGmtCreate(System.currentTimeMillis());
        thumb.setLiker(user.getId());
        int result = likeService.insert(thumb, user);
        if (result == 0 && thumbCreateDTO.getType() == 2)
            return ResultDTO.okOf("点赞成功！");
        if (result == 0 && thumbCreateDTO.getType() == 1)
            return ResultDTO.okOf("收藏成功！");
        if (result == 2022 && thumbCreateDTO.getType() == 2)
            return ResultDTO.errorOf(CustomizeErrorCode.CAN_NOT_LIKE_AGAIN);
        if (result == 2023 && thumbCreateDTO.getType() == 1)
            return ResultDTO.errorOf(CustomizeErrorCode.CAN_NOT_LIKE_QUESTION_AGAIN);
        return ResultDTO.errorOf(CustomizeErrorCode.CAN_NOT_LIKE);
    }
    @PostMapping("/user/question/like/remove/id")
    @ResponseBody
    public Map<String,Object> removeLikeById(HttpServletRequest request,
                                             @RequestParam(name = "id",defaultValue = "0") Integer id
            ,@RequestParam(name = "type",defaultValue = "0") Integer type) {

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            throw new CustomizeException(CustomizeErrorCode.NO_LOGIN);
        }
        Map<String,Object> map  = new HashMap<>();
        if(id==null||id==0||type==null||type==0) {
            map.put("code",500);
            map.put("msg","妈呀，出问题啦！");
        }
        else {
            int c = likeService.removeLikeByIdAndType(user.getId(),id,type);
            if(c==0) {
                map.put("code",500);
                map.put("msg","哎呀，该收藏已移除或您无权移除！");
            }
            else {
                map.put("code",200);
                map.put("msg","恭喜您，成功移除"+c+"条收藏！");
            }
        }
        return map;
    }
}
