package com.mv.data.panda.controller;

import com.mv.data.panda.common.UnLoggedInException;
import com.mv.data.panda.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * author: houying
 * date  : 18-1-24
 * desc  :
 */
@ControllerAdvice
public class ExceptionController {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionController.class);

    @ExceptionHandler(UnLoggedInException.class)
    @ResponseBody
    public Result unLoggedIn() {
        return new Result(302, "未登录");
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result exception(Exception e) {
        logger.error("exception occur", e);
        return new Result(-1, e.getMessage());
    }
}
