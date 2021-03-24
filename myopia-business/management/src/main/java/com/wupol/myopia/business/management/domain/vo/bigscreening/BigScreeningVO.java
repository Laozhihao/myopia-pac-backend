package com.wupol.myopia.business.management.domain.vo.bigscreening;

import com.wupol.myopia.business.management.domain.model.DistrictBigScreenStatistic;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * realScreening
     */
    private Object realScreening;
    /**
     * lowVision
     */
    private Object lowVision;
    /**
     * myopia
     */
    private Object myopia;
    /**
     * ametropia
     */
    private Object ametropia;
    /**
     * focusOjects
     */
    private Object focusObjects;
    /**
     * avgVision
     */
    private Object avgVision;
    /**
     * mapData
     */
    private Object mapData;


    private BigScreeningVO() {

    }

    /**
     *  创建对象
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
        bigScreeningVO.setTitle(districtName+"近视防控中心");
        bigScreeningVO.setValidDataNum(districtBigScreenStatistic.getValidDataNum());
        bigScreeningVO.setScreeningTitle(screeningNotice.getTitle());
        bigScreeningVO.setScreeningEndTime(screeningNotice.getEndTime());
        bigScreeningVO.setScreeningStartTime(screeningNotice.getStartTime());
        return bigScreeningVO;
    }
}
