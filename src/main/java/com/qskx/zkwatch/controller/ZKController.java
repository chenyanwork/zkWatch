package com.qskx.zkwatch.controller;

import com.qskx.zkwatch.core.ZKConf;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/zk")
public class ZKController {

    @RequestMapping(value = "/save", produces = "application/json; charset=utf-8", method = RequestMethod.POST)
    @ResponseBody
    public String setData(String key, String value){
        ZKConf.set(key, value);
        return "success";
    }
}
