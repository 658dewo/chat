package com.example.chat.model;

import lombok.Data;

@Data
public class User {
    private String userId;
    private String username;
    private String password;
    private boolean online;
}