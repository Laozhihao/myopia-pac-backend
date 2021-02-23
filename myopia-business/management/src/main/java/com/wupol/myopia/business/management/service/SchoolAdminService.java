package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.SchoolAdminMapper;
import com.wupol.myopia.business.management.domain.model.SchoolAdmin;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020-12-22
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

    public SchoolAdmin getAdminBySchoolId(Integer id) {
        return baseMapper
                .selectOne(new QueryWrapper<SchoolAdmin>()
                        .eq("school_id", id));
    }

    /**
     * 批量通过id查询学校
     *
     * @param ids 学校ID
     * @return List<SchoolAdmin>
     */
    public List<SchoolAdmin> getBySchoolIds(List<Integer> ids) {
        return baseMapper.selectBatchIds(ids);
    }

}
