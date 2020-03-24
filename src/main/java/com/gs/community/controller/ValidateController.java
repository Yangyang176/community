package com.gs.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.gs.community.dto.ValidateDTO;
import com.gs.community.provider.VaptchaProvider;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ValidateController {
    @ResponseBody//@ResponseBody返回json格式的数据
    @RequestMapping(value = "/validate", method = RequestMethod.POST)
    public Object post(@RequestParam(name = "token", required = false) String token,
                       @RequestParam(name = "scene", required = false) int scene,
                       @RequestParam(name = "ip", required = false) String ip) {
        String json = VaptchaProvider.getValidateResult(token, scene, ip);
        JSONObject obj = JSONObject.parseObject(json);
        Integer success = obj.getInteger("success");
        Integer score = obj.getInteger("score");
        String msg = obj.getString("msg");
        ValidateDTO validateDTO = new ValidateDTO();
        validateDTO.setMsg(msg);
        validateDTO.setScore(score);
        validateDTO.setSuccess(success);
        return validateDTO;
    }

    @ResponseBody//@ResponseBody返回json格式的数据
    @RequestMapping(value = "/getIp", method = RequestMethod.GET)
    public String getIp() {
        String returnCitySN = VaptchaProvider.getIp();
        String json = returnCitySN.split("=")[1].split(";")[0];
        JSONObject obj = JSONObject.parseObject(json);
        String cip = obj.getString("cip");
        String cid = obj.getString("cid");
        String cname = obj.getString("cname");
        return cip;
    }
}
