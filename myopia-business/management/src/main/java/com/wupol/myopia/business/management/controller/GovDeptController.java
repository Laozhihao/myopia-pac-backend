package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.vo.GovDeptVo;
import com.wupol.myopia.business.management.service.DistrictService;
import com.wupol.myopia.business.management.service.GovDeptService;
import com.wupol.myopia.business.management.validator.GovDeptAddValidatorGroup;
import com.wupol.myopia.business.management.validator.GovDeptUpdateValidatorGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/govDept")
public class GovDeptController {

    @Value(value = "${oem.province.code}")
    private Long oemProvinceCode;

    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private DistrictService districtService;

    /**
     * 获取部门列表
     *
     * @param queryParam 查询参数
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.GovDept>
     **/
    @GetMapping("/list")
    public List<GovDept> getGovDeptList(GovDept queryParam) throws IOException {
        if (Objects.isNull(queryParam.getDistrictId())) {
            throw new ValidationException("行政区ID为空");
        }
        return govDeptService.findByList(queryParam);
    }

    /**
     * 新增部门（用户只能为直属下级行政区创建部门）
     *
     * @param govDept 部门数据
     * @return com.wupol.myopia.business.management.domain.model.GovDept
     **/
    @PostMapping()
    public GovDept addGovDept(@RequestBody @Validated(value = GovDeptAddValidatorGroup.class) GovDept govDept) {
        // TODO：管理员的判断根据角色类型来、部门对应上行政区
        if ("admin".equals(CurrentUserUtil.getCurrentUser().getUsername())) {
            // 如果是管理员的话，需要选择部门，pid不能为空；行政区可以为任意
            if (Objects.isNull(govDept.getPid())) {
                throw new ValidationException("上级部门不能为空");
            }
        } else {
            District district = districtService.getById(govDept.getDistrictId());
            GovDept currentUserDept = govDeptService.getById(CurrentUserUtil.getCurrentUser().getOrgId());
            District parentDistrict = districtService.getById(currentUserDept.getDistrictId());
            if (Objects.isNull(district) || Objects.isNull(parentDistrict) || !parentDistrict.getCode().equals(district.getParentCode())) {
                throw new ValidationException("行政区ID无效，只能为下一级行政区创建部门");
            }
            // 非管理员用户，获取当前用户的部门作为上级部门
            govDept.setPid(CurrentUserUtil.getCurrentUser().getOrgId());
        }

        govDeptService.save(govDept);
        return govDept;
    }

    /**
     * 更新部门
     *
     * @param govDept 部门信息
     * @return com.wupol.myopia.business.management.domain.model.GovDept
     **/
    @PutMapping()
    public GovDept modifyGovDept(@RequestBody @Validated(value = GovDeptUpdateValidatorGroup.class) GovDept govDept) {
        // TODO: 非管理员用户，不允许修改pid
        govDeptService.updateById(govDept);
        return govDeptService.getById(govDept);
    }

    /**
     * 获取以当前登录用户所在部门的为根节点的部门树
     *
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.GovDept>
     **/
    @GetMapping("/structure")
    public List<GovDeptVo> getGovDeptTree() {
        return govDeptService.selectGovDeptTreeByPid(CurrentUserUtil.getCurrentUser().getOrgId());
    }

    /**
     * 获取部门详情
     *
     * @param govDeptId 部门ID
     * @return com.wupol.myopia.business.management.domain.model.GovDept
     **/
    @GetMapping("/{govDeptId}")
    public GovDept getGovDeptDetail(@PathVariable("govDeptId") Integer govDeptId) {
        return govDeptService.getById(govDeptId);
    }

    /**
     * 获取指定行政区的上级行政区的所有直属部门
     *
     * @param districtId 行政区ID
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.GovDept>
     **/
    @GetMapping("/superior/{districtId}")
    public List<GovDept> getSuperiorDept(@PathVariable("districtId") @NotNull(message = "行政区ID不能为空") String districtId) throws IOException {
        District district = districtService.getById(districtId);
        if (Objects.isNull(district)) {
            throw new ValidationException("不存在该行政区");
        }
        // 默认省级部门的父部门为运营中心，其行政区ID为-1 TODO：抽为常量，统一维护 -1 的数据
        Long parentCode = district.getCode().equals(oemProvinceCode) ? -1L : district.getParentCode();
        District parentDistrict = districtService.findOne(new District().setCode(parentCode));
        if (Objects.isNull(parentDistrict)) {
            throw new ValidationException("不存在该行政区的上级行政区");
        }
        return govDeptService.findByList(new GovDept().setDistrictId(parentDistrict.getId()));
    }

}
