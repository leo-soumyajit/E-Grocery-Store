package com.soumyajit.E_Grocery.Shop.Controller;

import com.soumyajit.E_Grocery.Shop.DTOS.LoginDTOS;
import com.soumyajit.E_Grocery.Shop.DTOS.LoginResponseDTO;
import com.soumyajit.E_Grocery.Shop.DTOS.SignUpRequestDTOS;
import com.soumyajit.E_Grocery.Shop.DTOS.UserDTOS;
import com.soumyajit.E_Grocery.Shop.Security.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${deploy.env}")
    private String deployEnv;
    private final AuthService authService;


    @PostMapping("/signup")
    public ResponseEntity<UserDTOS> signUp(@Valid @RequestBody SignUpRequestDTOS signUpRequestDTOS){
        return new ResponseEntity<>(authService.signUp(signUpRequestDTOS), HttpStatus.CREATED);
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTOS loginDTOS ,
                                                  HttpServletRequest request ,
                                                  HttpServletResponse response){
        LoginResponseDTO loginResponseDTO =  authService.login(loginDTOS);
        Cookie cookie = new Cookie("refreshToken", loginResponseDTO.getRefreshToken());
        cookie.setHttpOnly(true);

        cookie.setSecure("production".equals(deployEnv)); // when its on production mode it is true else development mode its false

        response.addCookie(cookie);
        return ResponseEntity.ok(loginResponseDTO);
    }


    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(HttpServletRequest request){
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(()->new AuthenticationServiceException("refreshToken Not Found inside the Cookies"));

        String newAccessToken =  authService.refreshToken(refreshToken);
        return ResponseEntity.ok(new LoginResponseDTO(newAccessToken));
    }



}
