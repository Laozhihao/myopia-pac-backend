package com.wupol.myopia.business.core.hospital.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.hospital.domain.dto.ReceiptDTO;
import com.wupol.myopia.business.core.hospital.domain.mapper.ReceiptListMapper;
import com.wupol.myopia.business.core.hospital.domain.model.ReceiptList;
import com.wupol.myopia.business.core.hospital.util.HospitalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @Author wulizhou
 * @Date 2022-01-04
 */
@Service
public class ReceiptListService extends BaseService<ReceiptListMapper, ReceiptList> {

    @Autowired
    private HospitalDoctorService hospitalDoctorService;

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
     * @param id
     * @return
     */
    public ReceiptDTO getDetailByHospitalAndId(Integer hospitalId, Integer id) {
        return getDetail(new ReceiptList().setFromHospitalId(hospitalId).setId(id));
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
        receiptList.setFromHospitalId(user.getOrgId());
        receiptList.setFromDoctorId(hospitalDoctorService.getDetailsByUserId(user.getId()).getId());
        saveOrUpdate(receiptList);
    }

}
