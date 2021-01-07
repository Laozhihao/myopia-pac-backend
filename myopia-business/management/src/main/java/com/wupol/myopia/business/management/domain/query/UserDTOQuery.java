package com.wupol.myopia.business.management.domain.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.dto.UserExtDTO;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 用户查询,查询oauth的
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
@Data
@Accessors(chain = true)
public class UserDTOQuery extends UserDTO {

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
}
