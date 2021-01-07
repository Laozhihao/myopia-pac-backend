package com.wupol.myopia.oauth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.util.BeanCopyUtil;
import com.wupol.myopia.oauth.domain.model.User;
import com.wupol.myopia.oauth.domain.model.UserRole;
import com.wupol.myopia.oauth.domain.mapper.UserRoleMapper;
import com.wupol.myopia.base.service.BaseService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户及对应的角色
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class UserRoleService extends BaseService<UserRoleMapper, UserRole> {


    /**
     * 更新该用户对应的角色
     * @param userId    用户id
     * @param newRoleIds   角色id数组
     * @return  是否成功
     */
    public Boolean updateByRoleIds(Integer userId, List<Integer> newRoleIds) throws Exception {
        if (Objects.isNull(newRoleIds)) {
            return false;
        }
        // 获取已存在的角色
        List<Integer> existRoleList = baseMapper.selectList(new QueryWrapper<UserRole>().eq("user_id", userId))
                .stream().map(UserRole::getRoleId).collect(Collectors.toList());
        // 对比已存在和传入的角色, 获取交集.
        Collection<Integer> sameRoleList = CollectionUtils.intersection(existRoleList, newRoleIds);
        // 获取需要删除的
        existRoleList.removeAll(sameRoleList);
        // 获取需要增加的
        List<Integer> addRoleIds = BeanCopyUtil.deepCopyListProperties(newRoleIds, Integer.class);
        addRoleIds.removeAll(sameRoleList);
        if (!CollectionUtils.isEmpty(existRoleList) && !baseMapper.deleteByRoleIds(userId, existRoleList)) {
            throw new Exception("删除该用户的角色失败");
        }
        if (!CollectionUtils.isEmpty(addRoleIds)) {
            List<UserRole> userRoleList = addRoleIds.stream().map(item -> new UserRole(userId, item)).collect(Collectors.toList());
            if (!baseMapper.insertBatch(userRoleList))
            throw new Exception("增加该用户的角色失败");
        }
        return true;
    }

}
