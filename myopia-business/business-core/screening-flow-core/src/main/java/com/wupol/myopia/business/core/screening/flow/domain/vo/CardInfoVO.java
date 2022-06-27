package com.wupol.myopia.business.core.screening.flow.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 学生档案卡基本信息
 *
 * @author Simple4H
 */
@Accessors(chain = true)
@Data
public class CardInfoVO {

    /**
     * 名字
     */
    private String name;

    /**
     * 生日
     */
    private Date birthday;

    /**
     * 身份证号码
     */
    private String idCard;

    /**
     * 性别 0-男 1-女
     */
    private Integer gender;

    /**
     * 检查时间
     */
    private Date screeningDate;

    /**
     * 班级信息
     */
    private String className;

    /**
     * 年级信息
     */
    private String gradeName;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 行政区域名
     */
    private String districtName;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 学号
     */
    private String sno;

    /**
     * 家长电话
     */
    private String parentPhone;

    /**
     * 不配合个数
     */
    private Integer countNotCooperate;

    /**
     * 民族 1-汉族、2-回族
     */
    private Integer nation;

    /**
     * 民族描述
     */
    private String nationDesc;

    /**
     * 护照号
     */
    private String passport;

    /**
     * 学龄段
     */
    private Integer schoolType;
}
