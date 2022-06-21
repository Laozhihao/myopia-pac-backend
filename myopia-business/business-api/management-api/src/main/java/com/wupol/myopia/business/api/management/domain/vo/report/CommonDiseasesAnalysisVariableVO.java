package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

/**
 * 常见病分析变量
 *
 * @author hang.yuan
 * @date 2022/6/6
 */
@Data
public class CommonDiseasesAnalysisVariableVO {
    /**
     * 筛查人数(实际参与筛查的学生数)
     */
    private Integer validScreeningNum;
    /**
     * 脊柱弯曲异常数
     */
    private Integer abnormalSpineCurvatureNum;

    /**
     * 龋均
     */
    private CommonDiseasesItemVO dmft;

    /**
     * 有龋
     */
    private CommonDiseasesItemVO saprodontia;

    /**
     * 龋失
     */
    private CommonDiseasesItemVO saprodontiaLoss;

    /**
     * 龋补
     */
    private CommonDiseasesItemVO saprodontiaRepair;

    /**
     * 龋患（失、补）
     */
    private CommonDiseasesItemVO saprodontiaLossAndRepair;

    /**
     * 龋患（失、补）牙数
     */
    private CommonDiseasesItemVO saprodontiaLossAndRepairTeeth;

    /**
     * 超重
     */
    private CommonDiseasesItemVO overweight;

    /**
     * 肥胖人数
     */
    private CommonDiseasesItemVO obese;

    /**
     * 血压偏高人数
     */
    private CommonDiseasesItemVO highBloodPressure;

    /**
     * 脊柱弯曲异常人数
     */
    private CommonDiseasesItemVO abnormalSpineCurvature;


}