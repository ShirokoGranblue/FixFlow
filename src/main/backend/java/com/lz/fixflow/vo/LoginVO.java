package com.lz.fixflow.vo;


import lombok.Data;

@Data
public class LoginVO {

    private String tokenName;

    private String tokenValue;

    private UserVO user;
}
