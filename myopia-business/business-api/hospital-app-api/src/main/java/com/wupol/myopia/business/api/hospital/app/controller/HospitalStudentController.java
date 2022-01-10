package com.wupol.myopia.business.api.hospital.app.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.hospital.app.domain.vo.HospitalStudentVO;
import com.wupol.myopia.business.api.hospital.app.facade.HospitalStudentFacade;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalStudentQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalStudentService;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

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
    private StudentService studentService;
    @Autowired
    private HospitalStudentFacade hospitalStudentFacade;

    @GetMapping()
    public HospitalStudentVO getStudent(String token, String idCard, String name) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (StringUtils.isEmpty(token)) {
            return hospitalStudentFacade.getStudent(user.getOrgId(), idCard, name);
        } else {
            return hospitalStudentFacade.getStudentByToken(user.getOrgId(), token);
        }
    }

    @GetMapping("/{id}")
    public HospitalStudentVO getStudent(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return hospitalStudentFacade.getStudentById(user.getOrgId(), id);
    }

    @GetMapping("/list")
    public List<HospitalStudentVO> getStudentVOList(String nameLike) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        HospitalStudentQuery query = new HospitalStudentQuery();
        query.setNameLike(nameLike).setHospitalId(user.getOrgId());
        return hospitalStudentFacade.getHospitalStudentVoList(query);
    }

    @PostMapping()
    public ApiResult<Integer> saveStudentArchive(@RequestBody @Valid HospitalStudentVO studentVo) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        Integer hospitalId = user.getOrgId();
        studentVo.setHospitalId(hospitalId);
        Student student = studentService.getByIdCard(studentVo.getIdCard());
        if (Objects.nonNull(student) && hospitalStudentService.existHospitalAndStudentRelationship(hospitalId, student.getId())) {
            return ApiResult.failure("该学生已建档，请勿重复建档");
        }
        studentVo.setCreateUserId(user.getId());
        studentVo.setStudentType(hospitalStudentService.getStudentType(user.getSystemCode(), studentVo.getStudentType()));
        Integer studentId = hospitalStudentFacade.saveStudent(studentVo, true);
        return ApiResult.success(studentId);
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
        studentVo.setStudentType(hospitalStudentService.getStudentType(user.getSystemCode(), studentVo.getStudentType()));
        hospitalStudentFacade.saveStudent(studentVo, false);
        return ApiResult.success("更新成功");
    }

}
