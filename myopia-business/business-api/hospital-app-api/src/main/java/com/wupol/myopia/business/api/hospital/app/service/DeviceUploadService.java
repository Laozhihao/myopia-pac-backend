package com.wupol.myopia.business.api.hospital.app.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vistel.Interface.util.ReturnInformation;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.hospital.app.domain.dto.DeviceRequestDTO;
import com.wupol.myopia.business.api.hospital.app.domain.dto.DicomDTO;
import com.wupol.myopia.business.api.hospital.app.domain.dto.DicomJsonDTO;
import com.wupol.myopia.business.api.hospital.app.domain.dto.FundusImageDTO;
import com.wupol.myopia.business.common.utils.config.UploadConfig;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.common.utils.util.UploadUtil;
import com.wupol.myopia.business.core.common.domain.model.ResourceFile;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.device.constant.OrgTypeEnum;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.service.DeviceService;
import com.wupol.myopia.business.core.hospital.domain.model.ImageDetail;
import com.wupol.myopia.business.core.hospital.domain.model.ImageOriginal;
import com.wupol.myopia.business.core.hospital.service.ImageDetailService;
import com.wupol.myopia.business.core.hospital.service.ImageOriginalService;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 设备数据上传
 *
 * @author Simple4H
 */
@Service
@Slf4j
public class DeviceUploadService {

    @Resource
    private UploadConfig uploadConfig;

    @Resource
    private ResourceFileService resourceFileService;

    @Resource
    private S3Utils s3Utils;

    @Resource
    private DeviceService deviceService;

    @Resource
    private StudentService studentService;

    @Resource
    private ImageOriginalService imageOriginalService;

    @Resource
    private ImageDetailService imageDetailService;

    @Resource
    private RedisUtil redisUtil;


    /**
     * 眼底检查数据上传
     *
     * @param requestDTO 请求DTO
     *
     * @return ReturnInformation
     */
    @Transactional(rollbackFor = Exception.class)
    public String fundusUpload(DeviceRequestDTO requestDTO) {
        String path = StringUtils.EMPTY;
        Integer patientId = null;
        try {
            DicomDTO dicomDTO = preCheckAndGetDicomDTO(requestDTO);
            patientId = dicomDTO.getPatientId();
            // 设置进行中,有效期30分钟
            redisUtil.set(String.format(RedisConstant.HOSPITAL_DEVICE_UPLOAD_FUNDUS_PATIENT, patientId), "ing", 1800L);
            TwoTuple<String, String> upload = UploadUtil.upload(requestDTO.getPic(), uploadConfig.getSavePath());
            path = upload.getSecond();
            ZipUtil.unzip(path);

            String s = StrUtil.removeSuffix(path, ".zip");
            File file = new File(s);
            File[] files = file.listFiles();
            if (Objects.isNull(files)) {
                return null;
            }

            // 获取后缀威jpg的图像文件
            List<File> fileList = new ArrayList<>();
            for (File f : files) {
                if (f.getName().endsWith(".jpg") || f.getName().endsWith(".pdf")) {
                    fileList.add(f);
                }
            }

            if (CollectionUtils.isEmpty(fileList)) {
                throw new BusinessException("图像文件为空");
            }

            // 上传原始文件
            Integer originalImageId = saveOriginalImage(dicomDTO, resourceFileService.uploadFileAndSave(requestDTO.getPic()).getId());

            // 读取DICOM,JSON文件信息
            FileReader fileReader = new FileReader(StrUtil.removeSuffix(path, ".zip") + "/" + dicomDTO.getBase());
            String dicomJson = fileReader.readString();
            if (StringUtils.isBlank(dicomJson)) {
                throw new BusinessException("JSON数据为空");
            }

            List<ImageDetail> imageDetailList = new ArrayList<>();

            // 上传图片信息
            for (File imageFile : fileList) {
                ResourceFile resourceFile = s3Utils.uploadS3AndGetResourceFileAndDeleteTempFile(imageFile, imageFile.getName());
                imageDetailList.add(getImageDetail(originalImageId, resourceFile.getId(), dicomDTO, dicomJson));
            }
            imageDetailService.saveBatch(imageDetailList);
            return ReturnInformation.returnSuccess();
        } catch (Exception e) {
            log.error("眼底影像上传异常,请求参数:{}", JSON.toJSONString(requestDTO), e);
            return ReturnInformation.return500001Exception(e);
        } finally {
            // 删除临时文件
            deletedFile(path);
            // 删除redis
            redisUtil.del(String.format(RedisConstant.HOSPITAL_DEVICE_UPLOAD_FUNDUS_PATIENT, patientId));
        }
    }

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
     * 检查并且获取Dicom数据
     *
     * @param requestDTO 请求DTO
     *
     * @return DicomDTO
     */
    private DicomDTO preCheckAndGetDicomDTO(DeviceRequestDTO requestDTO) {
        List<DicomDTO> dicomDTOS = JSON.parseArray(requestDTO.getJson(), DicomDTO.class);
        if (CollectionUtils.isEmpty(dicomDTOS)) {
            log.error("获取影像数据异常！参数:{}", JSON.toJSONString(requestDTO));
            throw new BusinessException("获取影像数据异常！");
        }
        DicomDTO dicomDTO = dicomDTOS.get(0);
        Integer deviceId = dicomDTO.getDeviceId();

        if (Objects.isNull(dicomDTO.getMd5())) {
            log.error("获取MD5异常！参数:{}", JSON.toJSONString(requestDTO));
            throw new BusinessException("获取MD5异常！");
        }

        Device device = deviceService.getById(deviceId);
        if (Objects.isNull(deviceId) || Objects.isNull(device)) {
            log.error("获取设备异常！参数:{}", JSON.toJSONString(requestDTO));
            throw new BusinessException("获取设备异常！");
        }
        // 检查mac地址是否相同
        if (!StringUtils.equals(device.getMacAddress(), dicomDTO.getMacAddress())) {
            log.error("mac地址异常！参数:{}", JSON.toJSONString(requestDTO));
            throw new BusinessException("mac地址异常！");
        }

        // 检查是否绑定
        if (Objects.isNull(dicomDTO.getHospitalId())
                || !Objects.equals(device.getBindingScreeningOrgId(), dicomDTO.getHospitalId())
                || !Objects.equals(device.getOrgType(), OrgTypeEnum.HOSPITAL.getCode())) {
            log.error("设备未绑定！参数:{}", JSON.toJSONString(requestDTO));
            throw new BusinessException("设备未绑定！");
        }

        // 检查患者是否存在
        Student student = studentService.getById(dicomDTO.getPatientId());
        if (Objects.isNull(dicomDTO.getPatientId()) || Objects.isNull(student)) {
            log.error("患者信息异常！参数:{}", JSON.toJSONString(requestDTO));
            throw new BusinessException("患者信息异常！");
        }

        // TODO: 是否合作

        return dicomDTO;
    }

