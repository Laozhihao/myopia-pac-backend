package com.wupol.myopia.business.common.utils.handler;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.wupol.myopia.base.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author wulizhou
 * @Date 2021/12/9 15:14
 */
@Slf4j
public class DateDeserializer extends JsonDeserializer<Date> {

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        if (jsonParser != null && StringUtils.isNotEmpty(jsonParser.getText())) {
            try {
                // 13位数字，即可能是时间戳，进行时间戳转化
                if (jsonParser.getText().matches("^\\d{13}$")) {
                    return new Date(Long.parseLong(jsonParser.getText()));
                }
                return format.parse(jsonParser.getText());
            } catch (Exception e) {
                log.error("时间转化异常，期待格式为'yyyy-MM-dd HH:mm'或为时间戳，内容为'{}'", jsonParser.getText());
                throw new BusinessException("时间转化异常");
            }
        } else {
            return null;
        }
    }

}
