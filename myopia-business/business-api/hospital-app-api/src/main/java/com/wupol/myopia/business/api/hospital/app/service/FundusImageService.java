package com.wupol.myopia.business.api.hospital.app.service;

import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.hospital.app.domain.dto.FundusImageDTO;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.hospital.domain.model.ImageDetail;
import com.wupol.myopia.business.core.hospital.service.ImageDetailService;
import com.wupol.myopia.business.core.hospital.service.ImageOriginalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 眼底影像
 *
 * @author Simple4H
 */
@Service
@Slf4j
public class FundusImageService {

    @Resource
    private ResourceFileService resourceFileService;

    @Resource
    private ImageOriginalService imageOriginalService;

    @Resource
    private ImageDetailService imageDetailService;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 获取患者当天眼底影像
     *
     * @param patientId  患者Id
     * @param hospitalId 医院Id
     *
     * @return 眼底影像
     */
    public List<FundusImageDTO> getPatientFundusFile(Integer patientId, Integer hospitalId) {
        Object obj = redisUtil.get(String.format(RedisConstant.HOSPITAL_DEVICE_UPLOAD_FUNDUS_PATIENT, patientId));
        if (Objects.nonNull(obj)) {
            throw new BusinessException("正在解析影像中");
        }
        List<ImageDetail> todayPatientFundusFile = imageDetailService.getTodayPatientFundusFile(patientId, hospitalId);
        return todayPatientFundusFile.stream().map(s -> {
            FundusImageDTO fundusImageDTO = new FundusImageDTO();
            fundusImageDTO.setFileId(s.getFileId());
            fundusImageDTO.setFileUrl(resourceFileService.getResourcePath(s.getFileId()));
            return fundusImageDTO;
        }).collect(Collectors.toList());
    }

    /**
     * 删除患者当天所有影像
     *
     * @param patientId  患者Id
     * @param hospitalId 医院Id
     *
     * @return ReturnInformation
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deletedPatientTodayImage(Integer patientId, Integer hospitalId) {
        Object obj = redisUtil.get(String.format(RedisConstant.HOSPITAL_DEVICE_UPLOAD_FUNDUS_PATIENT, patientId));
        if (Objects.nonNull(obj)) {
            throw new BusinessException("正在解析影像中");
        }
        List<ImageDetail> imageDetails = imageDetailService.getTodayPatientFundusFile(patientId, hospitalId);
        // 删除详情
        imageDetailService.removeByIds(imageDetails.stream().map(ImageDetail::getId).collect(Collectors.toList()));
        // 删除原始表
        imageOriginalService.removeByIds(imageDetails.stream().map(ImageDetail::getImageOriginalId).collect(Collectors.toList()));
        return true;
    }

}
