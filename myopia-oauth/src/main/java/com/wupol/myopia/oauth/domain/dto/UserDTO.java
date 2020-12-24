package com.wupol.myopia.oauth.domain.dto;

import com.wupol.myopia.oauth.domain.model.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020/12/24
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class UserDTO extends User {
    private String clientId;
    private List<Integer> roles;
}
