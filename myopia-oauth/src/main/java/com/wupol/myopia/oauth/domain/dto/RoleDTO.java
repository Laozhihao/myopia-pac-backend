package com.wupol.myopia.oauth.domain.dto;

import com.wupol.myopia.oauth.domain.model.Role;
import lombok.Data;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2021/1/27
 **/
@Data
public class RoleDTO extends Role {
    /**
     * 所属部门ID集
     */
    private List<Integer> orgIds;
}
