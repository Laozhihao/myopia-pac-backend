package com.wupol.myopia.business.aggregation.export.excel;

import cn.hutool.core.util.ZipUtil;
import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.business.aggregation.export.BaseExportFileService;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.common.constant.ExportAddressKey;
import com.wupol.myopia.business.core.common.domain.model.AddressCode;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021/3/24
 **/
@Log4j2
@Service
public abstract class BaseExportExcelFileService extends BaseExportFileService {


    public static ThreadLocal<String> localVar = new ThreadLocal<>();

    public static String excelSavePath = "/tmp/export/";

    @Autowired
    private DistrictService districtService;
    @Resource
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private ResourceFileService resourceFileService;

    /**
     * 导出文件
     *
     * @param exportCondition 导出条件
     * @return void
     **/
    @Async
    @Override
    public void export(ExportCondition exportCondition) {

        File excelFile = null;
        String noticeKeyContent = null;

        try {
            // 1.获取文件名
            String fileName = getFileName(exportCondition);
            // 2.获取通知的关键内容
            noticeKeyContent = getNoticeKeyContent(exportCondition);
            // 3.获取数据，生成List
            List data = getExcelData(exportCondition);
            // 4.数据处理
            excelFile = fileDispose(isPackage(), exportCondition, fileName, data);
            // 5.上传文件
            Integer fileId = uploadFile(excelFile);
            // 6.发送成功通知
            sendSuccessNotice(exportCondition.getApplyExportFileUserId(), noticeKeyContent, fileId);
        } catch (Exception e) {
            String requestData = JSON.toJSONString(exportCondition);
            log.error("【导出Excel异常】{}", requestData, e);
            // 发送失败通知
            if (!StringUtils.isEmpty(noticeKeyContent)) {
                sendFailNotice(exportCondition.getApplyExportFileUserId(), noticeKeyContent);
            }
        } finally {
            // 7.删除临时文件
            if (isPackage()){
                deleteTempFile(excelSavePath+localVar.get());
            }else {
                if (Objects.nonNull(excelFile)){
                    deleteTempFile(excelFile.getPath());
                }
            }
            // 8.释放锁
            unlock(getLockKey(exportCondition));
        }
    }


    /**
     * 开关
     *
     * @param
     * @return java.io.File
     **/
    public Boolean isPackage(){
       return false;
    }

    /**
     * 文件处理
     *
     * @param isPackage 是否压缩
     * @return java.io.File
     **/
    public File fileDispose(boolean isPackage,ExportCondition exportCondition,String fileName,List data) throws IOException {
        if (isPackage){
            generateExcelFile(fileName, data, exportCondition);
            File file = compressFile(excelSavePath + localVar.get());
            localVar.remove();
            return file;
        }else {
            return generateExcelFile(fileName, data, exportCondition);
        }
    }



    /**
     * 压缩文件
     *
     * @param fileSavePath 文件保存路径
     * @return java.io.File
     **/
    public File compressFile(String fileSavePath) {
        return ZipUtil.zip(fileSavePath);
    }

    /**
     * 获取文件保存父目录路径
     *
     * @return java.lang.String
     **/
    public String getFileSaveParentPath() {
        return Paths.get(excelSavePath, UUID.randomUUID().toString()).toString();
    }

    /**
     * 获取文件保存路径
     *
     * @param parentPath 文件名
     * @param fileName   文件名
     * @return java.lang.String
     **/
    public String getFileSavePath(String parentPath, String fileName) {
        return Paths.get(parentPath, fileName).toString();
    }

