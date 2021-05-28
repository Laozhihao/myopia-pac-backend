package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.stat.domain.dos.AvgVisionDO;
import com.wupol.myopia.business.core.stat.domain.dos.BigScreenScreeningDO;
import com.wupol.myopia.business.core.stat.domain.model.DistrictBigScreenStatistic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description
 * @Date 2021/3/7 20:46
 * @Author by Jacob
 */
@Data
public class BigScreeningVO implements Serializable {

    private static final String TITLE_SUFFIX_STRING = "近视防控中心";
    /**
     * 大屏统计标题
     */
    private String title;
    /**
     * 筛查标题
     */
    private String screeningTitle;
    /**
     * 筛查开始时间
     */
    private Date screeningStartTime;
    /**
     * 筛查结束时间
     */
    private Date screeningEndTime;
    /**
     * 有效学生数量
     */
    private Long validDataNum;
    /**
     * 计划筛查数量
     */
    private Long planScreeningNum;
    /**
     * 筛查进度
     */
    private Double progressRate;
    /**
     * 实际筛查数
     */
    private Long realScreeningNum;

    /**
     * 有效学生的 学生数据情况
     */
    private BigScreenScreeningDO realScreening;
    /**
     * 有效学生的 视力低下情况
     */
    private BigScreenScreeningDO lowVision;
    /**
     * 有效学生的 近视情况
     */
    private BigScreenScreeningDO myopia;
    /**
     * 有效学生的 屈光不正情况
     */
    private BigScreenScreeningDO ametropia;
    /**
     * 有效学生的 重点视力对象情况
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
     * 这个方法是为了大屏统计还没来得及统计的时候显示一些基本信息
     * 空对象
     *
     * @param districtName
     * @param planStudentNum
     */
    public static BigScreeningVO getImmutableEmptyInstance(String districtName, long planStudentNum) {
        BigScreeningVO bigScreeningVO = new BigScreeningVO();
        bigScreeningVO.title = districtName + TITLE_SUFFIX_STRING;
        bigScreeningVO.planScreeningNum = planStudentNum;
        bigScreeningVO.progressRate = 0.0;
        return bigScreeningVO;
    }

    /**
     * 创建对象
     *
     * @param screeningNotice
     * @param districtBigScreenStatistic
     * @param districtName
     * @param provinceMapData 省的地图数据
     * @return
     */
    public static BigScreeningVO getNewInstance(ScreeningNotice screeningNotice, DistrictBigScreenStatistic districtBigScreenStatistic, String districtName, Object provinceMapData) {
        BigScreeningVO bigScreeningVO = new BigScreeningVO();
        bigScreeningVO.setRealScreening(districtBigScreenStatistic.getRealScreening());
        bigScreeningVO.setAmetropia(districtBigScreenStatistic.getAmetropia());
        bigScreeningVO.setAvgVision(districtBigScreenStatistic.getAvgVision());
        bigScreeningVO.setFocusObjects(districtBigScreenStatistic.getFocusObjects());
        bigScreeningVO.setMapData(provinceMapData);
        bigScreeningVO.setLowVision(districtBigScreenStatistic.getLowVision());
        bigScreeningVO.setMyopia(districtBigScreenStatistic.getMyopia());
        bigScreeningVO.setTitle(districtName + TITLE_SUFFIX_STRING);
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
