package com.wupol.myopia.third.party.service;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.third.party.domain.VisionScreeningResultDTO;
import com.wupol.myopia.third.party.domain.constant.WearGlassTypeEnum;
import com.wupol.myopia.third.party.domain.model.StudentVisionScreeningResult;
import com.wupol.myopia.third.party.util.XinJiangParseDataUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
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

    /** 缺省值9 */
    public static final int DEFAULT_INT_9 = 9;

    @Autowired
    private StudentVisionScreeningResultService studentVisionScreeningResultService;

    /**
     * 处理原始数据入库
     *
     * @param originalData 原始数据
     */
    @Async
    public void handleScreeningResultData(VisionScreeningResultDTO originalData) {
        try {
            saveOrUpdateScreeningResult(originalData);
        } catch (Exception e) {
            log.error("【同步筛查数据到新疆中间库异常】原始数据：{}", JSON.toJSONString(originalData), e);
        }
    }

    /**
     * 处理原始数据入库
     *
     * @param originalData 原始数据
     */
    private void saveOrUpdateScreeningResult(VisionScreeningResultDTO originalData) {
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
                .setLeftNakedVision(XinJiangParseDataUtil.parseNakedVision(originalData.getLeftNakedVision()))
                .setRightNakedVision(XinJiangParseDataUtil.parseNakedVision(originalData.getRightNakedVision()))
                .setIsWear(XinJiangParseDataUtil.getIsWear(originalData.getGlassesType()))
                .setWearGlassType(WearGlassTypeEnum.getCodeByVistelGlassType(originalData.getGlassesType()))
                .setLeftGlassesDegree(XinJiangParseDataUtil.bigDecimalToStr(originalData.getLeftGlassesDegree()))
                .setRightGlassesDegree(XinJiangParseDataUtil.bigDecimalToStr(originalData.getRightGlassesDegree()));
        // 矫正视力数据
        if (WearGlassTypeEnum.OK.code.equals(newData.getWearGlassType())) {
            newData.setLeftCorrectedVision(XinJiangParseDataUtil.bigDecimalToStr(originalData.getLeftCorrectedVision()))
                    .setRightCorrectedVision(XinJiangParseDataUtil.bigDecimalToStr(originalData.getRightCorrectedVision()));
        } else {
            newData.setLeftGlassedVision(XinJiangParseDataUtil.bigDecimalToStr(originalData.getLeftCorrectedVision()))
                    .setRightGlassedVision(XinJiangParseDataUtil.bigDecimalToStr(originalData.getRightCorrectedVision()));
        }
        // 屈光数据
        newData.setLeftSphericalMirror(XinJiangParseDataUtil.parseSphOrCyl(originalData.getLeftSphericalMirror()))
                .setRightSphericalMirror(XinJiangParseDataUtil.parseSphOrCyl(originalData.getRightSphericalMirror()))
                .setLeftCylindricalMirror(XinJiangParseDataUtil.parseSphOrCyl(originalData.getLeftCylindricalMirror()))
                .setRightCylindricalMirror(XinJiangParseDataUtil.parseSphOrCyl(originalData.getRightCylindricalMirror()))
                .setLeftAxialPosition(XinJiangParseDataUtil.parseAxial(originalData.getLeftAxialPosition()))
                .setRightAxialPosition(XinJiangParseDataUtil.parseAxial(originalData.getRightAxialPosition()));
        // 缺省数据
        return newData.setLeftMirrorCheck(DEFAULT_INT_9).setRightMirrorCheck(DEFAULT_INT_9).setLeftAmetropia(DEFAULT_INT_9).setRightAmetropia(DEFAULT_INT_9);

    }

    /**
     * 更新筛查数据学校名称
     *
     * @param oldSchoolName 旧名称
     * @param newSchoolName 新名称
     */
    public void updateSchoolName(String oldSchoolName, String newSchoolName) {
        int count = studentVisionScreeningResultService.count(new StudentVisionScreeningResult().setSchoolName(oldSchoolName));
        if (count == 0) {
            log.warn("没有需要更新学校名称的筛查数据，旧名称={}，新名称={}", oldSchoolName, newSchoolName);
            return;
        }
        studentVisionScreeningResultService.update(new StudentVisionScreeningResult().setSchoolName(newSchoolName), new StudentVisionScreeningResult().setSchoolName(oldSchoolName));
    }

}
