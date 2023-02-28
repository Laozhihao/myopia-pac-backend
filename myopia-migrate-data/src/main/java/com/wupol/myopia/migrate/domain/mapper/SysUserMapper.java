package com.wupol.myopia.migrate.domain.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.migrate.domain.model.SysUser;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理员表Mapper接口
 *
 * @Author HaoHao
 * @Date 2022-03-23
 */
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@DS("data_source_db")
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据筛查机构ID获取出现最多的筛查人员名称
     *
     * @param deptId 筛查机构ID
     * @return java.lang.String
     **/
    String findMostStaffNameByDeptId(@Param("deptId") String deptId);
}
