package com.wupol.myopia.business.core.stat.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.BeanCopyUtil;
import com.wupol.myopia.business.core.stat.domain.mapper.ScreeningResultStatisticMapper;
import com.wupol.myopia.business.core.stat.domain.model.CommonDiseaseScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.domain.model.VisionScreeningResultStatistic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 筛查结果统计服务层
 * @author hang.yuan
 * @date 2022/4/7
 */
@Service
public class ScreeningResultStatisticService extends BaseService<ScreeningResultStatisticMapper,ScreeningResultStatistic> {


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


    /**
     *  保存筛查结果统计数据
     * @author hang.yuan
     * @date 2022/4/11
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveScreeningResultStatistic(VisionScreeningResultStatistic visionScreeningResultStatistic){
        ScreeningResultStatistic screeningResultStatistic = BeanCopyUtil.copyBeanPropertise(visionScreeningResultStatistic, ScreeningResultStatistic.class);
        saveOrUpdate(screeningResultStatistic);
    }





}
