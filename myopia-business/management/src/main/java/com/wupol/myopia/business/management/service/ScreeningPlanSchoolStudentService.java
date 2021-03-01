package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.myopia.common.exceptions.ManagementUncheckedException;
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

/*    public ScreeningPlanSchoolStudent getByScreeningPlanSchoolStudentId(Integer studentId) {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = new ScreeningPlanSchoolStudent();
        screeningPlanSchoolStudent.set(studentId);
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.setEntity(screeningPlanSchoolStudent);
        return baseMapper.selectOne(queryWrapper);
    }*/

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

    /**
     * @param schoolName
     * @param deptId
     * @return
     */
    public List<ScreeningPlanSchoolStudent> getSchoolByOrgIdAndSchoolName(String schoolName, Integer deptId) {
        if (deptId == null) {
            throw new ManagementUncheckedException("deptId 不能为空");
        }
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScreeningPlanSchoolStudent::getScreeningOrgId, deptId).like(ScreeningPlanSchoolStudent::getSchoolName, schoolName);
        List<ScreeningPlanSchoolStudent> screeningPlanSchools = baseMapper.selectList(queryWrapper);
        return screeningPlanSchools;
    }

    public List<ScreeningPlanSchoolStudent> getClassNameBySchoolNameAndGradeName(String schoolName, String gradeName, Integer deptId) {
        if (deptId == null) {
            throw new ManagementUncheckedException("deptId 不能为空");
        }
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScreeningPlanSchoolStudent::getScreeningOrgId, deptId).eq(ScreeningPlanSchoolStudent::getGradeName, gradeName).eq(ScreeningPlanSchoolStudent::getSchoolName, schoolName);
        List<ScreeningPlanSchoolStudent> screeningPlanSchools = baseMapper.selectList(queryWrapper);
        return screeningPlanSchools;
    }


}
