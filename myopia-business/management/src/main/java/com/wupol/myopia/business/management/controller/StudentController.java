package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.business.management.domain.query.HospitalQuery;
import com.wupol.myopia.business.management.domain.query.StudentQuery;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.service.StudentService;

import java.io.IOException;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/student")
public class StudentController extends BaseController<StudentService, Student> {

    @Autowired
    private ExcelFacade excelFacade;


    @GetMapping("/export")
    public ApiResult getHospitalExportData(StudentQuery query) throws IOException {
        //TODO 待检验日期范围
        return ApiResult.success(excelFacade.generateStudent(query));
    }
}