    /**
     * 导出前的校验
     *
     * @param exportCondition 导出条件
     * @return void
     **/
    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
        // 有需要校验其他的，重写覆盖该方法
        if (Objects.isNull(exportCondition.getDistrictId())) {
            throw new BusinessException("行政区域id不能为空");
        }
        District district = districtService.getById(exportCondition.getDistrictId());
        if (Objects.isNull(district)) {
            throw new BusinessException("未找到该行政区域");
        }
    }

    /**
     * 获取生成Excel的数据
     *
     * @param exportCondition 导出条件
     * @return java.util.List
     **/
    public abstract List getExcelData(ExportCondition exportCondition);


    /**
     * 生成Excel文件
     *
     * @param fileName 文件名
     * @param data Excel数据
     * @return java.io.File
     **/
    public File generateExcelFile(String fileName, List data,ExportCondition exportCondition) throws IOException {
        return ExcelUtil.exportListToExcel(fileName, data, getHeadClass());
    }

    /**
     * 获取Excel表头类
     *
     * @return java.lang.Class
     **/
    public abstract Class getHeadClass();

    /**
     * 获取通知的关键内容
     *
     * @param exportCondition 导出条件
     * @return java.lang.String
     **/
    public abstract String getNoticeKeyContent(ExportCondition exportCondition);

    /**
     * 根据id批量获取用户
     *
     * @param userIds 用户id列
     * @return Map<用户id ， 用户>
     */
    Map<Integer, User> getUserMapByIds(Set<Integer> userIds) {
        return oauthServiceClient.getUserBatchByIds(new ArrayList<>(userIds)).stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }

    /**
     * 获取压缩包名
     *
     * @param exportCondition 导出条件
     * @return java.lang.String
     **/
    String getPackageFileName(ExportCondition exportCondition) {
        return null;
    }

    /**
     * 获取省市区
     *
     * @param item AddressCode
     * @param <T>  实体
     * @return HashMap<String, String>
     */
    <T extends AddressCode> HashMap<String, String> generateAddressMap(T item) {
        HashMap<String, String> addressCodeMap = new HashMap<>(5);
        if (Objects.nonNull(item.getProvinceCode())) {
            addressCodeMap.put(ExportAddressKey.PROVIDE, districtService.getDistrictName(item.getProvinceCode()));
        }
        if (Objects.nonNull(item.getCityCode())) {
            addressCodeMap.put(ExportAddressKey.CITY, districtService.getDistrictName(item.getCityCode()));
        }
        if (Objects.nonNull(item.getAreaCode())) {
            addressCodeMap.put(ExportAddressKey.AREA, districtService.getDistrictName(item.getAreaCode()));
        }
        if (Objects.nonNull(item.getTownCode())) {
            addressCodeMap.put(ExportAddressKey.TOWN, districtService.getDistrictName(item.getTownCode()));
        }
        return addressCodeMap;
    }

    @Override
    public String syncExport(ExportCondition exportCondition) {
        File excelFile = null;
        String fileName = null;
        try {
            // 1.获取文件名
            fileName = getFileName(exportCondition);
            // 3.获取数据，生成List
            List data = getExcelData(exportCondition);
            // 2.获取文件保存父目录路径
            excelFile = syncFileDispose(isPackage(),exportCondition,fileName,data);
            return resourceFileService.getResourcePath(s3Utils.uploadS3AndGetResourceFile(excelFile.getAbsolutePath(), excelFile.getName()).getId());
        } catch (Exception e) {
            String requestData = JSON.toJSONString(exportCondition);
            log.error("【生成Excel异常】{}", requestData, e);
            // 发送失败通知
            throw new BusinessException("导出数据异常");
        } finally {
            // 5.删除临时文件
            if (Objects.nonNull(excelFile)) {
                deleteTempFile(excelFile.getPath());
            }
        }
    }

    public File syncFileDispose(boolean isPackage,ExportCondition exportCondition,String fileName,List data) throws IOException {
        if (isPackage){
            generateExcelFile(fileName, data, exportCondition);
            log.info("文件获取路径："+excelSavePath + localVar.get());
            log.info("文件名："+fileName);
            File file = new File(excelSavePath + localVar.get() + "/" + fileName);
            localVar.remove();
            return file;
        }else {
            return generateExcelFile(fileName, data, exportCondition);
        }
    }
}
