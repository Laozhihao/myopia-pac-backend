package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.management.domain.dto.ScreeningOrgResponse;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 筛查机构表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface ScreeningOrganizationMapper extends BaseMapper<ScreeningOrganization> {

    IPage<ScreeningOrgResponse> getScreeningOrganizationListByCondition(@Param("page") Page<?> page, @Param("govDeptId") List<Integer> govDeptId,
                                                                        @Param("name") String name, @Param("type") Integer type,
                                                                        @Param("orgNo") String orgNo, @Param("code") String code);

    ScreeningOrganization getLastOrgByNo(@Param("code") Long code);
}
