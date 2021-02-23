package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.VisionScreeningResultMapper;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.management.domain.vo.StudentScreeningCountVO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
public class VisionScreeningResultService extends BaseService<VisionScreeningResultMapper, VisionScreeningResult> {

    /**
     * 通过StudentId获取筛查结果
     *
     * @param studentId id
     * @return List<ScreeningResult>
     */
    public List<VisionScreeningResult> getByStudentId(Integer studentId) {
        return baseMapper.selectList(new QueryWrapper<VisionScreeningResult>()
                .eq("student_id", studentId)
                .orderByDesc("create_time"));
    }

    /**
     * 通过计划ID获取结果
     *
     * @param planId 计划ID
     * @return 结果
     */
    public VisionScreeningResult getByPlanId(Integer planId) {
        return baseMapper.selectOne(new QueryWrapper<VisionScreeningResult>().eq("plan_id", planId));
    }

    /**
     * 获取学校ID
     *
     * @param taskId 通知任务
     * @param orgId  机构ID
     * @return 学校ID
     */
    public List<Integer> getSchoolIdByTaskId(Integer taskId, Integer orgId) {
        return baseMapper.getSchoolIdByTaskId(taskId, orgId);
    }

    /**
     * 获取筛查人员ID
     *
     * @param taskId 通知任务
     * @return 学校ID
     */
    public List<Integer> getCreateUserIdByTaskId(Integer taskId) {
        return baseMapper.getCreateUserIdByTaskId(taskId);
    }

    /**
     * 获取学生筛查次数
     *
     * @return List<StudentScreeningCountVO>
     */
    public List<StudentScreeningCountVO> countScreeningTime() {
        return baseMapper.countScreeningTime();
    }

    /**
     * 获取昨天筛查数据的筛查计划Id
     * @return
     */
    public List<Integer> getYesterdayScreeningPlanIds() {
        return Collections.emptyList();
    }
}
