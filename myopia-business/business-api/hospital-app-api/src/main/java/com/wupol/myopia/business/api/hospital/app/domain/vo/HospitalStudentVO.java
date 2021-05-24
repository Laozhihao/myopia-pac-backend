package com.wupol.myopia.business.api.hospital.app.domain.vo;

import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.hospital.domain.dos.HospitalStudentDO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import lombok.Data;

/**
 * 医院-学生
 *
 * @author Simple4H
 */
@Data
public class HospitalStudentVO extends HospitalStudentDO {
    /** 省 */
    private District province;
    /** 市 */
    private District city;
    /** 区 */
    private District area;
    /** 乡/镇 */
    private District town;
    /** 学校 */
    private School school;
    /** 班级 */
    private SchoolClass schoolClass;
    /** 班级 */
    private SchoolGrade schoolGrade;
    /** 民族中文 */
    private String nationName;
}
