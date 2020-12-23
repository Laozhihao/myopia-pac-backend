package com.wupol.myopia.oauth.service;

import com.wupol.myopia.oauth.domain.model.User;
import com.wupol.myopia.oauth.domain.mapper.UserMapper;
import com.wupol.myopia.base.service.BaseService;
import org.springframework.stereotype.Service;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class UserService extends BaseService<UserMapper, User> {

}
