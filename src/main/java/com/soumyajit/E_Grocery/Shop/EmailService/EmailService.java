package com.soumyajit.E_Grocery.Shop.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendPasswordResetEmail(String to, String code, String name) {
        String subject = "🔐 Your Password Reset Request";
        String htmlContent = buildStyledEmail(
                "🔑 Password Reset Request",
                "To reset your password, please use the following code:",
                code,
                "⏳ Note: This code is valid for only 30 minutes.",
                name
        );
        sendHtmlEmail(to, subject, htmlContent);
    }

    public void sendOtpEmail(String to, String otp, String name) {
        String subject = "📩 Your OTP Code";
        String htmlContent = buildStyledEmail(
                "✅ OTP Verification",
                "Your OTP code is:",
                otp,
                "⏳ Note: This code is valid for a short duration.",
                name
        );
        sendHtmlEmail(to, subject, htmlContent);
    }

    @Async
    public void sendPasswordResetSuccessEmail(String to, String name) {
        String subject = "✅ Your Password Has Been Successfully Reset";
        String htmlContent = buildResetSuccessEmail(name);
        sendHtmlEmail(to, subject, htmlContent);
    }

    @Async
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send styled email", e);
        }
    }

    private String buildStyledEmail(String title, String message, String code, String note, String userName) {
        return String.format("""
            <div style="font-family: 'Segoe UI', sans-serif; background-color: #f4f8fc; padding: 20px;">
                <div style="max-width: 600px; margin: auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
                    <p style="text-align: center; font-size: 16px; color: #2a7ae2; margin-bottom: 20px;">
                        👋 <strong>Hello, %s</strong>
                    </p>
                    <h2 style="color: #1a237e; text-align: center;">%s</h2>
                    <p style="font-size: 14px; color: #333333; text-align: center;">📬 %s</p>
                    <div style="margin: 30px auto; text-align: center;">
                        <span style="font-size: 24px; font-weight: bold; color: #1a237e; padding: 10px 20px; background-color: #e3f2fd; border: 2px dashed #1a237e; border-radius: 6px;">%s</span>
                    </div>
                    <p style="font-size: 14px; color: #555555; text-align: center;">%s</p>
                    <hr style="margin-top: 30px; border: none; border-top: 1px solid #ccc;" />
                    <p style="font-size: 12px; color: #888888; text-align: center; margin-top: 10px;">
                        🛒 E-Grocery | Your Trusted Essentials Partner
                    </p>
                </div>
            </div>
            """, userName, title, message, code, note);
    }

    private String buildResetSuccessEmail(String userName) {
        return String.format("""
        <div style="font-family: 'Segoe UI', sans-serif; background-color: #f4f8fc; padding: 20px;">
            <div style="max-width: 600px; margin: auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
                <p style="text-align: center; font-size: 16px; color: #2a7ae2; margin: 10px 0;">
                    👋 <strong>Hello, %s</strong>
                </p>
                <h2 style="color: #1a237e; text-align: center;">🎉 Password Reset Successful</h2>
                <p style="font-size: 16px; color: #333333; text-align: center;">
                    ✅ Your password has been successfully reset. You can now log in with your new password.
                </p>
                <p style="font-size: 14px; color: #d32f2f; text-align: center;">
                    ⚠️ If you did not request this change, please contact our support team immediately.
                </p>
                <hr style="margin-top: 30px; border: none; border-top: 1px solid #ccc;" />
                <p style="font-size: 12px; color: #888888; text-align: center; margin-top: 10px;">
                    🛒 E-Grocery | Your Trusted Essentials Partner
                </p>
            </div>
        </div>
        """, userName);
    }
}
