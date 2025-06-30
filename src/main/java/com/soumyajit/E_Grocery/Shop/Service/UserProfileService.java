package com.soumyajit.E_Grocery.Shop.Service;

import com.soumyajit.E_Grocery.Shop.DTOS.UserProfileDTOS;
import com.soumyajit.E_Grocery.Shop.DTOS.UserUpdateRequestDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface UserProfileService {
    //UserProfileDTOS updateUserProfile(Map<String, Object> updates, MultipartFile image) throws IOException;

    public UserProfileDTOS getCurrentUserProfile();

    public void updateUserProfile(UserUpdateRequestDTO dto);
}
