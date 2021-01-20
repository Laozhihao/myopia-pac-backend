package com.wupol.myopia.business.management.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.model.StudentScreeningRawData;
import com.wupol.myopia.business.management.service.StudentScreeningRawDataService;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/studentScreeningRawData")
public class StudentScreeningRawDataController extends BaseController<StudentScreeningRawDataService, StudentScreeningRawData> {

}
