package com.wupol.myopia.business.core.school.domain.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * 学生查询
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StudentQueryDTO extends StudentExtraDTO {
    /** 名称 */
    private String nameLike;
    /** 身份证 */
    private String idCardLike;
    /** 学号 */
    private String snoLike;
    /** 手机号 */
    private String phoneLike;
    /** 导出本地报告数据起始时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;
    /** 导出本地报告数据结束时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startScreeningTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endScreeningTime;

    /**
     * 年级ids 逗号隔开
     */
    private String gradeIds;
    /** 年级ids */
    private List<Integer> gradeList;
    /** idCard列表 */
    private List<String> idCardList;

    /**
     * 视力标签
     */
    private String visionLabels;

    /**
     * 学校名称
     */
    private String schoolName;
    /**
     * 筛查计划ID
     */
    private Integer screeningPlanId;
    /**
     * 学校ID
     */
    private Integer schoolId;
}
