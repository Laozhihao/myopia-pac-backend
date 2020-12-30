package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.service.DistrictService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/district")
public class DistrictController extends BaseController<DistrictService, District> {

    /**
     * 获取当前登录用户所在部门的行政区树
     *
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.GovDept>
     **/
    @GetMapping("/structure")
    public List<District> getCurrentUserDistrictTree() {
        return baseService.getCurrentUserDistrictTree();
    }
}
