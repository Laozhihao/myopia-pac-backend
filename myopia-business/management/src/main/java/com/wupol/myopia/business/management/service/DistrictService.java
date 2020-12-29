package com.wupol.myopia.business.management.service;

import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.mapper.DistrictMapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.model.GovDept;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class DistrictService extends BaseService<DistrictMapper, District> {
    public List<GovDept> selectAllTree() {
        // TODO: code改为读配置
        return baseMapper.selectAllTree(140000000);
    }
}
