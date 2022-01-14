package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.management.service.UserService;
import com.wupol.myopia.business.common.utils.constant.BizMsgConstant;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.domain.dto.GovDeptDTO;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.government.validator.GovDeptAddValidatorGroup;
import com.wupol.myopia.business.core.government.validator.GovDeptUpdateValidatorGroup;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNoticeDeptOrg;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeDeptOrgService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/govDept")
public class GovDeptController {

    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private UserService userService;
    @Resource
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private ScreeningNoticeDeptOrgService noticeDeptOrgService;

    /**
     * 获取部门列表
     *
     * @param queryParam 查询参数
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.GovDept>
     **/
    @GetMapping("/list")
    public List<GovDept> getGovDeptList(GovDept queryParam) {
        Assert.notNull(queryParam.getDistrictId(), "行政区ID不能为空");
        Assert.isTrue(CurrentUserUtil.getCurrentUser().isPlatformAdminUser(), BizMsgConstant.NOT_ADMIN_NO_ACCESS);
        List<GovDept> govDeptList = govDeptService.findByListOrderByIdDesc(queryParam);
        // 填充创建人姓名、部门人数
        List<Integer> userIds = govDeptList.stream().map(GovDept::getCreateUserId).distinct().collect(Collectors.toList());
        Map<Integer, User> userMap = userService.getUserMapByIds(userIds);
        govDeptList.forEach(govDept -> {
            User createUser = userMap.get(govDept.getCreateUserId());
            if (Objects.nonNull(createUser)) {
                govDept.setCreateUserName(createUser.getRealName());
            }
            UserDTO userDTO = new UserDTO();
            userDTO.setOrgId(govDept.getId()).setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode()).setUserType(UserType.GOVERNMENT_ADMIN.getType());
            govDept.setUserCount(oauthServiceClient.count(userDTO));
        });
        return govDeptList;
    }

    /**
     * 新增部门（用户只能为直属下级行政区创建部门）
     *
     * @param govDept 部门数据
     * @return com.wupol.myopia.business.management.domain.model.GovDept
     **/
    @PostMapping()
    public GovDept addGovDept(@RequestBody @Validated(value = GovDeptAddValidatorGroup.class) GovDept govDept) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        // 如果是管理员的话，需要选择部门，pid不能为空；行政区可以为任意
        if (currentUser.isPlatformAdminUser()) {
            Assert.notNull(govDept.getPid(), "上级部门不能为空");
            // TODO: 判断pid部门是否为当前govDept.districtId的上级行政区的部门
        } else {
            District district = districtService.getById(govDept.getDistrictId());
            GovDept currentUserDept = govDeptService.getById(currentUser.getOrgId());
            District parentDistrict = districtService.getById(currentUserDept.getDistrictId());
            if (Objects.isNull(district) || Objects.isNull(parentDistrict) || !parentDistrict.getCode().equals(district.getParentCode())) {
                throw new ValidationException("行政区ID无效，只能为下一级行政区创建部门");
            }
            // 非管理员用户，获取当前用户的部门作为上级部门
            govDept.setPid(currentUser.getOrgId());
        }
        try {
            govDeptService.saveGovDept(govDept.setCreateUserId(currentUser.getId()));
            //新增部门获取上级发布的历史通知
            List<GovDept> lists = new ArrayList<>();
            List<GovDept> govList = findByParentIds(lists,govDept.getPid());
            if (!govList.isEmpty()){
                for (GovDept govDept1 : govList){
                noticeDeptOrgService.saveScreeningNotice(govDept1.getId(),govDept1.getDistrictId());
                }
            }
        } catch (DuplicateKeyException e) {
            throw new BusinessException("已经存在该部门名称");
        }
        return govDept;
    }

    /**
     * 根据部门父id获取所有上级部门
     * @param list
     * @param id
     * @return
     */

    private List<GovDept> findByParentIds(List<GovDept> list,Integer id) {
        GovDept govDept =  govDeptService.getById(id);
        list.add(govDept);
        if (govDept.getPid() == -1){
            return list;
        }
        findByParentIds(list,govDept.getPid());
        return list;
    }

    /**
     * 更新部门
     *
     * @param govDept 部门信息
     * @return com.wupol.myopia.business.management.domain.model.GovDept
     **/
    @PutMapping()
    public GovDept updateGovDept(@RequestBody @Validated(value = GovDeptUpdateValidatorGroup.class) GovDept govDept) {
        Assert.isTrue(CurrentUserUtil.getCurrentUser().isPlatformAdminUser(), BizMsgConstant.NOT_ADMIN_NO_ACCESS);
        govDeptService.updateGovDeptById(govDept);
        return govDept;
    }

    /**
     * 获取以当前登录用户所在部门的为根节点的部门树
     *
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.GovDept>
     **/
    @GetMapping("/structure")
    public List<GovDeptDTO> getGovDeptTree() {
        // TODO: 拼接一级节点“平台系统中心”
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
    public List<GovDept> getSuperiorDept(@PathVariable("districtId") @NotNull(message = "行政区ID不能为空") String districtId) {
        District district = districtService.getById(districtId);
        if (Objects.isNull(district)) {
            throw new ValidationException("不存在该行政区");
        }
        // 默认省级部门的父部门为运营中心，其行政区ID为-1 TODO：抽为常量，统一维护 -1 的数据
        if (district.getParentCode() == 100000000) {
            return govDeptService.findByList(new GovDept().setDistrictId(-1));
        }
        District parentDistrict = districtService.findOne(new District().setCode(district.getParentCode()));
        if (Objects.isNull(parentDistrict)) {
            throw new ValidationException("不存在该行政区的上级行政区");
        }
        return govDeptService.findByList(new GovDept().setDistrictId(parentDistrict.getId()));
    }

    /**
     * 修改部门状态
     *
     * @param govDeptId 部门ID
     * @param status    状态类型
     * @return boolean
     **/
    @PutMapping("/{govDeptId}/{status}")
    public boolean updateStatus(@PathVariable @NotNull(message = "部门ID不能为空") Integer govDeptId, @PathVariable @NotNull(message = "状态不能为空") Integer status) {
        Assert.isTrue(CurrentUserUtil.getCurrentUser().isPlatformAdminUser(), BizMsgConstant.NOT_ADMIN_NO_ACCESS);
        return govDeptService.updateGovDeptById(new GovDept().setId(govDeptId).setStatus(status));
    }

}
