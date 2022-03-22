package com.wupol.myopia.business.api.device.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.ValidatorUtils;
import com.wupol.myopia.business.aggregation.screening.domain.dto.DeviceDataRequestDTO;
import com.wupol.myopia.business.aggregation.screening.service.VisionScreeningBizService;
import com.wupol.myopia.business.api.device.domain.constant.BusinessTypeEnum;
import com.wupol.myopia.business.api.device.domain.dto.VisionDataVO;
import com.wupol.myopia.business.api.device.service.IDeviceDataService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.domain.model.DeviceSourceData;
import com.wupol.myopia.business.core.device.service.DeviceService;
import com.wupol.myopia.business.core.device.service.DeviceSourceDataService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
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
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private DeviceSourceDataService deviceSourceDataService;

    @Resource
    private DeviceService deviceService;

    @Resource
    private ScreeningOrganizationService screeningOrganizationService;

    @Resource
    private VisionScreeningBizService visionScreeningBizService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadDate(DeviceDataRequestDTO requestDTO) {

        String deviceSn = requestDTO.getDeviceSn();
        //先查找设备编码是否存在
        Device device = deviceService.getDeviceByDeviceSn(deviceSn);
        //如果不存在报错
        if (Objects.isNull(device)) {
            throw new BusinessException("无法找到设备:" + deviceSn);
        }
        ScreeningOrganization screeningOrganization = screeningOrganizationService.getById(device.getBindingScreeningOrgId());
        if (Objects.isNull(screeningOrganization) || CommonConst.STATUS_IS_DELETED.equals(screeningOrganization.getStatus())) {
            throw new BusinessException("无法找到筛查机构或该筛查机构已过期！");
        }
        String dataStr = requestDTO.getData();
        if (StringUtils.isBlank(dataStr)) {
            throw new BusinessException("数据不能为空！");
        }
        List<VisionDataVO> visionDataVOS = JSONObject.parseArray(dataStr, VisionDataVO.class);
        visionDataVOS.forEach(visionDataVO -> {
            ValidatorUtils.validate(visionDataVO);
            Integer planStudentId = Objects.nonNull(visionDataVO.getPlanStudentId()) ? visionDataVO.getPlanStudentId() : parsePlanStudentId(visionDataVO.getUid());
            log.info("planStudentId:{}", planStudentId);
            ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getById(planStudentId);
            if (Objects.isNull(planStudent)) {
                throw new BusinessException("不能通过该Id找到学生信息，请确认！");
            }
            Integer orgId = screeningOrganization.getId();
            if (!planStudent.getScreeningOrgId().equals(orgId)) {
                throw new BusinessException("筛查学生与筛查机构不匹配！");
            }
            Long screeningTime = visionDataVO.getScreeningTime();
            // 保存原始数据
            saveDeviceData(device, dataStr, planStudentId, orgId, screeningTime);
            // 更新或新增筛查学生结果
            saveOrUpdateScreeningResult(visionDataVO, planStudent);
        });
        log.info(JSONObject.toJSONString(requestDTO));
    }

    @Override
    public Integer getBusinessType() {
        return BusinessTypeEnum.VISION_DATA.getType();
    }

    @Override
    public Integer parsePlanStudentId(String uid) {
        try {
            if (uid.startsWith("SA@") || uid.startsWith("SV@")) {
                return Integer.valueOf(uid.substring(uid.indexOf("@") + 1));
            }
            if (uid.startsWith("[VS@")) {
                String s = StringUtils.substringBetween(uid, "@", ",");
                return Integer.valueOf(s.substring(s.indexOf("_") + 1));
            }
        } catch (Exception e) {
            throw new BusinessException("二维码解析异常");
        }
        return Integer.valueOf(uid);
    }

    /**
     * 保存原始信息
     *
     * @param device        设备信息
     * @param dataStr       数据
     * @param planStudentId 筛查学生
     * @param orgId         筛查机构
     * @param screeningTime 筛查时间
     */
    private void saveDeviceData(Device device, String dataStr, Integer planStudentId, Integer orgId, Long screeningTime) {
        DeviceSourceData data = new DeviceSourceData();
        data.setDeviceType(device.getType());
        data.setPatientId(String.valueOf(planStudentId));
        data.setDeviceId(device.getId());
        data.setDeviceCode(device.getDeviceCode());
        data.setDeviceSn(device.getDeviceSn());
        data.setSrcData(dataStr);
        data.setScreeningOrgId(orgId);
        data.setScreeningTime(Objects.nonNull(screeningTime) ? DateUtil.date(screeningTime) : new Date());
        deviceSourceDataService.save(data);
    }

    /**
     * 保存或更新筛查结果
     *
     * @param visionDataVO 上传数据实体
     * @param planStudent  筛查学生
     */
    private void saveOrUpdateScreeningResult(VisionDataVO visionDataVO, ScreeningPlanSchoolStudent planStudent) {
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
        if (ObjectsUtil.allNotNull(visionDataDTO.getLeftCorrectedVision(), visionDataDTO.getRightCorrectedVision())) {
            visionDataDTO.setGlassesType(WearingGlassesSituation.WEARING_FRAME_GLASSES_TYPE);
        } else {
            visionDataDTO.setGlassesType(WearingGlassesSituation.NOT_WEARING_GLASSES_TYPE);
        }
        visionScreeningBizService.saveOrUpdateStudentScreenData(visionDataDTO);
    }
}
