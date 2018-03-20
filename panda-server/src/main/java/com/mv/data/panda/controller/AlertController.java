package com.mv.data.panda.controller;

import com.mv.data.panda.service.AppStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Set;

/**
 * author: houying
 * date  : 17-7-7
 * desc  :
 */
public class AlertController {
    private final static Logger logger = LoggerFactory.getLogger(AlertController.class);

    @Resource
    private AppStateManager appStateManager;

    @RequestMapping("/api/spark/alert")
    @ResponseBody
    public Set<String> alert() {
        return appStateManager.errorApplications();
    }

    @RequestMapping("/api/spark/remove/error/app/{name}")
    @ResponseBody
    public String remove(@PathVariable String name) {
        appStateManager.remove(name);
        return "ok";
    }

    @RequestMapping("/api/spark/add/error/app/{name}")
    @ResponseBody
    public String add(@PathVariable String name) {
        appStateManager.add(name);
        return "ok";
    }
}
