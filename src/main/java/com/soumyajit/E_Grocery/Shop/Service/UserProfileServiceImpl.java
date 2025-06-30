package com.soumyajit.E_Grocery.Shop.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.soumyajit.E_Grocery.Shop.DTOS.UserProfileDTOS;
import com.soumyajit.E_Grocery.Shop.DTOS.UserUpdateRequestDTO;
import com.soumyajit.E_Grocery.Shop.Entities.User;
import com.soumyajit.E_Grocery.Shop.Exception.ResourceNotFound;
import com.soumyajit.E_Grocery.Shop.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public void updateUserProfile(UserUpdateRequestDTO dto) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }

        if (dto.getMob_no() != null) {
            user.setMob_no(dto.getMob_no());
        }

        userRepository.save(user); // no need to return anything
    }




    @Override
    public UserProfileDTOS getCurrentUserProfile() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return modelMapper.map(user, UserProfileDTOS.class);
    }


}
