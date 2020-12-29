package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.business.management.domain.model.GovDept;
import org.springframework.web.bind.annotation.*;
import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.service.DistrictService;

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
    public List<GovDept> getGovDeptPermissionTree() {
        return baseService.selectAllTree();
    }
}
