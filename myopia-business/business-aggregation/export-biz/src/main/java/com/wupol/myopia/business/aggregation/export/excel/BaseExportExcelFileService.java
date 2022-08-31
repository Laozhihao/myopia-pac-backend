package com.wupol.myopia.business.aggregation.export.excel;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.alibaba.fastjson.JSON;
import com.vistel.Interface.exception.UtilException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
@Service
public abstract class BaseExportExcelFileService extends BaseExportFileService {


    @Value("${file.temp.save-path}")
    public String excelSavePath;

    @Autowired
    private DistrictService districtService;
    @Resource
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private ResourceFileService resourceFileService;

    private static String errorExcelMsg = "【导出Excel异常】{}";

    /**
     * 导出文件
     *
     * @param exportCondition 导出条件
     * @return void
     **/
    @Async
    @Override
    public void export(ExportCondition exportCondition) {

        String noticeKeyContent = null;
        String parentPath = null;
        try {
            // 0.获取通知的关键内容
            noticeKeyContent = getNoticeKeyContent(exportCondition);
            // 前置处理
            preProcess(exportCondition);
            // 1.获取文件名
            String fileName = getFileName(exportCondition);
            // 2.获取文件保存父目录路径
            parentPath = getFileSaveParentPath();
            // 3.获取文件保存路径
            String fileSavePath = getFileSavePath(parentPath, fileName);
            // 4.获取数据，生成List
            List data = getExcelData(exportCondition);
            // 5.数据处理
            File excelFile = fileDispose(isPackage(), exportCondition, fileSavePath,fileName, data);
            // 没有文件直接返回
            if (Objects.isNull(excelFile)){
                throw new BusinessException("没有文件导出失败");
            }
            // 6.上传文件
            Integer fileId = uploadFile(excelFile);
            // 7.发送成功通知
            sendSuccessNotice(exportCondition.getApplyExportFileUserId(), noticeKeyContent, fileId);
        } catch (Exception e) {
            String requestData = JSON.toJSONString(exportCondition);
            log.error(getErrorMsg(), requestData, e);
            // 发送失败通知
            if (!StringUtils.isEmpty(noticeKeyContent)) {
                sendFailNotice(exportCondition.getApplyExportFileUserId(), noticeKeyContent);
            }
        } finally {
            // 7.删除临时文件
            deleteTempFile(parentPath);
            // 8.释放锁
            unlock(getLockKey(exportCondition));
        }
    }

    public String getErrorMsg(){
        return errorExcelMsg;
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
    public File fileDispose(Boolean isPackage,ExportCondition exportCondition,String filePath,String fileName,List data) throws IOException {
        if (Objects.equals(Boolean.TRUE,isPackage)){
            generateExcelFile(getSubFilePath(filePath), data, exportCondition);
            return compressFile(filePath);
        }else {
            return generateExcelFile(fileName, data, exportCondition);
        }

    }

    /**
     *
     * @param fileSavePath 文件保存路径（包含基础路径）
     */
    private String getSubFilePath(String fileSavePath){
        if (StrUtil.isBlank(fileSavePath)){
            return fileSavePath;
        }
        return fileSavePath.replace(excelSavePath,StrUtil.EMPTY);
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
        return Paths.get(excelSavePath, getUUID()).toString();
    }

    /**
     * 获取文件保存父目录路径
     *
     * @return java.lang.String
     **/
    public String getUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 获取文件保存路径
     *
     * @param parentPath 文件名
     * @param fileName   文件名
     * @return java.lang.String
     **/
    public String getFileSavePath(String parentPath, String fileName) {
       String fileSavePath = Paths.get(parentPath, fileName).toString();
        if(!FileUtil.exist(fileSavePath)){
            FileUtil.mkdir(fileSavePath);
        }
        return fileSavePath;
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
        return ExcelUtil.exportListToExcel(fileName, data, getHeadClass(exportCondition));
    }

    /**
     * 获取Excel表头类
     *
     * @return java.lang.Class
     **/
    public abstract Class getHeadClass(ExportCondition exportCondition);

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
        try {
            // 1.获取文件名
            String fileName = getFileName(exportCondition);
            // 2.获取数据，生成List
            List data = getExcelData(exportCondition);
            // 3.获取文件保存父目录路径
            excelFile = generateExcelFile(fileName, data, exportCondition);
            return resourceFileService.getResourcePath(s3Utils.uploadS3AndGetResourceFile(excelFile.getAbsolutePath(), excelFile.getName()).getId());
        } catch (UtilException | IOException e) {
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

    /**
     * 前置处理
     *
     * @param exportCondition 导出条件
     **/
    public void preProcess(ExportCondition exportCondition) {
        // 有需要前置处理的，重写覆盖该方法
    }

}
