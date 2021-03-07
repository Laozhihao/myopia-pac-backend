package com.wupol.myopia.business.management.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.DistrictBigScreenStatisticMapper;
import com.wupol.myopia.business.management.domain.model.DistrictBigScreenStatistic;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import com.wupol.myopia.business.management.domain.vo.bigscreening.BigScreeningVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author HaoHao
 * @Date 2021-03-07
 */
@Service
public class DistrictBigScreenStatisticService extends BaseService<DistrictBigScreenStatisticMapper, DistrictBigScreenStatistic> {

    @Autowired
    private ScreeningNoticeService screeningNoticeService;
    /**
     * 获取最新的数据
     * @param currentUser
     */
    public BigScreeningVO getLatestData(CurrentUser currentUser) {
        DistrictBigScreenStatistic districtBigScreenStatistic = baseMapper.selectById(1);
        BigScreeningVO bigScreeningVO = new BigScreeningVO();
        bigScreeningVO.setRealScreening(districtBigScreenStatistic.getRealScreening());
        bigScreeningVO.setAmetropia(districtBigScreenStatistic.getAmetropia());
        bigScreeningVO.setAvgVision(districtBigScreenStatistic.getAvgVision());
        bigScreeningVO.setFocusObjects(districtBigScreenStatistic.getFocusObjects());
        bigScreeningVO.setMapData(districtBigScreenStatistic.getMapdata());
        bigScreeningVO.setLowVision(districtBigScreenStatistic.getLowVision());
        bigScreeningVO.setMyopia(districtBigScreenStatistic.getMyopia());
        bigScreeningVO.setTitle("某某省近视防控中心");
        bigScreeningVO.setValidDataNum(234243234L);
        ScreeningNotice screeningNotice = screeningNoticeService.getBaseMapper().selectById(2);
        bigScreeningVO.setScreeningTitle(screeningNotice.getTitle());
        bigScreeningVO.setScreeningEndTime(screeningNotice.getEndTime());
        bigScreeningVO.setScreeningStartTime(screeningNotice.getStartTime());
        return bigScreeningVO;
    }
}
