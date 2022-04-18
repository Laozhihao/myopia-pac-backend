package com.wupol.myopia.business.aggregation.screening.service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wupol.myopia.business.api.screening.app.domain.dto.*;
import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import com.wupol.myopia.business.core.screening.flow.domain.dos.BloodPressureDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.util.ResourceHelper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @Author xz
 * @Date 2022/4/16 11:55
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
class VisionScreeningBizServiceTest {
    @Autowired
    private VisionScreeningBizService visionScreeningBizService;


    /**
     * 保存数据
     *
     * @throws IOException IOException
     */
    @Test
    void testSaveOrUpdateStudentScreenData() throws IOException {
        // 视力筛查
        VisionDataDTO visionDataDTO = JSON.parseObject(ResourceHelper.getResourceAsString(getClass(), "/json/visionData.json"), VisionDataDTO.class);
        if (visionDataDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(visionDataDTO);
        } else {
            System.out.println("视力筛查数据异常");
        }
        // 屈光
        ComputerOptometryDTO computerOptometryTO = JSON.parseObject(ResourceHelper.getResourceAsString(getClass(), "/json/computerOptometry.json"), ComputerOptometryDTO.class);
        if (computerOptometryTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(computerOptometryTO);
        } else {
            System.out.println("屈光数据异常");
        }
        // 其他眼病
        OtherEyeDiseasesDTO otherEyeDiseasesDTO = JSON.parseObject(ResourceHelper.getResourceAsString(getClass(), "/json/otherEyeDiseases.json"), OtherEyeDiseasesDTO.class);
        visionScreeningBizService.saveOrUpdateStudentScreenData(otherEyeDiseasesDTO);

        // 龋齿检查
        SaprodontiaDTO saprodontiaDTO = JSON.parseObject(ResourceHelper.getResourceAsString(getClass(), "/json/otherEyeDiseases.json"), SaprodontiaDTO.class);
        if (computerOptometryTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(saprodontiaDTO);
        } else {
            System.out.println("龋齿检查异常");
        }
        // 身高检查
        HeightAndWeightDataDTO heightAndWeightDataDTO = JSON.parseObject(ResourceHelper.getResourceAsString(getClass(), "/json/heightAndWeight.json"), HeightAndWeightDataDTO.class);
        if (heightAndWeightDataDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(heightAndWeightDataDTO);
        } else {
            System.out.println("身高检查异常");
        }
        // 脊柱检查
        SpineDTO spineDTO = JSON.parseObject(ResourceHelper.getResourceAsString(getClass(), "/json/spine.json"), SpineDTO.class);
        if (spineDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(spineDTO);
        } else {
            System.out.println("脊柱检查异常");
        }
        // 血压检查
        BloodPressureDTO bloodPressureDTO = JSON.parseObject(ResourceHelper.getResourceAsString(getClass(), "/json/bloodPressure.json"), BloodPressureDTO.class);
        if (bloodPressureDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(bloodPressureDTO);
        } else {
            System.out.println("血压检查异常");
        }
        // 疾病史
        DiseasesHistoryDTO diseasesHistoryDTO = JSON.parseObject(ResourceHelper.getResourceAsString(getClass(), "/json/bloodPressure.json"), DiseasesHistoryDTO.class);
        if (diseasesHistoryDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(diseasesHistoryDTO);
        } else {
            System.out.println("疾病史异常");
        }
        // 个人隐私
        PrivacyDTO privacyDTO = JSON.parseObject(ResourceHelper.getResourceAsString(getClass(), "/json/bloodPressure.json"), PrivacyDTO.class);
        if (privacyDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(privacyDTO);
        } else {
            System.out.println("个人隐私异常");
        }
    }
}