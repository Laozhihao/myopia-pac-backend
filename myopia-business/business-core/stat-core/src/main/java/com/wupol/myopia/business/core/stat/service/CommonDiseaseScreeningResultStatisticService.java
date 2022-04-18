package com.wupol.myopia.business.core.stat.service;

import com.wupol.myopia.business.core.stat.domain.model.CommonDiseaseScreeningResultStatistic;
import org.springframework.stereotype.Service;

/**
 * 常见病筛查结果统计
 *
 * @author hang.yuan 2022/4/15 17:06
 */
@Service
public class CommonDiseaseScreeningResultStatisticService extends ScreeningResultStatisticService{



    /**
     * 保存常见病筛查结果统计
     * @author hang.yuan
     * @date 2022/4/11
     */
    public void saveCommonDiseaseScreeningResultStatistic(CommonDiseaseScreeningResultStatistic commonDiseaseScreeningResultStatistic){
        Integer schoolType = commonDiseaseScreeningResultStatistic.getSchoolType();
        if (8 == schoolType){
            saveKindergartenCommonDiseaseScreening(commonDiseaseScreeningResultStatistic);
        }else {
            savePrimarySchoolAndAboveCommonDiseaseScreening(commonDiseaseScreeningResultStatistic);
        }
    }


    /**
     * 保存幼儿园常见病筛查
     * @author hang.yuan
     * @date 2022/4/11
     */
    private void saveKindergartenCommonDiseaseScreening(CommonDiseaseScreeningResultStatistic commonDiseaseScreeningResultStatistic){
        saveScreeningResultStatistic(commonDiseaseScreeningResultStatistic);
    }

    /**
     * 保存小学及以上常见病筛查
     * @author hang.yuan
     * @date 2022/4/11
     */
    private void savePrimarySchoolAndAboveCommonDiseaseScreening(CommonDiseaseScreeningResultStatistic commonDiseaseScreeningResultStatistic){
        saveScreeningResultStatistic(commonDiseaseScreeningResultStatistic);
    }

}
