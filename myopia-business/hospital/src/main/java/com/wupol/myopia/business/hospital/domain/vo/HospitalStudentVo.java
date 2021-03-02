package com.wupol.myopia.business.hospital.domain.vo;

import com.wupol.myopia.business.management.domain.model.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 医院的学生信息
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
public class HospitalStudentVo extends Student {
    private School school;
    private SchoolGrade schoolGrade;
    private SchoolClass schoolClass;
    private District province;
    private District city;
    private District area;
    private District town;
}
