package com.wupol.myopia.business.screening.processor;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.wupol.myopia.business.management.domain.dto.StudentScreeningInfoWithResultDTO;
import com.wupol.myopia.business.screening.domain.vo.RescreeningResultVO;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @Description
 * @Date 2021/2/3 14:57
 * @Author by Jacob
 */
public class RescreeningResultProcessorTest {


    /**
     * 复查人数为0，复查未开始
     *
     * @throws IOException
     */
    @Test
    public void testGetQualifiedAndReviewCount_SuccessWhenNotStarted() throws IOException {
        RescreeningResultVO rescreeningResultVO = new RescreeningResultVO();
        List<StudentScreeningInfoWithResultDTO> studentScreeningInfoWithResultDTOS = this.loadJsonIntoInfo("TEST_RESCREENING_NOT_STARTED_DATA.json");
        rescreeningResultVO.checkIfNeedToStatistic(studentScreeningInfoWithResultDTOS);
        Assert.assertTrue(rescreeningResultVO.getQualified() == RescreeningResultVO.RESCREENING_NOT_STARTED);
    }

    /**
     * 筛查人数为0
     *
     * @throws IOException
     */
    @Test
    public void testGetQualifiedAndReviewCount_SuccessWhenPass() throws IOException {
        RescreeningResultVO rescreeningResultVO = new RescreeningResultVO();
        List<StudentScreeningInfoWithResultDTO> studentScreeningInfoWithResultDTOS = this.loadJsonIntoInfo("TEST_RESCREENING_NUMBERS_ZERO_DATA.json");
        rescreeningResultVO.checkIfNeedToStatistic(studentScreeningInfoWithResultDTOS);
        Assert.assertTrue(rescreeningResultVO.getQualified() == RescreeningResultVO.SCREENING_NUMBER_ZERO);
    }

    /**
     * 测试有效数据： 未戴镜情况下的，没有裸眼数据
     *
     * @throws IOException
     */
    @Test
    public void testFilterIncompletedData_SuccessWithOutNakedVisionData() throws IOException {
        RescreeningResultVO rescreeningResultVO = new RescreeningResultVO();
        List<StudentScreeningInfoWithResultDTO> studentScreeningInfoWithResultDTOs = this.loadJsonIntoInfo("TEST_RESCREENING_VALIDATION_DATA.json");
        studentScreeningInfoWithResultDTOs = rescreeningResultVO.filterIncompletedData(studentScreeningInfoWithResultDTOs);
        Assert.assertTrue(studentScreeningInfoWithResultDTOs.size() == 1);
    }

    /**
     * 测试有效数据： 戴镜情况下，没有球镜数据
     *
     * @throws IOException
     */
    @Test
    public void testFilterIncompletedData_SuccessWithoutSPH() throws IOException {
        RescreeningResultVO rescreeningResultVO = new RescreeningResultVO();
        List<StudentScreeningInfoWithResultDTO> studentScreeningInfoWithResultDTOs = this.loadJsonIntoInfo("TEST_RESCREENING_VALIDATION_WITHOUT_SPH_DATA.json");
        studentScreeningInfoWithResultDTOs = rescreeningResultVO.filterIncompletedData(studentScreeningInfoWithResultDTOs);
        Assert.assertTrue(studentScreeningInfoWithResultDTOs.size() == 1);
    }

    /**
     * 测试设置数据内容
     *
     * @throws IOException
     */
    @Test
    public void testGetRescreeningResultContent_Success() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<StudentScreeningInfoWithResultDTO> studentScreeningInfoWithResultDTOs = this.loadJsonIntoInfo("TEST_RESCREENING_RESULTVO_DATA.json");
        RescreeningResultVO rescreeningResultVO = new RescreeningResultVO();
        Method setRescreeningResultContentListMethod = RescreeningResultVO.class.getDeclaredMethod("getRescreeningResultContent", List.class);
        setRescreeningResultContentListMethod.setAccessible(true);
        Object RescreeningResultContent = setRescreeningResultContentListMethod.invoke(rescreeningResultVO, studentScreeningInfoWithResultDTOs);
        JSON.toJSONString(RescreeningResultContent);
        Assert.assertTrue(RescreeningResultContent != null);
    }

    /**
     * 测试有效数据： 戴镜情况下，没有球镜数据
     *
     * @throws IOException
     */
    @Test
    public void testGetRescreeningResultVO_Success() throws IOException {
        List<StudentScreeningInfoWithResultDTO> studentScreeningInfoWithResultDTOs = this.loadJsonIntoInfo("TEST_RESCREENING_RESULTVO_DATA.json");
        RescreeningResultVO rescreeningResultVO = new RescreeningResultVO();
        rescreeningResultVO.stasticRescreeningData(studentScreeningInfoWithResultDTOs);
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(rescreeningResultVO));
    }


    private List<StudentScreeningInfoWithResultDTO> loadJsonIntoInfo(String resourceLocation) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(resourceLocation);
        File file = classPathResource.getFile();
        String json = "";
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            json = IOUtils.toString(fileInputStream);
        }
        //进行转换
        ObjectMapper objectMapper = new ObjectMapper();
        CollectionType collectionType = TypeFactory.defaultInstance().constructCollectionType(List.class, StudentScreeningInfoWithResultDTO.class);
        List<StudentScreeningInfoWithResultDTO> studentScreeningInfoWithResultDTOS = objectMapper.readValue(json, collectionType);
        return studentScreeningInfoWithResultDTOS;
    }
}