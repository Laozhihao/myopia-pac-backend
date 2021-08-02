package com.wupol.myopia.business.api.device.service;

import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.screening.service.VisionScreeningBizService;
import com.wupol.myopia.business.api.device.domain.dto.DeviceUploadDTO;
import com.wupol.myopia.business.api.device.util.CheckResultUtil;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreenDataDTO;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.service.DeviceScreeningDataService;
import com.wupol.myopia.business.core.device.service.DeviceService;
import com.wupol.myopia.business.core.device.service.DeviceSourceDataService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 设备数据上传
 *
 * @Author Jacob
 * @Date 2021-07-15 15:01:19
 */
@Service
public class DeviceUploadDataService {
    /**
     * 设备上传的默认用户id
     */
    private final Integer DEVICE_UPLOAD_DEFAULT_USER_ID = 0;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private DeviceSourceDataService deviceSourceDataService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private VisionScreeningBizService visionScreeningBizService;
    @Autowired
    private DeviceScreeningDataService deviceScreeningDataService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;


    /**
     * 保存设备上传数据到筛查结果中
     * @param deviceScreenDataDTOList
     */
    public void saveDeviceScreeningDatas2ScreeningResult(List<DeviceScreenDataDTO> deviceScreenDataDTOList) {
        deviceScreenDataDTOList = deviceScreenDataDTOList.stream().filter(DeviceUploadDataService::dealStrudentId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(deviceScreenDataDTOList)) {
            return;
        }
        // 将所有的patientId(planStudentId)拿出来
        Set<String> planStudentIdSet = deviceScreenDataDTOList.stream().map(DeviceScreenDataDTO::getPatientId).collect(Collectors.toSet());
        // 批量查找学生信息
        List<ScreeningPlanSchoolStudent> screeningPlanStudents = screeningPlanSchoolStudentService.getByIds(planStudentIdSet);
        Map<Integer, ScreeningPlanSchoolStudent> planStudentMap = screeningPlanStudents.stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getId, Function.identity()));
        deviceScreenDataDTOList.forEach(deviceScreenDataDTO -> {
            ScreeningPlanSchoolStudent screeningPlanSchoolStudent = planStudentMap.get(Integer.valueOf(deviceScreenDataDTO.getPatientId()));
            ComputerOptometryDTO computerOptometryDTO = getComputerOptometryDTO(deviceScreenDataDTO, screeningPlanSchoolStudent);
            visionScreeningBizService.saveOrUpdateStudentScreenData(computerOptometryDTO);
        });
    }

    /**
     * 获取ComputerOptometryDTO
     * @param deviceScreenDataDTO
     * @param screeningPlanSchoolStudent
     * @return
     */
    private ComputerOptometryDTO getComputerOptometryDTO(DeviceScreenDataDTO deviceScreenDataDTO,ScreeningPlanSchoolStudent screeningPlanSchoolStudent) {
        ComputerOptometryDTO computerOptometryDTO = new ComputerOptometryDTO();
        computerOptometryDTO.setLAxial(BigDecimal.valueOf(deviceScreenDataDTO.getLeftAxsi()));
        computerOptometryDTO.setRAxial(BigDecimal.valueOf(deviceScreenDataDTO.getRightAxsi()));
        computerOptometryDTO.setLCyl(BigDecimal.valueOf(deviceScreenDataDTO.getLeftCyl()));
        computerOptometryDTO.setRCyl(BigDecimal.valueOf(deviceScreenDataDTO.getRightCyl()));
        computerOptometryDTO.setRSph(BigDecimal.valueOf(deviceScreenDataDTO.getRightSph()));
        computerOptometryDTO.setLSph(BigDecimal.valueOf(deviceScreenDataDTO.getLeftSph()));
        computerOptometryDTO.setDeptId(screeningPlanSchoolStudent.getScreeningOrgId());
        computerOptometryDTO.setCreateUserId(DEVICE_UPLOAD_DEFAULT_USER_ID);
        computerOptometryDTO.setStudentId(String.valueOf(screeningPlanSchoolStudent.getStudentId()));
        computerOptometryDTO.setSchoolId(String.valueOf(screeningPlanSchoolStudent.getSchoolId()));
        return computerOptometryDTO;
    }

    /**
     * 上传设备数据
     * @param deviceUploadDto 接受
     */
    @Transactional(rollbackFor = Exception.class)
    public void uploadDeviceData(DeviceUploadDTO deviceUploadDto) {
        //先查找设备编码是否存在
        Device device = deviceService.getDeviceByDeviceSn(deviceUploadDto.getImei());
        //如果不存在报错
        if (device == null) {
            throw new BusinessException("无法找到设备");
        }
        //查询筛查机构是否过期
        ScreeningOrganization screeningOrganization = screeningOrganizationService.findOne(new ScreeningOrganization().setId(device.getBindingScreeningOrgId()).setStatus(CommonConst.STATUS_NOT_DELETED));
        if (screeningOrganization == null) {
            throw new BusinessException("无法找到筛查机构或该筛查机构已过期");
        }
        //设置检查结果
        this.setCheckResult(deviceUploadDto.getData());
        //判断id是否是特殊设备扫描出来的(如vs666)
        List<DeviceScreenDataDTO> deviceScreenDataDTOList = deviceUploadDto.getData();

        if (CollectionUtils.isEmpty(deviceScreenDataDTOList)) {
            throw new BusinessException("无法找到筛查数据");
        }
        //过滤掉重复上传的数据
        deviceScreenDataDTOList = deviceSourceDataService.filterExistData(device.getBindingScreeningOrgId(),device.getDeviceSn(),deviceScreenDataDTOList);
        if (CollectionUtils.isEmpty(deviceScreenDataDTOList)) {
            return;
        }

        // 保存Source数据
        deviceSourceDataService.saveDeviceSourceDataList(device, deviceScreenDataDTOList);
        // 保存设备数据
        deviceScreeningDataService.saveDeviceScreeningDataList(device,deviceScreenDataDTOList);
        // 过滤出计划学生,并保存到计划筛查数据中
        saveDeviceScreeningDatas2ScreeningResult(deviceScreenDataDTOList);
    }

    /**
     * 设置检查结果
     * @param deviceScreenDataDTOList
     */
    private void setCheckResult(List<DeviceScreenDataDTO> deviceScreenDataDTOList) {
        deviceScreenDataDTOList.forEach(deviceScreenDataDTO -> {
            String checkResult = CheckResultUtil.getCheckResult(deviceScreenDataDTO);
            deviceScreenDataDTO.setCheckResult(checkResult);
        });
    }


    /**
     * 处理studentId
     * detail:
     * 原有的patientId 应该是 VS@222_000000000000000011
     * 其中222代表planId,而000000000000000011除去前面0之外,也就是11是planStudentId,该方法正式通过这个逻辑取到planStudentId;
     * @param deviceScreenDataDTO
     * @return
     */
    private static boolean dealStrudentId(DeviceScreenDataDTO deviceScreenDataDTO) {
        String patientId = deviceScreenDataDTO.getPatientId();
        String reg = "^VS@\\d{1,}_\\d{1,}";
        if (!patientId.matches(reg) || patientId.length() != 35) {
            deviceScreenDataDTO.setPatientId(null);
            return false;
        }
        String planStudentIdWithZero = patientId.substring(patientId.indexOf("_") + 1);
        //主要是为了去除0, 如 000000001 ,通过转换后可以变成integer类型的1,再将其转换为字符串
        deviceScreenDataDTO.setPatientId(Integer.valueOf(planStudentIdWithZero) + "");
        return true;
    }

}