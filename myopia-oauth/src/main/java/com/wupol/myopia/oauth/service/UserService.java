package com.wupol.myopia.oauth.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.oauth.domain.model.User;
import com.wupol.myopia.oauth.domain.mapper.UserMapper;
import com.wupol.myopia.base.service.BaseService;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class UserService extends BaseService<UserMapper, User> {

    /**
     * 根据用户名查询
     *
     * @param username
     * @param systemCode
     * @return com.wupol.myopia.oauth.domain.model.User
     **/
    public User getByUsername(String username, Integer systemCode) {
        try {
            return findOne(new User().setUsername(username).setSystemCode(systemCode));
        } catch (IOException e) {
            throw new BusinessException("获取用户异常", e);
        }
    }
}
