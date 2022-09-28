package com.wupol.myopia.business.api.management.controller;

import com.alibaba.fastjson.JSONObject;
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

    /**
     * 获取各个端在线用户信息
     *
     * @return com.wupol.myopia.business.api.management.domain.vo.OnlineUserStatisticVO
     **/
    @GetMapping()
    public OnlineUserStatisticVO getOnlineUser(){
        OnlineUserStatisticVO onlineUserStatisticVO= new OnlineUserStatisticVO();
        Set<String> keys = redisUtil.getOnline();
        onlineUserStatisticVO.setManagementClientUser(getClientOnlineUser(keys, SystemCode.MANAGEMENT_CLIENT.getCode(), SystemCode.SCREENING_MANAGEMENT_CLIENT.getCode()))
                .setSchoolClientUser(getClientOnlineUser(keys, SystemCode.SCHOOL_CLIENT.getCode()))
                .setScreeningClientUser(getClientOnlineUser(keys, SystemCode.SCREENING_CLIENT.getCode()))
                .setHospitalClientUser(getClientOnlineUser(keys, SystemCode.HOSPITAL_CLIENT.getCode()))
                .setParentClientUser(getClientOnlineUser(keys, SystemCode.PARENT_CLIENT.getCode()))
                .setZeroToSixClientUser(getClientOnlineUser(keys, SystemCode.PRESCHOOL_CLIENT.getCode()))
                .setQuestionnaireClientUser(getClientOnlineUser(keys, SystemCode.QUESTIONNAIRE.getCode()))
                .setNoTokenAccessUser(getClientOnlineUser(keys, -1));
        return onlineUserStatisticVO;
    }

    /**
     * 获取各个端在线用户
     *
     * @param keys      缓存key
     * @param clientId
     * @return com.wupol.myopia.business.api.management.domain.vo.OnlineUserStatisticVO.OnlineUser
     **/
    private OnlineUserStatisticVO.OnlineUser getClientOnlineUser(Set<String> keys, Integer... clientId) {
        List<String> userList = getOnlineUserList(keys, clientId);
        return new OnlineUserStatisticVO.OnlineUser(userList.size(), userList);
    }

    /**
     * 获取各个端在线用户列表
     *
     * @param keys
     * @param clientId
     * @return java.util.List<java.lang.String>
     **/
    private List<String> getOnlineUserList(Set<String> keys, Integer... clientId) {
        if (CollectionUtils.isEmpty(keys)) {
            return Collections.emptyList();
        }
        List<String> userKeyList = keys.stream().filter(key -> Arrays.stream(clientId).anyMatch(x -> key.contains("online:" + x))).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userKeyList)) {
            return Collections.emptyList();
        }
        List<Object> userNameList = redisUtil.batchGet(userKeyList);
        if (CollectionUtils.isEmpty(userNameList)) {
            return Collections.emptyList();
        }
        return userNameList.stream().map(JSONObject::toJSONString).collect(Collectors.toList());
    }
}
