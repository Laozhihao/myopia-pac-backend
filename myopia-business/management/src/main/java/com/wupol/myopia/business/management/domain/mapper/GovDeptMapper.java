package com.wupol.myopia.business.management.domain.mapper;

import com.wupol.myopia.business.management.domain.model.GovDept;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * 政府部门表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-23
 */
public interface GovDeptMapper extends BaseMapper<GovDept> {

    /**
     * 获取指定部门及其下面的所有部门的数据树
     *
     * @param pid 部门ID
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.GovDept>
     **/
    List<GovDept> selectGovDeptTreeByPid(Integer pid);

    /**
     * 获取指定部门及其下面的所有部门的ID数据树
     *
     * @param pid 部门ID
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.GovDept>
     **/
    List<GovDept> selectIdTreeByPid(Integer pid);

}