    /**
     * 删除临时文件
     *
     * @param path 路径
     */
    private void deletedFile(String path) {
        String s = StrUtil.removeSuffix(path, ".zip");
        // 删除文件
        FileUtil.del(path);
        // 删除文件夹
        FileUtil.del(s);
    }

    /**
     * 保存原始文件
     *
     * @param dicomDTO dicom数据
     * @param fileId   文件Id
     */
    private Integer saveOriginalImage(DicomDTO dicomDTO, Integer fileId) {
        if (Objects.nonNull(imageOriginalService.getByMd5(dicomDTO.getMd5()))) {
            throw new BusinessException("DICOM数据重复！");
        }
        ImageOriginal imageOriginal = new ImageOriginal();
        imageOriginal.setFileId(fileId);
        imageOriginal.setPatientId(dicomDTO.getPatientId());
        imageOriginal.setHospitalId(dicomDTO.getHospitalId());
        imageOriginal.setDeviceId(dicomDTO.getDeviceId());
        imageOriginal.setMd5(dicomDTO.getMd5());
        imageOriginalService.save(imageOriginal);
        return imageOriginal.getId();
    }

    /**
     * 获取文件详情
     *
     * @param originalImageId 原始压缩包Id
     * @param fileId          文件Id
     * @param dicomDTO        dicomDTO
     * @param dicomJson       dicomJson数据
     *
     * @return ImageDetail
     */
    private ImageDetail getImageDetail(Integer originalImageId, Integer fileId,
                                       DicomDTO dicomDTO, String dicomJson) throws JsonProcessingException {
        ImageDetail imageDetail = new ImageDetail();
        imageDetail.setImageOriginalId(originalImageId);
        imageDetail.setFileId(fileId);
        imageDetail.setPatientId(dicomDTO.getPatientId());
        imageDetail.setHospitalId(dicomDTO.getHospitalId());
        imageDetail.setDcmJson(dicomJson);
        DicomJsonDTO dicomJsonDTO = new ObjectMapper().readValue(dicomJson, DicomJsonDTO.class);
        imageDetail.setBatchNo(dicomJsonDTO.getBatch());
        return imageDetail;
    }
}
