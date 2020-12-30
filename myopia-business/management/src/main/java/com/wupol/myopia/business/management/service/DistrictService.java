package com.wupol.myopia.business.management.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.mapper.DistrictMapper;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.mapper.DistrictMapper;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.model.GovDept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class DistrictService extends BaseService<DistrictMapper, District> {

    @Autowired
    private GovDeptService govDeptService;

    /**
     * 获取行政区树
     *
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.District>
     **/
    public List<District> getCurrentUserDistrictTree() {
        Integer orgId = CurrentUserUtil.getCurrentUser().getOrgId();
        GovDept govDept = govDeptService.getById(orgId);
        District district = getById(govDept.getDistrictId());
        return baseMapper.selectDistrictTree(district.getCode());
    }

    /**
     * 生成业务ID
     * @param type 类型 {@link Const.MANAGEMENT_TYPE}
     * @return 业务ID
     */
    public String generateSn(Integer type) {
        // TODO: 实现业务逻辑
        switch (type) {
            case 1:
                return "1L";
            case 2:
                return "2L";
            case 3:
                return "3L";
            case 4:
                return "4L";
            case 5:
                return "5L";
        }
        return null;
    }
}
