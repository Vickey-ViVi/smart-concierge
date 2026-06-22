package com.mixc.smartconcierge.dto;

import lombok.Data;

@Data
public class AdminUserSaveRequest {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String role;
    private String phone;
    private String email;
    private Integer status;
}
