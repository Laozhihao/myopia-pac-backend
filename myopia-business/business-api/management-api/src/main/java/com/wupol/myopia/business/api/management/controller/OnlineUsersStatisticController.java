package com.wupol.myopia.business.api.management.controller;

import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.management.domain.vo.OnlineUserStatisticVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户在线统计
 *
 * @author hang.yuan 2022/5/10 10:00
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/onlineUsers")
public class OnlineUsersStatisticController {
    @Autowired
    private RedisUtil redisUtil;

    @GetMapping()
    public OnlineUserStatisticVO getOnlineNum(){
        OnlineUserStatisticVO onlineUserStatisticVO= new OnlineUserStatisticVO();
        Set<String> keys = redisUtil.getOnline();
        onlineUserStatisticVO.setManagementClientUser(getClientUser(keys, SystemCode.MANAGEMENT_CLIENT.getCode(), SystemCode.SCREENING_MANAGEMENT_CLIENT.getCode()))
                .setSchoolClientUser(getClientUser(keys, SystemCode.SCHOOL_CLIENT.getCode()))
                .setScreeningClientUser(getClientUser(keys, SystemCode.SCREENING_CLIENT.getCode()))
                .setHospitalClientUser(getClientUser(keys, SystemCode.HOSPITAL_CLIENT.getCode()))
                .setParentClientUser(getClientUser(keys, SystemCode.PARENT_CLIENT.getCode()))
                .setZeroToSixClientUser(getClientUser(keys, SystemCode.PRESCHOOL_CLIENT.getCode()))
                .setQuestionnaireClientUser(getClientUser(keys, SystemCode.QUESTIONNAIRE.getCode()));
        return onlineUserStatisticVO;
    }

    private OnlineUserStatisticVO.OnlineUser getClientUser(Set<String> keys, Integer... clientId) {
        List<String> userList = getUserList(keys, clientId);
        return new OnlineUserStatisticVO.OnlineUser(userList.size(), userList);
    }

    private List<String> getUserList(Set<String> keys, Integer... clientId) {
        if (CollectionUtils.isEmpty(keys)) {
            return Collections.emptyList();
        }
        List<String> userKeyList = keys.stream().filter(key -> Arrays.stream(clientId).anyMatch(x -> key.contains("online:" + x))).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userKeyList)) {
            return Collections.emptyList();
        }
        List<Object> userNameList = redisUtil.batchGet(userKeyList);
        return userNameList.stream().map(Object::toString).collect(Collectors.toList());
    }
}
