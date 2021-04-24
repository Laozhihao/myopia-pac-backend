package com.wupol.myopia.business.api.management.domain.dto;

import com.wupol.myopia.business.api.management.domain.vo.UserVO;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 用户查询,查询oauth的
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class UserQueryDTO extends UserVO {
    /** 开始创建时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startCreateTime;
    /** 结束创建时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endCreateTime;
    /** 开始创建时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startLastLoginTime;
    /** 结束创建时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endLastLoginTime;
    /** 角色名 */
    private String roleName;

    public UserDTO convertToOauthUserDTO() {
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(this, userDTO);
        return userDTO;
    }
}
