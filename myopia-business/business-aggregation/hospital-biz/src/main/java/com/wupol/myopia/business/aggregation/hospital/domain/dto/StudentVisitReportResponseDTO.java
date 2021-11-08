package com.wupol.myopia.business.aggregation.hospital.domain.dto;

import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.business.core.hospital.domain.model.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 学生就诊记录详情
 *
 * @author Simple4H
 */
@Data
public class StudentVisitReportResponseDTO {

    /**
     * 学生信息
     */
    private StudentInfo student;

    /**
     * 医院名称
     */
    private String hospitalName;

    /**
     * 报告内容
     */
    private ReportInfo report;

    /**
     * 问诊内容
     */
    private Consultation consultation;

    /**
     * 视力检查
     */
    private VisionMedicalRecord vision;

    /**
     * 生物测量
     */
    private BiometricsMedicalRecord biometrics;

    /**
     * 屈光检查
     */
    private DiopterMedicalRecord diopter;

    /**
     * 角膜地形图
     */
    private ToscaMedicalRecord tosca;

    /**
     * 眼压
     */
    private EyePressure eyePressure;

    /** 右眼等效球镜SE */
    private String nonMydriasisComputerRightSE;
    /** 左眼等效球镜SE */
    private String nonMydriasisComputerLeftSE;

    /** 右眼等效球镜SE */
    private String mydriasisComputerRightSE;
    /** 左眼等效球镜SE */
    private String mydriasisComputerLeftSE;

    @Getter
    @Setter
    public static class StudentInfo {

        /**
         * 姓名
         */
        private String name;

        /**
         * 生日
         */
        private Date birthday;

        /**
         * 性别
         */
        private Integer gender;
    }

    @Setter
    @Getter
    public static class ReportInfo {

        private Integer reportId;
        /**
         * 报告编号
         */
        private String no;

        /**
         * 报告日期
         */
        private Date createTime;

        /**
         * 配镜情况。1配框架眼镜，2配OK眼镜，3配隐形眼镜。
         */
        private Integer glassesSituation;

        /**
         * 医生诊断
         */
        private String medicalContent;

        /**
         * 诊断处方图片
         */
        private List<String> imageUrlList;

        /**
         * 医生签名图片
         */
        private String doctorSign;
    }

    public String getNonMydriasisComputerRightSE() {
        if (Objects.isNull(diopter)) {
            return StringUtils.EMPTY;
        }
        DiopterMedicalRecord.Diopter diopterRecord = diopter.getNonMydriasis();
        return getSE(diopterRecord.getComputerRightDS(), diopterRecord.getComputerRightDC());
    }

    public String getNonMydriasisComputerLeftSE() {
        if (Objects.isNull(diopter)) {
            return StringUtils.EMPTY;
        }
        DiopterMedicalRecord.Diopter diopterRecord = diopter.getNonMydriasis();
        return getSE(diopterRecord.getComputerLeftDS(), diopterRecord.getComputerLeftDC());
    }

    public String getMydriasisComputerRightSE() {
        if (Objects.isNull(diopter)) {
            return StringUtils.EMPTY;
        }
        DiopterMedicalRecord.Diopter diopterRecord = diopter.getMydriasis();
        return getSE(diopterRecord.getComputerRightDS(), diopterRecord.getComputerRightDC());
    }

    public String getMydriasisComputerLeftSE() {
        if (Objects.isNull(diopter)) {
            return StringUtils.EMPTY;
        }
        DiopterMedicalRecord.Diopter diopterRecord = diopter.getMydriasis();
        return getSE(diopterRecord.getComputerLeftDS(), diopterRecord.getComputerLeftDC());
    }


    private String getSE(String val1, String val2) {
        if (!StringUtils.allHasLength(val1, val2)) {
            return StringUtils.EMPTY;
        }
        return new BigDecimal(val1).add(new BigDecimal(val2).multiply(new BigDecimal("0.5")))
                .setScale(2, RoundingMode.HALF_UP).toString();
    }
}
