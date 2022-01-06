package com.wupol.myopia.business.core.hospital.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.hospital.domain.dto.ReferralDTO;
import com.wupol.myopia.business.core.hospital.domain.model.ReferralRecord;
import org.apache.ibatis.annotations.Param;


/**
 * 转诊信息表Mapper接口
 *
 * @Author wulizhou
 * @Date 2022-01-04
 */
public interface ReferralRecordMapper extends BaseMapper<ReferralRecord> {

    /**
     * 获取转诊单详情
     * @param id
     * @return
     */
    ReferralDTO getDetails(@Param("id") Integer id);

}
