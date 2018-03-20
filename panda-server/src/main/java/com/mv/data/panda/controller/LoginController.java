package com.mv.data.panda.controller;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mv.data.panda.common.Result;
import com.mv.data.panda.common.auth.UserCache;
import com.mv.data.panda.model.User;
import com.mv.data.panda.service.LoginService;
import com.mv.data.panda.util.AuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.naming.NamingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * author: houying
 * date  : 17-2-24
 * desc  :
 */
@RestController
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final int expire = 7 * 24 * 3600;
    @Resource
    private LoginService loginService;
    @Resource
    private UserCache userCache;

    @RequestMapping("/api/login")
    public Result login(@RequestParam String username, @RequestParam String password, HttpServletResponse response) {
        try {
            User user = loginService.login(username, password);
            if (user == null) {
                return new Result(-1, "用户名或密码错误");
            }
            userCache.add(user);
            Cookie cookie = new Cookie("login_user", user.getUsername());
            cookie.setMaxAge(expire);
            cookie.setPath("/");
            response.addCookie(cookie);
            return new Result(true);
        } catch (Exception e) {
            return new Result(-1, e.getMessage());
        }
    }

    @RequestMapping("/api/logout")
    public Map logout(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie: cookies) {
            if (cookie.getName().equals("login_user")) {
                String username = cookie.getValue();
                userCache.remove(username);
            }
        }
        return ImmutableMap.of("status", 0);
    }

    @RequestMapping("/api/user")
    public Result user(HttpServletRequest request) {
        String username = AuthUtils.getUsername(request);
        logger.info("user status: {}", username);
        User user = userCache.getUser(username);
        if (username == null || user == null) {
            return new Result(-1, "未登录");
        } else {
            return new Result(username);
        }
    }
}
