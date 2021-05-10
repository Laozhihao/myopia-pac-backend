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

    @ExcelProperty("有效筛查学生数")
    private long validScreeningNum;

    @ExcelProperty("平均视力(左)")
    private Float averageVisionLeft;

    @ExcelProperty("平均视力(右)")
    private Float averageVisionRight;

    @ExcelProperty("视力低下率")
    private String lowVisionRatio;

    @ExcelProperty("屈光不正率")
    private String refractiveErrorRatio;

    @ExcelProperty("戴镜率")
    private String wearingGlassesRatio;

    @ExcelProperty("近视人数")
    private long myopiaNum;

    @ExcelProperty("近视率")
    private String myopiaRatio;

    @ExcelProperty("重点视力对象数量")
    private long focusTargetsNum;

    @ExcelProperty("0级预警率")
    private String warningLevelZeroRatio;

    @ExcelProperty("1级预警率")
    private String warningLevelOneRatio;

    @ExcelProperty("2级预警率")
    private String warningLevelTwoRatio;

    @ExcelProperty("3级预警率")
    private String warningLevelThreeRatio;

    @ExcelProperty("建议就诊数")
    private long recommendVisitNum;

    @ExcelProperty("视力筛查完成率")
    private String screeningFinishedRatio;

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
    private String incorrectRatio;
}
