package com.wupol.myopia.business.api.screening.app.domain.vo;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wupol.myopia.business.common.utils.constant.ScreeningConstant;
import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentScreeningInfoWithResultDTO;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description
 * @Date 2021/1/29 14:34
 * @Author by Jacob
 */
@Getter
public class RescreeningResultVO {
    public static final Integer RESCREENING_NOT_STARTED = -1;
    public static final Integer SCREENING_NUMBER_ZERO = 0;
    public static final Integer RESCREENING_PASS = 1;
    public static final Integer RESCREENING_NOT_PASS = 2;
    @JsonIgnore
    private final BigDecimal AVE_RANGE_VALUE = BigDecimal.valueOf(0.5);
    @JsonIgnore
    private final BigDecimal OTHERS_RANGE_VALUE = BigDecimal.valueOf(1.0);
    /**
     * -1 代表
     * 1 代表
     * 0 代表
     * 2 代表
     */
    private Integer qualified;
    /**
     * 年级名
     */
    private String gradeName;

    /**
     * 复查数量
     */
    private Integer reviewsCount;
    /**
     * 学校名
     */
    private String schoolName;
    /**
     * 班级名
     */
    private String clazzName;
    /**
     * 详细内容
     */
    private List<RescreeningResultContent> content;

    /**
     *  以下字段是新加的 todo
     *
     * @param passNum 合格项目数量
     */
    private int passNum;
    /**
     * 以下字段是新加的 todo
     *
     * @param errorRatio 误差率
     */
    private int errorRatio;
    /**
     *
     *
     * @param errorNum 错误项次数（不合格项） 根据app 来的
     */
    private int eyeResult;
    /**
     * 以下字段是新加的
     *
     * @param totalScreeningNum 检查筛查结果项
     */
    private int eyesCount;

    /**
     * 设置其他星系
     *
     * @param studentScreeningInfoWithResultDTO
     */
    public void setOtherInfo(StudentScreeningInfoWithResultDTO studentScreeningInfoWithResultDTO) {
        this.schoolName = studentScreeningInfoWithResultDTO.getSchoolName();
        this.clazzName = studentScreeningInfoWithResultDTO.getClazzName();
        this.gradeName = studentScreeningInfoWithResultDTO.getGradeName();
    }

    /**
     * 获取重新筛查结果
     *
     * @param studentScreeningInfoWithResultDTOs 这是分类后的结果
     */
    public static RescreeningResultVO getRescreeningResult(List<StudentScreeningInfoWithResultDTO> studentScreeningInfoWithResultDTOs) {
        if (studentScreeningInfoWithResultDTOs == null) {
            throw new ManagementUncheckedException("studentScreeningInfoWithResultDTOs参数异常，不能为空");
        }
        RescreeningResultVO rescreeningResultVO = new RescreeningResultVO();
        //先看基本的统计情况
        rescreeningResultVO.stasticRescreeningData(studentScreeningInfoWithResultDTOs);
        return rescreeningResultVO;
    }

    /**
     * 统计筛查信息
     *
     * @param studentScreeningInfoWithResultDTOS 某计划里，初筛和复筛的数据
     */
    public void stasticRescreeningData(List<StudentScreeningInfoWithResultDTO> studentScreeningInfoWithResultDTOS) {
        //先过滤一遍
        studentScreeningInfoWithResultDTOS = this.filterIncompletedData(studentScreeningInfoWithResultDTOS);
        Boolean ifNeedToStatistic = this.checkIfNeedToStatistic(studentScreeningInfoWithResultDTOS);
        if (!ifNeedToStatistic) {
            return;
        }
        //按学生分组成复筛和初筛数据
        this.setContent(studentScreeningInfoWithResultDTOS);
        //获取到每个学生的详细数据后，进行统计
        this.setStatisticResult(content);
        //设置其他信息
        this.setOtherInfo(studentScreeningInfoWithResultDTOS.stream().findFirst().get());
    }

