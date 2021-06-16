package com.wupol.myopia.business.api.screening.app.schedules;

import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.MsgTemplateEnum;
import com.wupol.myopia.business.common.utils.util.MsgContentUtil;
import com.wupol.myopia.business.core.school.domain.dto.StudentBasicInfoDTO;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.WarningMsg;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.WarningMsgService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 医院-定时任务
 *
 * @author Jacob
 * @date 2021-06-08
 */
@Component
@Slf4j
public class SendWarningMsgScheduledTask {

    @Autowired
    private WarningMsgService warningMsgService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private StatConclusionService statConclusionService;

    /**
     * 昨天的异常vision,今天进行短信提醒;
     */
    @Scheduled(cron = "0 0 10 * * *", zone = "GMT+8:00")
    public void sendWarningMsg() {
        // 找出需要发送短信的数据
        List<WarningMsg> warningMsgs = warningMsgService.needNoticeMsg();
        // 处理短信
        dealMsg(warningMsgs);
        // 更新状态
        warningMsgService.updateBatchById(warningMsgs);
    }


    /**
     * 每天检查30天前接受到异常提醒的学生的数据是否需要重新推送
     */
    @Scheduled(cron = "0 0 11 * * *", zone = "GMT+8:00")
    public void repeatNoticeWarningMsg() {
        //查找发送时间是30天内并且发送成功或者失败的短信
        List<WarningMsg> warningMsgs = warningMsgService.needRepeatNoticeMsg(-30);
        // 查询目前是否是正常的
        List<Integer> studentIdList = warningMsgs.stream().map(WarningMsg::getStudentId).collect(Collectors.toList());
        List<Student> studentList = studentService.getByIds(studentIdList);
        Map<Integer, StatConclusion> studentIdStatconclusion = statConclusionService.getByStudentIds(studentIdList);
        //todo warningMsgs进行过滤掉已经正常的.
        // 处理短信事宜
        dealMsg(warningMsgs);
        // 这里是发送过程完才新增一条呢
        warningMsgService.addNewOne(warningMsgs);
    }

    /**
     * 处理短信
     *
     * @param warningMsgs
     */
    private void dealMsg(List<WarningMsg> warningMsgs) {
        //过滤掉已经是正常的,或者没有电话号码的
        if (CollectionUtils.isEmpty(warningMsgs)) {
            //没有发送消息的任务
            log.info("{} 没有学生的警告短信需要重新发送.", DateUtil.getNowDateTimeStr());
            return;
        }
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
            //发送日期: 有发送的,无论发送失败或者都有发送日志
            warningMsg.setSendTime(new Date());
        }
    }

}