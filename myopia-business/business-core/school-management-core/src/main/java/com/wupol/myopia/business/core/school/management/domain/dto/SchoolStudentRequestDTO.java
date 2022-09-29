package com.wupol.myopia.business.core.school.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 学校端-学生列表请求DTO
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SchoolStudentRequestDTO {

    /**
     * 学号
     */
    private String sno;

    /**
     * 名称
     */
    private String name;

    /**
     * 年级Id
     */
    private Integer gradeId;

    /**
     * 班级Id
     */
    private Integer classId;

    /**
     * 预警等级
     */
    private Integer visionLabel;

    /**
     * 身份证
     */
    private String idCard;

    /**
     * 护照
     */
    private String passport;

    /**
     * 是否绑定公众号
     */
    private Boolean isBindMp;

    /**
     * 身份证/护照
     */
    private String idCardOrPassportLike;
}