    /**
     * 设置主要内容
     * @param studentScreeningInfoWithResultDTOS
     */
    private void setContent(List<StudentScreeningInfoWithResultDTO> studentScreeningInfoWithResultDTOS) {
        Map<Integer, List<StudentScreeningInfoWithResultDTO>> studentIdDataMap = studentScreeningInfoWithResultDTOS.stream().collect(Collectors.groupingBy(StudentScreeningInfoWithResultDTO::getStudentId));
        Set<Integer> studentIds = studentIdDataMap.keySet();
        //组合数据
        content = studentIds.stream().map(studentId -> {
            List<StudentScreeningInfoWithResultDTO> studentScreeningDataList = studentIdDataMap.get(studentId);
            return this.getRescreeningResultContent(studentScreeningDataList);
        }).collect(Collectors.toList());
    }


    /**
     * 查看是否合格
     *
     * @param studentScreeningInfoWithResultDTOs
     * @return
     */
    public Boolean checkIfNeedToStatistic(List<StudentScreeningInfoWithResultDTO> studentScreeningInfoWithResultDTOs) {
        //筛查人数是否为0：一条筛查记录都没有
        boolean isNotScreeningNumberZero = studentScreeningInfoWithResultDTOs.stream().anyMatch(studentScreeningInfoWithResultDTO -> studentScreeningInfoWithResultDTO.getUpdateTime() != null);
        if (!isNotScreeningNumberZero) {
            //"筛查人数为0"
            qualified = SCREENING_NUMBER_ZERO;
            return false;
        }
        //进行数据统计
        Long count = studentScreeningInfoWithResultDTOs.stream().filter(studentScreeningInfoWithResultDTO -> ScreeningConstant.SCREENING_RESCREENING == studentScreeningInfoWithResultDTO.getIsDoubleScreen()).count();
        if (count == 0) {
            //复筛未否开始:起码有一个是有复查的
            qualified = RESCREENING_NOT_STARTED;
            return false;
        }
        return true;
    }

    /**
     * 获取通过率,计算失败率错误项目等等；
     *
     * @param rescreeningResultContentList
     */
    private void setStatisticResult(List<RescreeningResultContent> rescreeningResultContentList) {
        // 错误项次数ok、复测人数ok，筛查检查结果项目（合格项 不合格项），误差率  总的筛查结果ok 请看xmind。1个小时搞好。
        // 复测人数
        reviewsCount = rescreeningResultContentList.size();
        for (RescreeningResultContent rescreeningResultContent : rescreeningResultContentList) {
            eyeResult += rescreeningResultContent.getLsl().getErrorTimes() + rescreeningResultContent.getAve().getErrorTimes() + rescreeningResultContent.getJzsl().getErrorTimes();
            eyesCount += rescreeningResultContent.jzsl == null ? 4 : 6;//todo 魔数
        }
        //不合格项
        passNum = eyesCount - eyeResult;
        //错误率(误差率）
        if (eyesCount != 0) {
            errorRatio = eyeResult * 100 / eyesCount;
        }
        //总的筛查结果 todo 魔数
        qualified = errorRatio > 10 ? RESCREENING_NOT_PASS : RESCREENING_PASS;
    }

    /**
     * 过滤掉非完整数据
     *
     * @param studentScreeningInfoWithResultDTOS
     */
    public List<StudentScreeningInfoWithResultDTO> filterIncompletedData(List<StudentScreeningInfoWithResultDTO> studentScreeningInfoWithResultDTOS) {
        if (CollectionUtils.isEmpty(studentScreeningInfoWithResultDTOS)) {
            return new ArrayList<>();
        }
        return studentScreeningInfoWithResultDTOS.stream().filter(StudentScreeningInfoWithResultDTO::judgeCompleted).collect(Collectors.toList());
    }

