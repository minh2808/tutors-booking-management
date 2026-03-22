package org.tutorbooking.service.Impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tutorbooking.domain.entity.Parent;
import org.tutorbooking.domain.entity.Tutor;
import org.tutorbooking.domain.entity.User;
import org.tutorbooking.domain.enums.AuthProvider;
import org.tutorbooking.domain.enums.Role;
import org.tutorbooking.dto.request.GoogleLoginRequest;
import org.tutorbooking.dto.request.LoginRequest;
import org.tutorbooking.dto.request.RegisterRequest;
import org.tutorbooking.dto.response.AuthResponse;
import org.tutorbooking.repository.ParentRepository;
import org.tutorbooking.repository.TutorRepository;
import org.tutorbooking.repository.UserRepository;
import org.tutorbooking.security.GoogleTokenVerifier;
import org.tutorbooking.security.JwtTokenProvider;
import org.tutorbooking.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Value;
import java.io.UnsupportedEncodingException;
import jakarta.mail.MessagingException;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TutorRepository tutorRepository;
    @Autowired
    private ParentRepository parentRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private GoogleTokenVerifier googleTokenVerifier;

    @Autowired
    private org.springframework.mail.javamail.JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${frontend.url:http://localhost:3000}") 
    private String frontendUrl;

    @Transactional
    @Override
    public void registerUser(RegisterRequest signUpRequest) {

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        User user = User.builder()
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .fullName(signUpRequest.getFullName())
                .phone(signUpRequest.getPhone())
                .role(signUpRequest.getRole())
                .authProvider(AuthProvider.LOCAL)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        if (signUpRequest.getRole() == Role.TUTOR) {
            Tutor tutor = Tutor.builder()
                    .user(savedUser)
                    .approvalStatus("pending")
                    .build();
            tutorRepository.save(tutor);
        } else if (signUpRequest.getRole() == Role.PARENT) {
            Parent parent = Parent.builder()
                    .user(savedUser)
                    .build();
            parentRepository.save(parent);
        }
    }

    @Override
    public AuthResponse loginUser(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy User sau khi đăng nhập"));

        user.setRefreshToken(refreshToken);
        userRepository.save(user);


        return AuthResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest googleLoginRequest) {

        GoogleIdToken.Payload payload = googleTokenVerifier.verify(googleLoginRequest.getIdToken());
        if (payload == null) {
            throw new RuntimeException("Lỗi: Token Google không hợp lệ hoặc đã hết hạn!");
        }
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {

            Role role = googleLoginRequest.getRole() != null ? googleLoginRequest.getRole() : Role.PARENT;

            user = User.builder()
                    .email(email)
                    .fullName(name)
                    .avatarUrl(pictureUrl)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Mật khẩu rác vì mượn acc Google rồi
                    .role(role)
                    .authProvider(AuthProvider.GOOGLE)
                    .isActive(true)
                    .build();
            user = userRepository.save(user);

            if (role == Role.TUTOR) {
                Tutor tutor = Tutor.builder()
                        .user(user)
                        .approvalStatus("pending")
                        .build();
                tutorRepository.save(tutor);
            } else {
                Parent parent = Parent.builder()
                        .user(user)
                        .build();
                parentRepository.save(parent);
            }
        }

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        org.springframework.security.core.userdetails.User userPrincipal = new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), Collections.singletonList(authority));

        Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return AuthResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Override
    public AuthResponse refreshToken(org.tutorbooking.dto.request.RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateJwtToken(requestRefreshToken)) {
            throw new RuntimeException("Refresh Token không hợp lệ hoặc đã hết hạn!");
        }

        String email = jwtTokenProvider.getEmailFromJwtToken(requestRefreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (!requestRefreshToken.equals(user.getRefreshToken())) {
            throw new RuntimeException("Refresh Token không khớp với hệ thống!");
        }

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        org.springframework.security.core.userdetails.User userPrincipal = new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), Collections.singletonList(authority));
        Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

        String newAccessToken = jwtTokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(requestRefreshToken) 
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Override
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        user.setRefreshToken(null);
        userRepository.save(user);
    }

    @Override
    public void forgotPassword(org.tutorbooking.dto.request.ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống"));

        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            throw new RuntimeException("Tài khoản này được đăng nhập bằng Google. Vui lòng sử dụng tính năng Đăng nhập bằng Google!");
        }

        java.util.Date now = new java.util.Date();
        java.util.Date expiryDate = new java.util.Date(now.getTime() + 15 * 60 * 1000); // 15 phút

        String resetToken = io.jsonwebtoken.Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtTokenProvider.getSigningKey())
                .compact();

        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, "GIASUPRO");
            helper.setTo(user.getEmail());
            helper.setSubject("Yêu cầu đặt lại mật khẩu - GIASUPRO");

            String htmlContent = "<div style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">"
                    + "<h2 style=\"color: #2563eb;\">Xin chào " + user.getFullName() + ",</h2>"
                    + "<p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn tại hệ thống <b>GIASUPRO</b>.</p>"
                    + "<p>Vui lòng nhấn vào nút dưới đây để thiết lập mật khẩu mới (Đường dẫn có hiệu lực trong <b>15 phút</b>):</p>"
                    + "<div style=\"text-align: center; margin: 30px 0;\">"
                    + "<a href=\"" + resetLink + "\" style=\"background-color: #2563eb; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;\">Đặt lại mật khẩu</a>"
                    + "</div>"
                    + "<p>Nếu nút bấm không hoạt động, bạn có thể copy và dán đường link này vào trình duyệt:</p>"
                    + "<p style=\"word-break: break-all; color: #2563eb;\">" + resetLink + "</p>"
                    + "<p><i>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email để đảm bảo an toàn cho tài khoản.</i></p>"
                    + "<hr style=\"border: none; border-top: 1px solid #eee; margin-top: 30px;\" />"
                    + "<p style=\"font-size: 12px; color: #999;\">Trân trọng,<br>Đội ngũ hỗ trợ GIASUPRO</p>"
                    + "</div>";

            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Lỗi khi gửi email: ", e);
            throw new RuntimeException("Có lỗi xảy ra khi gửi email xác nhận. Vui lòng thử lại sau!");
        }
    }

    @Override
    public void resetPassword(org.tutorbooking.dto.request.ResetPasswordRequest request) {
        if (!jwtTokenProvider.validateJwtToken(request.getToken())) {
            throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn!");
        }

        String email = jwtTokenProvider.getEmailFromJwtToken(request.getToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setRefreshToken(null); 
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(String email, org.tutorbooking.dto.request.ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng trong hệ thống."));

        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            throw new RuntimeException("Tài khoản liên kết với Google không thể đổi mật khẩu theo cách này!");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác!");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu mới không được trùng với mật khẩu hiện tại!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        
        user.setRefreshToken(null); 
        
        userRepository.save(user);
    }
}