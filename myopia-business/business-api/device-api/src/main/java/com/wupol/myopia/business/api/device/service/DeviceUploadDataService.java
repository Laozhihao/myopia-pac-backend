package com.wupol.myopia.business.api.device.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.domain.ResultCode;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.screening.service.VisionScreeningBizService;
import com.wupol.myopia.business.api.device.domain.dto.*;
import com.wupol.myopia.business.api.device.util.CheckResultUtil;
import com.wupol.myopia.business.api.device.util.ParsePlanStudentUtils;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.util.VS666Util;
import com.wupol.myopia.business.core.common.domain.model.Cooperation;
import com.wupol.myopia.business.core.device.constant.OrgTypeEnum;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreenDataDTO;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.domain.model.DeviceSourceData;
import com.wupol.myopia.business.core.device.service.DeviceScreeningDataService;
import com.wupol.myopia.business.core.device.service.DeviceService;
import com.wupol.myopia.business.core.device.service.DeviceSourceDataService;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 设备数据上传
 *
 * @Author Jacob
 * @Date 2021-07-15 15:01:19
 */
@Service
@Log4j2
public class DeviceUploadDataService {
    /**
     * 设备上传的默认用户id
     */
    private static final Integer DEVICE_UPLOAD_DEFAULT_USER_ID = 0;

    /**
     * 分割符
     */
    private static final String DELIMITER_CHAR = "-";

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
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private SchoolService schoolService;

    /**
     * 处理studentId
     * detail:
     * 原有的patientId 应该是 VS@222_000000000000000011
     * 其中222代表planId,而000000000000000011除去前面0之外,也就是11是planStudentId,该方法正式通过这个逻辑取到planStudentId;
     *
     * @param deviceScreenDataDTO
     * @return
     */
    private static boolean dealStudentId(DeviceScreenDataDTO deviceScreenDataDTO) {
        deviceScreenDataDTO.setPatientId(String.valueOf(ParsePlanStudentUtils.parsePlanStudentId(deviceScreenDataDTO.getPatientId())));
        return true;
    }

