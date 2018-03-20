package com.mv.data.panda.controller;

import com.mv.data.panda.common.Result;
import com.mv.data.panda.common.auth.UserCache;
import com.mv.data.panda.model.Submit;
import com.mv.data.panda.model.SubmitState;
import com.mv.data.panda.model.User;
import com.mv.data.panda.service.InstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * author: houying
 * date  : 18-1-23
 * desc  :
 */
@RestController
@RequestMapping("/instance")
public class InstanceController {
    private final static Logger logger = LoggerFactory.getLogger(InstanceController.class);

    @Resource
    private InstanceService instanceService;

    @Resource
    private UserCache userCache;

    @RequestMapping(value = "/detail/{projectId}", method = RequestMethod.GET)
    public Result detail(@PathVariable long projectId, @CookieValue("login_user") String username) {
        userCache.validate(username);
        try {
            Submit submit = instanceService.detail(projectId);
            return new Result(submit);
        } catch (Exception e) {
            return new Result(-1, e.getMessage());
        }
    }

    @RequestMapping(value = "/deploy/{projectId}", method = RequestMethod.POST)
    public Result deploy(@PathVariable long projectId, @CookieValue("login_user") String username) {
        User user = userCache.getLoginUser(username);
        try {
            long tid = instanceService.deployRequest(projectId, user);
            return new Result(tid);
        } catch (Exception e) {
            return new Result(-1, e.getMessage());
        }
    }

    @RequestMapping(value = "/state/{instanceId}", method = RequestMethod.GET)
    public Result state(@PathVariable long instanceId) {
        int state = instanceService.state(instanceId);
        if (SubmitState.valueOf(state) == null) {
            return new Result(-1, "未知发布记录");
        } else {
            return new Result(state);
        }
    }

    @RequestMapping(value = "/kill/{instanceId}", method = RequestMethod.POST)
    public Result kill(@PathVariable int instanceId, @CookieValue("login_user") String username)
            throws InterruptedException {
        User user = userCache.getLoginUser(username);
        boolean result = instanceService.kill(instanceId, user);
        if (result) {
            return new Result(true);
        } else {
            return new Result(-1, "kill失败");
        }
    }

    @RequestMapping(value = "/restart/{instanceId}", method = RequestMethod.POST)
    public Result restart(@PathVariable int instanceId, @CookieValue("login_user") String username) {
        return null;
    }

}
