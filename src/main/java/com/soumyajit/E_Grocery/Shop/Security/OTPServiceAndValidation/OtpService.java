package com.soumyajit.E_Grocery.Shop.Security.OTPServiceAndValidation;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

    public void sendOTPEmail(String email, String otp) {
        try {
            // Validate email address format
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();

            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("newssocialmedia2025@gmail.com");
            helper.setTo(email);
            helper.setSubject("🔐 Your OTP Code");

            // Plain text fallback
            String plainText = "Your OTP code is: " + otp + "\nNote: This OTP is valid for only 10 minutes.";

            // Styled HTML content
            String htmlContent = buildStyledOtpEmail(otp);

            helper.setText(plainText, htmlContent); // set both plain text and HTML

            emailSender.send(message);
            System.out.println("OTP sent successfully. Check your Gmail.");
        } catch (MessagingException e) {
            System.err.println("Failed to send OTP: " + e.getMessage());
            e.printStackTrace();
        } catch (jakarta.mail.MessagingException e) {
            System.err.println("Invalid email address: " + e.getMessage());
        }
    }

    private String buildStyledOtpEmail(String otp) {
        return """
        <div style="font-family: 'Segoe UI', sans-serif; background-color: #f4f8fc; padding: 20px;">
            <div style="max-width: 600px; margin: auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
                <div style="text-align: center; margin-bottom: 10px;">
                    <img src="https://res.cloudinary.com/dcrmg4j1l/image/upload/v1743970965/isalogo-removebg-preview_h5wtkm.png"
                         alt="ISA Logo" style="width: 60px; height: auto;" />
                </div>
                <hr style="margin-top: 30px; border: none; border-top: 1px solid #ccc;" />
                <p style="text-align: center; font-size: 16px; color: #003366; margin: 10px 0;">
                    👋 <strong>Greetings from ISA HIT Student Chapter</strong>
                </p>
                <p style="font-size: 16px; color: #333333; text-align: center;">
                    ✉️ To verify yourself, use this OTP:
                </p>
                <div style="margin: 30px auto; text-align: center;">
                    <span style="font-size: 24px; font-weight: bold; color: #004080; padding: 10px 20px; background-color: #e0ebf5; border: 2px dashed #004080; border-radius: 6px;">%s</span>
                </div>
                <p style="font-size: 14px; color: #555555; text-align: center;">
                    ⏳ Note: This OTP is valid for only 10 minutes.
                </p>
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