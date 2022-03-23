package com.wupol.myopia.migrate.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.migrate.domain.model.SysStudentEye;
import com.wupol.myopia.migrate.service.SysStudentEyeService;

/**
 * @Author HaoHao
 * @Date 2022-03-23
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/sysStudentEye")
public class SysStudentEyeController extends BaseController<SysStudentEyeService, SysStudentEye> {

}
