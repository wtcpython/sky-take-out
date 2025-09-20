package com.wtc.service;

import com.wtc.dto.UserLoginDTO;
import com.wtc.entity.User;

public interface UserService {
    User wxLogin(UserLoginDTO userLoginDTO);
}
