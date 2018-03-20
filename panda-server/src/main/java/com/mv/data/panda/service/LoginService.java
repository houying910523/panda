package com.mv.data.panda.service;

import com.mobvista.dataplatform.LdapClient;
import com.mobvista.dataplatform.LdapClientBuilder;
import com.mv.data.panda.common.mapper.RowWrapper;
import com.mv.data.panda.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.naming.NamingException;
import java.util.List;

/**
 * author: houying
 * date  : 18-1-23
 * desc  :
 */
@Service
public class LoginService {
    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);
    @Resource
    private JdbcTemplate jdbcTemplate;

    private RowWrapper<User> userRowWrapper = new RowWrapper<>(User.class);

    public User login(String username, String password) throws Exception {
        LdapClient client = LdapClientBuilder.newClient().build();
        try {
            client.login(username, password);
            com.mobvista.dataplatform.User u = client.getUser();
            client.close();
            List<User> userList = jdbcTemplate.query("select * from user where username=?", userRowWrapper, username);
            if (userList.isEmpty()) {
                throw new Exception("未授权的用户，请联系管理员");
            }
            User user = userList.get(0);
            user.setEmail(u.getEmail());
            return user;
        } catch (NamingException e) {
            logger.info(e.getMessage(), e);
        }
        return null;
    }


}
