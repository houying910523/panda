package com.mv.data.panda.common.mapper;

import com.google.common.collect.Maps;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * author: houying
 * date  : 17-6-29
 * desc  :
 */
public class RowWrapper<T> implements RowMapper<T> {

    private Class<T> clazz;
    private Map<String, Field> fieldMap;
    public RowWrapper(Class<T> clazz) {
        this.clazz = clazz;
        this.fieldMap = Maps.newHashMap();
        Field[] fields = clazz.getDeclaredFields();
        for (Field f: fields) {
            f.setAccessible(true);
            Column column = f.getAnnotation(Column.class);
            if (column != null) {
                fieldMap.put(column.value(), f);
            } else {
                fieldMap.put(f.getName(), f);
            }
        }
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        T t = null;
        try {
            t = clazz.newInstance();
            ResultSetMetaData dataMeta = rs.getMetaData();
            int columns = dataMeta.getColumnCount();
            for (int i = 1; i <= columns; i++) {
                String label = dataMeta.getColumnLabel(i);
                Field f = fieldMap.get(label);
                if (f != null) {
                    f.set(t, rs.getObject(i));
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return t;
    }
}
