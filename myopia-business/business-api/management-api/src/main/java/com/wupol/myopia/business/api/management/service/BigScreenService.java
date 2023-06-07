package com.wupol.myopia.business.api.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.business.api.management.domain.builder.BigScreenStatDataBuilder;
import com.wupol.myopia.business.api.management.domain.builder.DistrictBigScreenStatisticBuilder;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
     * @param district        区域
     * @param screeningNotice 通知Id
     * @return DistrictBigScreenStatistic
     */
    public DistrictBigScreenStatistic generateResultAndSave(District district, ScreeningNotice screeningNotice) {
        DistrictBigScreenStatistic districtBigScreenStatistic = this.generateResult(district, screeningNotice);
        if (Objects.nonNull(districtBigScreenStatistic)) {
            districtBigScreenStatisticService.saveOrUpdateByDistrictIdAndNoticeId(districtBigScreenStatistic);
        }
        return districtBigScreenStatistic;
    }

    /**
     * 生成某个省的数据
     *
     * @param district
     * @param screeningNotice
     * @return
     */
    public DistrictBigScreenStatistic generateResult(District district, ScreeningNotice screeningNotice) {
        Integer districtId = district.getId();
        //根据条件查找所有的元素：条件 cityDistrictIds 非复测 有效
        List<BigScreenStatDataDTO> bigScreenStatDataDTOs = getByNoticeIdAndDistrictIds(screeningNotice.getId(), district);
        //实际筛查数量
        int realScreeningNum = CollectionUtils.size(bigScreenStatDataDTOs);
        //获取地图数据
        Map<String, List<Double>> cityCenterLocationMap = bigScreenMapService.getCityCenterLocationByDistrictId(districtId);
        //将基本数据放入构造器
        bigScreenStatDataDTOs = bigScreenStatDataDTOs.stream().filter(BigScreenStatDataDTO::getIsValid).collect(Collectors.toList());
        int realValidScreeningNum = CollectionUtils.size(bigScreenStatDataDTOs);
        DistrictBigScreenStatisticBuilder districtBigScreenStatisticBuilder = DistrictBigScreenStatisticBuilder.getBuilder()
                .setRealValidScreeningNum((long) realValidScreeningNum)
                .setRealScreeningNum((long) realScreeningNum)
                .setDistrictId(districtId)
                .setCityCenterMap(cityCenterLocationMap)
                .setNoticeId(screeningNotice.getId())
                .setIsProvince(districtService.isProvince(districtId))
                .setPlanScreeningNum(Long.valueOf(screeningPlanSchoolStudentService.countPlanSchoolStudentByNoticeId(screeningNotice.getId(), district)));
        if (realScreeningNum > 0 && realValidScreeningNum > 0) {
            //更新城市名
            bigScreenStatDataDTOs = this.updateCityName(bigScreenStatDataDTOs, districtService.getCityAllDistrictIds(districtId));
            // 设置学校名
            generateSchoolName(bigScreenStatDataDTOs);
            //构建数据
            districtBigScreenStatisticBuilder.setBigScreenStatDataDTOList(bigScreenStatDataDTOs);
        }
        return districtBigScreenStatisticBuilder.build();
    }

    /**
     * 获取筛查统计结果
     *
     * @param noticeId 通知
     * @param district 区域
     * @return List<BigScreenStatDataDTO>
     */
    public List<BigScreenStatDataDTO> getByNoticeIdAndDistrictIds(Integer noticeId, District district) {
        LambdaQueryWrapper<StatConclusion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatConclusion::getSrcScreeningNoticeId, noticeId).eq(StatConclusion::getIsRescreen, false);
        if (Objects.equals(districtService.isProvince(district), Boolean.FALSE)) {
            // 获取当前区域下的区域
            List<Integer> districtIds = districtService.getSpecificDistrictTreeAllDistrictIds(district.getDistrictId());
            queryWrapper.in(StatConclusion::getDistrictId, districtIds);
        }
        List<StatConclusion> statConclusionList = statConclusionService.list(queryWrapper);
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