    /**
     * 保存设备上传数据到筛查结果中
     *
     * @param deviceScreenDataDTOList
     */
    public void updateOrSaveDeviceScreeningDatas2ScreeningResult(List<DeviceScreenDataDTO> deviceScreenDataDTOList) {
        deviceScreenDataDTOList = deviceScreenDataDTOList.stream().filter(DeviceUploadDataService::dealStudentId).collect(Collectors.toList());
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
            if (Objects.isNull(screeningPlanSchoolStudent)) {
                log.error("上传数据异常，学生Id:{}", Integer.valueOf(deviceScreenDataDTO.getPatientId()));
                throw new BusinessException("学生数据异常");
            }
            ComputerOptometryDTO computerOptometryDTO = getComputerOptometryDTO(deviceScreenDataDTO, screeningPlanSchoolStudent);
            visionScreeningBizService.saveOrUpdateStudentScreenData(computerOptometryDTO);
        });
    }

    /**
     * 获取ComputerOptometryDTO
     *
     * @param deviceScreenDataDTO
     * @param screeningPlanSchoolStudent
     * @return
     */
    private ComputerOptometryDTO getComputerOptometryDTO(DeviceScreenDataDTO deviceScreenDataDTO, ScreeningPlanSchoolStudent screeningPlanSchoolStudent) {
        ComputerOptometryDTO computerOptometryDTO = new ComputerOptometryDTO();
        if (deviceScreenDataDTO.getLeftAxsi() != null) {
            computerOptometryDTO.setLAxial(BigDecimal.valueOf(deviceScreenDataDTO.getLeftAxsi()));
        }

        if (deviceScreenDataDTO.getRightAxsi() != null) {
            computerOptometryDTO.setRAxial(BigDecimal.valueOf(deviceScreenDataDTO.getRightAxsi()));
        }

        Double leftCyl = VS666Util.getDisplayValue(deviceScreenDataDTO.getLeftCyl());
        if (leftCyl != null) {
            computerOptometryDTO.setLCyl(BigDecimal.valueOf(leftCyl));
        }

        Double rightCyl = VS666Util.getDisplayValue(deviceScreenDataDTO.getRightCyl());
        if (rightCyl != null) {
            computerOptometryDTO.setRCyl(BigDecimal.valueOf(rightCyl));
        }

        Double rightSph = VS666Util.getDisplayValue(deviceScreenDataDTO.getRightSph());
        if (rightSph != null) {
            computerOptometryDTO.setRSph(BigDecimal.valueOf(rightSph));
        }

        Double leftSph = VS666Util.getDisplayValue(deviceScreenDataDTO.getLeftSph());
        if (leftSph != null) {
            computerOptometryDTO.setLSph(BigDecimal.valueOf(leftSph));
        }

        Double leftAxsi = VS666Util.getDisplayValue(deviceScreenDataDTO.getLeftAxsi());
        if (leftAxsi != null) {
            computerOptometryDTO.setLAxial(BigDecimal.valueOf(Math.round(leftAxsi)));
        }

        Double rightAxsi = VS666Util.getDisplayValue(deviceScreenDataDTO.getRightAxsi());
        if (rightAxsi != null) {
            computerOptometryDTO.setRAxial(BigDecimal.valueOf(Math.round(rightAxsi)));
        }

        computerOptometryDTO.setDeptId(screeningPlanSchoolStudent.getScreeningOrgId());
        computerOptometryDTO.setCreateUserId(DEVICE_UPLOAD_DEFAULT_USER_ID);
        computerOptometryDTO.setPlanStudentId(String.valueOf(screeningPlanSchoolStudent.getId()));
        computerOptometryDTO.setSchoolId(String.valueOf(screeningPlanSchoolStudent.getSchoolId()));
        return computerOptometryDTO;
    }

    /**
     * 上传设备数据
     *
     * @param deviceUploadDto 上传数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void uploadDeviceData(DeviceUploadDTO deviceUploadDto) {
        //先查找设备编码是否存在
        Device device = deviceService.getDeviceByDeviceSn(deviceUploadDto.getImei());
        //如果不存在报错
        if (device == null) {
            throw new BusinessException("无法找到设备");
        }
        Integer bindingScreeningOrgId = device.getBindingScreeningOrgId();
        String deviceSn = device.getDeviceSn();
        Integer orgId = getOrganizationId(device);
        if (Objects.isNull(orgId)) {
            throw new BusinessException("无法找到筛查机构或该筛查机构已过期");
        }
        //设置检查结果
        this.setCheckResult(deviceUploadDto.getData());
        //判断id是否是特殊设备扫描出来的(如vs666)
        List<DeviceScreenDataDTO> deviceScreenDataDTOList = deviceUploadDto.getData();

        if (CollectionUtils.isEmpty(deviceScreenDataDTOList)) {
            throw new BusinessException("无法找到筛查数据");
        }
        //更新或者插入DeviceScreenData的数据
        List<DeviceScreenDataDTO> existDeviceScreeningDataDTOs = deviceSourceDataService.listBatchWithMutiConditions(bindingScreeningOrgId, deviceSn, deviceScreenDataDTOList);
        deviceSourceDataService.updateOrAddDeviceSourceDataList(device, getUpdateAndAddData(deviceScreenDataDTOList, bindingScreeningOrgId, deviceSn, existDeviceScreeningDataDTOs));
        //更新或者插入deviceSource的数据
        List<DeviceScreenDataDTO> existDeviceSourceDataDTOs = deviceScreeningDataService.listBatchWithMutiConditions(bindingScreeningOrgId, deviceSn, deviceScreenDataDTOList);
        deviceScreeningDataService.updateOrAddDeviceScreeningDataList(device, getUpdateAndAddData(deviceScreenDataDTOList, bindingScreeningOrgId, deviceSn, existDeviceSourceDataDTOs));
        //更新或者插入学生筛查数据的数据
        updateOrSaveDeviceScreeningDatas2ScreeningResult(deviceScreenDataDTOList);
    }

    /**
     * 获取更新和插入的数据
     *
     * @param deviceScreenDataDTOList
     * @param bindingScreeningOrgId
     * @param deviceSn
     * @param existDeviceScreeningDataDTO
     * @return
     */
    private Map<Boolean, List<DeviceScreenDataDTO>> getUpdateAndAddData(List<DeviceScreenDataDTO> deviceScreenDataDTOList, Integer bindingScreeningOrgId, String deviceSn, List<DeviceScreenDataDTO> existDeviceScreeningDataDTO) {
        // 将存在的数据的唯一索引组成String Set
        Set<String> existSet = existDeviceScreeningDataDTO.stream().map(this::getUnikeyString).collect(Collectors.toSet());
        // true 为需要更新的数据  false为需要插入的数据
        return deviceScreenDataDTOList.stream().collect(Collectors.partitioningBy(deviceScreenDataDTO -> {
            deviceScreenDataDTO.setScreeningOrgId(bindingScreeningOrgId);
            deviceScreenDataDTO.setDeviceSn(deviceSn);
            String unikeyString = getUnikeyString(deviceScreenDataDTO);
            return existSet.contains(unikeyString);
        }));
    }

    /**
     * 设置检查结果
     *
     * @param deviceScreenDataDTOList
     */
    private void setCheckResult(List<DeviceScreenDataDTO> deviceScreenDataDTOList) {
        deviceScreenDataDTOList.forEach(deviceScreenDataDTO -> {
            String checkResult = CheckResultUtil.getCheckResult(deviceScreenDataDTO);
            deviceScreenDataDTO.setCheckResult(checkResult);
        });
    }

    /**
     * 获取唯一keyString
     *
     * @return 唯一keyString
     */
    private String getUnikeyString(DeviceScreenDataDTO deviceScreenDataDTO) {
        if (ObjectsUtil.hasNull(deviceScreenDataDTO.getScreeningOrgId(), deviceScreenDataDTO.getDeviceSn(), deviceScreenDataDTO.getPatientId(), deviceScreenDataDTO.getCheckTime())) {
            throw new BusinessException(String.format("获取唯一key失败,存在参数为空,screeningOrgId = %s , deviceSn = %s, patientId = %s, checkTime = %s", deviceScreenDataDTO.getScreeningOrgId(), deviceScreenDataDTO.getDeviceSn(), deviceScreenDataDTO.getPatientId(), deviceScreenDataDTO.getCheckTime()));
        }
        return deviceScreenDataDTO.getScreeningOrgId() + DELIMITER_CHAR + deviceScreenDataDTO.getDeviceSn() + DELIMITER_CHAR + deviceScreenDataDTO.getPatientId() + DELIMITER_CHAR + deviceScreenDataDTO.getCheckTime();
    }

    /**
     * 体脂秤数据上传
     *
     * @param requestDTO 入参
     * @return ScalesResponseDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public ScalesResponseDTO bodyFatScaleUpload(ScalesRequestDTO requestDTO) {
        if (!StringUtils.equals("webResults", requestDTO.getAction())) {
            return new ScalesResponseDTO("0", "事件类型异常，请确认");
        }
        Device device = deviceService.getDeviceByDeviceSn(requestDTO.getDeviceID());
        if (Objects.isNull(device)) {
            return new ScalesResponseDTO("0", "无法找到设备:" + requestDTO.getDeviceID());
        }
        Integer orgId = getOrganizationId(device);
        if (Objects.isNull(orgId)) {
            return new ScalesResponseDTO("0", "无法找到筛查机构或该筛查机构已过期");
        }
        List<ScalesData> datas = requestDTO.getDatas();
        if (CollectionUtils.isEmpty(datas)) {
            return new ScalesResponseDTO("0", "数据为空");
        }
        for (ScalesData data : datas) {
            String uid = data.getUid();
            if (StringUtils.isBlank(uid)) {
                return new ScalesResponseDTO("0", "uid数据为空");
            }
            Integer parsePlanStudentId = ParsePlanStudentUtils.parsePlanStudentId(uid);
            ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getById(parsePlanStudentId);
            if (Objects.isNull(planStudent)) {
                return new ScalesResponseDTO("0", "uid找不到学生数据");
            }
            BmiData bmiData = data.getBmi();
            if (Objects.isNull(bmiData)) {
                return new ScalesResponseDTO("0", "身体质量指数值为空");
            }
            // 保存原始数据
            saveDeviceData(device, JSON.toJSONString(data), parsePlanStudentId, orgId, System.currentTimeMillis());
            HeightAndWeightDataDTO heightAndWeightDataDTO = new HeightAndWeightDataDTO();
            heightAndWeightDataDTO.setHeight(new BigDecimal(bmiData.getHeight()));
            heightAndWeightDataDTO.setWeight(new BigDecimal(bmiData.getWeight()));
            heightAndWeightDataDTO.setBmi(new BigDecimal(bmiData.getBmi()));
            heightAndWeightDataDTO.setDeptId(planStudent.getScreeningOrgId());
            heightAndWeightDataDTO.setCreateUserId(-1);
            heightAndWeightDataDTO.setPlanStudentId(String.valueOf(planStudent.getId()));
            heightAndWeightDataDTO.setSchoolId(String.valueOf(planStudent.getSchoolId()));
            visionScreeningBizService.saveOrUpdateStudentScreenData(heightAndWeightDataDTO);
        }
        return new ScalesResponseDTO("1", "success");
    }

    /**
     * 获取学生信息
     *
     * @param request 请求入参
     * @return UserInfoResponseDTO
     */
    public UserInfoResponseDTO getUserInfo(UserInfoRequestDTO request) {
        String deviceSn = request.getDeviceSn();
        String uid = Base64.decodeStr(request.getUid());

        Device device = getDevice(deviceSn);
        Integer orgId = getOrganizationIdThrowException(device);
        Integer planStudentId = ParsePlanStudentUtils.parsePlanStudentId(uid);
        ScreeningPlanSchoolStudent planStudent = getScreeningPlanSchoolStudent(orgId, planStudentId);

        SchoolGrade schoolGrade = schoolGradeService.getById(planStudent.getGradeId());
        SchoolClass schoolClass = schoolClassService.getById(planStudent.getClassId());
        return new UserInfoResponseDTO(planStudent.getStudentName(),
                GenderEnum.getName(planStudent.getGender()),
                Objects.nonNull(schoolGrade) ? schoolGrade.getName() : StringUtils.EMPTY,
                Objects.nonNull(schoolClass) ? schoolClass.getName() : StringUtils.EMPTY);

    }

    /**
     * 获取设备
     *
     * @param deviceSn 设备唯一标识
     * @return 设备
     */
    public Device getDevice(String deviceSn) {
        Device device = deviceService.getDeviceByDeviceSn(deviceSn);
        if (Objects.isNull(device)) {
            throw new BusinessException("无法找到设备:" + deviceSn, ResultCode.DATA_UPLOAD_DEVICE_ERROR.getCode());
        }
        return device;
    }

    /**
     * 通过设备获取筛查机构
     *
     * @param device 设备
     *
     * @return orgId
     */
    public Integer getOrganizationIdThrowException(Device device) {

        Integer orgId = getOrganizationId(device);
        if (Objects.isNull(orgId)) {
            throw new BusinessException("设备数据异常！", ResultCode.DATA_UPLOAD_SCREENING_ORG_ERROR.getCode());
        }
        return orgId;
    }

    /**
     * 通过设备获取筛查机构
     *
     * @param device 设备
     *
     * @return orgId
     */
    private Integer getOrganizationId(Device device) {
        if (Objects.equals(OrgTypeEnum.SCREENING.getCode(), device.getOrgType())) {
            ScreeningOrganization screeningOrganization = screeningOrganizationService.getById(device.getBindingScreeningOrgId());
            checkOrgStatus(screeningOrganization);
            return screeningOrganization.getId();
        } else if (Objects.equals(OrgTypeEnum.HOSPITAL.getCode(), device.getOrgType())) {
            Hospital hospital = hospitalService.getById(device.getBindingScreeningOrgId());
            checkOrgStatus(hospital);
            return hospital.getId();
        } else if (Objects.equals(OrgTypeEnum.SCHOOL.getCode(), device.getOrgType())) {
            School school = schoolService.getById(device.getBindingScreeningOrgId());
            checkOrgStatus(school);
            return school.getId();
        }
        return null;
    }

    /**
     * 校验机构状态
     *
     * @param t   机构
     * @param <T> 合作
     */
    private <T extends Cooperation> void checkOrgStatus(T t) {
        if (Objects.isNull(t) || CommonConst.STATUS_IS_DELETED.equals(t.getStatus())) {
            throw new BusinessException("无法找到筛查机构或该筛查机构已过期！", ResultCode.DATA_UPLOAD_SCREENING_ORG_ERROR.getCode());
        }
    }

    /**
     * 获取筛查学生
     *
     * @param orgId 筛查机构Id
     * @param planStudentId         筛查学生Id
     * @return 筛查学生
     */
    public ScreeningPlanSchoolStudent getScreeningPlanSchoolStudent(Integer orgId, Integer planStudentId) {
        ScreeningPlanSchoolStudent planStudent = screeningPlanSchoolStudentService.getById(planStudentId);
        if (Objects.isNull(planStudent)) {
            throw new BusinessException("不能通过该uid找到学生信息，请确认！", ResultCode.DATA_UPLOAD_PLAN_STUDENT_ERROR.getCode());
        }
        if (!planStudent.getScreeningOrgId().equals(orgId)) {
            throw new BusinessException("筛查学生与筛查机构不匹配！", ResultCode.DATA_UPLOAD_PLAN_STUDENT_MATCH_ERROR.getCode());
        }
        return planStudent;
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
    public void saveDeviceData(Device device, String dataStr, Integer planStudentId, Integer orgId, Long screeningTime) {
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

}