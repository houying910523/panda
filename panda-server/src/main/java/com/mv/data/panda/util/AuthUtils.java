package com.mv.data.panda.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * author: houying
 * date  : 17-3-2
 * desc  :
 */
public class AuthUtils {

    public static String getUsername(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie: request.getCookies()) {
            if (cookie.getName().equals("login_user")) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
