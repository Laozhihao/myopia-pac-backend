package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.dto.ScreeningResultSearchDTO;
import com.wupol.myopia.business.management.domain.dto.StudentScreeningInfoWithResultDTO;
import com.wupol.myopia.business.management.domain.mapper.ScreeningPlanSchoolStudentMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
public class ScreeningPlanSchoolStudentService extends BaseService<ScreeningPlanSchoolStudentMapper, ScreeningPlanSchoolStudent> {

    List<ScreeningPlanSchoolStudent> getByStudentId(Integer studentId) {
        return baseMapper.selectList(new QueryWrapper<ScreeningPlanSchoolStudent>().eq("student_id", studentId));
    }

    /**
     * 批量查找数据
     *
     * @param screeningResultSearchDTO
     * @return
     */
    public List<StudentScreeningInfoWithResultDTO> getStudentInfoWithResult(ScreeningResultSearchDTO screeningResultSearchDTO) {
        List<StudentScreeningInfoWithResultDTO> visionScreeningResults = baseMapper.selectStudentInfoWithResult(screeningResultSearchDTO);
        return visionScreeningResults;
    }

}
