package com.wupol.myopia.third.party.service;

import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.third.party.domain.VisionScreeningResultDTO;
import com.wupol.myopia.third.party.domain.constant.WearGlassTypeEnum;
import com.wupol.myopia.third.party.domain.model.StudentVisionScreeningResult;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 新疆业务处理类
 *
 * @Author lzh
 * @Date 2023/4/13
 **/
@Log4j2
@Service
public class XinJiangService {

    @Autowired
    private StudentVisionScreeningResultService studentVisionScreeningResultService;

    /**
     * 处理原始数据入库
     *
     * @param originalData 原始数据
     */
    public void handleScreeningResultData(VisionScreeningResultDTO originalData) {
        // 构建数据
        StudentVisionScreeningResult newData = buildStudentVisionScreeningResult(originalData);
        // 获取旧数据
        StudentVisionScreeningResult oldData = studentVisionScreeningResultService.findOne(new StudentVisionScreeningResult().setSchoolName(originalData.getSchoolName()).setYearTest(originalData.getYear()).setSecond(originalData.getTime()).setStudentIdCard(originalData.getStudentIdCard()));
        newData.setUuid(Objects.isNull(oldData) ? UUID.randomUUID().toString().replaceAll("-", "") : oldData.getUuid());
        // 新增或更新
        studentVisionScreeningResultService.saveOrUpdate(newData);
    }

    /**
     * 构建新疆中间库数据实体
     *
     * @param originalData  原始数据
     * @return StudentVisionScreeningResult
     */
    private StudentVisionScreeningResult buildStudentVisionScreeningResult(VisionScreeningResultDTO originalData) {
        StudentVisionScreeningResult newData = new StudentVisionScreeningResult()
                // 基本信息
                .setSchoolName(originalData.getSchoolName())
                .setYearTest(originalData.getYear())
                .setSecond(originalData.getTime())
                .setStudentName(originalData.getStudentName())
                .setStudentIdCard(originalData.getStudentIdCard())
                .setStudentNum(originalData.getStudentNo())
                .setUpdateTime(new Date())
                // 视力数据
                .setLeftNakedVision(parseNakedVision(originalData.getLeftNakedVision()))
                .setRightNakedVision(parseNakedVision(originalData.getRightNakedVision()))
                .setIsWear(getIsWear(originalData.getGlassesType()))
                .setWearGlassType(WearGlassTypeEnum.getCodeByVistelGlassType(originalData.getGlassesType()))
                .setLeftGlassesDegree(bigDecimalToStr(originalData.getLeftGlassesDegree()))
                .setRightGlassesDegree(bigDecimalToStr(originalData.getRightGlassesDegree()));
        // 矫正视力数据
        if (WearGlassTypeEnum.OK.code.equals(newData.getWearGlassType())) {
            newData.setLeftCorrectedVision(bigDecimalToStr(originalData.getLeftCorrectedVision()))
                    .setRightCorrectedVision(bigDecimalToStr(originalData.getRightCorrectedVision()));
        } else {
            newData.setLeftGlassedVision(bigDecimalToStr(originalData.getLeftCorrectedVision()))
                    .setRightGlassedVision(bigDecimalToStr(originalData.getRightCorrectedVision()));
        }
        // 屈光数据
        newData.setLeftSphericalMirror(parseSphOrCyl(originalData.getLeftSphericalMirror())).setRightSphericalMirror(parseSphOrCyl(originalData.getRightSphericalMirror()))
                .setLeftCylindricalMirror(parseSphOrCyl(originalData.getLeftCylindricalMirror())).setRightCylindricalMirror(parseSphOrCyl(originalData.getRightCylindricalMirror()))
                .setLeftAxialPosition(parseAxial(originalData.getLeftAxialPosition())).setRightAxialPosition(parseAxial(originalData.getRightAxialPosition()));
        // 缺省数据
        return newData.setLeftMirrorCheck(9).setRightMirrorCheck(9).setLeftAmetropia(9).setRightAmetropia(9);

    }

    /**
     * 解析转换裸眼视力
     *
     * @param nakedVision 裸眼视力
     * @return String
     */
    private String parseNakedVision(BigDecimal nakedVision) {
        return Optional.ofNullable(nakedVision).map(x -> x.compareTo(BigDecimal.valueOf(3)) < 0 ? "9" : x.toString()).orElse(null);
    }

    /**
     * 获取是否带镜
     *
     * @param glassesType 戴镜类型
     * @return Integer
     */
    private Integer getIsWear(Integer glassesType) {
        if (Objects.isNull(glassesType)) {
            return null;
        }
        return GlassesTypeEnum.NOT_WEARING.getCode().equals(glassesType) ? 0 : 1;
    }

    /**
     * 解析转换sph或cyl
     *
     * @param sphOrCyl sph或cyl
     * @return String
     */
    private String parseSphOrCyl(BigDecimal sphOrCyl) {
        return Optional.ofNullable(sphOrCyl).map(x -> x.compareTo(BigDecimal.valueOf(0)) >= 0 ? "+" + x : x.toString()).orElse("999");
    }

    /**
     * 解析转换
     *
     * @param axial 轴位
     * @return String
     */
    private String parseAxial(BigDecimal axial) {
        return Optional.ofNullable(axial).map(x -> x.compareTo(BigDecimal.valueOf(180)) > 0 || x.compareTo(BigDecimal.valueOf(0)) < 0 ? "999" : x.toString()).orElse("999");
    }

    /**
     * BigDecimal转String
     *
     * @param bigDecimal 数值类型
     * @return String
     */
    private String bigDecimalToStr(BigDecimal bigDecimal) {
        return Optional.ofNullable(bigDecimal).map(BigDecimal::toString).orElse(null);
    }
}
