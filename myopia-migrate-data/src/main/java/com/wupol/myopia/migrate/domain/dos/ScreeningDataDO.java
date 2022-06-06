package com.wupol.myopia.migrate.domain.dos;

import com.wupol.myopia.migrate.domain.model.SysStudentEye;
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
public class ScreeningDataDO {

    /**
     * 待迁移学生筛查数据
     */
    private List<SysStudentEye> sysStudentEyeList;
    /**
     * 新的学校ID
     */
    private Integer schoolId;
    /**
     * 新的筛查机构ID
     */
    private Integer screeningOrgId;
    /**
     * 筛查人员用户ID
     */
    private Integer screeningStaffUserId;
    /**
     * 筛查计划ID
     */
    private Integer planId;
}
