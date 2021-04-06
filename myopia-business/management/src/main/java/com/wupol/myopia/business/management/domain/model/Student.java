package com.wupol.myopia.business.management.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.RegularUtils;
import com.wupol.myopia.business.common.constant.GlassesType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.Range;
import sun.swing.StringUIClientPropertyKey;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 学校-学生表
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_student")
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学校编码
     */
    private String schoolNo;

    /**
     * 创建人ID
     */
    private Integer createUserId;

    /**
     * 学号
     */
    private String sno;

    /**
     * 年级ID
     */
    private Integer gradeId;

    /**
     * 学龄段
     */
    private Integer gradeType;

    /**
     * 班级id
     */
    private Integer classId;

    /**
     * 学生姓名
     */
    @NotBlank(message = "学生姓名不能为空")
    private String name;

    /**
     * 性别 0-男 1-女
     */
    @Range(min=0, max=1)
    private Integer gender;

    /**
     * 出生日期
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @NotNull(message = "出生日期不能为空")
    private Date birthday;

    /**
     * 民族 0-汉族
     */
    private Integer nation;

    /**
     * 身份证号码
     */
    @Pattern(regexp = RegularUtils.REGULAR_ID_CARD, message = "身份证格式错误")
    @NotNull(message = "身份证号码不能为空")
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
     * 省代码
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long provinceCode;

    /**
     * 市代码
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long cityCode;

    /**
     * 区代码
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long areaCode;

    /**
     * 镇/乡代码
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long townCode;

    /**
     * 详细地址
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String address;

    /**
     * 头像
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer avatarFileId;

    /**
     * 当前情况
     */
    private String currentSituation;

    /**
     * 视力标签 0-零级、1-一级、2-二级、3-三级
     */
    private Integer visionLabel;

    /**
     * 最近筛选次数
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastScreeningTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态 0-启用 1-禁止 2-删除
     */
    private Integer status;

    /**
     * 戴镜类型
     */
    private Integer glassesType;

    /**
     * 是否近视
     */
    private Boolean isMyopia;

    /**
     * 是否远视
     */
    private Boolean isHyperopia;

    /**
     * 是否散光
     */
    private Boolean isAstigmatism;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 视力筛查次数
     */
    @TableField(exist = false)
    private Integer screeningCount;

    /**
     * 问卷数
     */
    @TableField(exist = false)
    private Integer questionnaireCount;

    /**
     * 就诊次数
     */
    @TableField(exist = false)
    private Integer numOfVisits;

    /**
     * 上传筛查学生时，判断学生需更新信息是否一致
     * 由于只有部分字段，所以不使用equals
     * @param excelStudent
     * @return
     */
    public boolean checkNeedUpdate(Student excelStudent) {
        return !StringUtils.equalsIgnoreCase(this.name, excelStudent.name) ||
                !this.gender.equals(excelStudent.gender) ||
                !StringUtils.equalsIgnoreCase(DateFormatUtil.format(this.birthday, DateFormatUtil.FORMAT_ONLY_DATE),DateFormatUtil.format(excelStudent.birthday, DateFormatUtil.FORMAT_ONLY_DATE)) ||
                (Objects.nonNull(excelStudent.nation) && (Objects.nonNull(this.nation)) && !this.nation.equals(excelStudent.nation)) ||
                !this.gradeId.equals(excelStudent.gradeId) ||
                !this.classId.equals(excelStudent.classId) ||
                !StringUtils.equalsIgnoreCase(this.sno, excelStudent.sno) ||
                (Objects.nonNull(excelStudent.provinceCode)) ||
                (Objects.nonNull(excelStudent.cityCode)) ||
                (Objects.nonNull(excelStudent.areaCode)) ||
                (Objects.nonNull(excelStudent.townCode)) ||
                (StringUtils.isNotBlank(excelStudent.address)) ||
                (StringUtils.isNotBlank(excelStudent.parentPhone) &&!StringUtils.equalsIgnoreCase(this.parentPhone, excelStudent.parentPhone));
    }

    /**
     * 视力标签预警
     *
     * @return String
     */
    public String visionLabel2Str() {
        if (Objects.isNull(visionLabel)) {
            return "";
        }
        switch (visionLabel){
            case 0:return "0级预警";
            case 1:return "1级预警";
            case 2:return "2级预警";
            case 3:return "3级预警";
            default: return "";
        }
    }

    /**
     * 视力情况
     *
     * @return 视力情况
     */
    public String situation2Str() {
        StringBuilder result = new StringBuilder();
        if (Objects.nonNull(glassesType)) {
            result.append(GlassesType.get(glassesType).desc).append("、");
        }
        if (Objects.nonNull(isMyopia)) {
            if (isMyopia) {
                result.append("近视、");
            }
        }
        if (Objects.nonNull(isHyperopia)) {
            if (isHyperopia) {
                result.append("远视、");
            }
        }
        if (Objects.nonNull(isAstigmatism)) {
            if (isAstigmatism) {
                result.append("散光");
            }
        }
        return result.toString();
    }
}
