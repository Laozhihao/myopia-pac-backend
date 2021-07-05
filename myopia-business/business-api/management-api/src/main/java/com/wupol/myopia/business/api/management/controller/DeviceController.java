package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.StatusConstant;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.management.domain.dto.DeviceDTO;
import com.wupol.myopia.business.api.management.domain.vo.DeviceVO;
import com.wupol.myopia.business.api.management.service.DeviceBizService;
import com.wupol.myopia.business.api.management.validator.DeviceAddValidatorGroup;
import com.wupol.myopia.business.api.management.validator.DeviceUpdateValidatorGroup;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

/**
 * 设备管理接口
 *
 * @Author HaoHao
 * @Date 2021/6/28
 **/
@Validated
@ResponseResultBody
@RestController
@RequestMapping("/management/device")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DeviceBizService deviceBizService;

    /**
     * 获取列表（分页）
     *
     * @param deviceDTO 查询条件
     * @param pageRequest 分页参数
     * @return {@link com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.wupol.myopia.business.api.management.domain.vo.DeviceVO> }
     **/
    @GetMapping("/list")
    public Page<DeviceVO> getListByPage(DeviceDTO deviceDTO, @Validated PageRequest pageRequest) {
        return deviceBizService.getDeviceListByPage(deviceDTO, pageRequest);
    }

    /**
     * 新增设备
     *
     * @param deviceDTO 设备数据
     * @return java.lang.Boolean
     **/
    @PostMapping()
    public Boolean addDevice(@RequestBody @Validated(value = {DeviceAddValidatorGroup.class, Default.class}) DeviceDTO deviceDTO) {
        try {
            return deviceService.save(deviceDTO.toDevice());
        } catch (DuplicateKeyException e) {
            throw new BusinessException("唯一标识码重复，请重新填写", e);
        }
    }

    /**
     * 编辑设备
     *
     * @param deviceDTO 设备数据
     * @return java.lang.Boolean
     **/
    @PutMapping()
    public Boolean updateDevice(@RequestBody @Validated(value = {DeviceUpdateValidatorGroup.class, Default.class}) DeviceDTO deviceDTO) {
        return deviceService.updateById(deviceDTO.toDevice());
    }

    /**
     * 停用|启用设备
     *
     * @param id 设备ID
     * @param status 状态
     * @return java.lang.Boolean
     **/
    @PutMapping("/{id}/{status}")
    public Boolean updateDeviceStatus(@PathVariable @NotNull(message = "设备ID不能为空") Integer id, @PathVariable @NotNull(message = "状态值不能为空") Integer status) {
        Assert.isTrue(status == StatusConstant.DISABLE || status == StatusConstant.ENABLE, "无效状态值");
        return deviceService.updateById(new Device().setId(id).setStatus(status));
    }

}
