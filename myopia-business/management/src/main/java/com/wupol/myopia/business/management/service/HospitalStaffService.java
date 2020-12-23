package com.wupol.myopia.business.management.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.HospitalStaffMapper;
import com.wupol.myopia.business.management.domain.model.HospitalStaff;
import org.springframework.stereotype.Service;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Service
public class HospitalStaffService extends BaseService<HospitalStaffMapper, HospitalStaff> {

    public Integer saveStaff(Integer createUserId, Integer hospitalId, Integer userId) {
        HospitalStaff hospitalStaff = new HospitalStaff();
        hospitalStaff.setCreateUserId(createUserId);
        hospitalStaff.setHospitalId(hospitalId);
        hospitalStaff.setUserId(userId);
        return baseMapper.insert(hospitalStaff);
    }

}
