package com.wupol.myopia.business.api.school.management.domain.vo;

import com.wupol.myopia.business.core.hospital.domain.dto.MonthAgeStatusDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 *  学校学生信息
 * @author hang.yuan
 * @date 2022/9/20
 */
@Data
@Accessors(chain = true)
public class StudentInfoVO implements Serializable {
    /**
     * 学生姓名
     */
    private String name;

    /**
     * 性别 -1未知 0-男 1-女
     */
    private Integer gender;
    /**
     * 检查建档编码
     */
    private String recordNo;

    /**
     * 年龄
     */
    private String birthdayInfo;

    /**
     * 年龄段对应的检查的状态
     */
    private List<MonthAgeStatusDTO> ageStageStatusList;
}