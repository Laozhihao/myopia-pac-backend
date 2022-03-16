package com.wupol.myopia.business.core.parent.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @Author xjl
 * @Date 2022/3/8 17:31
 */
@Data
public class WorkOrderRequestDTO {

    /**
     * 工单id
     */
    @NotNull(message = "工单id不能为空")
    private Integer workOrderId;

    /**
     * 学生id
     */
    @NotNull(message = "学生id不能为空")
    private Integer studentId;

    /**
     * 学生姓名
     */
    @NotNull(message = "学生姓名不能为空")
    private String name;

    /**
     * 性别 0-男 1-女
     */
    @NotNull(message = "学生性别不能为空")
    private Integer gender;

    /**
     * 护照
     */
    private String passport;

    /**
     * 身份证号码
     */
    private String idCard;

    /**
     * 出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "学生出生日期不能为空")
    private Date birthday;

    /**
     * 学校ID
     */
    @NotNull(message = "学校id不能为空")
    private Integer schoolId;

    /**
     * 年级ID
     */
    @NotNull(message = "年级id不能为空")
    private Integer gradeId;

    /**
     * 班级ID
     */
    @NotNull(message = "班级id不能为空")
    private Integer classId;

    /**
     * 学号
     */
    private String sno;

    /**
     * 留言内容
     */
    private String content;

    /**
     * 筛查id
     */
    private Integer screeningId;

    /**
     * 筛查编号
     */
    private Long screeningCode;

    /**
     * 筛查日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "筛查日期不能为空")
    private Date screeningDate;

    /**
     * 筛查标题
     */
    private String screeningTitle;

}
