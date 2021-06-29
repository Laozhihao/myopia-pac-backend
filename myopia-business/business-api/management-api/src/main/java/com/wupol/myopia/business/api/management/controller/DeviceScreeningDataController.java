package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.domain.ResultCode;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.management.service.DeviceScreeningDataBizService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreeningDataAndOrgDTO;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreeningDataQueryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author wulizhou
 * @Date 2021/6/29 17:39
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/device")
public class DeviceScreeningDataController {

    @Autowired
    private DeviceScreeningDataBizService deviceScreeningDataBizService;

    @GetMapping("/list")
    public IPage<DeviceScreeningDataAndOrgDTO> queryDeptPage(DeviceScreeningDataQueryDTO query, PageRequest pageRequest) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (user.isScreeningUser()) {
            query.setScreeningOrgId(user.getOrgId());
        } else if (user.isGovDeptUser()) {
            throw new BusinessException("政府人员无权查看", ResultCode.USER_ACCESS_UNAUTHORIZED.getCode());
        }
        return deviceScreeningDataBizService.getPage(query, pageRequest);
    }

}
