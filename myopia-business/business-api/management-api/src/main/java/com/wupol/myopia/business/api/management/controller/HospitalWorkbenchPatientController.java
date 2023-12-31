package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.management.service.HospitalStudentBizService;
import com.wupol.myopia.business.api.management.service.StudentBizService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalStudentRequestDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalStudentResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.core.hospital.service.HospitalStudentService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

/**
 * 工作台-患者
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/hospital/workbench/patient")
public class HospitalWorkbenchPatientController {

    @Resource
    private HospitalStudentBizService hospitalStudentBizService;

    @Resource
    private HospitalStudentService hospitalStudentService;

    @Resource
    private DistrictService districtService;

    @Resource
    private StudentBizService studentBizService;

    /**
     * 获取医院学生列表
     *
     * @param pageRequest 分页请求
     * @param requestDTO  条件
     * @return IPage<HospitalStudentResponseDTO>
     */
    @GetMapping("list")
    public IPage<HospitalStudentResponseDTO> getByList(@Validated PageRequest pageRequest, HospitalStudentRequestDTO requestDTO) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (user.isHospitalUser()) {
            requestDTO.setHospitalId(CurrentUserUtil.getCurrentUser().getOrgId());
        } else {
            if (Objects.isNull(requestDTO.getHospitalId())) {
                throw new BusinessException("医院Id不能为空");
            }
        }
        return hospitalStudentBizService.getHospitalStudent(pageRequest, requestDTO);
    }

    /**
     * 删除医院学生
     *
     * @param hospitalStudentId 医院学生Id
     * @return ApiResult
     */
    @DeleteMapping("{hospitalStudentId}")
    public ApiResult deleted(@PathVariable("hospitalStudentId") Integer hospitalStudentId) {
        hospitalStudentService.deletedById(hospitalStudentId);
        return ApiResult.success();
    }

    /**
     * 更新医院学生
     *
     * @param hospitalStudent 医院学生
     * @return ApiResult
     */
    @PutMapping
    public void updateHospitalStudent(@RequestBody HospitalStudent hospitalStudent) {
        hospitalStudent.checkStudentInfo();
        hospitalStudentService.updateById(hospitalStudent);
    }

    /**
     * 通过Id获取医院学生
     *
     * @param hospitalStudentId 医院学生Id
     * @return HospitalStudentResponseDTO
     */
    @GetMapping("{hospitalStudentId}")
    public HospitalStudentResponseDTO getByHospitalStudentId(@PathVariable("hospitalStudentId") Integer hospitalStudentId) {
        return hospitalStudentBizService.getByHospitalStudentId(hospitalStudentId);
    }

    /**
     * 根据指定code，获取其下级行政区域集
     *
     * @param code code
     * @return List<District>
     */
    @GetMapping("child/district/{code}")
    public List<District> getChildDistrict(@PathVariable("code") @NotNull(message = "行政区域编号不能为空") Long code) {
        return districtService.getChildDistrictByParentIdPriorityCache(code);
    }

    /**
     * 获取学生就诊列表
     *
     * @param pageRequest 分页请求
     * @param studentId   学生ID
     * @return List<MedicalReportDO>
     */
    @GetMapping("/report/list")
    public IPage<ReportAndRecordDO> getReportList(@Validated PageRequest pageRequest, @NotNull(message = "学生Id不能为空") Integer studentId) {
        return studentBizService.getReportList(pageRequest, studentId, CurrentUserUtil.getCurrentUser(), null);
    }
}
