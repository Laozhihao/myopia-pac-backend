package com.wupol.myopia.oauth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.oauth.domain.dto.UserDTO;
import com.wupol.myopia.oauth.domain.model.User;
import com.wupol.myopia.oauth.domain.mapper.UserMapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.oauth.domain.model.UserRole;
import com.wupol.myopia.oauth.domain.model.UserWithRole;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class UserService extends BaseService<UserMapper, User> {

    @Autowired
    private UserRoleService userRoleService;

    /**
     * 根据用户名查询
     *
     * @param username   用户名
     * @param systemCode 系统编号
     * @return com.wupol.myopia.oauth.domain.model.User
     **/
    public User getByUsername(String username, Integer systemCode) {
        try {
            return findOne(new User().setUsername(username).setSystemCode(systemCode));
        } catch (IOException e) {
            throw new BusinessException("获取用户异常", e);
        }
    }

    /**
     * 分页查询用户
     *
     * @param queryParam 查询参数
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.wupol.myopia.oauth.domain.model.UserWithRole>
     **/
    public IPage<UserWithRole> getUserListPage(UserDTO queryParam) {
        if (Objects.isNull(queryParam.getCurrent()) || Objects.isNull(queryParam.getSize())) {
            throw new BusinessException("页码或页数为空");
        }
        Page<UserWithRole> page = new Page<>(queryParam.getCurrent(), queryParam.getSize());
        return baseMapper.selectUserListWithRole(page, queryParam);
    }

    public void addUser(UserDTO userDTO) {
        // TODO: 判断部门ID有效性——是否属于当前用户所属部门或其下面的部门（admin则无限制）
        // TODO: 角色ID有效性判断——是否都存在该角色、且是在所属部门下的（admin则无限制）

        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        save(user.setPassword(new BCryptPasswordEncoder().encode(userDTO.getPassword())));
        // 绑定角色
        userDTO.getRoleIds().forEach(x -> userRoleService.save(new UserRole().setUserId(user.getId()).setRoleId(x)));
    }
}
