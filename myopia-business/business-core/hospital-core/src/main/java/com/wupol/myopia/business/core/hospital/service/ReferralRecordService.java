package com.wupol.myopia.business.core.hospital.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.hospital.domain.dos.ReferralDO;
import com.wupol.myopia.business.core.hospital.domain.dto.ReferralDTO;
import com.wupol.myopia.business.core.hospital.domain.mapper.ReferralRecordMapper;
import com.wupol.myopia.business.core.hospital.domain.model.ReferralRecord;
import com.wupol.myopia.business.core.hospital.util.HospitalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @Author wulizhou
 * @Date 2022-01-04
 */
@Service
public class ReferralRecordService extends BaseService<ReferralRecordMapper, ReferralRecord> {

    @Autowired
    private HospitalDoctorService hospitalDoctorService;

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
    public List<ReferralDO> getByStudentId(Integer studentId) {
        return baseMapper.getByStudentId(studentId);
    }

    /**
     * 保存或更新转诊单
     * @param record
     * @param user
     */
    public void saveOrUpdateReferral(ReferralRecord record, CurrentUser user) {
        record.setFromHospitalId(user.getOrgId());
        record.setFromDoctorId(hospitalDoctorService.getDetailsByUserId(user.getId()).getId());
        saveOrUpdate(record);
    }

}
