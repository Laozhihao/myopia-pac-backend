package com.wupol.myopia.business.api.hospital.app.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.alibaba.fastjson.JSON;
import com.vistel.Interface.util.ReturnInformation;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.hospital.app.domain.dto.DeviceRequestDTO;
import com.wupol.myopia.business.api.hospital.app.domain.dto.DicomDTO;
import com.wupol.myopia.business.common.utils.config.UploadConfig;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.common.utils.util.UploadUtil;
import com.wupol.myopia.business.core.common.domain.model.ResourceFile;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.device.constant.OrgTypeEnum;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.service.DeviceService;
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


    /**
     * 眼底检查数据上传
     *
     * @param requestDTO 请求DTO
     *
     * @return ReturnInformation
     */
    @Transactional(rollbackFor = Exception.class)
    public String fundusUpload(DeviceRequestDTO requestDTO) {
        String path = null;
        try {
            DicomDTO dicomDTO = preCheckAndGetDicomDTO(requestDTO);
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
            ResourceFile sourceFileZip = resourceFileService.uploadFileAndSave(requestDTO.getPic());

            // 读取DICOM,JSON文件信息
            FileReader fileReader = new FileReader(StrUtil.removeSuffix(path, ".zip") + "/" + dicomDTO.getBase());
            String dicomJson = fileReader.readString();

            // 上传图片信息
            for (File imageFile : fileList) {
                ResourceFile resourceFile = s3Utils.uploadS3AndGetResourceFileAndDeleteTempFile(imageFile, imageFile.getName());
            }


            return ReturnInformation.returnSuccess();
        } catch (Exception e) {
            log.error("眼底影像上传异常,请求参数:{}", JSON.toJSONString(requestDTO), e);
            return ReturnInformation.return500001Exception(e);
        } finally {
            // 删除临时文件
            deletedFile(path);
        }
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
}
