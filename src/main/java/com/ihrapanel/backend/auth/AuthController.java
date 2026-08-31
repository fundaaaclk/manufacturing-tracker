package com.ihrapanel.backend.auth;

import com.ihrapanel.backend.security.JwtService;
import com.ihrapanel.backend.user.User;
import com.ihrapanel.backend.user.UserService;
import com.ihrapanel.backend.user.dto.LoginRequest;
import com.ihrapanel.backend.user.dto.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.ihrapanel.backend.user.dto.RegisterUserRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
   private final AuthService authService;

    public AuthController(UserService userService,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService, AuthService authService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request
    ) {

        User user = userService.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException("Email veya şifre hatalı.")
                );

        boolean passwordCorrect = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        ); 
        //BCryptPasswordEncoder.matches() metodu, düz metin şifreyi (request.getPassword()) 
        // ve hashlenmiş şifreyi (user.getPasswordHash()) karşılaştırır. Eğer
        //  eşleşiyorsa true döner, aksi takdirde false döner.

        if (!passwordCorrect) {
            throw new IllegalArgumentException("Email veya şifre hatalı.");
        }

         if (!user.isActive()) {
        throw new IllegalArgumentException("Kullanıcı hesabı pasif.");
    }

        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(
                new LoginResponse(token)
        );
    }

@PostMapping("/register")
public ResponseEntity<LoginResponse> register(
      @RequestBody RegisterUserRequest request) 
         {

        String token = authService.register(request);

       return ResponseEntity.ok(
            new LoginResponse(token)
       );
       }


}