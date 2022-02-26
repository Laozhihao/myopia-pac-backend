package com.wupol.myopia.business.aggregation.screening.domain.dto;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

/**
 * 更新筛查学生学生
 *
 * @author Simple4H
 */
@Data
public class UpdatePlanStudentRequestDTO {

    @NotBlank(message = "名字不能为空")
    private String name;

    @NotNull(message = "性别不能为空")
    private Integer gender;

    @NotNull(message = "年龄不能为空")
    private Integer studentAge;

    private String parentPhone;

    private Date birthday;

    private String sno;

    @NotNull(message = "学生ID不能为空")
    private Integer planStudentId;

    private Integer studentId;

    /**
     * 身份证
     */
    private String idCard;

    /**
     * 护照
     */
    private String passport;

    private Integer schoolId;

    @NotNull(message = "班级不能为空")
    private Integer classId;

    @NotNull(message = "年级不能为空")
    private Integer gradeId;

    @Null
    private Integer userId;

    public Date getBirthday() {
        if (Objects.nonNull(birthday) || Objects.isNull(studentAge)) {
            return birthday;
        }
        LocalDate date = LocalDate.of(DateUtil.getYear(new Date()) - studentAge, 1, 1);
        return DateUtil.toDate(date);
    }

    /**
     * 检查学生信息是否正确
     * <p>
     * 身份证和护照二选一
     * </p>
     */
    public void checkStudentInfo() {
        if (StringUtils.isNotBlank(idCard) && StringUtils.isNotBlank(passport)) {
            throw new BusinessException("身份证、护照信息异常,不能同时存在");
        } else if (StringUtils.isAllBlank(idCard, passport)) {
            throw new BusinessException("身份证、护照信息异常,不能同时为空");
        }

        if (passport == null) {
            passport = StringUtils.EMPTY;
        }

        if (idCard == null) {
            idCard = StringUtils.EMPTY;
        }

    }

    /**
     * 更新计划学生
     *
     * @param screeningPlanSchoolStudent
     */
    public ScreeningPlanSchoolStudent handlePlanStudentData(ScreeningPlanSchoolStudent screeningPlanSchoolStudent) {
        if (screeningPlanSchoolStudent == null) {
            return screeningPlanSchoolStudent;
        }
        checkStudentInfo();
        screeningPlanSchoolStudent.setStudentName(getName());
        screeningPlanSchoolStudent.setGender(getGender());
        screeningPlanSchoolStudent.setStudentAge(getStudentAge());
        screeningPlanSchoolStudent.setBirthday(getBirthday());

        screeningPlanSchoolStudent.setSchoolId(getSchoolId());
        screeningPlanSchoolStudent.setClassId(getClassId());
        screeningPlanSchoolStudent.setGradeId(getGradeId());
        if (StringUtils.isNotBlank(getParentPhone())) {
            screeningPlanSchoolStudent.setParentPhone(getParentPhone());
        }
        if (StringUtils.isNotBlank(getSno())) {
            screeningPlanSchoolStudent.setStudentNo(getSno());
        }
        return screeningPlanSchoolStudent;
    }
}
