package com.wupol.myopia.business.aggregation.student.domain.builder;

import com.wupol.myopia.business.common.utils.constant.LowVisionLevelEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.experimental.UtilityClass;

import java.util.Date;
import java.util.Objects;

/**
 * 学生信息构建类（管理端学生）
 *
 * @author hang.yuan 2022/10/18 14:36
 */
@UtilityClass
public class StudentInfoBuilder {

    /**
     * 通过筛查结论信息设置学生对象信息
     * @param student
     * @param statConclusion
     */
    public void setStudentInfoByStatConclusion(Student student,StatConclusion statConclusion,Date lastScreeningTime){
        student.setIsAstigmatism(statConclusion.getIsAstigmatism());
        student.setIsHyperopia(statConclusion.getIsHyperopia());
        student.setIsMyopia(statConclusion.getIsMyopia());
        student.setIsAnisometropia(statConclusion.getIsAnisometropia());
        student.setIsRefractiveError(statConclusion.getIsRefractiveError());
        student.setVisionCorrection(statConclusion.getVisionCorrection());
        student.setGlassesType(statConclusion.getGlassesType());
        student.setVisionLabel(statConclusion.getWarningLevel());
        student.setLastScreeningTime(lastScreeningTime);
        student.setUpdateTime(new Date());
        student.setAstigmatismLevel(statConclusion.getAstigmatismLevel());
        student.setHyperopiaLevel(statConclusion.getHyperopiaLevel());
        String schoolGradeCode = statConclusion.getSchoolGradeCode();
        GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(schoolGradeCode);
        student.setLowVision(Boolean.TRUE.equals(statConclusion.getIsLowVision()) ? LowVisionLevelEnum.LOW_VISION.code : null);
        if (!Objects.equals(SchoolAge.KINDERGARTEN.code,gradeCodeEnum.getType())){
            //小学及以上的数据同步
            student.setMyopiaLevel(statConclusion.getMyopiaLevel());
            student.setScreeningMyopia(statConclusion.getScreeningMyopia());
        }
    }
}
