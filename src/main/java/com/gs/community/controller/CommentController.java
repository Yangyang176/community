package com.gs.community.controller;

import com.gs.community.dto.CommentCreateDTO;
import com.gs.community.dto.CommentDTO;
import com.gs.community.dto.ResultDTO;
import com.gs.community.enums.CommentTypeEnum;
import com.gs.community.exception.CustomizeErrorCode;
import com.gs.community.exception.CustomizeException;
import com.gs.community.model.Comment;
import com.gs.community.model.User;
import com.gs.community.model.UserAccount;
import com.gs.community.provider.BaiduCloudProvider;
import com.gs.community.service.CommentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CommentController {
    @Autowired
    private CommentService commentService;
    @Autowired
    private BaiduCloudProvider baiduCloudProvider;

    @ResponseBody
    @RequestMapping(value = "/comment", method = RequestMethod.POST)
    public Object post(@RequestBody CommentCreateDTO commentCreateDTO,
                       HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        UserAccount userAccount = (UserAccount) request.getSession().getAttribute("userAccount");
        if (user == null) {
            return ResultDTO.errorOf(CustomizeErrorCode.NO_LOGIN);
        }
        if (commentCreateDTO == null || StringUtils.isBlank(commentCreateDTO.getContent())) {
            return ResultDTO.errorOf(CustomizeErrorCode.CONTENT_IS_EMPTY);
        }
        //审核
        ResultDTO resultDTO = baiduCloudProvider.getTextCensorResult(commentCreateDTO.getContent());
        if (resultDTO.getCode() != 1) {
            return resultDTO;
        }
        Comment comment = new Comment();
        comment.setParentId(commentCreateDTO.getParentId());
        comment.setContent(commentCreateDTO.getContent());
        comment.setType(commentCreateDTO.getType());
        comment.setGmtCreate(System.currentTimeMillis());
        comment.setGmtModified(comment.getGmtCreate());
        comment.setLikeCount(0);
        comment.setCommentCount(0);
        comment.setCommentator(user.getId());
        commentService.insert(comment, user, userAccount);
        return ResultDTO.okOf();
    }

    @ResponseBody
    @RequestMapping(value = "/comment/{id}", method = RequestMethod.GET)
    public ResultDTO<List<CommentDTO>> comments(@PathVariable(name = "id") Integer id) {
        List<CommentDTO> commentDTOS = commentService.listByTargetId(id, CommentTypeEnum.COMMENT);
        return ResultDTO.okOf(commentDTOS);
    }

    @PostMapping("/comment/del/id")
    @ResponseBody
    public Map<String, Object> deleteCommentById(HttpServletRequest request,
                                                 @RequestParam(name = "id", defaultValue = "0") Integer id
            , @RequestParam(name = "type", defaultValue = "0") Integer type) {

        User user = (User) request.getSession().getAttribute("user");
        UserAccount userAccount = (UserAccount) request.getSession().getAttribute("userAccount");
        if (user == null || userAccount == null) {
            throw new CustomizeException(CustomizeErrorCode.NO_LOGIN);
        }
        Map<String, Object> map = new HashMap<>();
        if (id == null || id == 0 || type == null || type == 0) {
            map.put("code", 500);
            map.put("msg", "妈呀，出问题啦！");
        } else {
            int c = commentService.delCommentByIdAndType(user.getId(), userAccount.getGroupId(), id, type);
            if (c == 0) {
                map.put("code", 500);
                map.put("msg", "哎呀，该评论已删除或您无权删除！");
            } else {
                map.put("code", 200);
                map.put("msg", "恭喜您，成功删除" + c + "条评论及子评论！");
            }
        }
        return map;
    }
}
