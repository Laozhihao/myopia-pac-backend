package com.wupol.myopia.business.api.device.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.domain.ResultCode;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.ValidatorUtils;
import com.wupol.myopia.business.aggregation.screening.domain.dto.DeviceDataRequestDTO;
import com.wupol.myopia.business.aggregation.screening.service.VisionScreeningBizService;
import com.wupol.myopia.business.api.device.domain.constant.BusinessTypeEnum;
import com.wupol.myopia.business.api.device.domain.dto.VisionDataVO;
import com.wupol.myopia.business.api.device.service.DeviceUploadDataService;
import com.wupol.myopia.business.api.device.service.IDeviceDataService;
import com.wupol.myopia.business.api.device.util.ParsePlanStudentUtils;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.service.DeviceService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 视力数据
 *
 * @author Simple4H
 */
@Slf4j
@Service
public class VisionDataServiceImpl implements IDeviceDataService {

    @Resource
    private DeviceService deviceService;

    @Resource
    private ScreeningOrganizationService screeningOrganizationService;

    @Resource
    private VisionScreeningBizService visionScreeningBizService;

    @Resource
    private DeviceUploadDataService deviceUploadDataService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadDate(DeviceDataRequestDTO requestDTO,String clientId) {

        String deviceSn = requestDTO.getDeviceSn();
        //先查找设备编码是否存在
        Device device = deviceService.getDeviceByDeviceSn(deviceSn);
        if (Objects.isNull(device)) {
            throw new BusinessException("无法找到设备:" + deviceSn, ResultCode.DATA_UPLOAD_DEVICE_ERROR.getCode());
        }
        ScreeningOrganization screeningOrganization = screeningOrganizationService.getById(device.getBindingScreeningOrgId());
        if (Objects.isNull(screeningOrganization) || CommonConst.STATUS_IS_DELETED.equals(screeningOrganization.getStatus())) {
            throw new BusinessException("无法找到筛查机构或该筛查机构已过期！", ResultCode.DATA_UPLOAD_SCREENING_ORG_ERROR.getCode());
        }
        String dataStr = requestDTO.getData();
        if (StringUtils.isBlank(dataStr)) {
            throw new BusinessException("数据不能为空！", ResultCode.DATA_UPLOAD_DATA_EMPTY_ERROR.getCode());
        }
        List<VisionDataVO> visionDataVOS = JSONObject.parseArray(dataStr, VisionDataVO.class);
        visionDataVOS.forEach(visionDataVO -> {
            ValidatorUtils.validate(visionDataVO);
            Integer planStudentId = Objects.nonNull(visionDataVO.getPlanStudentId()) ? visionDataVO.getPlanStudentId() : ParsePlanStudentUtils.parsePlanStudentId(visionDataVO.getUid());
            log.info("planStudentId:{}", planStudentId);
            ScreeningPlanSchoolStudent planStudent = deviceUploadDataService.getScreeningPlanSchoolStudent(screeningOrganization, planStudentId);
            Long screeningTime = visionDataVO.getScreeningTime();
            // 保存原始数据
            deviceUploadDataService.saveDeviceData(device, dataStr, planStudentId, screeningOrganization.getId(), screeningTime);
            // 更新或新增筛查学生结果
            saveOrUpdateScreeningResult(visionDataVO, planStudent,clientId);
        });
        log.info(JSONObject.toJSONString(requestDTO));
    }

    @Override
    public Integer getBusinessType() {
        return BusinessTypeEnum.VISION_DATA.getType();
    }

    /**
     * 保存或更新筛查结果
     *
     * @param visionDataVO 上传数据实体
     * @param planStudent  筛查学生
     */
    private void saveOrUpdateScreeningResult(VisionDataVO visionDataVO, ScreeningPlanSchoolStudent planStudent,String clientId) {
        VisionDataDTO visionDataDTO = new VisionDataDTO();
        visionDataDTO.setRightNakedVision(StringUtils.isNotBlank(visionDataVO.getRightNakedVision()) ? new BigDecimal(visionDataVO.getRightNakedVision()) : null);
        visionDataDTO.setRightCorrectedVision(StringUtils.isNotBlank(visionDataVO.getRightCorrectedVision()) ? new BigDecimal(visionDataVO.getRightCorrectedVision()) : null);
        visionDataDTO.setLeftNakedVision(StringUtils.isNotBlank(visionDataVO.getLeftNakedVision()) ? new BigDecimal(visionDataVO.getLeftNakedVision()) : null);
        visionDataDTO.setLeftCorrectedVision(StringUtils.isNotBlank(visionDataVO.getLeftCorrectedVision()) ? new BigDecimal(visionDataVO.getLeftCorrectedVision()) : null);
        visionDataDTO.setIsCooperative(0);
        visionDataDTO.setDeptId(planStudent.getScreeningOrgId());
        visionDataDTO.setCreateUserId(-1);
        visionDataDTO.setPlanStudentId(String.valueOf(planStudent.getId()));
        visionDataDTO.setSchoolId(String.valueOf(planStudent.getSchoolId()));
        if (Objects.nonNull(visionDataVO.getGlassesType())) {
            try {
                visionDataDTO.setGlassesType(WearingGlassesSituation.getType(visionDataVO.getGlassesType()));
            } catch (Exception e) {
                throw new BusinessException("戴镜类型异常，请确认");
            }
        } else {
            if (ObjectsUtil.allNotNull(visionDataDTO.getLeftCorrectedVision(), visionDataDTO.getRightCorrectedVision())) {
                visionDataDTO.setGlassesType(WearingGlassesSituation.WEARING_FRAME_GLASSES_TYPE);
            } else {
                visionDataDTO.setGlassesType(WearingGlassesSituation.NOT_WEARING_GLASSES_TYPE);
            }
        }
        visionScreeningBizService.saveOrUpdateStudentScreenData(visionDataDTO,clientId);
    }
}
