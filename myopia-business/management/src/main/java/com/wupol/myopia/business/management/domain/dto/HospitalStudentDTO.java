package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.model.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 医院-学生
 *
 * @author Simple4H
 */
@Getter
@Setter
public class HospitalStudentDTO extends Student {

    /**
     * 省
     */
    private District province;

    /**
     * 市
     */
    private District city;

    /**
     * 区
     */
    private District area;

    /**
     * 乡/镇
     */
    private District town;

    /**
     * 学校
     */
    private School school;

    /**
     * 班级
     */
    private SchoolClass schoolClass;

    /**
     * 班级
     */
    private SchoolGrade schoolGrade;

    /**
     * 民族中文
     */
    private String nationName;
}