    /**
     * 获取详情内容
     *
     * @param studentScreeningDataList
     * @return
     */
    private RescreeningResultContent getRescreeningResultContent(List<StudentScreeningInfoWithResultDTO> studentScreeningDataList) {
        //参数校验，一定要有初筛数据
        if (CollectionUtils.size(studentScreeningDataList) < 1) {
            throw new ManagementUncheckedException("入参异常,至少需要初筛和复筛数据。studentScreeningInfoWithResultDTOS = " + JSON.toJSONString(studentScreeningDataList));
        }
        TwoTuple<StudentScreeningInfoWithResultDTO, StudentScreeningInfoWithResultDTO> dataMap = this.getDataMap(studentScreeningDataList);
        StudentScreeningInfoWithResultDTO rescreeningData = dataMap.getSecond();
        StudentScreeningInfoWithResultDTO firstScreeningData = dataMap.getFirst();
        RescreeningResultContent rescreeningResultContent = new RescreeningResultContent();
        this.setBasicData(firstScreeningData, rescreeningData, rescreeningResultContent);
        this.setCoreData(firstScreeningData, rescreeningData, rescreeningResultContent);
        return rescreeningResultContent;
    }


    /**
     * 设置核心数据
     *
     * @param firstScreeningData
     * @param rescreeningData
     * @param rescreeningResultContent
     */
    private void setCoreData(StudentScreeningInfoWithResultDTO firstScreeningData, StudentScreeningInfoWithResultDTO rescreeningData, RescreeningResultContent rescreeningResultContent) {
        RescreeningGenericStructure sphResult = RescreeningGenericStructure.getRescreeningGenericStructureBuilder()
                .setReviewRight(rescreeningData.getComputerOptometry().getRightEyeData().getSph())
                .setReviewLeft(rescreeningData.getComputerOptometry().getLeftEyeData().getSph())
                .setFirstLeft(firstScreeningData.getComputerOptometry().getLeftEyeData().getSph())
                .setFirstRight(firstScreeningData.getComputerOptometry().getRightEyeData().getSph())
                .setRangeValue(OTHERS_RANGE_VALUE).build();
        rescreeningResultContent.setSph(sphResult);

        RescreeningGenericStructure aveResult = RescreeningGenericStructure.getRescreeningGenericStructureBuilder()
                .setReviewRight(rescreeningData.getComputerOptometry().getRightEyeData().getSph().add(sphResult.getReviewRight().divide(new BigDecimal(2))))
                .setReviewLeft(rescreeningData.getComputerOptometry().getLeftEyeData().getSph().add(sphResult.getReviewLeft().divide(new BigDecimal(2))))
                .setFirstLeft(firstScreeningData.getComputerOptometry().getLeftEyeData().getSph().add(sphResult.getFirstLeft().divide(new BigDecimal(2))))
                .setFirstRight(firstScreeningData.getComputerOptometry().getRightEyeData().getSph().add(sphResult.getFirstRight().divide(new BigDecimal(2))))
                .setRangeValue(AVE_RANGE_VALUE).build();
        rescreeningResultContent.setAve(aveResult);

        RescreeningGenericStructure nakedVisionResult = RescreeningGenericStructure.getRescreeningGenericStructureBuilder()
                .setReviewRight(rescreeningData.getVisionData().getRightEyeData().getNakedVision())
                .setReviewLeft(rescreeningData.getVisionData().getLeftEyeData().getNakedVision())
                .setFirstLeft(firstScreeningData.getVisionData().getLeftEyeData().getNakedVision())
                .setFirstRight(firstScreeningData.getVisionData().getRightEyeData().getNakedVision())
                .setRangeValue(OTHERS_RANGE_VALUE).build();
        rescreeningResultContent.setLsl(nakedVisionResult);

        RescreeningGenericStructure jzslResult = RescreeningGenericStructure.getRescreeningGenericStructureBuilder()
                .setReviewRight(rescreeningData.getVisionData().getRightEyeData().getCorrectedVision())
                .setReviewLeft(rescreeningData.getVisionData().getLeftEyeData().getCorrectedVision())
                .setFirstLeft(firstScreeningData.getVisionData().getLeftEyeData().getCorrectedVision())
                .setFirstRight(firstScreeningData.getVisionData().getRightEyeData().getCorrectedVision())
                .setRangeValue(OTHERS_RANGE_VALUE).build();
        rescreeningResultContent.setJzsl(jzslResult);

        RescreeningGenericStructure cylResult = RescreeningGenericStructure.getRescreeningGenericStructureBuilder()
                .setReviewRight(rescreeningData.getComputerOptometry().getRightEyeData().getCyl())
                .setReviewLeft(rescreeningData.getComputerOptometry().getLeftEyeData().getCyl())
                .setFirstLeft(firstScreeningData.getComputerOptometry().getLeftEyeData().getCyl())
                .setFirstRight(firstScreeningData.getComputerOptometry().getRightEyeData().getCyl())
                .setRangeValue(OTHERS_RANGE_VALUE).build();
        rescreeningResultContent.setCyl(cylResult);
    }

