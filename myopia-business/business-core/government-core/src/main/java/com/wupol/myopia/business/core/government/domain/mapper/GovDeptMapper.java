package com.wupol.myopia.business.core.government.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.government.domain.dto.GovDeptDTO;
import com.wupol.myopia.business.core.government.domain.dto.GovDistrictDTO;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
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
    List<GovDeptDTO> selectGovDeptTreeByPid(Integer pid);

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
    List<GovDeptDTO> selectGovDeptWithDistrictByIds(@Param("ids") List<Integer> ids);

    /**
     * 获取部门列表
     *
     * @param govDept 查询参数
     * @return java.util.List<com.wupol.myopia.business.management.domain.vo.GovDeptDTO>
     **/
    List<GovDept> selectGovDeptList(GovDept govDept);

    GovDept findByIdAndNeStatus(@Param("id") Integer id, @Param("status") Integer status);

    List<GovDept> findByPidAndNeStatus(@Param("pids") List<Integer> pids, @Param("status") Integer status);

    List<GovDistrictDTO> getByPid(@Param("pids") List<Integer> pids);

    List<GovDistrictDTO> getAll();

}
