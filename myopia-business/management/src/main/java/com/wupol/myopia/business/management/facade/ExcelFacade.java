package com.wupol.myopia.business.management.facade;

import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.base.util.IOUtils;
import com.wupol.myopia.business.management.constant.HospitalEnum;
import com.wupol.myopia.business.management.domain.model.BillRecord;
import com.wupol.myopia.business.management.domain.model.BillStat;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningOrganizationExportVo;
import com.wupol.myopia.business.management.service.ScreeningOrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 统一处理 Excel 上传/下载
 *
 * @Author HaoHao
 * @Date 2020/12/21
 **/
@Slf4j
@Service
public class ExcelFacade {
    /** 日账单Excel文件名，如 Logs_2020-10-16_(GMT+2).xlsx */
    private static final String BILL_EXCEL_FILE_NAME_OF_DAY = "Logs_%s_(%s).xlsx";
    private static final String SCREENING_ORGANIZATION = "筛查机构_%s.xlsx";

    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;

    /**
     * 生成筛查机构Excel
     *
     * @param query    机构查询条件
     * @return java.io.File
     **/
    public String generateScreeningOrganization(ScreeningOrganizationQuery query) throws IOException {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(query.getIdLike())) builder.append("_").append(query.getIdLike());
        if (StringUtils.isNotBlank(query.getNameLike())) builder.append("_").append(query.getNameLike());
        if (Objects.nonNull(query.getType())) builder.append("_").append(HospitalEnum.getNameByType(query.getType()));
        if (StringUtils.isNotBlank(query.getCode())) builder.append("_").append(query.getCode());
        String fileName = String.format(SCREENING_ORGANIZATION, builder.toString());
        //TODO 待写模糊搜索
//        List<ScreeningOrganization> list = screeningOrganizationService.findByList(query);
        log.info("导出文件: {}", fileName);
//        File file = ExcelUtil.exportListToExcel(fileName, list, ScreeningOrganizationExportVo.class);


        //TODO 待上传文件
        return "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png";
//        return ExcelUtil.exportListToExcel(getFilePathName(fileName), billStatList, BillStat.class);
    }

    /**
     * 生成日账单明细Excel
     *
     * @param billRecordList    账单数据
     * @param timezone          时区
     * @param date              日期
     * @return java.io.File
     **/
    public File generateBillExcelOfDay(List<BillRecord> billRecordList, String timezone, String date) throws IOException {
        String fileName = String.format(BILL_EXCEL_FILE_NAME_OF_DAY, date, timezone);
        return ExcelUtil.exportListToExcel(getFilePathName(fileName), billRecordList, BillRecord.class);
    }


    /**
     * 获取文件路径
     *
     * @param fileName  文件名
     * @return java.lang.String
     **/
    private String getFilePathName(String fileName) {
        return FilenameUtils.concat(IOUtils.getTempSubPath("excel"), fileName);
    }
}
