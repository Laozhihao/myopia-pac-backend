package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.model.DataCommit;
import com.wupol.myopia.business.management.exception.UploadException;
import com.wupol.myopia.business.management.service.DataCommitService;
import com.wupol.myopia.business.management.util.TwoTuple;
import com.wupol.myopia.business.management.util.UploadUtil;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.Date;
import java.util.Objects;

/**
 * 公共的API接口
 *
 * @author Alix
 * @Date 2021-02-03
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/common")
public class CommonController extends BaseController<DataCommitService, DataCommit> {
//    /**
//     * 档案视图, 手工上传图片 TODO 前端需要修改
//     */
//    @PostMapping("/fileViewUpload")
//    public void fileViewUpload(MultipartFile file) throws AccessDeniedException {
//        CurrentUser user = CurrentUserUtil.getCurrentUser();
//        if (Objects.isNull(user)) {
//            throw new AccessDeniedException("请先登录");
//        }
//        try {
//            String savePath = systemConfigService.getConfig(SystemConfigKey.IMAGE_SAVE_PATH);
//            TwoTuple<String, String> uploadToServerResults = UploadUtil.upload(file, savePath);
//            String originalFilename = uploadToServerResults.getFirst();
//            String tempPath = uploadToServerResults.getSecond();
//            // 判断上传的文件是否图片或者PDF
//            String allowExtension = "pdf," + systemConfigService.getConfig(SystemConfigKey.IMAGE_SUFFIXS);
//            UploadUtil.validateFileIsAllowed(file, allowExtension.split(","));
//
//            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
//            // 普通影像上传逻辑
//            UploadImageResultVo uploadImageResultVo = patientFileUploadFacade.manualUpload(tempPath, originalFilename, patientId, studyDate, userProfileVo, imageType);
//            // 判断是否能触发AI
//
//            if (aiFacade.isCanTriggerAI(userProfileVo.getHospitalId())) {
//                if (ImageType.isEyeGround(uploadImageResultVo.getImageType())) {
//                    aiFacade.triggerAI(department.getServiceType(), department.getBiz(), uploadImageResultVo.getImage());
//                } else if (ImageType.ULTRA_WIDE.getValue().equals(uploadImageResultVo.getImageType())) {
//                    aiFacade.triggerAI(department.getUwfServiceType(), department.getBiz(), uploadImageResultVo.getImage());
//                }
//            }
//        } catch (Exception e) {
//            throw new UploadException("文件上传失败", e);
//        }
//    }
}
