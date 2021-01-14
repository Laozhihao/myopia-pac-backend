package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.model.HospitalAdmin;
import com.wupol.myopia.business.management.service.HospitalAdminService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/hospitalStaff")
public class HospitalAdminController extends BaseController<HospitalAdminService, HospitalAdmin> {

}
