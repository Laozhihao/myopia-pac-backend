package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wupol.myopia.business.management.service.ScreeningDataInterface;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @Description 筛查结果基本数据
 * @Date 2021/1/26 1:04
 * @Author by Jacob
 */
@Data
public abstract class ScreeningResultBasicData implements ScreeningDataInterface {
    /**
     * 学校id
     */
    private String schoolId;
    /**
     * 机构id
     */
    private Integer deptId;
    /**
     * 用户id
     */
    @JsonProperty("userId")
    private Integer createUserId;
    /**
     * 学生id
     */
    private String studentId;
    /**
     * 默认是初筛，app设计如此
     */
    private Integer isState=0;

    public Integer getStudentId() {
       return stringToInteger(studentId);
    }

    public Integer getSchoolId() {
        return stringToInteger(schoolId);
    }

    private Integer stringToInteger(String value){
        if (StringUtils.isBlank(value)) {
            return null;
        }
        Double doubleData = Double.valueOf(value);
        return (int)Math.ceil(doubleData);
    }
}
