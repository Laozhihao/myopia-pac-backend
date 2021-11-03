package com.wupol.myopia.business.core.school.management.domain.model;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.RegularUtils;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.util.VisionUtil;
import com.wupol.myopia.business.core.common.domain.model.AddressCode;
import freemarker.core.BugException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 学校-学生表
 *
 * @author Simple4H
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_school_student")
public class SchoolStudent extends AddressCode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学生Id
     */
    private Integer studentId;

    /**
     * 学校Id
     */
    @NotNull(message = "学校Id不能为空")
    private Integer schoolId;

    /**
     * 学校编码
     */
    @NotBlank(message = "学校编码不能为空")
    private String schoolNo;

    /**
     * 创建人ID
     */
    private Integer createUserId;

    /**
     * 学号
     */
    @NotBlank(message = "学号不能为空")
    private String sno;

    /**
     * 年级ID
     */
    private Integer gradeId;

    /**
     * 年级名称
     */
    private String gradeName;

    /**
     * 学龄段
     */
    private Integer gradeType;

    /**
     * 班级id
     */
    private Integer classId;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 学生姓名
     */
    @NotBlank(message = "学生姓名不能为空")
    private String name;

    /**
     * 性别 0-男 1-女
     */
    @Range(min = 0, max = 1)
    private Integer gender;

    /**
     * 出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "出生日期不能为空")
    private Date birthday;

    /**
     * 民族 0-汉族
     */
    private Integer nation;

    /**
     * 身份证号码
     */
    @NotBlank(message = "身份证不能为空")
    private String idCard;

    /**
     * 家长手机号码
     */
    private String parentPhone;

    /**
     * 家长公众号手机号码
     */
    private String mpParentPhone;

    /**
     * 详细地址
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String address;

    /**
     * 状态 0-启用 1-禁止 2-删除
     */
    private Integer status;

    /**
     * 戴镜类型
     */
    private Integer glassesType;

    /**
     * 最近筛选时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastScreeningTime;

    /**
     * 视力标签 0-零级、1-一级、2-二级、3-三级
     */
    private Integer visionLabel;

    /**
     * 近视等级，0-正常、1-筛查性近视、2-近视前期、3-低度近视、4-中度近视、5-重度近视
     */
    private Integer myopiaLevel;

    /**
     * 远视等级，0-正常、1-远视、2-低度远视、3-中度远视、4-重度远视
     */
    private Integer hyperopiaLevel;

    /**
     * 散光等级，0-正常、1-低度散光、2-中度散光、3-重度散光
     */
    private Integer astigmatismLevel;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 上传筛查学生时，判断学生需更新信息是否一致
     * 由于只有部分字段，所以不使用equals
     *
     * @param excelStudent 导入学生
     * @return 更新信息是否一致
     */
    public boolean checkNeedUpdate(SchoolStudent excelStudent) {
        return !StringUtils.equalsIgnoreCase(this.name, excelStudent.name) ||
                !this.gender.equals(excelStudent.gender) ||
                !StringUtils.equalsIgnoreCase(DateFormatUtil.format(this.birthday, DateFormatUtil.FORMAT_ONLY_DATE), DateFormatUtil.format(excelStudent.birthday, DateFormatUtil.FORMAT_ONLY_DATE)) ||
                (Objects.nonNull(excelStudent.nation) && (Objects.nonNull(this.nation)) && !this.nation.equals(excelStudent.nation)) ||
                !this.gradeId.equals(excelStudent.gradeId) ||
                !this.classId.equals(excelStudent.classId) ||
                !StringUtils.equalsIgnoreCase(this.sno, excelStudent.sno) ||
                (Objects.nonNull(excelStudent.getProvinceCode())) ||
                (Objects.nonNull(excelStudent.getCityCode())) ||
                (Objects.nonNull(excelStudent.getAreaCode())) ||
                (Objects.nonNull(excelStudent.getTownCode())) ||
                (StringUtils.isNotBlank(excelStudent.getAddress())) ||
                (StringUtils.isNotBlank(excelStudent.parentPhone) && !StringUtils.equalsIgnoreCase(this.parentPhone, excelStudent.parentPhone));
    }

    /**
     * 视力情况
     *
     * @return 视力情况
     */
    public String situation2Str() {
        return VisionUtil.getVisionSummary(glassesType, myopiaLevel, hyperopiaLevel, astigmatismLevel);
    }

    /**
     * 生日是否超出限制
     *
     * @return true-是 false-否
     */
    public boolean checkBirthdayExceedLimit() {
        // 1970-01-01 毫秒时间戳
        Date checkDate = new Date(-28800000L);
        return Objects.nonNull(birthday) && birthday.before(checkDate);
    }

    /**
     * 获取学生模板
     *
     * @return 0-幼儿园版本 1-中小学版本
     */
    public Integer getSchoolAgeStatus() {
        if (Objects.nonNull(gradeType)) {
            if (SchoolAge.KINDERGARTEN.code.equals(gradeType)) {
                return 0;
            } else {
                return 1;
            }
        }
        if (Objects.isNull(birthday)) {
            return null;
        }
        if (DateUtil.ageOfNow(birthday) > 6) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * 检查身份证
     */
    public void checkIdCard() {
        if (!RegularUtils.isIdCard(idCard)) {
            throw new BugException("身份证信息异常");
        }
    }
}
