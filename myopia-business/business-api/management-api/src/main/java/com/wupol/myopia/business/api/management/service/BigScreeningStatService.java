package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.business.api.management.constant.BigScreeningProperties;
import com.wupol.myopia.business.api.management.domain.vo.BigScreeningVO;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.stat.domain.model.DistrictBigScreenStatistic;
import com.wupol.myopia.business.core.stat.service.DistrictBigScreenStatisticService;
import com.wupol.myopia.business.core.system.constants.BigScreeningMapConstants;
import com.wupol.myopia.business.core.system.service.BigScreenMapService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021/4/27
 **/
@Log4j2
@Service
public class BigScreeningStatService {

    @Autowired
    private ScreeningNoticeService screeningNoticeService;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private BigScreenMapService bigScreenMapService;
    @Autowired
    private DistrictBigScreenStatisticService districtBigScreenStatisticService;
    @Autowired
    private BigScreeningProperties bigScreeningProperties;
    @Autowired
    private BigScreenService bigScreenService;


    /**
     * 获取大屏数据
     *
     * @param screeningNotice
     * @param district
     * @return
     */
    // TODO: 开启缓存
//    @Cacheable(cacheNames = BigScreeningProperties.BIG_SCREENING_DATA_CACHE_KEY_PREFIX, key = "#screeningNotice.id + '_' + #district.id", cacheManager = BigScreeningMapConstants.BIG_SCREENING_MAP_CACHE_MANAGEMANT_BEAN_ID)
    public BigScreeningVO getBigScreeningVO(ScreeningNotice screeningNotice, District district, String districtName)  {
        //根据noticeId 和 districtId 查找数据
        DistrictBigScreenStatistic districtBigScreenStatistic = this.getDistrictBigScreenStatistic(screeningNotice, district.getId());
        //查找map数据
        Object provinceMapData = bigScreenMapService.getMapDataByDistrictId(district.getId());
        //对数据进行整合
        return BigScreeningVO.getNewInstance(screeningNotice, districtBigScreenStatistic, districtName, provinceMapData);
    }

    /**
     * 获取大屏数据
     * NOTES: 根据模式的不同获取数据的方式不同,如果是"TASK"模式,则从数据库表中查出数据,如果是"REALTIME"模式,则是实时计算.
     *
     * @param screeningNotice
     * @param districtId
     * @return
     */
    private DistrictBigScreenStatistic getDistrictBigScreenStatistic(ScreeningNotice screeningNotice, Integer districtId) {
        if (bigScreeningProperties.isDebug()) {
            return bigScreenService.generateResult(districtId, screeningNotice);
        }
        DistrictBigScreenStatistic districtBigScreenStatistic = districtBigScreenStatisticService.getByNoticeIdAndDistrictId(screeningNotice.getId(), districtId);
        if (districtBigScreenStatistic == null) {
            //如果是第一天的话,直接触发第一次计算
            districtBigScreenStatistic = bigScreenService.generateResultAndSave(districtId, screeningNotice);
        }
        return districtBigScreenStatistic;
    }

    /**
     * 调用统计大屏数据
     *
     */
    public void statisticBigScreen() {
        log.info("开始统计大屏数据（仅统计省级部门所发的筛查通知）......");
        //找到所有省级部门
        List<GovDept> proviceGovDepts = govDeptService.getProvinceGovDept();
        Set<Integer> govDeptIds = proviceGovDepts.stream().map(GovDept::getId).collect(Collectors.toSet());
        //通过所有省级部门查找所有通知
        List<ScreeningNotice> screeningNotices = screeningNoticeService.getNoticeByReleaseOrgId(govDeptIds, ScreeningNotice.TYPE_GOV_DEPT);
        //发布过的省级部门的省地区id
        Map<Integer, List<ScreeningNotice>> districtIdNoticeListMap = screeningNotices.stream().collect(Collectors.groupingBy(ScreeningNotice::getDistrictId));
        //将每个省最新发布的notice拿出来
        Set<Integer> provinceDistrictIds = districtIdNoticeListMap.keySet();
        for (Integer provinceDistrictId : provinceDistrictIds) {
            List<ScreeningNotice> screeningNoticeList = districtIdNoticeListMap.get(provinceDistrictId);
            //生成数据
            batchGenerateResultAndSave(provinceDistrictId, screeningNoticeList);
        }
        log.info("统计大屏数据完成。");
    }

    /**
     * 生成数据
     *
     * @param provinceDistrictId
     * @param districtIdNotices
     */
    public void batchGenerateResultAndSave(Integer provinceDistrictId, List<ScreeningNotice> districtIdNotices) {
        for (ScreeningNotice screeningNotice : districtIdNotices) {
            log.info("【统计大屏数据】noticeId = {}，通知：{}", screeningNotice.getId(), screeningNotice.getTitle());
            try {
                bigScreenService.generateResultAndSave(provinceDistrictId, screeningNotice);
            } catch (Exception e) {
                log.error("【统计大屏数据】失败！noticeId = {}，通知：{}", screeningNotice.getId(), screeningNotice.getTitle(), e);
            }
        }
    }

}
