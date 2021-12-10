package com.wupol.myopia.oauth.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.oauth.domain.mapper.UserRoleMapper;
import com.wupol.myopia.oauth.domain.model.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户及对应的角色
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class UserRoleService extends BaseService<UserRoleMapper, UserRole> {

}
