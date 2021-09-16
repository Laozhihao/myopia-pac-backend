package com.wupol.myopia.business.core.screening.flow.domain.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * 学生查询
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class ScreeningStudentQueryDTO extends StudentExtraDTO {
    /** 名称 */
    private String nameLike;
    /** 身份证 */
    private String idCardLike;
    /** 学号 */
    private String snoLike;
    /** 手机号 */
    private String phoneLike;
    /** 导出本地报告数据起始时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startDate;
    /** 导出本地报告数据结束时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startScreeningTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
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
    /**
     * 年级ID
     */
    private Integer gradeId;
    /**
     * 班级D
     */
    private Integer classId;
    /**
     * 筛查机构ID
     */
    private Integer screeningOrgId;
    /**
     * 筛查计划ID集合
     */
    private List<Integer> planIds;
}
