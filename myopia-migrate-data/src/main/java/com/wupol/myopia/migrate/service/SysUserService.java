package com.wupol.myopia.migrate.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.migrate.domain.mapper.SysUserMapper;
import com.wupol.myopia.migrate.domain.model.SysUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * @Author HaoHao
 * @Date 2022-03-23
 */
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@Service
public class SysUserService extends BaseService<SysUserMapper, SysUser> {

    /**
     * 根据筛查机构ID获取出现最多的筛查人员名称
     *
     * @param deptId 筛查机构ID
     * @return java.lang.String
     **/
    public String findMostStaffNameByDeptId(String deptId) {
        Assert.hasText(deptId, "筛查机构ID不能为空");
        return baseMapper.findMostStaffNameByDeptId(deptId);
    }
}
