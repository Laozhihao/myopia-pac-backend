package com.wupol.myopia.business.api.management.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 学生预警跟踪档案
 *
 * @Author HaoHao
 * @Date 2021/10/20
 **/
@Accessors(chain = true)
@Data
public class StudentWarningArchiveVO {

    /** 筛查计划学生ID */
    private Integer screeningPlanSchoolStudentId;

    /** 学生ID */
    private Integer studentId;

    /** 学龄：5-幼儿园、0-小学、1-初中 */
    private Integer schoolAge;

    /** 性别 */
    private Integer gender;

    /** 年龄 */
    private Integer age;

    /** 身高 */
    private Float height;

    /** 眼镜类型 */
    private Integer glassesType;

    /** 近视预警等级 */
    private Integer myopiaLevel;

    /** 远视预警等级 */
    private Integer hyperopiaLevel;

    /** 散光预警等级 */
    private Integer astigmatismLevel;

    /**
     * 视力标签 0-零级、1-一级、2-二级、3-三级
     */
    private Integer visionLabel;

    /** 筛查时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date screeningDate;

    /** 筛查标题 */
    private String screeningTitle;

    /** 是否已经就诊 */
    private Boolean isVisited;

    /** 就诊结论(医生反馈) */
    private String visitResult;

    /** 配镜建议 */
    private Integer glassesSuggest;

    /** 课桌型号 */
    private List<Integer> deskType;

    /** 课桌建议高度 */
    private Integer deskAdviseHeight;

    /** 课椅型号 */
    private List<Integer> chairType;

    /** 课椅建议高度 */
    private Integer chairAdviseHeight;

}
