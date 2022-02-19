package com.xiao.diners.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class LoginDinerInfo implements Serializable {

    private String nickname;
    private String token;
    private String avatarUrl;

}