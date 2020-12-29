package com.wupol.myopia.business.management.domain.mapper;

import com.wupol.myopia.business.management.domain.model.District;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.model.GovDept;

import java.util.List;

/**
 * 行政区域表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-23
 */
public interface DistrictMapper extends BaseMapper<District> {
    List<GovDept> selectAllTree(Integer parentCode);
}
