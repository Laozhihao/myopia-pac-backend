package com.wupol.myopia.oauth.domain.mapper;

import com.wupol.myopia.oauth.domain.model.DistrictPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.oauth.domain.model.Permission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 行政区权限表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-23
 */
public interface DistrictPermissionMapper extends BaseMapper<DistrictPermission> {

    /**
     * 根据模板类型获取模板权限-树结构
     *
     * @param pid 根节点的ID
     * @param templateType 模板类型
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    List<Permission> selectTemplatePermissionTree(@Param("pid")Integer pid, @Param("templateType")Integer templateType);
}
