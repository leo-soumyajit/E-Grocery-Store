package com.soumyajit.E_Grocery.Shop.Security.OTPServiceAndValidation;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.util.HashMap;
import java.util.Map;

@Service
public class OtpService {

    @Autowired
    private JavaMailSender emailSender;

    private final Map<String, OTPDetails> otpStorage = new HashMap<>();
    private final Map<String, Boolean> otpVerified = new HashMap<>();

    public String generateOTP() {
        int otp = (int) (Math.random() * 9000) + 1000;
        return String.valueOf(otp);
    }

    @Async
    public void sendOTPEmail(String email, String otp) {
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();

            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("newssocialmedia2025@gmail.com");
            helper.setTo(email);
            helper.setSubject("🔐 Your OTP Code - E-Grocery");

            String plainText = "Hi there,\n\n"
                    + "Your one-time password (OTP) is: " + otp + "\n"
                    + "Please use this code to complete your verification.\n"
                    + "Note: This OTP is valid for only 10 minutes.\n\n"
                    + "Thanks,\n"
                    + "Team E-Grocery";

            String htmlContent = buildStyledOtpEmail(otp);

            helper.setText(plainText, htmlContent);

            emailSender.send(message);
            System.out.println("OTP sent successfully. Check your inbox.");
        } catch (MessagingException e) {
            System.err.println("Failed to send OTP: " + e.getMessage());
            e.printStackTrace();
        } catch (jakarta.mail.MessagingException e) {
            System.err.println("Invalid email address: " + e.getMessage());
        }
    }

    private String buildStyledOtpEmail(String otp) {
        return """
        <div style="font-family: 'Segoe UI', sans-serif; background-color: #f7faff; padding: 20px;">
            <div style="max-width: 600px; margin: auto; background-color: #ffffff; padding: 30px; border-radius: 10px;
                        box-shadow: 0 0 10px rgba(0,0,0,0.05);">
                <h2 style="color: #2a7ae2; text-align: center;">🔐 OTP Verification - E-Grocery</h2>
                <p style="font-size: 16px; color: #333333; text-align: center;">
                    Hello,<br/><br/>
                    Use the OTP below to verify your email address and continue using our services.
                </p>
                <div style="margin: 30px auto; text-align: center;">
                    <span style="font-size: 24px; font-weight: bold; color: #1a237e;
                                 padding: 12px 24px; background-color: #e3f2fd;
                                 border: 2px dashed #1a237e; border-radius: 8px;">
                        %s
                    </span>
                </div>
                <p style="font-size: 14px; color: #555555; text-align: center;">
                    ⏳ This OTP is valid for only <strong>10 minutes</strong>.<br/>
                    Do not share this code with anyone.
                </p>
                <hr style="margin-top: 30px; border: none; border-top: 1px solid #ddd;" />
                <p style="font-size: 12px; color: #888888; text-align: center; margin-top: 20px;">
                    Thank you for choosing E-Grocery! <br/>
                    © 2025 E-Grocery. All rights reserved.
                </p>
            </div>
        </div>
        """.formatted(otp);
    }


    public void saveOTP(String email, String otp) {
        otpStorage.put(email, new OTPDetails(otp, System.currentTimeMillis()));
        otpVerified.put(email, false); // Set OTP verification status to false
    }

    public OTPDetails getOTPDetails(String email) {
        return otpStorage.get(email);
    }

    public void deleteOTP(String email) {
        otpStorage.remove(email);
        // Do not remove verification status here to retain it for signup
    }

    public void clearOTPVerified(String email) {
        otpVerified.remove(email); // Clear OTP verification status
    }

    public boolean isOTPExpired(long timestamp) {
        long currentTime = System.currentTimeMillis();
        return (currentTime - timestamp) > (10 * 60 * 1000); // 10 minutes in milliseconds
    }

    public void markOTPVerified(String email) {
        otpVerified.put(email, true); // Mark OTP as verified
    }

    public boolean isOTPVerified(String email) {
        return otpVerified.getOrDefault(email, false);
    }
}
//<h2 style="color: #003366; text-align: center;">✅ OTP Verification</h2>