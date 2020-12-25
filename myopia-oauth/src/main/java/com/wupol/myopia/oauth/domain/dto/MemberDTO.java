package com.wupol.myopia.oauth.domain.dto;

import com.wupol.myopia.oauth.domain.model.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author HaoHao
 * @Date 2020/12/24
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class MemberDTO extends User {
    private String clientId;
    private String avatar;
    private String nickname;

}
