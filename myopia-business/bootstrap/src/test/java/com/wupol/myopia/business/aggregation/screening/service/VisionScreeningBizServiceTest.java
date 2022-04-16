package com.wupol.myopia.business.aggregation.screening.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wupol.myopia.business.api.screening.app.domain.dto.*;
import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import com.wupol.myopia.business.core.screening.flow.domain.dos.BloodPressureDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ComputerOptometryDTO;
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
        VisionDataDTO visionDataDTO = this.getObj("/json/visionData.json", VisionDataDTO.class);
        if (visionDataDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(visionDataDTO);
        } else {
            System.out.println("视力筛查数据异常");
        }
        // 屈光
        ComputerOptometryDTO computerOptometryTO = this.getObj("/json/visionData.json", ComputerOptometryDTO.class);
        if (computerOptometryTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(computerOptometryTO);
        } else {
            System.out.println("屈光数据异常");
        }
        // 其他眼病
        OtherEyeDiseasesDTO otherEyeDiseasesDTO = this.getObj("/json/visionData.json", OtherEyeDiseasesDTO.class);
        visionScreeningBizService.saveOrUpdateStudentScreenData(otherEyeDiseasesDTO);

        // 龋齿检查
        SaprodontiaDTO saprodontiaDTO = this.getObj("/json/visionData.json", SaprodontiaDTO.class);
        if (computerOptometryTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(saprodontiaDTO);
        } else {
            System.out.println("龋齿检查异常");
        }
        // 身高检查
        HeightAndWeightDataDTO heightAndWeightDataDTO = this.getObj("/json/visionData.json", HeightAndWeightDataDTO.class);
        if (heightAndWeightDataDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(heightAndWeightDataDTO);
        } else {
            System.out.println("身高检查异常");
        }
        // 脊柱检查
        SpineDTO spineDTO = this.getObj("/json/visionData.json", SpineDTO.class);
        if (spineDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(spineDTO);
        } else {
            System.out.println("脊柱检查异常");
        }
        // 血压检查
        BloodPressureDTO bloodPressureDTO = this.getObj("/json/visionData.json", BloodPressureDTO.class);
        if (bloodPressureDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(bloodPressureDTO);
        } else {
            System.out.println("血压检查异常");
        }
        // 疾病史
        DiseasesHistoryDTO diseasesHistoryDTO = this.getObj("/json/visionData.json", DiseasesHistoryDTO.class);
        if (diseasesHistoryDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(diseasesHistoryDTO);
        } else {
            System.out.println("疾病史异常");
        }
        // 个人隐私
        PrivacyDTO privacyDTO = this.getObj("/json/visionData.json", PrivacyDTO.class);
        if (privacyDTO.isValid()) {
            visionScreeningBizService.saveOrUpdateStudentScreenData(privacyDTO);
        } else {
            System.out.println("个人隐私异常");
        }
    }



    /**
     * @param classPathStr
     * @param clazz
     * @return
     * @throws IOException
     */
    private <T> T getObj(String classPathStr, Class<T> clazz) throws IOException {
        File file = ResourceUtils.getFile("classpath:" + classPathStr);
        String jsonStr = FileUtils.readFileToString(file);
        // 转换
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        T obj = objectMapper.readValue(jsonStr, clazz);
        return obj;
    }
}