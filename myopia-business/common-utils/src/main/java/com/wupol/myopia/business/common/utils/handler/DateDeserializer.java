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
        try {
            // TODO wulizhou 后期增强功能，使其能支持时间戳转Date
            if (jsonParser != null && StringUtils.isNotEmpty(jsonParser.getText())) {
                return format.parse(jsonParser.getText());
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("时间转化异常，期待格式为'yyyy-MM-dd HH:mm'，内容为'{}'", jsonParser.getText());
            throw new BusinessException("时间转化异常");
        }
    }

}
