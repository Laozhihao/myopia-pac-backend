package com.wupol.myopia.business.management.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScreeningDataContrastVo {
    @ExcelProperty("标题")
    private String title;

    @ExcelProperty("筛查学生数")
    private long screeningNum;

    @ExcelProperty("实际筛查学生数")
    private long actualScreeningNum;

    @ExcelProperty("平均视力(左)")
    private Float averageVisionLeft;

    @ExcelProperty("平均视力(右)")
    private Float averageVisionRight;

    @ExcelProperty("视力低下率")
    private Float lowVisionRatio;

    @ExcelProperty("屈光不正率")
    private Float refractiveErrorRatio;

    @ExcelProperty("戴镜率")
    private Float wearingGlassesRatio;

    @ExcelProperty("近视人数")
    private long myopiaNum;

    @ExcelProperty("近视率")
    private Float myopiaRatio;

    @ExcelProperty("重点视力对象数量")
    private long focusTargetsNum;

    @ExcelProperty("0级预警率")
    private Float warningLevelZeroRatio;

    @ExcelProperty("1级预警率")
    private Float warningLevelOneRatio;

    @ExcelProperty("2级预警率")
    private Float warningLevelTwoRatio;

    @ExcelProperty("3级预警率")
    private Float warningLevelThreeRatio;

    @ExcelProperty("建议就诊数")
    private long recommendVisitNum;

    @ExcelProperty("视力筛查完成率")
    private Float screeningFinishedRatio;

    @ExcelProperty("复测人数")
    private long rescreenNum;

    @ExcelProperty("戴镜复测人数")
    private long wearingGlassesRescreenNum;

    @ExcelProperty("戴镜复测指标数")
    private long wearingGlassesRescreenIndexNum;

    @ExcelProperty("非戴镜复测人数")
    private long withoutGlassesRescreenNum;

    @ExcelProperty("非戴镜复测指标数")
    private long withoutGlassesRescreenIndexNum;

    @ExcelProperty("复测项次")
    private long rescreenItemNum;

    @ExcelProperty("错误项次数")
    private long incorrectItemNum;

    @ExcelProperty("发生率")
    private float incorrectRatio;
}
