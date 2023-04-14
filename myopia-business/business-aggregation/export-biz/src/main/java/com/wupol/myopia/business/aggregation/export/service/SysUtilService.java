package com.wupol.myopia.business.aggregation.export.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.CurrentUserUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2022/01/04/14:45
 * @Description:
 */
@Service
public class SysUtilService {

    /**
     * 次数扩展字段
     */
    private static final String COUNT = "count";
    /**
     * 每天下载次数
     */
    private static final  int CALL_COUNT = 2;

    private final RedisUtil redisUtil;

    public SysUtilService(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    /***
     * @Description: 非平台管理员一天只能下载2次
     * @Param: [key]
     * @return: void
     * @Author: 钓猫的小鱼
     * @Date: 2022/1/17
     */
    public void isNoPlatformRepeatExport(String key, String lockKey,Integer classId) {
        if (Objects.nonNull(classId)){
            return;
        }
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (currentUser.isPlatformAdminUser()) {
            return;
        }
        if (Objects.equals(currentUser.getClientId(), SystemCode.SCHOOL_CLIENT.getCode().toString())) {
            return;
        }
        if (currentUser.isOverviewUser()) {
            return;
        }
        isExport(key, lockKey);
    }

    /**
    * @Description: 查看用户是否多次导出，
    * @Param: [key值, 参数结果]
    * @return: boolean true:允许导出  false：禁止导出
    * @Author: 钓猫的小鱼
    * @Date: 2022/1/4
    */
    public void isExport(String key, String localKey){
        Object object = redisUtil.get(key);

        if (Objects.isNull(object)) {
            Map<String, Integer> param  = new HashMap<>(2);
            param.put(COUNT, 1);
            redisUtil.cSet(key,param);
            return;
        }
        Map<String, Integer> result = JSON.parseObject(JSON.toJSONString(object), new TypeReference<Map<String, Integer>>(){});
        int count = result.get(COUNT);
        if (count >= CALL_COUNT){
            if (Objects.nonNull(redisUtil.get(localKey))) {
                redisUtil.del(localKey);
            }
            throw new BusinessException("今天的次数已用完，请明天再操作！！！");
        }
        count = count + 1;
        result.put(COUNT, count);
        redisUtil.cSet(key, result);
    }

}
