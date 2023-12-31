package com.wupol.myopia.business.core.hospital.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.hospital.domain.dos.ReferralDO;
import com.wupol.myopia.business.core.hospital.domain.dto.ReferralDTO;
import com.wupol.myopia.business.core.hospital.domain.mapper.ReferralRecordMapper;
import com.wupol.myopia.business.core.hospital.domain.model.ReferralRecord;
import com.wupol.myopia.business.core.hospital.util.HospitalUtil;
import com.wupol.myopia.business.core.hospital.util.PreschoolCheckRecordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;


/**
 * @Author wulizhou
 * @Date 2022-01-04
 */
@Service
public class ReferralRecordService extends BaseService<ReferralRecordMapper, ReferralRecord> {

    @Autowired
    private HospitalDoctorService hospitalDoctorService;

    /**
     * 通过id获取转诊单详情
     * @param id
     * @return
     */
    public ReferralDTO getDetailById(Integer id) {
        return getDetail(new ReferralRecord().setId(id));
    }

    /**
     * 通过检查号id与医院id获取详情
     * @param hospitalId
     * @param id
     * @return
     */
    public ReferralDTO getDetailByHospitalAndId(Integer hospitalId, Integer id) {
        return getDetail(new ReferralRecord().setFromHospitalId(hospitalId).setId(id));
    }

    /**
     * 获取转诊单详情
     * @param referral
     * @return
     */
    public ReferralDTO getDetail(ReferralRecord referral) {
        ReferralDTO details = baseMapper.getDetail(referral);
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
    public Integer saveOrUpdateReferral(ReferralRecord record, CurrentUser user) {
        if (Objects.isNull(record.getId())) {
            // 保证一个检查记录只有一条转诊信息
            ReferralRecord oldReferral = findOne(new ReferralRecord().setPreschoolCheckRecordId(record.getPreschoolCheckRecordId()));
            if (Objects.nonNull(oldReferral)) {
                record.setId(oldReferral.getId());
            }
        }
        record.setFromHospitalId(user.getOrgId());
        record.setFromDoctorId(hospitalDoctorService.getDetailsByUserId(user.getId()).getId());
        record.setConclusion(PreschoolCheckRecordUtil.referralConclusion(record));
        saveOrUpdate(record);
        return record.getId();
    }

}
