package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.service.ScreeningDataInterface;
import lombok.Data;

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
    private Integer schoolId;
    /**
     * 机构id
     */
    private Integer deptId;
    /**
     * 用户id
     */
    private Integer createUserId;
    /**
     * 学生id
     */
    private Integer studentId;
}
