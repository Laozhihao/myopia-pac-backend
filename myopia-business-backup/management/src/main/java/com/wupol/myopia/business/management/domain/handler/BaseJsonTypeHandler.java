package com.wupol.myopia.business.management.domain.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wupol.framework.core.util.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class BaseJsonTypeHandler<T> extends BaseTypeHandler<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseJsonTypeHandler.class);
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        setNonNullParameter(ps, i, parameter);
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String jsonStr = rs.getString(columnName);
        return Json2TypeObject(jsonStr);
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String jsonStr = rs.getNString(columnIndex);
        return Json2TypeObject(jsonStr);
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String jsonStr = cs.getNString(columnIndex);
        return Json2TypeObject(jsonStr);
    }

    public abstract Class<T> getTypeClass();
    public T Json2TypeObject(String jsonStr) {
        if(StringUtils.isEmpty(jsonStr) || "{}".equals(jsonStr)) {
            return null;
        }
        try {
            return new ObjectMapper().readValue(jsonStr, getTypeClass());
        } catch (IOException e) {
            LOGGER.error("[{}]存数据库时，string转Object失败", getTypeClass(), e);
            return null;
        }
    }


    public void setNonNullParameter(PreparedStatement ps, int i, T parameter) throws SQLException {
        if(parameter == null) {
            return;
        }
        try {
            //将JSONObject的字符串设置给ps
            ps.setString(i, new ObjectMapper().writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            LOGGER.error("[{}] 存数据库时，Object转string失败", getTypeClass(), e);
        }
    }
}
