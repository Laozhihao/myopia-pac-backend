package com.wupol.myopia.business.core.hospital.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.hospital.domain.dto.ReceiptDTO;
import com.wupol.myopia.business.core.hospital.domain.mapper.ReceiptListMapper;
import com.wupol.myopia.business.core.hospital.domain.model.ReceiptList;
import com.wupol.myopia.business.core.hospital.util.HospitalUtil;
import org.springframework.stereotype.Service;


/**
 * @Author wulizhou
 * @Date 2022-01-04
 */
@Service
public class ReceiptListService extends BaseService<ReceiptListMapper, ReceiptList> {

    /**
     * 获取回执单详情
     * @param id
     * @return
     */
    public ReceiptDTO getDetails(Integer id) {
        ReceiptDTO details = baseMapper.getDetails(id);
        HospitalUtil.setParentInfo(details);
        return details;
    }

}
