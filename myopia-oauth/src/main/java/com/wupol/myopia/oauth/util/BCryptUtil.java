package com.wupol.myopia.oauth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @Author HaoHao
 * @Date 2020/12/25
 **/
public class BCryptUtil {
    public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder().encode("123456"));
        System.out.println(new BCryptPasswordEncoder().encode("123456"));
    }
}
