package com.wupol.myopia.business.core.screening.organization.domain.dto;

import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

/**
 * 用户表扩展类
 *
 * @author Simple4H
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class ScreeningOrgStaffUserDTO extends UserDTO {

    public ScreeningOrgStaffUserDTO(User user) {
        BeanUtils.copyProperties(user, this);
    }
    /**
     * 筛查机构人员表id
     */
    private Integer staffId;

    /**
     * 签名图片
     */
    private String signFileUrl;

}
