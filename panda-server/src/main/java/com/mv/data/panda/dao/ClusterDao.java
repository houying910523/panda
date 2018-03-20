package com.mv.data.panda.dao;

import com.mv.data.panda.service.Sql;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * author: houying
 * date  : 18-2-5
 * desc  :
 */
@Repository
public class ClusterDao {
    @Resource
    private JdbcTemplate jdbcTemplate;

    public String queryClusterByDep(String dep) {
        List<String> list = jdbcTemplate.queryForList(Sql.QUERY_CLUSTER_BY_DEP, String.class, dep);
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }
}
