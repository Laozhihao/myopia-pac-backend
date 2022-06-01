package com.wupol.myopia.business.core.stat.service;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.BeanCopyUtil;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.stat.domain.mapper.ScreeningResultStatisticMapper;
import com.wupol.myopia.business.core.stat.domain.model.CommonDiseaseScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.domain.model.VisionScreeningResultStatistic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 筛查结果统计服务层
 * @author hang.yuan
 * @date 2022/4/7
 */
@Service
public class ScreeningResultStatisticService extends BaseService<ScreeningResultStatisticMapper,ScreeningResultStatistic> {

    @Autowired
    private DistrictService districtService;

    /**
     * 保存视力筛查结果统计
     * @author hang.yuan
     * @date 2022/4/11
     */
    public void saveVisionScreeningResultStatistic(VisionScreeningResultStatistic visionScreeningResultStatistic){
        Integer schoolType = visionScreeningResultStatistic.getSchoolType();
        if (8 == schoolType){
            saveKindergartenVisionScreening(visionScreeningResultStatistic);
        }else {
            savePrimarySchoolAndAboveVisionScreening(visionScreeningResultStatistic);
        }
    }

    /**
     * 保存幼儿园视力筛查
     * @author hang.yuan
     * @date 2022/4/11
     */
    private void saveKindergartenVisionScreening(VisionScreeningResultStatistic visionScreeningResultStatistic){
        saveScreeningResultStatistic(visionScreeningResultStatistic);
    }

    /**
     * 保存小学及以上视力筛查
     * @author hang.yuan
     * @date 2022/4/11
     */
    private void savePrimarySchoolAndAboveVisionScreening(VisionScreeningResultStatistic visionScreeningResultStatistic){
        saveScreeningResultStatistic(visionScreeningResultStatistic);
    }

    /**
     * 保存常见病筛查结果统计
     * @author hang.yuan
     * @date 2022/4/11
     */
    public void saveCommonDiseaseScreeningResultStatistic(CommonDiseaseScreeningResultStatistic commonDiseaseScreeningResultStatistic){
        Integer schoolType = commonDiseaseScreeningResultStatistic.getSchoolType();
        if (8 == schoolType){
            saveKindergartenCommonDiseaseScreening(commonDiseaseScreeningResultStatistic);
        }else {
            savePrimarySchoolAndAboveCommonDiseaseScreening(commonDiseaseScreeningResultStatistic);
        }
    }


    /**
     * 保存幼儿园常见病筛查
     * @author hang.yuan
     * @date 2022/4/11
     */
    private void saveKindergartenCommonDiseaseScreening(CommonDiseaseScreeningResultStatistic commonDiseaseScreeningResultStatistic){
        saveScreeningResultStatistic(commonDiseaseScreeningResultStatistic);
    }

    /**
     * 保存小学及以上常见病筛查
     * @author hang.yuan
     * @date 2022/4/11
     */
    private void savePrimarySchoolAndAboveCommonDiseaseScreening(CommonDiseaseScreeningResultStatistic commonDiseaseScreeningResultStatistic){
        saveScreeningResultStatistic(commonDiseaseScreeningResultStatistic);
    }