    /**
     * 设置基础数据
     *
     * @param firstScreeningData
     * @param rescreeningData
     * @param rescreeningResultContent
     */
    private void setBasicData(StudentScreeningInfoWithResultDTO firstScreeningData, StudentScreeningInfoWithResultDTO rescreeningData, RescreeningResultContent rescreeningResultContent) {
        //性别、复检医生、初检医生
        rescreeningResultContent.setStudentSchool(firstScreeningData.getSchoolName());
        rescreeningResultContent.setStudentGrade(firstScreeningData.getGradeName());
        rescreeningResultContent.setStudentClazz(firstScreeningData.getClazzName());
        rescreeningResultContent.setStudentName(firstScreeningData.getStudentName());
        rescreeningResultContent.setFirstTime(firstScreeningData.getUpdateTime());
        rescreeningResultContent.setReviewTime(rescreeningData.getUpdateTime());
        Integer studentGender = firstScreeningData.getStudentGender();
        //todo 性别转换成文字
        rescreeningResultContent.setStudentSex(String.valueOf(studentGender));
        rescreeningResultContent.setFirstDoctor(firstScreeningData.getDoctorName());
        rescreeningResultContent.setReviewDoctor(rescreeningData.getDoctorName());
    }

    /**
     * 将数据拆分成2个
     *
     * @param studentScreeningInfoWithResultDTOS
     * @return
     */
    private TwoTuple<StudentScreeningInfoWithResultDTO, StudentScreeningInfoWithResultDTO> getDataMap(@NonNull List<StudentScreeningInfoWithResultDTO> studentScreeningInfoWithResultDTOS) {
        Map<Integer, List<StudentScreeningInfoWithResultDTO>> collect = studentScreeningInfoWithResultDTOS.stream().collect(Collectors.groupingBy(StudentScreeningInfoWithResultDTO::getIsDoubleScreen));
        List<StudentScreeningInfoWithResultDTO> firstScreeningData = collect.get(0);
        List<StudentScreeningInfoWithResultDTO> RescreeningData = collect.get(1);
        if (CollectionUtils.isEmpty(firstScreeningData) || CollectionUtils.isEmpty(RescreeningData)) {
            throw new ManagementUncheckedException("复筛数据不能为空，studentScreeningInfoWithResultDTOS = " + JSON.toJSONString(studentScreeningInfoWithResultDTOS));
        }
        TwoTuple<StudentScreeningInfoWithResultDTO, StudentScreeningInfoWithResultDTO> twoTupleData = new TwoTuple<>();
        twoTupleData.setFirst(firstScreeningData.get(0));
        twoTupleData.setSecond(RescreeningData.get(0));
        return twoTupleData;
    }

    @Data
    public class RescreeningResultContent {

        private String studentSex;
        /**
         * reviewDoctor
         */
        private String reviewDoctor;
        /**
         * 学校名称
         */
        private String studentSchool;
        /**
         * firstDoctor
         */
        private String firstDoctor;
        /**
         * firstTime
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

        private Date firstTime;
        /**
         * reviewsId
         */
        private String reviewsId;
        /**
         * 学校年级
         */
        private String studentGrade;
        /**
         * 学校班级
         */
        private String studentClazz;
        /**
         * 学生姓名
         */
        private String studentName;
        /**
         * 复检时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date reviewTime;
        /**
         * 矫正视力
         */
        private RescreeningGenericStructure jzsl;
        /**
         * 柱镜
         */
        private RescreeningGenericStructure cyl;
        /**
         * 球镜
         */
        private RescreeningGenericStructure sph;
        /**
         * 等效球镜
         */
        private RescreeningGenericStructure ave;
        /**
         * 裸眼视力
         */
        private RescreeningGenericStructure lsl;
    }

}
