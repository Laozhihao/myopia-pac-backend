package com.wupol.myopia.business.core.hospital.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.hospital.domain.mapper.HospitalAdminMapper;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalAdmin;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 医院管理员
 *
 * @author Simple4H
 */
@Service
public class HospitalAdminService extends BaseService<HospitalAdminMapper, HospitalAdmin> {

    /**
     * 新增医院管理员
     *
     * @param createUserId 创建人
     * @param hospitalId   医院ID
     * @param userId       用户ID
     * @param govDeptId    部门ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveAdmin(Integer createUserId, Integer hospitalId, Integer userId, Integer govDeptId) {
        HospitalAdmin hospitalAdmin = new HospitalAdmin();
        hospitalAdmin.setCreateUserId(createUserId);
        hospitalAdmin.setHospitalId(hospitalId);
        hospitalAdmin.setUserId(userId);
        hospitalAdmin.setGovDeptId(govDeptId);
        baseMapper.insert(hospitalAdmin);
    }

    /**
     * 通过医院ID获取医院ADMIN
     *
     * @param hospitalId 医院id
     * @return admin
     */
    public HospitalAdmin getByHospitalId(Integer hospitalId) {
        return baseMapper.getByHospitalId(hospitalId);
    }

    /**
     * 获取筛查机构绑定的医院管理员信息
     * @param orgIds
     * @return
     */
    public List<HospitalAdmin> getHospitalAdminByOrgIds(List<Integer> orgIds) {
        if (CollectionUtils.isEmpty(orgIds)) {
            return new ArrayList<>();
        }
        return baseMapper.getHospitalAdminByOrgIds(orgIds);
    }

}
