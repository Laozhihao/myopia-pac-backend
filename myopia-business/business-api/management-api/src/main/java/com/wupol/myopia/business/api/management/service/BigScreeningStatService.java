package com.wupol.myopia.business.api.management.service;

import com.alibaba.fastjson.JSON;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.api.management.constant.BigScreeningProperties;
import com.wupol.myopia.business.api.management.domain.builder.BigScreenStatDataBuilder;
import com.wupol.myopia.business.api.management.domain.builder.DistrictBigScreenStatisticBuilder;
import com.wupol.myopia.business.api.management.domain.vo.BigScreeningVO;
import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.stat.domain.dto.BigScreenStatDataDTO;
import com.wupol.myopia.business.core.stat.domain.model.DistrictBigScreenStatistic;
import com.wupol.myopia.business.core.stat.service.DistrictBigScreenStatisticService;
import com.wupol.myopia.business.core.system.service.BigScreenMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021/4/27
 **/
@Service
public class BigScreeningStatService {

    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private ScreeningNoticeService screeningNoticeService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private BigScreenMapService bigScreenMapService;
    @Autowired
    private DistrictBigScreenStatisticService districtBigScreenStatisticService;
    @Autowired
    private BigScreeningStatService bigScreeningStatService;
    @Autowired
    private DistrictBizService districtBizService;
    @Autowired
    private BigScreeningProperties bigScreeningProperties;


    /**
     * 获取大屏的数据
     *
     * @param currentUser
     */
    public BigScreeningVO getBigScreeningVO(CurrentUser currentUser, Integer noticeId) throws IOException {
        if (ObjectsUtil.hasNull(currentUser, noticeId)) {
            throw new ManagementUncheckedException("noticeId 或者 currentUser 不能为空");
        }
        //查找 district
        District district = districtBizService.getNotPlatformAdminUserDistrict(currentUser);
        if (district == null) {
            throw new ManagementUncheckedException("无法找到该用户的找到所在区域，user = " + JSON.toJSONString(currentUser));
        }
        //查找最新的notice
        ScreeningNotice screeningNotice = screeningNoticeService.getReleasedNoticeById(noticeId);
        if (screeningNotice == null) {
            throw new ManagementUncheckedException("无法找到该noticeId = " + noticeId);
        }
        return getBigScreeningVO(screeningNotice, district);
    }

    /**
     * 获取大屏数据
     *
     * @param screeningNotice
     * @param district
     * @return
     */
    public BigScreeningVO getBigScreeningVO(ScreeningNotice screeningNotice, District district) throws IOException {
        //根据noticeId 和 districtId 查找数据
        DistrictBigScreenStatistic districtBigScreenStatistic = this.getDistrictBigScreenStatistic(screeningNotice, district.getId());
        if (districtBigScreenStatistic == null) {
            //如果是第一天的话,直接触发第一次计算
            districtBigScreenStatistic = bigScreeningStatService.generateResultAndSave(district.getId(), screeningNotice);
            //long planStudentNum = screeningPlanService.getAllPlanStudentNumByNoticeId(screeningNotice.getId());
            //实际筛查学生数
            //return BigScreeningVO.getImmutableEmptyInstance(district.getName(),planStudentNum);
        }
        //查找map数据
        Object provinceMapData = bigScreenMapService.getMapDataByDistrictId(district.getId());
        //对数据进行整合
        return BigScreeningVO.getNewInstance(screeningNotice, districtBigScreenStatistic, district.getName(), provinceMapData);
    }

    /**
     * 获取大屏数据
     * NOTES: 根据模式的不同获取数据的方式不同,如果是"TASK"模式,则从数据库表中查出数据,如果是"REALTIME"模式,则是实时计算.
     *
     * @param screeningNotice
     * @param districtId
     * @return
     * @throws IOException
     */
    private DistrictBigScreenStatistic getDistrictBigScreenStatistic(ScreeningNotice screeningNotice, Integer districtId) throws IOException {
        if (bigScreeningProperties.isDebug()) {
            return bigScreeningStatService.generateResult(districtId, screeningNotice);
        }
        return districtBigScreenStatisticService.getByNoticeIdAndDistrictId(screeningNotice.getId(), districtId);
    }

    /**
     * 调用统计大屏数据
     *
     * @throws IOException
     */
    public void statisticBigScreen() throws IOException {
        //找到所有省级部门
        List<GovDept> proviceGovDepts = govDeptService.getProviceGovDept();
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
            execute(provinceDistrictId, screeningNoticeList);
        }
    }

    /**
     * 生成数据
     *
     * @param provinceDistrictId
     * @param districtIdNotices
     * @throws IOException
     */
    private void execute(Integer provinceDistrictId, List<ScreeningNotice> districtIdNotices) throws IOException {
        for (ScreeningNotice screeningNotice : districtIdNotices) {
            this.generateResultAndSave(provinceDistrictId, screeningNotice);
        }
    }

    /**
     * 生成结果并报错
     *
     * @param provinceDistrictId
     * @param screeningNotice
     * @return
     * @throws IOException
     */
    public DistrictBigScreenStatistic generateResultAndSave(Integer provinceDistrictId, ScreeningNotice screeningNotice) throws IOException {
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
     * @throws IOException
     */
    public DistrictBigScreenStatistic generateResult(Integer provinceDistrictId, ScreeningNotice screeningNotice) throws IOException {
        //根据条件查找所有的元素：条件 cityDistrictIds 非复测 有效
        List<BigScreenStatDataDTO> bigScreenStatDataDTOs = bigScreeningStatService.getByNoticeIdAndDistrictIds(screeningNotice.getId());
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
                .setPlanScreeningNum(screeningPlanService.getAllPlanStudentNumByNoticeId(screeningNotice.getId()));
        if (realScreeningNum > 0 && realValidScreeningNum > 0) {
            //更新城市名
            bigScreenStatDataDTOs = this.updateCityName(bigScreenStatDataDTOs, districtService.getCityAllDistrictIds(provinceDistrictId));
            //构建数据
            districtBigScreenStatisticBuilder.setBigScreenStatDataDTOList(bigScreenStatDataDTOs);
        }
        return districtBigScreenStatisticBuilder.build();
    }

    /**
     * 更新大屏数据的城市名
     *
     * @param bigScreenStatDataDTOs
     * @param districtSetMap
     */
    private List<BigScreenStatDataDTO> updateCityName(List<BigScreenStatDataDTO> bigScreenStatDataDTOs, Map<District, Set<Integer>> districtSetMap) {
        return bigScreenStatDataDTOs.stream().map(bigScreenStatDataDTO -> {
            Set<District> districtSet = districtSetMap.keySet();
            for (District cityDistrict : districtSet) {
                Set<Integer> districtIds = districtSetMap.get(cityDistrict);
                if (districtIds.contains(bigScreenStatDataDTO.getDistrictId()) || cityDistrict.getId().equals(bigScreenStatDataDTO.getDistrictId())) {
                    bigScreenStatDataDTO.setCityDistrictId(cityDistrict.getId());
                    bigScreenStatDataDTO.setCityDistrictName(cityDistrict.getName());
                    break;
                }
            }
            return bigScreenStatDataDTO;
        }).collect(Collectors.toList());
    }


    /**
     * 获取通知
     *
     * @param noticeId
     * @param noticeId
     * @return
     */
    public List<BigScreenStatDataDTO> getByNoticeIdAndDistrictIds(Integer noticeId) throws IOException {
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
}
