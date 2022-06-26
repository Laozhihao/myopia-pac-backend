package com.wupol.myopia.business.aggregation.export.pdf.constant;

import com.wupol.myopia.base.exception.BusinessException;
import lombok.Getter;

import java.util.Arrays;

/**
 * 档案卡导出类型
 *
 * @Author HaoHao
 * @Date 2022/5/24
 **/
@Getter
public enum ArchiveExportTypeEnum {

    DISTRICT(1, "区域", ExportReportServiceNameConstant.EXPORT_DISTRICT_ARCHIVES_SERVICE, true),
    SCHOOL(2, "学校", ExportReportServiceNameConstant.EXPORT_SCHOOL_OR_GRADE_ARCHIVES_SERVICE, true),
    GRADE(3, "年级", ExportReportServiceNameConstant.EXPORT_SCHOOL_OR_GRADE_ARCHIVES_SERVICE, true),
    CLASS(4, "班级", ExportReportServiceNameConstant.EXPORT_CLASS_OR_STUDENT_ARCHIVES_SERVICE, false),
    STUDENT(5, "多个或单个学生", ExportReportServiceNameConstant.EXPORT_CLASS_OR_STUDENT_ARCHIVES_SERVICE, false);

    /**
     * 类型
     */
    private Integer type;
    /**
     * 描述
     */
    private String descr;
    /**
     * 业务类名
     */
    private String serviceClassName;
    /**
     * 是否异步导出
     */
    private Boolean asyncExport;

    ArchiveExportTypeEnum(Integer type, String descr, String serviceClassName, boolean asyncExport) {
        this.type = type;
        this.descr = descr;
        this.serviceClassName = serviceClassName;
        this.asyncExport = asyncExport;
    }

    public static ArchiveExportTypeEnum getByType(Integer type) {
        return Arrays.stream(ArchiveExportTypeEnum.values())
                .filter(item -> item.type.equals(type))
                .findFirst()
                .orElseThrow(() -> new BusinessException("无效导出类型"));
    }
}
