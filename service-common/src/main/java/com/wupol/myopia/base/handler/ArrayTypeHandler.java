package com.wupol.myopia.base.handler;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.util.List;
import java.util.TimeZone;

public abstract class ArrayTypeHandler<T extends Object> extends JacksonTypeHandler {

    private static final ObjectMapper LIST_OBJECT_MAPPER = Jackson2ObjectMapperBuilder
            .json()
            .failOnUnknownProperties(false)
            .timeZone(TimeZone.getTimeZone("GMT+8"))
            .build();

    public ArrayTypeHandler(Class<List<T>> type) {
        super(type);
    }

    protected List<T> parse(String json) {
        try {
            if (StringUtils.isBlank(json)) {
                return null;
            }
            ObjectReader reader = LIST_OBJECT_MAPPER.readerFor(specificType());
            return reader.readValue(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract TypeReference<List<T>> specificType();

}
