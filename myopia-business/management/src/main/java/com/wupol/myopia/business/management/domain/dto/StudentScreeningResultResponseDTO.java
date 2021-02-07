package com.wupol.myopia.business.management.domain.dto;
import com.google.common.collect.Lists;

import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 学生筛查档案
 *
 * @author Simple4H
 */
@Getter
@Setter
public class StudentScreeningResultResponseDTO {

    /**
     * 0 为左眼 1 为右眼
     */
    private Integer lateriality;

    /**
     * 详情
     */
    private StudentResultDetails detail;

    public List<StudentScreeningResultResponseDTO> packageDTO(VisionScreeningResult result) {

        // 设置左眼
        StudentScreeningResultResponseDTO left = new StudentScreeningResultResponseDTO();
        StudentResultDetails leftDetails = new StudentResultDetails();
        leftDetails.setGlassesType(result.getVisionData().getLeftEyeData().getGlassesType());
        leftDetails.setCorrectedVision(result.getVisionData().getLeftEyeData().getCorrectedVision());
        leftDetails.setNakedVision(result.getVisionData().getLeftEyeData().getNakedVision());
        leftDetails.setAxial(result.getComputerOptometry().getLeftEyeData().getAxial());
        leftDetails.setSph(result.getComputerOptometry().getLeftEyeData().getSph());
        leftDetails.setCyl(result.getComputerOptometry().getLeftEyeData().getCyl());
        leftDetails.setAD(result.getBiometricData().getLeftEyeData().getAD());
        leftDetails.setAL(result.getBiometricData().getLeftEyeData().getAL());
        leftDetails.setCCT(result.getBiometricData().getLeftEyeData().getCCT());
        leftDetails.setLT(result.getBiometricData().getLeftEyeData().getLT());
        leftDetails.setWTW(result.getBiometricData().getLeftEyeData().getWTW());
        leftDetails.setEyeDiseases(result.getOtherEyeDiseases().getLeftEyeData().getEyeDiseases());
        left.setLateriality(0);
        left.setDetail(leftDetails);

        //设置右眼
        StudentScreeningResultResponseDTO right = new StudentScreeningResultResponseDTO();
        StudentResultDetails rightDetails = new StudentResultDetails();
        rightDetails.setGlassesType(result.getVisionData().getRightEyeData().getGlassesType());
        rightDetails.setCorrectedVision(result.getVisionData().getRightEyeData().getCorrectedVision());
        rightDetails.setNakedVision(result.getVisionData().getRightEyeData().getNakedVision());
        rightDetails.setAxial(result.getComputerOptometry().getRightEyeData().getAxial());
        rightDetails.setSph(result.getComputerOptometry().getRightEyeData().getSph());
        rightDetails.setCyl(result.getComputerOptometry().getRightEyeData().getCyl());
        rightDetails.setAD(result.getBiometricData().getRightEyeData().getAD());
        rightDetails.setAL(result.getBiometricData().getRightEyeData().getAL());
        rightDetails.setCCT(result.getBiometricData().getRightEyeData().getCCT());
        rightDetails.setLT(result.getBiometricData().getRightEyeData().getLT());
        rightDetails.setWTW(result.getBiometricData().getRightEyeData().getWTW());
        rightDetails.setEyeDiseases(result.getOtherEyeDiseases().getRightEyeData().getEyeDiseases());
        right.setLateriality(1);
        right.setDetail(rightDetails);
        return Lists.newArrayList(left,right);
    }
}
