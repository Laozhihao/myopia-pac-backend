package com.wupol.myopia.business.api.preschool.app.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.core.hospital.domain.dto.ReceiptDTO;
import com.wupol.myopia.business.core.hospital.domain.model.PreschoolCheckRecord;
import com.wupol.myopia.business.core.hospital.domain.model.ReceiptList;
import com.wupol.myopia.business.core.hospital.service.PreschoolCheckRecordService;
import com.wupol.myopia.business.core.hospital.service.ReceiptListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 回执单
 * @Author wulizhou
 * @Date 2022/1/4 20:20
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/preschool/app/receipt")
@Slf4j
public class ReceiptController {

    @Autowired
    private ReceiptListService receiptListService;

    @Autowired
    private PreschoolCheckRecordService preschoolCheckRecordService;

    /**
     * 获取回执单详情
     * @param preschoolCheckRecordId
     * @return
     */
    @GetMapping("/{preschoolCheckRecordId}")
    public ReceiptDTO getDetail(@PathVariable("preschoolCheckRecordId") Integer preschoolCheckRecordId) {
        return receiptListService.getDetailByHospitalAndPreschoolCheckRecordId(CurrentUserUtil.getCurrentUser().getOrgId(), preschoolCheckRecordId);
    }

    /**
     * 获取编辑详情，即回执单内容+最新专项检查结果
     * @param preschoolCheckRecordId
     * @return
     */
    @GetMapping("/edit/{preschoolCheckRecordId}")
    public ReceiptDTO getEditDetail(@PathVariable("preschoolCheckRecordId") Integer preschoolCheckRecordId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return receiptListService.getEditDetailByHospitalAndPreschoolCheckRecordId(user.getOrgId(), preschoolCheckRecordId, user.getId());
    }

    /**
     * 保存转诊单
     * @param receiptList
     * @return
     */
    @PostMapping()
    public Integer saveOrUpdate(@RequestBody @Valid ReceiptList receiptList) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        PreschoolCheckRecord preschoolCheckRecord = preschoolCheckRecordService.checkOrgOperation(user.getOrgId(), receiptList.getPreschoolCheckRecordId());
        receiptList.setStudentId(preschoolCheckRecord.getStudentId());
        return receiptListService.saveOrUpdateReceiptList(receiptList, user);
    }

}
