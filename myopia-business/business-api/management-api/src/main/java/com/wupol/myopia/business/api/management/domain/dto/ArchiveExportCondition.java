package com.wupol.myopia.business.api.management.domain.dto;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * 档案卡导出条件
 *
 * @Author HaoHao
 * @Date 2022/4/25
 **/
@Data
public class ArchiveExportCondition {

    /**
     * 导出类型，1-区域、2-学校、3-年级、4-班级、5-多个或单个学生
     */
    @NotNull(message = "type不能为空")
    private Integer type;

    /**
     * 筛查计划ID，type=2,、3、4、5时必填
     */
    private Integer planId;

    /**
     * 学校ID，type=2时必填
     */
    private Integer schoolId;

    /**
     * 年级ID，type=3时必填
     */
    private Integer gradeId;

    /**
     * 班级ID，type=4、5时必填
     */
    private Integer classId;

    /**
     * 筛查学生ID集，type=5时必填
     */
    private Set<Integer> planStudentIds;

    /**
     * 筛查通知ID，type=1时必填
     */
    private Integer noticeId;

    /**
     * 行政区域ID，type=1时必填
     */
    private Integer districtId;

    public String getPlanStudentIdsStr() {
        return StringUtils.join(planStudentIds, StrUtil.COMMA);
    }
}
