package com.wupol.myopia.business.core.school.domain.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 筛查机构（学校）
 *
 * @author hang.yuan 2022/9/27 16:56
 */
@Data
public class ScreeningSchoolOrgDTO implements Serializable {

    /**
     * 查询开始时间
     *
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startTime;
    /**
     * 查询结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endTime;

    /**
     * 行政区域ID
     */
    @NotNull(message = "行政区域ID不能为空")
    private Integer districtId;


    /**
     *  是否需要查询是否有任务（配合开始结束时间）
     */
    @NotNull(message = "是否需要查询是否有任务不能为空")
    private Boolean needCheckHaveTask;

    /**
     * 限定筛查机构Id集
     */
    private List<Integer> ids;

    /**
     * 部门id
     */
    private Integer govDeptId;
}
