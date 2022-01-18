package com.wupol.myopia.business.api.preschool.app.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.domain.ResultCode;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.hospital.domain.vo.HospitalStudentVO;
import com.wupol.myopia.business.aggregation.hospital.service.HospitalAggService;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @Author wulizhou
 * @Date 2022/1/17 19:10
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/preschool/app/student")
public class PreschoolHospitalStudentController {

    private final String NOT_HOSPITAL_STUDENT = "NotHospitalStudent";

    @Autowired
    private HospitalAggService hospitalAggService;

    /**
     * 获取学生医院档案信息，如若没有，若返回学生信息
     * @param token
     * @return
     */
    @GetMapping()
    public ApiResult<HospitalStudentVO> getStudent(@RequestParam String token) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return getStudentResult(hospitalAggService.getStudentByToken(user.getOrgId(), token));
    }

    @GetMapping("/{id}")
    public ApiResult<HospitalStudentVO> getStudent(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return getStudentResult(hospitalAggService.getStudentById(user.getOrgId(), id));
    }

    private ApiResult<HospitalStudentVO> getStudentResult(TwoTuple<HospitalStudentVO, Boolean> studentInfo) {
        return ApiResult.success(hospitalAggService.setPreschoolDistrict(studentInfo.getFirst())).setMessage(studentInfo.getSecond() ?
                ResultCode.SUCCESS.getMessage() : NOT_HOSPITAL_STUDENT);
    }

    @PostMapping()
    public ApiResult<Integer> saveStudentArchive(@RequestBody @Valid HospitalStudentVO studentVo) {
        studentVo.setProvince(null);
        studentVo.setCity(null);
        studentVo.setArea(null);
        studentVo.setTown(null);
        studentVo.setProvinceId(null);
        studentVo.setCityId(null);
        studentVo.setAreaId(null);
        studentVo.setTownId(null);
        return hospitalAggService.saveStudentArchive(studentVo, CurrentUserUtil.getCurrentUser());
    }

}
