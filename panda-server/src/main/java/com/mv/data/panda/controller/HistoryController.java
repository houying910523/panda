package com.mv.data.panda.controller;


import com.mv.data.panda.common.Result;
import com.mv.data.panda.common.auth.UserCache;
import com.mv.data.panda.model.Submit;
import com.mv.data.panda.service.HistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * author: houying
 * date  : 17-6-29
 * desc  :
 */
@RestController
@RequestMapping("/history")
public class HistoryController {
    private final static Logger logger = LoggerFactory.getLogger(HistoryController.class);

    @Resource
    private HistoryService historyService;

    @Resource
    private UserCache userCache;

    @RequestMapping("/list/{projectId}")
    public Result list(@PathVariable long projectId, int page, int size, @CookieValue("login_user") String username) {
        userCache.validate(username);
        List<Submit> list = historyService.history(projectId, page, size);
        return new Result(list);
    }

    @RequestMapping("/log/{instanceId}")
    public Result log(@PathVariable long instanceId) throws IOException {
        try {
            List<String> content = historyService.log(instanceId);
            if (content != null) {
                return new Result(content);
            } else {
                return new Result(-1, "日志文件未找到");
            }
        } catch (Exception e) {
            logger.error("获取日志失败", e);
            return new Result(-1, "获取日志失败");
        }
    }
}
