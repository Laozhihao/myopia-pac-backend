package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.business.api.management.constant.BigScreeningProperties;
import com.wupol.myopia.business.api.management.domain.builder.BigScreenStatDataBuilder;
import com.wupol.myopia.business.api.management.domain.builder.DistrictBigScreenStatisticBuilder;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.stat.domain.dto.BigScreenStatDataDTO;
import com.wupol.myopia.business.core.stat.domain.model.DistrictBigScreenStatistic;
import com.wupol.myopia.business.core.stat.service.DistrictBigScreenStatisticService;
import com.wupol.myopia.business.core.system.service.BigScreenMapService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 大屏业务处理
 *
 * @Author lzh
 * @Date 2023-03-29
 */
@Service
public class BigScreenService {

    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private BigScreenMapService bigScreenMapService;
    @Autowired
    private DistrictBigScreenStatisticService districtBigScreenStatisticService;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private SchoolService schoolService;

    /**
     * 生成结果
     *
     * @param provinceDistrictId
     * @param screeningNotice
     * @return
     */
    @CacheEvict(value = BigScreeningProperties.BIG_SCREENING_DATA_CACHE_KEY_PREFIX,key = "#result.screeningNoticeId + '_' + #result.districtId")
    public DistrictBigScreenStatistic generateResultAndSave(Integer provinceDistrictId, ScreeningNotice screeningNotice) {
        DistrictBigScreenStatistic districtBigScreenStatistic = this.generateResult(provinceDistrictId, screeningNotice);
        if (districtBigScreenStatistic != null) {
            districtBigScreenStatisticService.saveOrUpdateByDistrictIdAndNoticeId(districtBigScreenStatistic);
        }
        return districtBigScreenStatistic;
    }

    /**
     * 生成某个省的数据
     *
     * @param provinceDistrictId
     * @param screeningNotice
     * @return
     */
    public DistrictBigScreenStatistic generateResult(Integer provinceDistrictId, ScreeningNotice screeningNotice) {
        //根据条件查找所有的元素：条件 cityDistrictIds 非复测 有效
        List<BigScreenStatDataDTO> bigScreenStatDataDTOs = getByNoticeIdAndDistrictIds(screeningNotice.getId());
        //实际筛查数量
        int realScreeningNum = CollectionUtils.size(bigScreenStatDataDTOs);
        //获取地图数据
        Map<Integer, List<Double>> cityCenterLocationMap = bigScreenMapService.getCityCenterLocationByDistrictId(provinceDistrictId);
        //将基本数据放入构造器
        bigScreenStatDataDTOs = bigScreenStatDataDTOs.stream().filter(BigScreenStatDataDTO::getIsValid).collect(Collectors.toList());
        int realValidScreeningNum = CollectionUtils.size(bigScreenStatDataDTOs);
        DistrictBigScreenStatisticBuilder districtBigScreenStatisticBuilder = DistrictBigScreenStatisticBuilder.getBuilder()
                .setRealValidScreeningNum((long) realValidScreeningNum)
                .setRealScreeningNum((long) realScreeningNum)
                .setDistrictId(provinceDistrictId)
                .setCityCenterMap(cityCenterLocationMap)
                .setNoticeId(screeningNotice.getId())
                .setPlanScreeningNum(Long.valueOf(screeningPlanSchoolStudentService.countPlanSchoolStudentByNoticeId(screeningNotice.getId())));
        if (realScreeningNum > 0 && realValidScreeningNum > 0) {
            //更新城市名
            bigScreenStatDataDTOs = this.updateCityName(bigScreenStatDataDTOs, districtService.getCityAllDistrictIds(provinceDistrictId));
            // 设置学校名
            generateSchoolName(bigScreenStatDataDTOs);
            //构建数据
            districtBigScreenStatisticBuilder.setBigScreenStatDataDTOList(bigScreenStatDataDTOs);
        }
        return districtBigScreenStatisticBuilder.build();
    }

    /**
     * 获取通知
     *
     * @param noticeId
     * @param noticeId
     * @return
     */
    public List<BigScreenStatDataDTO> getByNoticeIdAndDistrictIds(Integer noticeId) {
        List<StatConclusion> statConclusionList = statConclusionService.findByList(new StatConclusion().setSrcScreeningNoticeId(noticeId).setIsRescreen(false));
        return this.getBigScreenStatDataDTOList(statConclusionList);
    }

    /**
     * 获取大屏统计的基础数据
     * @param statConclusionList
     * @return
     */
    private List<BigScreenStatDataDTO> getBigScreenStatDataDTOList(List<StatConclusion> statConclusionList) {
        return statConclusionList.stream().map(BigScreenStatDataBuilder::build).collect(Collectors.toList());
    }

    /**
     * 更新大屏数据的城市名
     *
     * @param bigScreenStatDataDTOs
     * @param districtSetMap
     */
    private List<BigScreenStatDataDTO> updateCityName(List<BigScreenStatDataDTO> bigScreenStatDataDTOs, Map<District, Set<Integer>> districtSetMap) {
        return bigScreenStatDataDTOs.stream().peek(bigScreenStatDataDTO -> {
            Set<District> districtSet = districtSetMap.keySet();
            for (District cityDistrict : districtSet) {
                Set<Integer> districtIds = districtSetMap.get(cityDistrict);
                if (districtIds.contains(bigScreenStatDataDTO.getDistrictId()) || cityDistrict.getId().equals(bigScreenStatDataDTO.getDistrictId())) {
                    bigScreenStatDataDTO.setCityDistrictId(cityDistrict.getId());
                    bigScreenStatDataDTO.setCityDistrictName(cityDistrict.getName());
                    break;
                }
            }
        }).collect(Collectors.toList());
    }

    /**
     * 设置学校名
     *
     * @param bigScreenStatDataDTOs bigScreenStatDataDTOs
     */
    private void generateSchoolName(List<BigScreenStatDataDTO> bigScreenStatDataDTOs) {
        Map<Integer, School> schoolMap = schoolService.getSchoolMap(bigScreenStatDataDTOs, BigScreenStatDataDTO::getSchoolId);
        bigScreenStatDataDTOs.forEach(bigScreenStatData -> {
            School school = schoolMap.getOrDefault(bigScreenStatData.getSchoolId(), new School());
            bigScreenStatData.setSchoolName(school.getName());
            bigScreenStatData.setSchoolType(school.getType());
        });
    }

}