    /**
     *  保存筛查结果统计数据
     * @author hang.yuan
     * @date 2022/4/11
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveScreeningResultStatistic(VisionScreeningResultStatistic visionScreeningResultStatistic){
        ScreeningResultStatistic screeningResultStatistic = BeanCopyUtil.copyBeanPropertise(visionScreeningResultStatistic, ScreeningResultStatistic.class);
        LambdaQueryWrapper<ScreeningResultStatistic> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(ScreeningResultStatistic::getScreeningPlanId,screeningResultStatistic.getScreeningPlanId());
        queryWrapper.eq(ScreeningResultStatistic::getScreeningType,screeningResultStatistic.getScreeningType());
        queryWrapper.eq(ScreeningResultStatistic::getScreeningOrgId,screeningResultStatistic.getScreeningOrgId());
        queryWrapper.eq(ScreeningResultStatistic::getSchoolId,screeningResultStatistic.getSchoolId());
        queryWrapper.eq(ScreeningResultStatistic::getSchoolType,screeningResultStatistic.getSchoolType());
        queryWrapper.eq(ScreeningResultStatistic::getDistrictId,screeningResultStatistic.getDistrictId());
        queryWrapper.eq(ScreeningResultStatistic::getIsTotal,screeningResultStatistic.getIsTotal());
        ScreeningResultStatistic dbData = baseMapper.selectOne(queryWrapper);
        if (Objects.nonNull(dbData)){
            screeningResultStatistic.setId(dbData.getId());
        }
        saveOrUpdate(screeningResultStatistic);
    }


    public List<ScreeningResultStatistic> getStatisticByNoticeIdAndCurrentChildDistrictIds(Integer noticeId, Integer currentDistrictId,
                                                                                           boolean isTotal, Integer screeningType, boolean isKindergarten)  {
        if (ObjectsUtil.allNotNull(noticeId,currentDistrictId)){
            List<ScreeningResultStatistic> screeningResultStatistics = new ArrayList<>();
            Set<Integer> districtIds = Sets.newHashSet();
            try {
                districtIds = districtService.getChildDistrictIdsByDistrictId(currentDistrictId);
            } catch (IOException e) {
                log.error("获取行政区域失败");
            }
            districtIds.add(currentDistrictId);

            Consumer<List<Integer>> consumer = getAction(screeningResultStatistics, noticeId, isTotal, screeningType,isKindergarten);

            Lists.partition(new ArrayList<>(districtIds), 100).forEach(consumer);
            return screeningResultStatistics;
        }
        return Lists.newArrayList();
    }

    private Consumer<List<Integer>> getAction(List<ScreeningResultStatistic> screeningResultStatistics,
                                              Integer noticeId, boolean isTotal,Integer screeningType,boolean isKindergarten){
        return districtIdList -> {
            LambdaQueryWrapper<ScreeningResultStatistic> queryWrapper = getQueryWrapper(noticeId, isTotal, screeningType,isKindergarten);
            queryWrapper.in(ScreeningResultStatistic::getDistrictId, districtIdList);
            screeningResultStatistics.addAll(this.list(queryWrapper));
        };
    }

    private List<Integer> getSchoolType(boolean isKindergarten) {
        return isKindergarten?Lists.newArrayList(8):Lists.newArrayList(0,1,2,3,4,5,6,7);
    }

    public List<ScreeningResultStatistic> getStatisticByNoticeIdAndCurrentDistrictId(Integer noticeId, Integer currentDistrictId, boolean isTotal,Integer screeningType,boolean isKindergarten)  {
        if (ObjectsUtil.allNotNull(noticeId,currentDistrictId)){
            LambdaQueryWrapper<ScreeningResultStatistic> queryWrapper = getQueryWrapper(noticeId, isTotal, screeningType,isKindergarten);
            queryWrapper.eq(ScreeningResultStatistic::getDistrictId, currentDistrictId);
            return this.list(queryWrapper);
        }
        return Lists.newArrayList();
    }

    private LambdaQueryWrapper<ScreeningResultStatistic> getQueryWrapper(Integer noticeId,boolean isTotal,Integer screeningType,boolean isKindergarten){
        LambdaQueryWrapper<ScreeningResultStatistic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScreeningResultStatistic::getScreeningNoticeId, noticeId);
        queryWrapper.eq(ScreeningResultStatistic::getIsTotal, isTotal);
        queryWrapper.eq(ScreeningResultStatistic::getScreeningType, screeningType);
        queryWrapper.in(ScreeningResultStatistic::getSchoolType,getSchoolType(isKindergarten));
        return queryWrapper;
    }


    public List<ScreeningResultStatistic> getStatisticByDistrictIds(Set<Integer> districtIds, Boolean isTotal) {
        List<ScreeningResultStatistic> screeningResultStatistics=Lists.newArrayList();
        if (CollectionUtil.isNotEmpty(districtIds)){
            Lists.partition(Lists.newArrayList(districtIds),100).forEach(ids->{
                LambdaQueryWrapper<ScreeningResultStatistic> queryWrapper= new LambdaQueryWrapper<>();
                Optional.ofNullable(isTotal).ifPresent(b->queryWrapper.eq(ScreeningResultStatistic::getIsTotal,b));
                queryWrapper.in(ScreeningResultStatistic::getDistrictId,districtIds);
                queryWrapper.orderByDesc(ScreeningResultStatistic::getUpdateTime);
                screeningResultStatistics.addAll(baseMapper.selectList(queryWrapper));
            });
        }
        return screeningResultStatistics;
    }

    public List<ScreeningResultStatistic> getStatisticByCurrentDistrictId(Integer districtId, Boolean isTotal) {
        LambdaQueryWrapper<ScreeningResultStatistic> queryWrapper= new LambdaQueryWrapper<>();
        Optional.ofNullable(isTotal).ifPresent(b->queryWrapper.eq(ScreeningResultStatistic::getIsTotal,b));
        queryWrapper.eq(ScreeningResultStatistic::getDistrictId,districtId);
        queryWrapper.orderByDesc(ScreeningResultStatistic::getUpdateTime);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 通过planId、学校ID获取列表
     * @param planIds
     * @param schoolId
     */
    public List<ScreeningResultStatistic> getByPlanIdsAndSchoolId(List<Integer> planIds, Integer schoolId) {
        LambdaQueryWrapper<ScreeningResultStatistic> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.in(ScreeningResultStatistic::getScreeningPlanId,planIds);
        queryWrapper.eq(ScreeningResultStatistic::getSchoolId,schoolId);
        return list(queryWrapper);
    }

    public Set<Integer> getDistrictIdByNoticeId(Integer noticeId){
        LambdaQueryWrapper<ScreeningResultStatistic> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(ScreeningResultStatistic::getScreeningNoticeId,noticeId);
        List<ScreeningResultStatistic> list = list(queryWrapper);
        if (CollectionUtil.isNotEmpty(list)){
            return list.stream().map(ScreeningResultStatistic::getDistrictId).collect(Collectors.toSet());
        }
        return Sets.newHashSet();
    }
}
