package com.wupol.myopia.business.parent.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.parent.domain.model.Parent;
import com.wupol.myopia.business.parent.service.ParentService;

/**
 * @Author HaoHao
 * @Date 2021-02-26
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/parent/parent")
public class ParentController extends BaseController<ParentService, Parent> {

}
