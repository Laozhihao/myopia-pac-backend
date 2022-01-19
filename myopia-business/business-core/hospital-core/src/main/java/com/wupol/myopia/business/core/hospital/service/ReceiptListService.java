package com.wupol.myopia.business.core.hospital.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.hospital.domain.dto.ReceiptDTO;
import com.wupol.myopia.business.core.hospital.domain.mapper.ReceiptListMapper;
import com.wupol.myopia.business.core.hospital.domain.model.PreschoolCheckRecord;
import com.wupol.myopia.business.core.hospital.domain.model.ReceiptList;
import com.wupol.myopia.business.core.hospital.domain.model.SpecialMedical;
import com.wupol.myopia.business.core.hospital.util.HospitalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;


/**
 * @Author wulizhou
 * @Date 2022-01-04
 */
@Service
public class ReceiptListService extends BaseService<ReceiptListMapper, ReceiptList> {

    @Autowired
    private HospitalDoctorService hospitalDoctorService;

    @Autowired
    private PreschoolCheckRecordService preschoolCheckRecordService;

    /**
     * 通过id获取回执单详情
     * @param id
     * @return
     */
    public ReceiptDTO getDetailById(Integer id) {
        return getDetail(new ReceiptList().setId(id));
    }

    /**
     * 通过检查号id与医院id获取详情
     * @param hospitalId
     * @param preschoolCheckRecordId
     * @return
     */
    public ReceiptDTO getDetailByHospitalAndPreschoolCheckRecordId(Integer hospitalId, Integer preschoolCheckRecordId) {
        return getDetail(new ReceiptList().setFromHospitalId(hospitalId).setPreschoolCheckRecordId(preschoolCheckRecordId));
    }

    /**
     * 获取编辑详情，即回执单内容+最新专项检查结果
     * @param hospitalId
     * @param preschoolCheckRecordId
     * @return
     */
    public ReceiptDTO getEditDetailByHospitalAndPreschoolCheckRecordId(Integer hospitalId, Integer preschoolCheckRecordId) {
        ReceiptDTO detail = getDetail(new ReceiptList().setFromHospitalId(hospitalId).setPreschoolCheckRecordId(preschoolCheckRecordId));
        PreschoolCheckRecord preschool = preschoolCheckRecordService.getById(detail.getPreschoolCheckRecordId());
        SpecialMedical specialMedical = new SpecialMedical(preschool.getRedReflex(), preschool.getOcularInspection(),
                preschool.getMonocularMaskingAversionTest(), preschool.getRefractionData());
        detail.setSpecialMedical(specialMedical);
        return detail;
    }

    /**
     * 获取回执单详情
     * @param receipt
     * @return
     */
    public ReceiptDTO getDetail(ReceiptList receipt) {
        ReceiptDTO details = baseMapper.getDetail(receipt);
        HospitalUtil.setParentInfo(details);
        return details;
    }

    /**
     * 保存回执单信息
     * @param receiptList
     * @param user
     */
    public void saveOrUpdateReceiptList(ReceiptList receiptList, CurrentUser user) {
        if (Objects.isNull(receiptList.getId())) {
            // 保证一个检查记录只有一条回执信息
            ReceiptList oldReceiptList = findOne(new ReceiptList().setPreschoolCheckRecordId(receiptList.getPreschoolCheckRecordId()));
            if (Objects.nonNull(oldReceiptList)) {
                receiptList.setId(oldReceiptList.getId());
            }
        }
        receiptList.setFromHospitalId(user.getOrgId());
        receiptList.setFromDoctorId(hospitalDoctorService.getDetailsByUserId(user.getId()).getId());
        saveOrUpdate(receiptList);
    }

}
