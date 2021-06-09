package com.wupol.myopia.business.api.screening.app.schedules;

import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.MsgTemplateEnum;
import com.wupol.myopia.business.common.utils.util.MsgContentUtil;
import com.wupol.myopia.business.core.school.domain.dto.StudentBasicInfoDTO;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.model.WarningMsg;
import com.wupol.myopia.business.core.screening.flow.service.WarningMsgService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
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

    /**
     * 定时任务发送
     */
    //@Scheduled(cron = "0 0 10 * * *", zone = "GMT+8:00")
    @Scheduled(fixedDelay = 1000000, zone = "GMT+8:00")
    public void sendWarningMsg() {
        //找出需要发送短信的数据
        Set<WarningMsg> warningMsgs = warningMsgService.needNoticeMsg();
        if (CollectionUtils.isEmpty(warningMsgs)) {
            //没有发送消息的任务
            log.info("{} 没有学生的警告短信需要发送.", DateUtil.getNowDateTimeStr());
            return;
        }
        //查找学生名字及对应的电话号码
        Map<Integer, StudentBasicInfoDTO> studentPhonesMap = studentService.getPhonesMap(warningMsgs.stream().map(WarningMsg::getStudentId).collect(Collectors.toSet()));
        if (MapUtils.isEmpty(studentPhonesMap)) {
            //todo 特殊情况： 由于某些原因这些学生id，找不到这些学生都要发送失败.
            return;
        }
        try {
            warningMsgs.stream().forEach(warningMsg -> {
                StudentBasicInfoDTO studentBasicInfoDTO = studentPhonesMap.get(warningMsg.getStudentId());
                String content = MsgContentUtil.getMsgContent(MsgTemplateEnum.TO_PARENTS_WARING_KIDS_VISION, studentBasicInfoDTO.getStudentName());
                List<String> phoneNums = studentBasicInfoDTO.getPhoneNums();
                if (CollectionUtils.isEmpty(phoneNums)) {
                    //todo 特殊情况： 本来有电话被改没了,发送日期改为明天,这样等他有数据的时候,就可以发送了,但是可能会每天改一次
                    return;
                }
                warningMsgService.sengMsgAndUpdateStatus(warningMsg, content, phoneNums);
            //  要拆出来统一修改状态
            });
        } finally {
            //无论为下次新增一条，但是如果根据学生id没有找到数据的话，就不新增了
            warningMsgService.addNewOne(warningMsgs);
        }
    }

}