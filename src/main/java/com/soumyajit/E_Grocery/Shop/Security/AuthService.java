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

    private void sendWelcomeEmail(SignUpRequestDTOS signUpRequestDTOS) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(signUpRequestDTOS.getEmail());
            helper.setSubject("🎉 Welcome to ISA HIT Student Chapter!");

            String name = signUpRequestDTOS.getName();

            String plainText = "Welcome to our website, dear " + name + ".\n\n"
                    + "We are excited to have you onboard.\n"
                    + "ISA & ISOI HIT SC";

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
    <div style="font-family: 'Segoe UI', sans-serif; background-color: #f4f8fc; padding: 20px;">
        <div style="max-width: 600px; margin: auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
            <div style="text-align: center; margin-bottom: 10px;">
               <img src="https://res.cloudinary.com/dcrmg4j1l/image/upload/v1743970965/isalogo-removebg-preview_h5wtkm.png"
                         alt="ISA Logo" style="width: 60px; height: auto;" />
            </div>
           <hr style="margin-top: 30px; border: none; border-top: 1px solid #ccc;" />
            <h2 style="color: #003366; text-align: center;">🎓 Welcome to ISA HIT Student Chapter 🎓</h2>
            <p style="font-size: 16px; color: #333333; text-align: center;">
                Dear <strong>%s</strong>,<br/><br/>
                🎉 We're thrilled to have you with us!<br/>
                🌟 Start exploring and engaging with our awesome community.
            </p>
            <div style="text-align: center; margin: 30px 0;">
                <a href="https://www.linkedin.com/company/isa-hit-student-chapter/posts/?feedView=all" style="background-color: #004080; color: #ffffff; padding: 12px 20px; text-decoration: none; border-radius: 5px;">👉 Get Started</a>
            </div>
            <hr style="margin-top: 30px; border: none; border-top: 1px solid #ccc;" />
            <p style="font-size: 12px; color: #888888; text-align: center; margin-top: 10px;">
                 ISA & ISOI HIT SC | 📍 Haldia, West Bengal
            </p>
            <div style="text-align: center; margin-top: 10px;">
                <a href="https://www.instagram.com" style="margin: 0 8px;">
                    <img src="https://cdn-icons-png.flaticon.com/512/1384/1384063.png" width="18" alt="Instagram" />
                </a>
                <a href="https://www.facebook.com" style="margin: 0 8px;">
                    <img src="https://cdn-icons-png.flaticon.com/512/1384/1384053.png" width="18" alt="Facebook" />
                </a>
                <a href="https://www.linkedin.com" style="margin: 0 8px;">
                    <img src="https://cdn-icons-png.flaticon.com/512/145/145807.png" width="18" alt="LinkedIn" />
                </a>
            </div>
        </div>
    </div>
    """.formatted(name);
    }

}
