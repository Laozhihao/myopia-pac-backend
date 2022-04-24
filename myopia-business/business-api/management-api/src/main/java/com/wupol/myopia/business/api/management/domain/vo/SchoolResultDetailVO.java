package com.wupol.myopia.business.api.management.domain.vo;

import cn.hutool.core.collection.CollectionUtil;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

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



    public void setItemData(boolean isKindergarten,Integer screeningNoticeId,
                            Integer screeningType,School school,
                            List<ScreeningResultStatistic> screeningResultStatistics) {

        if(isKindergarten){
            if (CollectionUtil.isNotEmpty(screeningResultStatistics)){
                KindergartenResultDetailVO detailVO=new KindergartenResultDetailVO();
                detailVO.setBaseData(screeningNoticeId,school.getDistrictId(),screeningType,school.getName());
                detailVO.setItemData(screeningResultStatistics.get(0));
                this.kindergartenResultDetail=detailVO;
            }
        }else {
            if (CollectionUtil.isNotEmpty(screeningResultStatistics)){
                PrimarySchoolAndAboveResultDetailVO detailVO = new PrimarySchoolAndAboveResultDetailVO();
                detailVO.setBaseData(screeningNoticeId,school.getDistrictId(),screeningType,school.getName());
                detailVO.setItemData(screeningResultStatistics.get(0));
                this.primarySchoolAndAboveResultDetail=detailVO;
            }
        }

    }
}
