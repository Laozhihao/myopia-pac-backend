package com.wupol.myopia.business.api.management.domain.vo;

import cn.hutool.core.collection.CollectionUtil;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;

/**
 * 学校结果详情实体
 *
 * @author hang.yuan 2022/4/8 12:26
 */
@Accessors(chain = true)
@Data
public class SchoolResultDetailVO {

    /**
     * 通知id
     */
    private Integer screeningNoticeId;

    /**
     * 筛查类型 （0-视力筛查、1-常见病筛查）
     */
    private Integer screeningType;

    /**
     * 筛查范围、所属的地区id
     */
    private Integer districtId;

    /**
     * 筛查范围 范围名称
     */
    private String rangeName;

    /**
     * 幼儿园
     */
    private KindergartenResultDetailVO kindergartenResultDetail;

    /**
     * 小学及以上
     */
    private PrimarySchoolAndAboveResultDetailVO primarySchoolAndAboveResultDetail;

    public void setBaseData(ScreeningNotice screeningNotice, School school) {
        if (Objects.nonNull(screeningNotice)){
            this.screeningType = screeningNotice.getScreeningType();
        }
        if (Objects.nonNull(school)){
            this.districtId = school.getDistrictId();
            this.rangeName=school.getName();
        }
    }

    public void setItemData(boolean isKindergarten, List<ScreeningResultStatistic> screeningResultStatistics) {

        if(isKindergarten){
            if (CollectionUtil.isNotEmpty(screeningResultStatistics)){
                KindergartenResultDetailVO detailVO=new KindergartenResultDetailVO();
                detailVO.setBaseData(screeningType,districtId,rangeName);
                detailVO.setItemData(screeningResultStatistics.get(0));
                this.kindergartenResultDetail=detailVO;
            }
        }else {
            if (CollectionUtil.isNotEmpty(screeningResultStatistics)){
                PrimarySchoolAndAboveResultDetailVO detailVO = new PrimarySchoolAndAboveResultDetailVO();
                detailVO.setBaseData(screeningType,districtId,rangeName);
                detailVO.setItemData(screeningResultStatistics.get(0));
                this.primarySchoolAndAboveResultDetail=detailVO;
            }
        }

    }
}
