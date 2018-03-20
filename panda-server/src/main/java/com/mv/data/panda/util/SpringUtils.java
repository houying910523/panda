package com.mv.data.panda.util;

import com.mv.data.panda.dao.InstanceDao;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;

/**
 * author: houying
 * date  : 17-6-28
 * desc  :
 */
public class SpringUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public static InstanceDao getInstanceDao() {
        return applicationContext.getBean(InstanceDao.class);
    }
}
