package com.wupol.myopia.business.api.management.domain.builder;

import com.wupol.myopia.business.api.management.domain.vo.*;
import com.wupol.myopia.business.core.stat.domain.dos.CommonDiseaseDO;
import com.wupol.myopia.business.core.stat.domain.dos.KindergartenVisionAnalysisDO;
import com.wupol.myopia.business.core.stat.domain.dos.PrimarySchoolAndAboveVisionAnalysisDO;
import com.wupol.myopia.business.core.stat.domain.dos.SaprodontiaDO;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import lombok.experimental.UtilityClass;
import org.springframework.beans.BeanUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 小学及以上
 *
 * @author hang.yuan 2022/6/20 11:44
 */
@UtilityClass
public class ItemBuilder {


    public static PrimarySchoolAndAboveItem getPrimarySchoolAndAboveItem(Boolean isSchool,
                                                    ScreeningResultStatistic screeningResultStatistic,
                                                    String schoolDistrictName, Integer districtId,
                                                    Map<String, Boolean> hasRescreenReportMap){
        PrimarySchoolAndAboveItem item;
        if (Objects.equals(isSchool,Boolean.TRUE)){
            item = new SchoolPrimarySchoolAndAboveResultVO.Item();
            Boolean hasRescreenReport = Optional.ofNullable(hasRescreenReportMap.get(screeningResultStatistic.getScreeningPlanId() + "_" + screeningResultStatistic.getSchoolId())).orElse(Boolean.FALSE);
            item.setHasRescreenReport(hasRescreenReport);
        }else {
            item = new PrimarySchoolAndAboveResultVO.Item();
        }

        BeanUtils.copyProperties(screeningResultStatistic,item);
        item.setScreeningRangeName(schoolDistrictName);
        item.setDistrictId(districtId);
        item.setIsKindergarten(Boolean.FALSE);
        PrimarySchoolAndAboveVisionAnalysisDO visionAnalysis=null;
        if (Objects.nonNull(screeningResultStatistic.getVisionAnalysis())){
            visionAnalysis = (PrimarySchoolAndAboveVisionAnalysisDO)screeningResultStatistic.getVisionAnalysis();
            BeanUtils.copyProperties(visionAnalysis,item);
        }

        if (Objects.equals(0,screeningResultStatistic.getScreeningType())){
            VisionItem visionItem = new VisionItem();
            if (Objects.nonNull(visionAnalysis)){
                BeanUtils.copyProperties(visionAnalysis,visionItem);
            }
            item.setVisionItem(visionItem);
        }else {
            SaprodontiaDO saprodontia = screeningResultStatistic.getSaprodontia();
            CommonDiseaseDO commonDisease = screeningResultStatistic.getCommonDisease();
            CommonDiseaseItem commonDiseaseItem= new CommonDiseaseItem();
            if (Objects.nonNull(saprodontia)){
                commonDiseaseItem.setDmftNum(saprodontia.getDmftNum());
                commonDiseaseItem.setDmftRatio(saprodontia.getDmftRatio());
            }
            if (Objects.nonNull(commonDisease)){
                BeanUtils.copyProperties(commonDisease,commonDiseaseItem);
            }

            item.setCommonDiseaseItem(commonDiseaseItem);
        }

        return item;
    }

    public static KindergartenItem getKindergartenItem(Boolean isSchool,ScreeningResultStatistic screeningResultStatistic,
                                                 String schoolDistrictName, Integer districtId,
                                                 Map<String, Boolean> hasRescreenReportMap){
        KindergartenItem item;
        if (Objects.equals(isSchool,Boolean.TRUE)){
            item = new SchoolKindergartenResultVO.Item();
            Boolean hasRescreenReport = Optional.ofNullable(hasRescreenReportMap.get(screeningResultStatistic.getScreeningPlanId() + "_" + screeningResultStatistic.getSchoolId())).orElse(Boolean.FALSE);
            item.setHasRescreenReport(hasRescreenReport);
        }else {
            item = new KindergartenResultVO.Item();
        }
        BeanUtils.copyProperties(screeningResultStatistic,item);
        item.setScreeningRangeName(schoolDistrictName);
        item.setDistrictId(districtId);
        item.setIsKindergarten(Boolean.TRUE);
        if (Objects.nonNull(screeningResultStatistic.getVisionAnalysis())){
            KindergartenVisionAnalysisDO visionAnalysis = (KindergartenVisionAnalysisDO)screeningResultStatistic.getVisionAnalysis();
            BeanUtils.copyProperties(visionAnalysis,item);
        }
        return item;
    }
}
