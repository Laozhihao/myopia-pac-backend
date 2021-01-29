package com.wupol.myopia.business.management.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Description
 * @Date 2021/1/28 17:00
 * @Author by Jacob
 */
public abstract class BaseJsonTypeHandler<T> extends BaseTypeHandler<T>{
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseJsonTypeHandler.class);

    public BaseJsonTypeHandler() {
    }

    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        this.setNonNullParameter(ps, i, parameter);
    }

    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String jsonStr = rs.getString(columnName);
        return this.Json2TypeObject(jsonStr);
    }

    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String jsonStr = rs.getNString(columnIndex);
        return this.Json2TypeObject(jsonStr);
    }

    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String jsonStr = cs.getNString(columnIndex);
        return this.Json2TypeObject(jsonStr);
    }

    public abstract Class<T> getTypeClass();

    private T Json2TypeObject(String jsonStr) {
        if (StringUtils.isEmpty(jsonStr)) {
            return null;
        } else {
            try {
                return (new ObjectMapper()).readValue(jsonStr, this.getTypeClass());
            } catch (IOException var3) {
                LOGGER.error("[{}]存数据库时，string转Object失败", this.getTypeClass(), var3);
                return null;
            }
        }
    }

    private void setNonNullParameter(PreparedStatement ps, int i, T parameter) throws SQLException {
        if (parameter != null) {
            try {
                ps.setString(i, (new ObjectMapper()).writeValueAsString(parameter));
            } catch (JsonProcessingException var5) {
                LOGGER.error("[{}] 存数据库时，Object转string失败", this.getTypeClass(), var5);
            }

        }
    }
}
