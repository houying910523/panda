package com.mv.data.panda.common.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mv.data.panda.util.AuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * author: houying
 * date  : 17-3-1
 * desc  :
 */
public class AuthInterceptor extends HandlerInterceptorAdapter {
    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);
    private final String message;

    @Resource
    private UserCache userCache;

    public AuthInterceptor() throws IOException {
        message = new ObjectMapper().writeValueAsString(ImmutableMap.of("status", 302, "message", "未登录"));
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String username = AuthUtils.getUsername(request);
        if (username == null) {
            redirectToLogin(response);
            return false;
        }
        if (!userCache.isLogin(username)) {
            redirectToLogin(response);
            return false;
        }
        String uri = request.getRequestURI();
        logger.info("{} visit {}", username, uri);
        return true;
    }

    private void redirectToLogin(HttpServletResponse response) throws IOException {
        response.setHeader("Content-Type", "application/json;charset=utf-8");
        response.getWriter().write(message);
    }
}
