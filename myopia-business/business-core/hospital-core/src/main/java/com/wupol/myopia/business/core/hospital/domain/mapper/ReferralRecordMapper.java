package com.wupol.myopia.business.core.hospital.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.hospital.domain.dos.ReferralDO;
import com.wupol.myopia.business.core.hospital.domain.dto.ReferralDTO;
import com.wupol.myopia.business.core.hospital.domain.model.ReferralRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 转诊信息表Mapper接口
 *
 * @Author wulizhou
 * @Date 2022-01-04
 */
public interface ReferralRecordMapper extends BaseMapper<ReferralRecord> {

    /**
     * 获取转诊单详情
     * @param referral
     * @return
     */
    ReferralDTO getDetails(ReferralRecord referral);

    /**
     * 获取指定学生转诊单信息
     * @param studentId
     * @return
     */
    List<ReferralDO> getByStudentId(@Param("studentId") Integer studentId);

}
