package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wupol.myopia.base.util.CurrentUserUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @Description 筛查结果基本数据
 * @Date 2021/1/26 1:04
 * @Author by Jacob
 */
@Accessors(chain = true)
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
    @JsonProperty("studentId")
    private String planStudentId;
    /**
     * 默认是初筛，app设计如此
     */
    private Integer isState = 0;
    /**
     * 初步诊断结果：0-正常、1-（疑似）异常
     */
    private Integer diagnosis;

    public Integer getPlanStudentId() {
        return stringToInteger(planStudentId);
    }

    public Integer getSchoolId() {
        return stringToInteger(schoolId);
    }

    public Integer getDeptId() {
        if (Objects.isNull(deptId)) {
            deptId = CurrentUserUtil.getCurrentUser().getOrgId();
        }
        return deptId;
    }

    public Integer getCreateUserId() {
        if (Objects.isNull(createUserId)) {
            createUserId = CurrentUserUtil.getCurrentUser().getId();
        }
        return createUserId;
    }

    private Integer stringToInteger(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        Double doubleData = Double.valueOf(value);
        return (int) Math.ceil(doubleData);
    }

    public Integer getIsState() {
        if (Objects.isNull(isState)) {
            isState = 0;
        }
        return isState;
    }

    public void setIsState(Integer isState) {
        this.isState = isState;
    }
}
