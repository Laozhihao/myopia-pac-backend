package com.wupol.myopia.business.api.school.management.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.base.domain.vo.FamilyInfoVO;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.core.common.domain.model.AddressCode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 学生基本信息
 *
 * @author hang.yuan 2022/9/16 16:44
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StudentBaseInfoDTO extends AddressCode implements Serializable {

    /**
     * 学校学生ID
     */
    @NotNull(message = "学校学生ID不能为空")
    private Integer id;

    /**
     * 学生Id
     */
    private Integer studentId;


    /**
     * 学号
     */
    private String sno;

    /**
     * 学生姓名
     */
    @NotBlank(message = "学生姓名不能为空")
    private String name;

    /**
     * 身份证号码
     */
    private String idCard;

    /**
     * 护照
     */
    private String passport;

    /**
     * 出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "出生日期不能为空")
    private Date birthday;

    /**
     * 学校Id
     */
    @NotNull(message = "学校Id不能为空")
    private Integer schoolId;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 民族 0-汉族
     */
    private Integer nation;

    /**
     * 性别 0-男 1-女
     */
    @Range(min = 0, max = 1,message = "性别不能为空")
    private Integer gender;

    /**
     * 家长手机号码
     */
    private String parentPhone;

    /**
     * 年级ID
     */
    @NotNull(message = "年级不能为空")
    private Integer gradeId;

    /**
     * 班级ID
     */
    @NotNull(message = "班级不能为空")
    private Integer classId;

    /**
     * 委会行政区域code
     */
    private Long committeeCode;

    /**
     * 父亲信息
     */
    private FamilyInfoVO.MemberInfo fatherInfo;

    /**
     * 母亲信息
     */
    private FamilyInfoVO.MemberInfo motherInfo;

    /**
     * 检查学生信息是否正确
     * <p>
     *     身份证和护照二选一
     * </p>
     */
    public void checkStudentInfo() {
        if (StringUtils.isAllBlank(idCard, passport) || (StringUtils.isNotBlank(idCard) && StringUtils.isNotBlank(passport))) {
            throw new BusinessException("身份证、护照信息异常");
        }
    }

}
