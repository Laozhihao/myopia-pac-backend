package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.common.exceptions.ManagementUncheckedException;
import com.wupol.myopia.business.management.constant.RescreeningStatisticEnum;
import com.wupol.myopia.business.management.domain.query.AppPageRequest;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

/**
 * @Description 搜索复查结果使用
 * @Date 2021/1/29 14:34
 * @Author by Jacob
 */
@Data
@Accessors(chain = true)
public class ScreeningResultSearchDTO extends AppPageRequest {
    /**
     * 筛查机构id
     */
    private Integer depId;
    /**
     * 年级名
     */
    private String gradeName;
    /**
     * 学校id
     */
    private Integer schoolId;
    /**
     * 班级名
     */
    private String clazzName;

    public RescreeningStatisticEnum getStatisticType() {
        if (StringUtils.isNoneBlank(clazzName)) {
            return RescreeningStatisticEnum.CLASS;
        }
        if (StringUtils.isNoneBlank(gradeName)) {
            return RescreeningStatisticEnum.GRADE;
        }
        if (schoolId != null) {
            return RescreeningStatisticEnum.SCHOOL;
        }
        throw new ManagementUncheckedException("无法确定统计维度");
    }

}

