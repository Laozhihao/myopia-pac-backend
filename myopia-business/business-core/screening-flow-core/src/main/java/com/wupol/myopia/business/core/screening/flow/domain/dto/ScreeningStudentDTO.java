package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.common.utils.domain.model.NotificationConfig;
import com.wupol.myopia.business.common.utils.domain.model.ResultNoticeConfig;
import com.wupol.myopia.business.core.screening.flow.domain.dos.StudentDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 学生DTO
 *
 * @author Simple4H
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ScreeningStudentDTO extends StudentDO {
    /**
     * 年级名称
     */
    private String gradeName;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 护照
     */
    private String passport;

    /**
     * 学校ID
     */
    private Integer schoolId;

    /**
     * 民族描述
     */
    private String nationDesc;

    /**
     * 性别描述
     */
    private String genderDesc;

    /**
     * 筛查二维码地址
     */
    private String qrCodeUrl;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * token
     */
    private String token;

    /**
     * 筛查计划Id
     */
    private Integer planId;

    /**
     * 筛查计划学生Id
     */
    private Integer planStudentId;

    /**
     * 学生年龄
     */
    private Integer age;

    /**
     * 学生编码
     */
    private Long screeningCode;

    /**
     * 结果通知配置
     */
    private ResultNoticeConfig resultNoticeConfig;


    private NotificationConfig screeningOrgConfigs;

    /**
     * 结果通知配置文件URL
     */
    private String noticeQrCodeFileUrl;


    /**
     * 裸视力 右/左
     */
    private String nakedVision;

    /**
     * 矫正 右/左
     */
    private String correctedVision;

    /**
     * 球镜 右
     */
    private String rSph;

    /**
     * 球镜 左
     */
    private String lSph;

    /**
     * 柱镜 右
     */
    private String rCyl;
    /**
     * 柱镜 左
     */
    private String lCyl;

    /**
     * 轴位 右/左
     */
    private String axial;
    /**
     * 戴镜类型描述
     */
    private String glassesTypeDes;

    /**
     * 是否已经筛查过
     **/
    private Boolean hasScreening;

    /**
     * 筛查机构名称
     **/
    private String screeningOrgName;

    /**
     * 筛查时间
     */
    private Date screeningTime;

    /**
     * 筛查机构id
     */
    private Integer screeningOrgId;

    /**
     * 未做检查说明【0:无；1：请假；2：转学;3:其他】
     */
    private Integer state;

    /**
     * 筛查结果--是否复筛（0否，1是）
     */
    private Boolean isDoubleScreen;

    /**
     * 出生年月
     */
    private Date birthday;
}
