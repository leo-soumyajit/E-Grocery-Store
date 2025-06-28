package com.soumyajit.E_Grocery.Shop.Security.OTPServiceAndValidation;

import com.soumyajit.E_Grocery.Shop.Advices.ApiError;
import com.soumyajit.E_Grocery.Shop.Advices.ApiResponse;
import com.soumyajit.E_Grocery.Shop.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController()
@RequestMapping("/otp")
@RequiredArgsConstructor
public class OtpAuthController {

    private final OtpService otpService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<String>> sendOTP(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email").trim(); // Extract and trim the email address
            String otp = otpService.generateOTP();
            otpService.saveOTP(email, otp);
            otpService.sendOTPEmail(email, otp); // This will use your existing email sending logic
            return ResponseEntity.ok(new ApiResponse<>("OTP sent successfully. Check your gmail"));
        } catch (Exception e) {
            ApiError apiError = ApiError.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Failed to send OTP")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(apiError));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOTP(@RequestBody OTPVerificationRequest request) {
        try {
            OTPDetails otpDetails = otpService.getOTPDetails(request.getEmail());
            if (otpDetails == null || !otpDetails.getOtp().equals(request.getOtp())) {
                ApiError apiError = ApiError.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message("Invalid OTP")
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(apiError));
            }
            if (otpService.isOTPExpired(otpDetails.getTimestamp())) {
                ApiError apiError = ApiError.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message("OTP has expired")
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(apiError));
            }
            otpService.markOTPVerified(request.getEmail());
            return ResponseEntity.ok(new ApiResponse<>("OTP verified successfully. Now you can signup your email"));
        } catch (Exception e) {
            ApiError apiError = ApiError.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Failed to verify OTP")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(apiError));
        }
    }



}
