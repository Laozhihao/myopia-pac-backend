package com.wupol.myopia.business.parent.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.parent.domain.model.ParentStudent;
import com.wupol.myopia.business.parent.service.ParentStudentService;

/**
 * @Author HaoHao
 * @Date 2021-02-26
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/parent/parentStudent")
public class ParentStudentController extends BaseController<ParentStudentService, ParentStudent> {

}
