package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.vo.GovDeptVo;
import org.apache.ibatis.annotations.Param;

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
    List<GovDeptVo> selectGovDeptTreeByPid(Integer pid);

    /**
     * 获取指定部门及其下面的所有部门的ID数据树
     *
     * @param pid 部门ID
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.GovDept>
     **/
    List<GovDept> selectIdTreeByPid(Integer pid);

    /**
     * 获取政府部门（带有行政区域）
     *
     * @param ids 部门ID集
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.GovDept>
     **/
    List<GovDeptVo> selectGovDeptWithDistrictByIds(@Param("ids") List<Integer> ids);

    /**
     * 获取部门列表
     *
     * @param govDept 查询参数
     * @return java.util.List<com.wupol.myopia.business.management.domain.vo.GovDeptVo>
     **/
    List<GovDept> selectGovDeptList(GovDept govDept);

    GovDept findByIdAndNeStatus(@Param("id") Integer id, @Param("status") Integer status);

    List<GovDept> findByPidAndNeStatus(@Param("pids") List<Integer> pids, @Param("status") Integer status);

}
