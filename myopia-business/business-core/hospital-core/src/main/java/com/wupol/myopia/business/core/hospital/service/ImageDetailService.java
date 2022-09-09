package com.wupol.myopia.business.core.hospital.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.hospital.domain.mapper.ImageDetailMapper;
import com.wupol.myopia.business.core.hospital.domain.model.ImageDetail;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Simple4H
 */
@Service
public class ImageDetailService extends BaseService<ImageDetailMapper, ImageDetail> {


    /**
     * 获取当天的眼底影像
     *
     * @param patientId  患者Id
     * @param hospitalId 医院Id
     *
     * @return List<ImageDetail>
     */
    public List<ImageDetail> getTodayPatientFundusFile(Integer patientId, Integer hospitalId) {
        return baseMapper.getTodayPatientFundusFile(patientId, hospitalId);
    }

}
