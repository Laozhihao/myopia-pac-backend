package com.wupol.myopia.business.api.management.service.report;

import cn.hutool.core.collection.CollectionUtil;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseasesAnalysisVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictSchoolScreeningMonitorVO;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 各个学校筛查情况
 *
 * @author hang.yuan 2022/5/25 15:06
 */
@Service
public class SchoolScreeningMonitorService {


    /**
     * 各学校筛查情况
     */
    public void getDistrictSchoolScreeningMonitorVO(List<StatConclusion> statConclusionList, DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO) {

        if (CollectionUtil.isEmpty(statConclusionList)){
            return;
        }
        DistrictSchoolScreeningMonitorVO districtSchoolScreeningMonitorVO = new DistrictSchoolScreeningMonitorVO();
        //说明变量
        getSchoolScreeningMonitorVariableVO(statConclusionList,districtSchoolScreeningMonitorVO);
        //表格数据
        getSchoolScreeningMonitorTableList(statConclusionList,districtSchoolScreeningMonitorVO);

        districtCommonDiseasesAnalysisVO.setDistrictSchoolScreeningMonitorVO(districtSchoolScreeningMonitorVO);

    }

    private void getSchoolScreeningMonitorVariableVO(List<StatConclusion> statConclusionList, DistrictSchoolScreeningMonitorVO districtSchoolScreeningMonitorVO) {

    }

    private void getSchoolScreeningMonitorTableList(List<StatConclusion> statConclusionList, DistrictSchoolScreeningMonitorVO districtSchoolScreeningMonitorVO) {

    }
}
