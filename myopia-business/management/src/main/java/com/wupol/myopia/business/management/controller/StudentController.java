package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.RegularUtils;
import com.wupol.myopia.business.management.constant.NationEnum;
import com.wupol.myopia.business.management.constant.VisionLabelsEnum;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.StudentQuery;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.service.StudentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/student")
public class StudentController {

    @Autowired
    private ExcelFacade excelFacade;

    @Autowired
    private StudentService studentService;


    @PostMapping()
    public Object saveStudent(@RequestBody Student student) {
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        checkStudentIsLegal(student);
        student.setCreateUserId(user.getId());
        return studentService.saveStudent(student);
    }

    @PutMapping()
    public Object updateStudent(@RequestBody Student student) {
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        checkStudentIsLegal(student);
        student.setCreateUserId(user.getId());
        return studentService.updateStudent(student);
    }

    @DeleteMapping("{id}")
    public Object deletedStudent(@PathVariable("id") Integer id) {
        CurrentUserUtil.getLegalCurrentUser();
        return studentService.deletedStudent(id);
    }

    @GetMapping("{id}")
    public Object getStudent(@PathVariable("id") Integer id) {
        CurrentUserUtil.getLegalCurrentUser();
        return studentService.getById(id);
    }

    @GetMapping("list")
    public Object getStudentsList(PageRequest pageRequest, StudentQuery studentQuery) throws ParseException {
        CurrentUserUtil.getLegalCurrentUser();
        return studentService.getStudentLists(pageRequest, studentQuery);
    }

    @GetMapping("/export")
    public ApiResult getHospitalExportData(StudentQuery query) throws IOException {
        //TODO 待检验日期范围
        return ApiResult.success(excelFacade.generateStudent(query));
    }

    @PostMapping("/import")
    public ApiResult importStudent(MultipartFile file) throws IOException {
        Integer schoolId = 12;
        Integer createUserId = 12;
        excelFacade.importStudent(schoolId, createUserId, file);
        return ApiResult.success();
    }

    @GetMapping("labels")
    public Object getVisionLabels() {
        CurrentUserUtil.getLegalCurrentUser();
        return VisionLabelsEnum.getVisionLabels();
    }

    @GetMapping("nation")
    public Object getNationLists() {
        CurrentUserUtil.getLegalCurrentUser();
        return NationEnum.getNationList();
    }

    /**
     * 数据校验
     *
     * @param student 学生实体类
     */
    private void checkStudentIsLegal(Student student) {

        if (null == student.getSchoolId() || null == student.getSno()
                || null == student.getGradeId() || null == student.getClassId()
                || StringUtils.isBlank(student.getName()) || null == student.getGender()
                || null == student.getBirthday() || null == student.getNation()
                || null == student.getCityCode() || null == student.getProvinceCode()
                || null == student.getAreaCode() || null == student.getTownCode()
                || StringUtils.isBlank(student.getAddress())) {
            throw new BusinessException("数据异常");
        }
        // 检查身份证
        if (!RegularUtils.isIdCard(student.getIdCard())) {
            throw new BusinessException("身份证不正确");
        }
        // 检查手机号码
        if (!RegularUtils.isMobile(student.getParentPhone())) {
            throw new BusinessException("手机号不正确");
        }
    }
}
