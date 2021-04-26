package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.management.service.DistrictBizService;
import com.wupol.myopia.business.core.government.domain.model.District;
import com.wupol.myopia.business.core.government.service.DistrictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.io.IOException;
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

    @Autowired
    private DistrictBizService districtBizService;

    /**
     * 获取以当前登录用户所在部门的行政区域作为根节点的行政区域树
     *
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    @GetMapping("/structure")
    public List<District> getCurrentUserDistrictTree() throws IOException {
        return districtBizService.getCurrentUserDistrictTree(CurrentUserUtil.getCurrentUser());
    }

    /**
     * 获取全国行政区域-树结构
     *
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    @GetMapping("/all")
    public List<District> getWholeCountryDistrictTree() {
        return baseService.getWholeCountryDistrictTreePriorityCache();
    }

    /**
     * 获取指定行政区域的下级区域
     *
     * @param code 行政区域编号不能为空
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    @GetMapping("/child/{code}")
    public List<District> getChildDistrict(@PathVariable @NotNull(message = "行政区域编号不能为空") Long code) throws IOException {
        return baseService.getChildDistrictByParentIdPriorityCache(code);
    }
    /**
     * 获取指定行政区域的下级区域
     *
     * @param id 行政区域的id
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    @GetMapping("/child/id/{id}")
    public List<District> getChildDistrict(@PathVariable @NotNull(message = "行政区域编号不能为空") Integer id) throws IOException {
        return baseService.getChildDistrictByParentIdPriorityCache(id);
    }

    /**
     * 获取当前登录用户所属层级位置 - 层级链(从省开始到所属层级)
     *
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    @GetMapping("/current/position")
    public List<District> getCurrentUserPosition() {
        return districtBizService.getCurrentUserDistrictPositionDetail(CurrentUserUtil.getCurrentUser());
    }

    /**
     * 获取当前登录用户所属省级的行政区树
     *
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    @GetMapping("/province")
    public List<District> getCurrentUserProvinceTree() {
        return districtBizService.getCurrentUserProvinceTree(CurrentUserUtil.getCurrentUser());
    }
}
