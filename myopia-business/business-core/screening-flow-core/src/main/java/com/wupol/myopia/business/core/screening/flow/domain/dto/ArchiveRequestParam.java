package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * 获取档案卡数据请求参数
 *
 * @Author HaoHao
 * @Date 2022/4/25
 **/
@Data
public class ArchiveRequestParam {

    /**
     * 导出类型，1-区域、2-学校、3-年级、4-班级、5-多个或单个学生
     */
    @NotNull(message = "type不能为空")
    private Integer type;

    /**
     * 模板ID
     */
    @NotNull(message = "templateId不能为空")
    private Integer templateId;

    /**
     * 筛查计划ID
     */
    @NotNull(message = "planId不能为空")
    private Integer planId;

    /**
     * 班级ID
     */
    @NotNull(message = "classId不能为空")
    private Integer classId;

    /**
     * 筛查学生ID集，type=5时必填
     */
    private Set<Integer> planStudentIds;

}
