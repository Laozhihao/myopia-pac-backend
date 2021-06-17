package com.wupol.myopia.business.api.screening.app.service;

import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.MsgTemplateEnum;
import com.wupol.myopia.business.common.utils.util.MsgContentUtil;
import com.wupol.myopia.business.core.school.domain.dto.StudentBasicInfoDTO;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.model.WarningMsg;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.WarningMsgService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 筛查端App接口
 *
 * @Author Jacob
 * @Date 2021-06-15
 */
@Service
@Slf4j
public class ScreeningVisionMsgService {

    private static final int BEFORE_30_DAYS = -30;

    @Autowired
    private WarningMsgService warningMsgService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private StatConclusionService statConclusionService;

    /**
     * 每天发送警告短信
     */
    public void sendWarningMsg() {
        // 找出需要发送短信的数据
        List<WarningMsg> warningMsgs = warningMsgService.needNoticeMsg();
        if (CollectionUtils.isEmpty(warningMsgs)) {
            //没有发送消息的任务
            log.info("{} 没有学生的警告短信需要重新发送.", DateUtil.getNowDateTimeStr());
            return;
        }
        // 处理短信
        dealMsg(warningMsgs);
        // 更新状态
        warningMsgService.updateBatchById(warningMsgs);
    }


    /**
     * 每隔几天重复推送
     */
    public void repeatNoticeWarningMsg() {
        // 查找发送时间是30天内并且发送成功或者失败的短信
        List<WarningMsg> warningMsgs = warningMsgService.needRepeatNoticeMsg(BEFORE_30_DAYS);
        // 过滤掉正常的数据
        warningMsgs = filterNormalVision(warningMsgs);
        if (CollectionUtils.isEmpty(warningMsgs)) {
            //没有发送消息的任务
            log.info("今天{}没有学生需要重复警告短信需要重新发送.", DateUtil.getNowDateTimeStr());
            return;
        }
        // 处理短信事宜
        dealMsg(warningMsgs);
        // 这里是发送过程完才新增一条
        warningMsgService.addNewOne(warningMsgs);
    }

    /**
     * 过滤掉正常视力的数据
     * @param warningMsgs
     * @return
     */
    private List<WarningMsg> filterNormalVision(List<WarningMsg> warningMsgs) {
        List<Integer> studentIdList = warningMsgs.stream().map(WarningMsg::getStudentId).collect(Collectors.toList());
        Map<Integer, Boolean> studentIdVisionWarningMap = statConclusionService.getByStudentIds(studentIdList);
        return warningMsgs.stream().filter(warningMsg -> studentIdVisionWarningMap.get(warningMsg.getStudentId())).collect(Collectors.toList());
    }

    /**
     * 处理短信
     *
     * @param warningMsgs
     */
    public void dealMsg(List<WarningMsg> warningMsgs) {
        //查找学生名字及对应的电话号码
        Map<Integer, StudentBasicInfoDTO> studentPhonesMap = studentService.getPhonesMap(warningMsgs.stream().map(WarningMsg::getStudentId).collect(Collectors.toSet()));
        for (WarningMsg warningMsg : warningMsgs) {
            warningMsg.setUpdateTime(new Date());
            //先设置今天的天数
            warningMsg.setSendDayOfYear(DateUtil.getDayOfYear(new Date(), 0));
            StudentBasicInfoDTO studentBasicInfoDTO = studentPhonesMap.get(warningMsg.getStudentId());

            if (studentBasicInfoDTO == null) {
                log.warn("发送视力预警短信异常,无法找到该学生,studentId = {}", warningMsg.getStudentId());
                warningMsg.setSendStatus(WarningMsg.STATUS_SEND_CANCEL);
                continue;
            }

            String content = MsgContentUtil.getMsgContent(MsgTemplateEnum.TO_PARENTS_WARING_KIDS_VISION, studentBasicInfoDTO.getStudentName());
            List<String> phoneNums = studentBasicInfoDTO.getPhoneNums();
            if (CollectionUtils.isEmpty(phoneNums)) {
                log.warn("发送视力预警短信的时候没有找到该学生的电话号码,studentId = {}", warningMsg.getStudentId());
                warningMsg.setSendStatus(WarningMsg.STATUS_SEND_CANCEL);
                continue;
            }

            //发送短信
            boolean isFail = warningMsgService.sendMsg(content, phoneNums);
            warningMsg.setSendStatus(isFail ? WarningMsg.STATUS_SEND_FAILURE : WarningMsg.STATUS_SEND_SUCCESS);
            //发送日期: 有发送的,无论发送失败或者都有发送日期
            warningMsg.setSendTime(new Date());
        }
    }
}