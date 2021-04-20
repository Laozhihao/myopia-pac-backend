package com.wupol.myopia.business.core.school.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.SchoolAdminMapper;
import com.wupol.myopia.business.management.domain.model.SchoolAdmin;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 学校管理员
 *
 * @author Simple4H
 */
@Service
public class SchoolAdminService extends BaseService<SchoolAdminMapper, SchoolAdmin> {

    /**
     * 创建员工
     *
     * @param schoolId     学校id
     * @param createUserId 创建人
     * @param govDeptId    部门id
     * @param userId       用户id
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertStaff(Integer schoolId, Integer createUserId, Integer govDeptId, Integer userId) {
        SchoolAdmin schoolAdmin = new SchoolAdmin();
        schoolAdmin.setSchoolId(schoolId);
        schoolAdmin.setUserId(userId);
        schoolAdmin.setCreateUserId(createUserId);
        schoolAdmin.setGovDeptId(govDeptId);
        baseMapper.insert(schoolAdmin);
    }

    /**
     * 通过学校ID获取学校管理员
     *
     * @param id 学校ID
     * @return SchoolAdmin
     */
    public SchoolAdmin getAdminBySchoolId(Integer id) {
        return baseMapper.getBySchoolId(id);
    }

    /**
     * 批量通过schoolId查询学校
     *
     * @param schoolIds 学校ID
     * @return List<SchoolAdmin>
     */
    public List<SchoolAdmin> getBySchoolIds(List<Integer> schoolIds) {
        return baseMapper.getBySchoolIds(schoolIds);
    }

}
