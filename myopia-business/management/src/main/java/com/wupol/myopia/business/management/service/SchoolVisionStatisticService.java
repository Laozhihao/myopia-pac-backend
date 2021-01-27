package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.SchoolVisionStatisticMapper;
import com.wupol.myopia.business.management.domain.model.SchoolVisionStatistic;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
public class SchoolVisionStatisticService extends BaseService<SchoolVisionStatisticMapper, SchoolVisionStatistic> {


    /**
     * 通过taskId获取列表
     *
     * @param taskId taskId
     * @return List<SchoolVisionStatistic>
     */
    public List<SchoolVisionStatistic> getByTaskId(Integer taskId) {
        return baseMapper.selectList(new QueryWrapper<SchoolVisionStatistic>().eq("screening_task_id", taskId));
    }

    /**
     * 通过planId获取列表
     *
     * @param planId planId
     * @return List<SchoolVisionStatistic>
     */
    public List<SchoolVisionStatistic> getByPlanId(Integer planId) {
        return baseMapper.selectList(new QueryWrapper<SchoolVisionStatistic>().eq("screening_plan_id", planId));
    }

    /**
     * 通过taskId和schoolIds获取统计信息
     *
     * @param taskId    通知任务ID
     * @param schoolIds 学校ID
     * @return List<SchoolVisionStatistic>
     */
    public List<SchoolVisionStatistic> getBySchoolIds(Integer taskId, List<Integer> schoolIds) {
        return baseMapper.selectList(new QueryWrapper<SchoolVisionStatistic>()
                .eq("screening_task_id", taskId)
                .in("school_id", schoolIds));
    }
}
