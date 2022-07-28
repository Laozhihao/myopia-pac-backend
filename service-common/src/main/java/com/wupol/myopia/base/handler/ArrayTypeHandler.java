package com.wupol.myopia.base.handler;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.wupol.myopia.base.exception.BusinessException;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

public abstract class ArrayTypeHandler<T extends Object> extends JacksonTypeHandler {

    private static final ObjectMapper LIST_OBJECT_MAPPER = Jackson2ObjectMapperBuilder
            .json()
            .failOnUnknownProperties(false)
            .timeZone(TimeZone.getTimeZone("GMT+8"))
            .build();

    protected ArrayTypeHandler(Class<List<T>> type) {
        super(type);
    }

    @Override
    protected List<T> parse(String json) {
        try {
            if (StringUtils.isBlank(json)) {
                return Collections.emptyList();
            }
            ObjectReader reader = LIST_OBJECT_MAPPER.readerFor(specificType());
            return reader.readValue(json);
        } catch (IOException e) {
            throw new BusinessException("转换异常");
        }
    }

    protected abstract TypeReference<List<T>> specificType();

}
