package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.SchoolVisionStatisticMapper;
import com.wupol.myopia.business.management.domain.model.SchoolVisionStatistic;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
     * 通过planId、学校ID获取列表
     *
     * @param planIds  planIds
     * @param schoolId 学校ID
     * @return List<SchoolVisionStatistic>
     */
    public List<SchoolVisionStatistic> getByPlanIdsAndSchoolId(List<Integer> planIds, Integer schoolId) {
        return baseMapper
                .selectList(new QueryWrapper<SchoolVisionStatistic>()
                        .in("screening_plan_id", planIds)
                        .eq("school_id", schoolId));
    }

    /**
     * 通过taskId和schoolIds获取统计信息
     *
     * @param taskId    通知任务ID
     * @param schoolIds 学校ID
     * @return List<SchoolVisionStatistic>
     */
    public List<SchoolVisionStatistic> getBySchoolIds(Integer taskId, List<Integer> schoolIds) {
        return baseMapper
                .selectList(new QueryWrapper<SchoolVisionStatistic>()
                        .eq("screening_task_id", taskId)
                        .in("school_id", schoolIds));
    }

    /**
     * 根据条件查找所有数据
     *
     * @param noticeId
     * @param user
     * @return
     */
    public List<SchoolVisionStatistic> getStatisticDtoByNoticeIdAndOrgId(Integer noticeId, CurrentUser user) {
        if (noticeId == null || user == null) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<SchoolVisionStatistic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SchoolVisionStatistic::getScreeningNoticeId, noticeId);
        if (user.isScreeningUser()) {
            queryWrapper.eq(SchoolVisionStatistic::getScreeningOrgId,user.getOrgId());
        }
        List<SchoolVisionStatistic> schoolVisionStatistics = baseMapper.selectList(queryWrapper);
        return schoolVisionStatistics;
    }
}
