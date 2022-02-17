package com.wupol.myopia.business.api.hospital.app.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.hospital.domain.vo.HospitalStudentVO;
import com.wupol.myopia.business.aggregation.hospital.service.HospitalAggService;
import com.wupol.myopia.business.core.hospital.service.HospitalStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 医院的学生管理的App接口
 * @author Chikong
 * @date 2021-02-10
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/hospital/app/student")
public class HospitalStudentController {

    @Autowired
    private HospitalStudentService hospitalStudentService;

    @Autowired
    private HospitalAggService hospitalAggService;

    @GetMapping()
    public HospitalStudentVO getStudent(String token, String idCard, String name) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (StringUtils.isEmpty(token)) {
            return hospitalAggService.getStudent(user.getOrgId(), idCard, name);
        } else {
            return hospitalAggService.getStudentByToken(user.getOrgId(), token).getFirst();
        }
    }

    @GetMapping("/{id}")
    public HospitalStudentVO getStudent(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return hospitalAggService.getStudentById(user.getOrgId(), id).getFirst();
    }

    @GetMapping("/list")
    public List<HospitalStudentVO> getStudentVOList(String nameLike) {
        return hospitalAggService.getStudentVOList(nameLike, CurrentUserUtil.getCurrentUser().getOrgId());
    }

    @PostMapping()
    public ApiResult<Integer> saveStudentArchive(@RequestBody @Valid HospitalStudentVO studentVo) {
        return hospitalAggService.saveStudentArchive(studentVo, CurrentUserUtil.getCurrentUser());
    }

    @PutMapping()
    public ApiResult<String> updateStudent(@RequestBody @Valid HospitalStudentVO studentVo) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        Integer hospitalId = user.getOrgId();
        studentVo.setHospitalId(hospitalId);
        // 如果医院没有该学生的档案,则不允许操作
        if (!hospitalStudentService.existHospitalAndStudentRelationship(hospitalId, studentVo.getStudentId())) {
            return ApiResult.failure("该学生未建档");
        }
        studentVo.setStudentType(hospitalStudentService.getStudentType(user.getClientId(), studentVo.getStudentType()));
        hospitalAggService.saveStudent(studentVo, false);
        return ApiResult.success("更新成功");
    }

}
