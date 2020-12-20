package com.wupol.myopia.business.management.facade;

import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.base.util.IOUtils;
import com.wupol.myopia.business.management.domain.model.BillRecord;
import com.wupol.myopia.business.management.domain.model.BillStat;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 统一处理 Excel 上传/下载
 *
 * @Author HaoHao
 * @Date 2020/12/21
 **/
@Service
public class ExcelFacade {
    /** 日账单Excel文件名，如 Logs_2020-10-16_(GMT+2).xlsx */
    private static final String BILL_EXCEL_FILE_NAME_OF_DAY = "Logs_%s_(%s).xlsx";
    /** 月账单Excel文件名，如 Statistics_2020-10_(GMT+2).xlsx */
    private static final String BILL_EXCEL_FILE_NAME_OF_MONTH = "Statistics_%s_(%s).xlsx";

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
     * 生成月账单统计Excel
     *
     * @param billStatList    账单统计数据
     * @param timezone          时区
     * @param date              日期
     * @return java.io.File
     **/
    public File generateBillExcelOfMonth(List<BillStat> billStatList, String timezone, String date) throws IOException {
        String fileName = String.format(BILL_EXCEL_FILE_NAME_OF_MONTH, date, timezone);
        return ExcelUtil.exportListToExcel(getFilePathName(fileName), billStatList, BillStat.class);
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
