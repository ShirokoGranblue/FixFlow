package com.lz.fixflow.vo;


import lombok.Data;

@Data
public class LoginVO {

    private String satokenName;

    private String satokenValue;

    private UserVO user;
}
