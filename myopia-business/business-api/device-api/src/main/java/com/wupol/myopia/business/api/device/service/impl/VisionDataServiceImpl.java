package com.wupol.myopia.business.api.device.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.screening.domain.dto.DeviceDataRequestDTO;
import com.wupol.myopia.business.api.device.domain.constant.BusinessTypeEnum;
import com.wupol.myopia.business.api.device.domain.dto.VisionDataVO;
import com.wupol.myopia.business.api.device.service.IDeviceDataService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.GlassesTypeEnum;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.domain.model.DeviceSourceData;
import com.wupol.myopia.business.core.device.service.DeviceService;
import com.wupol.myopia.business.core.device.service.DeviceSourceDataService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
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
    private VisionScreeningResultService visionScreeningResultService;


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
            throw new BusinessException("无法找到筛查机构或该筛查机构已过期");
        }
        String dataStr = requestDTO.getData();
        if (StringUtils.isBlank(dataStr)) {
            throw new BusinessException("数据不能为空");
        }
        List<VisionDataVO> visionDataVOS = JSONObject.parseArray(dataStr, VisionDataVO.class);
        visionDataVOS.forEach(visionDataVO -> {
            Integer planStudentId = visionDataVO.getPlanStudentId();
            ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getById(planStudentId);
            if (Objects.isNull(planStudent)) {
                throw new BusinessException("学生信息异常");
            }
            Integer orgId = screeningOrganization.getId();
            if (!planStudent.getScreeningOrgId().equals(orgId)) {
                throw new BusinessException("筛查学生与筛查机构不匹配");
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
        VisionScreeningResult result = visionScreeningResultService.getByPlanStudentId(visionDataVO.getPlanStudentId());
        if (Objects.isNull(result)) {
            result = new VisionScreeningResult();
            result.setTaskId(planStudent.getScreeningTaskId());
            result.setScreeningOrgId(planStudent.getScreeningOrgId());
            result.setSchoolId(planStudent.getSchoolId());
            result.setScreeningPlanSchoolStudentId(planStudent.getId());
            result.setCreateUserId(-1);
            result.setStudentId(planStudent.getStudentId());
            result.setPlanId(planStudent.getScreeningPlanId());
            result.setDistrictId(planStudent.getPlanDistrictId());
        }
        VisionDataDO visionDataDO = new VisionDataDO();
        visionDataDO.setRightEyeData(generateData(visionDataVO, false));
        visionDataDO.setLeftEyeData(generateData(visionDataVO, true));
        visionDataDO.setIsCooperative(0);
        visionDataDO.setCreateUserId(-1);
        result.setVisionData(visionDataDO);
        result.setUpdateTime(new Date());
        visionScreeningResultService.saveOrUpdate(result);
    }

    /**
     * 生成数据
     *
     * @param visionDataVO 上传数据实体
     * @param isLeft       是否左眼
     * @return VisionDataDO.VisionData
     */
    private VisionDataDO.VisionData generateData(VisionDataVO visionDataVO, boolean isLeft) {
        VisionDataDO.VisionData visionData = new VisionDataDO.VisionData();
        try {
            if (isLeft) {
                visionData.setLateriality(CommonConst.LEFT_EYE);
                visionData.setCorrectedVision(StringUtils.isBlank(visionDataVO.getLeftCorrectedVision()) ? null : new BigDecimal(visionDataVO.getLeftCorrectedVision()));
                visionData.setNakedVision(StringUtils.isBlank(visionDataVO.getLeftNakedVision()) ? null : new BigDecimal(visionDataVO.getLeftNakedVision()));
                if (Objects.nonNull(visionData.getCorrectedVision())) {
                    visionData.setGlassesType(GlassesTypeEnum.FRAME_GLASSES.code);
                } else {
                    visionData.setGlassesType(GlassesTypeEnum.NOT_WEARING.code);
                }
                return visionData;
            }
            visionData.setLateriality(CommonConst.RIGHT_EYE);
            visionData.setCorrectedVision(StringUtils.isBlank(visionDataVO.getRightCorrectedVision()) ? null : new BigDecimal(visionDataVO.getRightCorrectedVision()));
            visionData.setNakedVision(StringUtils.isBlank(visionDataVO.getRightNakedVision()) ? null : new BigDecimal(visionDataVO.getRightNakedVision()));
            if (Objects.nonNull(visionData.getCorrectedVision())) {
                visionData.setGlassesType(GlassesTypeEnum.FRAME_GLASSES.code);
            } else {
                visionData.setGlassesType(GlassesTypeEnum.NOT_WEARING.code);
            }
        } catch (NumberFormatException e) {
            throw new BusinessException("数据格式异常");
        }
        return visionData;
    }
}
