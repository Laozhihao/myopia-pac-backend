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

    /**
     * 新增医院员工
     *
     * @param createUserId 创建人
     * @param hospitalId   医院id
     * @param userId       用户ID
     * @return 新增个数
     */
    public Integer saveStaff(Integer createUserId, Integer hospitalId, Integer userId) {
        HospitalStaff hospitalStaff = new HospitalStaff();
        hospitalStaff.setCreateUserId(createUserId);
        hospitalStaff.setHospitalId(hospitalId);
        hospitalStaff.setUserId(userId);
        return baseMapper.insert(hospitalStaff);
    }

}
