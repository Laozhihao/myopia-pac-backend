package com.wupol.myopia.business.core.stat.service;

import com.wupol.myopia.business.core.stat.domain.model.VisionScreeningResultStatistic;
import org.springframework.stereotype.Service;

/**
 * 视力筛查结果统计
 *
 * @author hang.yuan 2022/4/15 17:06
 */
@Service
public class VisionScreeningResultStatisticService extends ScreeningResultStatisticService{


    /**
     * 保存视力筛查结果统计
     * @author hang.yuan
     * @date 2022/4/11
     */
    public void saveVisionScreeningResultStatistic(VisionScreeningResultStatistic visionScreeningResultStatistic){
        Integer schoolType = visionScreeningResultStatistic.getSchoolType();
        if (8 == schoolType){
            saveKindergartenVisionScreening(visionScreeningResultStatistic);
        }else {
            savePrimarySchoolAndAboveVisionScreening(visionScreeningResultStatistic);
        }
    }

    /**
     * 保存幼儿园视力筛查
     * @author hang.yuan
     * @date 2022/4/11
     */
    private void saveKindergartenVisionScreening(VisionScreeningResultStatistic visionScreeningResultStatistic){
        saveScreeningResultStatistic(visionScreeningResultStatistic);
    }

    /**
     * 保存小学及以上视力筛查
     * @author hang.yuan
     * @date 2022/4/11
     */
    private void savePrimarySchoolAndAboveVisionScreening(VisionScreeningResultStatistic visionScreeningResultStatistic){
        saveScreeningResultStatistic(visionScreeningResultStatistic);
    }


}
