package com.wupol.myopia.business.core.stat.handler;

import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wupol.myopia.business.common.utils.util.JsonUtil;
import com.wupol.myopia.business.core.stat.domain.dos.KindergartenVisionAnalysisDO;
import com.wupol.myopia.business.core.stat.domain.dos.PrimarySchoolAndAboveVisionAnalysisDO;
import com.wupol.myopia.business.core.stat.domain.dos.VisionAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.io.IOException;
import java.util.Objects;

/**
 * 视力分析json处理器
 *
 * @author hang.yuan 2022/4/17 15:43
 */
@Slf4j
@MappedTypes({VisionAnalysis.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class VisionAnalysisTypeHandler extends AbstractJsonTypeHandler<VisionAnalysis> {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private final Class<?> type;

    public VisionAnalysisTypeHandler(Class<?> type) {
        if (log.isTraceEnabled()) {
            log.trace("JacksonTypeHandler(" + type + ")");
        }
        Assert.notNull(type, "Type argument cannot be null");
        this.type = type;
    }

    @Override
    protected VisionAnalysis parse(String json) {
        try {
            Integer schoolType = JsonUtil.getValue(json, "schoolType", Integer.class);
            if(Objects.equals(8,schoolType)){
                return objectMapper.readValue(json, KindergartenVisionAnalysisDO.class);
            }else {
                return objectMapper.readValue(json, PrimarySchoolAndAboveVisionAnalysisDO.class);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String toJson(VisionAnalysis obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setObjectMapper(ObjectMapper objectMapper) {
        Assert.notNull(objectMapper, "ObjectMapper should not be null");
        VisionAnalysisTypeHandler.objectMapper = objectMapper;
    }
}
