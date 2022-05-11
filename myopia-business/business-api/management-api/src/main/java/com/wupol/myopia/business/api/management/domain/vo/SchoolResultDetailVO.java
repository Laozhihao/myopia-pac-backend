package com.wupol.myopia.business.api.management.domain.vo;

import cn.hutool.core.collection.CollectionUtil;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学校结果详情实体
 *
 * @author hang.yuan 2022/4/8 12:26
 */
@Accessors(chain = true)
@Data
public class SchoolResultDetailVO {


    /**
     * 幼儿园
     */
    private KindergartenResultDetailVO kindergartenResultDetail;

    /**
     * 小学及以上
     */
    private PrimarySchoolAndAboveResultDetailVO primarySchoolAndAboveResultDetail;



    public void setItemData(Integer screeningNoticeId,
                            Integer screeningType,School school,
                            List<ScreeningResultStatistic> screeningResultStatistics) {

        if (CollectionUtil.isNotEmpty(screeningResultStatistics)){
            Map<Integer, ScreeningResultStatistic> resultStatisticMap = screeningResultStatistics.stream().collect(Collectors.toMap(ScreeningResultStatistic::getSchoolType, Function.identity()));
            resultStatisticMap.forEach((schoolType,resultStatistic)->{
                if (Objects.equals(schoolType, SchoolEnum.TYPE_KINDERGARTEN.getType())){
                    KindergartenResultDetailVO detailVO=new KindergartenResultDetailVO();
                    detailVO.setBaseData(screeningNoticeId,school.getDistrictId(),screeningType,school.getName());
                    detailVO.setItemData(resultStatistic);
                    this.kindergartenResultDetail=detailVO;
                }else {
                    PrimarySchoolAndAboveResultDetailVO detailVO = new PrimarySchoolAndAboveResultDetailVO();
                    detailVO.setBaseData(screeningNoticeId,school.getDistrictId(),screeningType,school.getName());
                    detailVO.setItemData(resultStatistic);
                    this.primarySchoolAndAboveResultDetail=detailVO;
                }
            });
        }

    }
}
