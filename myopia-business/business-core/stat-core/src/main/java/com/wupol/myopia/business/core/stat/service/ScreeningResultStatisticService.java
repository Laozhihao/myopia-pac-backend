package com.wupol.myopia.business.core.stat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.BeanCopyUtil;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.stat.domain.mapper.ScreeningResultStatisticMapper;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.domain.model.VisionScreeningResultStatistic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

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
     *  保存筛查结果统计数据
     * @author hang.yuan
     * @date 2022/4/11
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveScreeningResultStatistic(VisionScreeningResultStatistic visionScreeningResultStatistic){
        ScreeningResultStatistic screeningResultStatistic = BeanCopyUtil.copyBeanPropertise(visionScreeningResultStatistic, ScreeningResultStatistic.class);
        saveOrUpdate(screeningResultStatistic);
    }

    public List<ScreeningResultStatistic> getStatisticByNoticeIdAndCurrentChildDistrictIds(Integer noticeId, Integer currentDistrictId,
                                                                                           boolean isTotal, Integer screeningType, boolean isKindergarten)  {
        if (ObjectsUtil.allNotNull(noticeId,currentDistrictId)){
            List<ScreeningResultStatistic> screeningResultStatistics = new ArrayList<>();
            Set<Integer> districtIds = null;
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
        queryWrapper.isNull(ScreeningResultStatistic::getSchoolId);
        queryWrapper.in(ScreeningResultStatistic::getSchoolType,getSchoolType(isKindergarten));
        return queryWrapper;
    }



}
