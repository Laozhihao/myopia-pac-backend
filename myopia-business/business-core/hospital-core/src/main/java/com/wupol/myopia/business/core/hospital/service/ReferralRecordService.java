package com.wupol.myopia.business.core.hospital.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.hospital.domain.dto.ReferralDTO;
import com.wupol.myopia.business.core.hospital.domain.mapper.ReferralRecordMapper;
import com.wupol.myopia.business.core.hospital.domain.model.ReferralRecord;
import com.wupol.myopia.business.core.hospital.util.HospitalUtil;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @Author wulizhou
 * @Date 2022-01-04
 */
@Service
public class ReferralRecordService extends BaseService<ReferralRecordMapper, ReferralRecord> {

    /**
     * 获取转诊单详情
     * @param id
     * @return
     */
    public ReferralDTO getDetails(Integer id) {
        ReferralDTO details = baseMapper.getDetails(id);
        HospitalUtil.setParentInfo(details);
        return details;
    }

    /**
     * 获取学生所有转诊单信息
     * @param studentId
     * @return
     */
    public List<ReferralRecord> getByStudentId(Integer studentId) {
        return baseMapper.getByStudentId(studentId);
    }

}
