package com.wupol.myopia.migrate.domain.dos;

import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.migrate.domain.model.SysStudentEyeSimple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @Author HaoHao
 * @Date 2022/3/31
 **/
@AllArgsConstructor
@Accessors(chain = true)
@Data
public class PlanAndStudentDO {

    /**
     * 筛查计划
     */
    private ScreeningPlan screeningPlan;

    /**
     * 当前计划待迁移筛查数据的学生（map的key为schoolId）
     */
    private Map<String, List<SysStudentEyeSimple>> currentPlanStudentGroupBySchoolIdMap;

    /**
     * 筛查人员用户ID
     */
    private Integer screeningStaffUserId;
}
