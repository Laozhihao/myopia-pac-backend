package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.stat.domain.dos.AvgVisionDO;
import com.wupol.myopia.business.core.stat.domain.dos.BigScreenScreeningDO;
import com.wupol.myopia.business.core.stat.domain.model.DistrictBigScreenStatistic;
import lombok.Data;

import java.util.Date;

/**
 * @Description
 * @Date 2021/3/7 20:46
 * @Author by Jacob
 */
@Data
public class BigScreeningVO {


    private String title;
    /**
     * screeningTitle
     */
    private String screeningTitle;
    /**
     * screeningStartTime
     */
    private Date screeningStartTime;
    /**
     * screeningEndTime
     */
    private Date screeningEndTime;
    /**
     * validDataNum
     */
    private Long validDataNum;
    /**
     * planScreeningNum
     */
    private Long planScreeningNum;
    /**
     * progressRate
     */
    private Double progressRate;
    /**
     * realScreeningNum
     */
    private Long realScreeningNum;

    /**
     * realScreening
     */
    private BigScreenScreeningDO realScreening;
    /**
     * lowVision
     */
    private BigScreenScreeningDO lowVision;
    /**
     * myopia
     */
    private BigScreenScreeningDO myopia;
    /**
     * ametropia
     */
    private BigScreenScreeningDO ametropia;
    /**
     * focusOjects
     */
    private BigScreenScreeningDO focusObjects;
    /**
     * 平均视力
     */
    private AvgVisionDO avgVision;
    /**
     * 地图数据
     */
    private Object mapData;


    private BigScreeningVO() {

    }


    /**
     * 空对象
     */
    public static BigScreeningVO getImmutableEmptyInstance() {
        return new BigScreeningVO();
    }

    /**
     * 创建对象
     *
     * @param screeningNotice
     * @param districtBigScreenStatistic
     * @param districtName
     * @return
     */
    public static BigScreeningVO getNewInstance(ScreeningNotice screeningNotice, DistrictBigScreenStatistic districtBigScreenStatistic, String districtName) {
        BigScreeningVO bigScreeningVO = new BigScreeningVO();
        bigScreeningVO.setRealScreening(districtBigScreenStatistic.getRealScreening());
        bigScreeningVO.setAmetropia(districtBigScreenStatistic.getAmetropia());
        bigScreeningVO.setAvgVision(districtBigScreenStatistic.getAvgVision());
        bigScreeningVO.setFocusObjects(districtBigScreenStatistic.getFocusObjects());
        bigScreeningVO.setMapData(districtBigScreenStatistic.getMapdata());
        bigScreeningVO.setLowVision(districtBigScreenStatistic.getLowVision());
        bigScreeningVO.setMyopia(districtBigScreenStatistic.getMyopia());
        bigScreeningVO.setTitle(districtName + "近视防控中心");
        bigScreeningVO.setValidDataNum(districtBigScreenStatistic.getValidDataNum());
        bigScreeningVO.setPlanScreeningNum(districtBigScreenStatistic.getPlanScreeningNum());
        bigScreeningVO.setRealScreeningNum(districtBigScreenStatistic.getRealScreeningNum());
        bigScreeningVO.setProgressRate(districtBigScreenStatistic.getProgressRate());
        bigScreeningVO.setScreeningTitle(screeningNotice.getTitle());
        bigScreeningVO.setScreeningEndTime(screeningNotice.getEndTime());
        bigScreeningVO.setScreeningStartTime(screeningNotice.getStartTime());
        return bigScreeningVO;
    }
}
