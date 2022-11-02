package com.wupol.myopia.business.core.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.constant.StatusConstant;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.device.domain.mapper.DeviceMapper;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.domain.query.DeviceQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author jacob
 * @Date 2021-06-28
 */
@Service
public class DeviceService extends BaseService<DeviceMapper, Device> {

    /**
     * 分页查询（仅支持查询条件为模糊查询）
     *
     * @param pageRequest 分页参数
     * @param queryLike 模糊查询条件
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.wupol.myopia.business.core.device.domain.model.Device>
     **/
    public IPage<Device> getPageByLikeQuery(PageRequest pageRequest, DeviceQuery queryLike){
        return baseMapper.getPageByLikeQuery(pageRequest.toPage(), queryLike);
    }

    /**
     *  检查是否存在
     * @param deviceSn
     * @return
     */
    public Device getDeviceByDeviceSn(String deviceSn) {
        Device device = new Device().setDeviceSn(deviceSn).setStatus(StatusConstant.ENABLE);
        return findOne(device);
    }

    public List<Device> getByOrgIdAndOrgType(Integer orgId, Integer orgType) {
        return list(new LambdaQueryWrapper<Device>()
                .eq(Device::getBindingScreeningOrgId, orgId)
                .eq(Device::getOrgType, orgType));
    }

    /**
     * 获取设备Map
     *
     * @param list     集合
     * @param function function
     * @param <T>      T
     *
     * @return Map<Integer, Hospital>
     */
    public <T> Map<Integer, Device> getDeviceMap(List<T> list, Function<T, Integer> function) {
        return listByIds(list.stream().map(function).collect(Collectors.toList())).stream().collect(Collectors.toMap(Device::getId, Function.identity()));
    }
}
