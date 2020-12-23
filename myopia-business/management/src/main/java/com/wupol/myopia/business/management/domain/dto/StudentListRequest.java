package com.wupol.myopia.business.management.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 学生列表请求体
 *
 * @author Simple4H
 */
@Getter
@Setter
public class StudentListRequest {

    /**
     * 学校id
     */
    @NotNull
    private Integer schoolId;

    /**
     * 学号
     */
    private Long sno;

    /**
     * 身份证
     */
    private String idCard;

    /**
     * 学生姓名
     */
    private String name;

    /**
     * 家长手机号码
     */
    private String parentPhone;

    /**
     * 性别 1-男 2-女
     */
    private Integer gender;

    /**
     * 年级id
     */
    private Integer gradeId;

    /**
     * 班级id
     */
    private Integer classId;

    /**
     * 开始筛查时间
     */
    private String startScreeningTime;

    /**
     * 结束筛查时间
     */
    private String endScreeningTime;

    /**
     * 视力标签
     */
    private String labels;

    /**
     * 页数
     */
    private Integer current;

    /**
     * 页码
     */
    private Integer size;
}
