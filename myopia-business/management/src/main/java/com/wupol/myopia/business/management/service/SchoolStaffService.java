package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.SchoolStaffMapper;
import com.wupol.myopia.business.management.domain.model.SchoolStaff;
import org.springframework.stereotype.Service;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Service
public class SchoolStaffService extends BaseService<SchoolStaffMapper, SchoolStaff> {

    /**
     * 创建员工
     *
     * @param schoolId     学校id
     * @param createUserId 创建人
     * @param govDeptId    部门id
     * @param userId       用户id
     * @return 创建个数
     */
    public Integer insertStaff(Integer schoolId, Integer createUserId, Integer govDeptId, Integer userId) {
        SchoolStaff schoolStaff = new SchoolStaff();
        schoolStaff.setSchoolId(schoolId);
        schoolStaff.setUserId(userId);
        schoolStaff.setCreateUserId(createUserId);
        schoolStaff.setGovDeptId(govDeptId);
        return baseMapper.insert(schoolStaff);
    }

    public SchoolStaff getStaffBySchoolId(Integer id) {
        return baseMapper.selectOne(new QueryWrapper<SchoolStaff>().eq("school_id", id));
    }

}
