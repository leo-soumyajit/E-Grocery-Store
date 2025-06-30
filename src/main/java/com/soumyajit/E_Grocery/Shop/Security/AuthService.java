package com.soumyajit.E_Grocery.Shop.Security;

import com.soumyajit.E_Grocery.Shop.DTOS.LoginDTOS;
import com.soumyajit.E_Grocery.Shop.DTOS.LoginResponseDTO;
import com.soumyajit.E_Grocery.Shop.DTOS.SignUpRequestDTOS;
import com.soumyajit.E_Grocery.Shop.DTOS.UserDTOS;
import com.soumyajit.E_Grocery.Shop.Entities.Roles;
import com.soumyajit.E_Grocery.Shop.Entities.User;
import com.soumyajit.E_Grocery.Shop.Exception.ResourceNotFound;
import com.soumyajit.E_Grocery.Shop.Repository.UserRepository;
import com.soumyajit.E_Grocery.Shop.Security.OTPServiceAndValidation.OtpService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final OtpService otpService;
    @Autowired
    private JavaMailSender mailSender;

    //signup function with otp validation
    public UserDTOS signUp(SignUpRequestDTOS signUpRequestDTOS){
        Optional<User> user = userRepository.findByEmail(signUpRequestDTOS.getEmail());
        if (user.isPresent()) {
            throw new BadCredentialsException("User with this Email is already present");
        }

        // Check if OTP was verified
        if (!otpService.isOTPVerified(signUpRequestDTOS.getEmail())) {
            throw new BadCredentialsException("OTP not verified");
        }

        User newUser = modelMapper.map(signUpRequestDTOS, User.class);
        newUser.setRoles(Set.of(Roles.USER).toString());
        newUser.setPassword(passwordEncoder.encode(signUpRequestDTOS.getPassword()));
        User savedUser = userRepository.save(newUser);

        // Clear OTP verification status after successful signup
        otpService.clearOTPVerified(signUpRequestDTOS.getEmail());

        sendWelcomeEmail(signUpRequestDTOS);
        return modelMapper.map(savedUser, UserDTOS.class);
    }

    public LoginResponseDTO login(LoginDTOS loginDTOS){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTOS.getEmail(), loginDTOS.getPassword())
        );

        User userEntities = (User) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(userEntities);
        String refreshToken = jwtService.generateRefreshToken(userEntities);

        return new LoginResponseDTO(userEntities.getId(), accessToken, refreshToken);
    }

    public String refreshToken(String refreshToken) {
        Long uerId = jwtService.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(uerId)
                .orElseThrow(() ->
                        new ResourceNotFound("User not found with id : " + uerId));
        return jwtService.generateAccessToken(user);
    }
    @Async
    private void sendWelcomeEmail(SignUpRequestDTOS signUpRequestDTOS) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(signUpRequestDTOS.getEmail());
            helper.setSubject("🎉 Welcome to ISA HIT Student Chapter!");

            String name = signUpRequestDTOS.getName();

            String plainText = "Welcome to E-Grocery, " + name + "!\n\n"
                    + "We're excited to have you onboard.\n"
                    + "Enjoy smooth grocery shopping, access real-time discounts, and Start managing your monthly ration list easily.\n\n"
                    + "Happy shopping!\n"

                    + "— The E-Grocery Team";

            String htmlContent = buildStyledWelcomeEmail(name);

            helper.setText(plainText, htmlContent);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildStyledWelcomeEmail(String name) {
        return """
    <div style="font-family: 'Segoe UI', sans-serif; background-color: #f5f5f5; padding: 20px;">
        <div style="max-width: 600px; margin: auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.05);">
            <h2 style="color: #2c3e50; text-align: center;">👋 Welcome to E‑Grocery, %s!</h2>
            
            <p style="font-size: 16px; color: #333333; line-height: 1.6;">
                We're excited to have you onboard!
                "Enjoy smooth grocery shopping, access real-time discounts, and Start managing your monthly ration list easily.
            </p>
            
            <p style="font-size: 16px; color: #333333; line-height: 1.6;">
                🚀 Enjoy real-time discounts, smart cart management, and quick checkout and start by building your first ration list today and simplify your monthly essentials.
            </p>
            
            <div style="text-align: center; margin: 30px 0;">
                <a href="https://yourapp.com/ration" 
                   style="background-color: #28a745; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                    🛒 Start shopping now 
                </a>
            </div>
            
            <hr style="margin: 30px 0; border: none; border-top: 1px solid #eee;" />
            
            <p style="font-size: 14px; color: #777777; text-align: center;">
                If you have any questions or need help, just reply to this email or visit our support page.
                <br/><br/>
                🛒 Happy shopping!<br/>
                — The E‑Grocery Team
            </p>
        </div>
    </div>
    """.formatted(name);
    }


}
