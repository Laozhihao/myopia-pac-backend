package com.wupol.myopia.oauth.sdk.domain.request;

import com.wupol.myopia.oauth.sdk.domain.response.Role;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2021/1/27
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class RoleDTO extends Role {
    /**
     * 所属部门ID集
     */
    private List<Integer> orgIds;

    /**
     * 当前页码
     **/
    private Integer current;

    /**
     * 每页条数
     **/
    private Integer size;

    /**
     * 角色类型集合
     */
    private List<Integer> roleTypes;
}
