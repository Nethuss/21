package ru.didcvee.ecd.security.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.didcvee.ecd.security.dto.request.LoginRequest;
import ru.didcvee.ecd.security.dto.request.SignupRequest;
import ru.didcvee.ecd.security.dto.response.MessageResponse;
import ru.didcvee.ecd.security.dto.response.UserInfoResponse;
import ru.didcvee.ecd.security.exception.TokenRefreshException;
import ru.didcvee.ecd.security.jwt.access.AccessTokenFacade;
import ru.didcvee.ecd.security.repository.JdbcRoleRepository;
import ru.didcvee.ecd.security.repository.JdbcUserRepository;
import ru.didcvee.ecd.security.repository.model.RefreshToken;
import ru.didcvee.ecd.security.repository.model.Role;
import ru.didcvee.ecd.security.repository.model.User;
import ru.didcvee.ecd.security.service.RefreshTokenService;
import ru.didcvee.ecd.security.service.UserDetailsImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

// for Angular Client (withCredentials)
// @CrossOrigin(origins = "http://localhost:8081", maxAge = 3600, allowCredentials="true")
// @CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JdbcUserRepository userRepository;

    @Autowired
    JdbcRoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    AccessTokenFacade accessTokenFacade;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie jwtCookie = accessTokenFacade.generateJwtCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        ResponseCookie jwtRefreshCookie = accessTokenFacade.generateRefreshJwtCookie(refreshToken.getToken());

        var userInfo = new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), roles);
        loginRequest.setPassword(passwordEncoder.encode(loginRequest.getPassword()));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
                .body(userInfo);
    }

//    @PostMapping("/signup")
//    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
//        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
//            return ResponseEntity.badRequest().body(new MessageResponse("Имя пользователя занято"));
//        }
//
//        // Create new user's account
//        User user = new User(
//                signUpRequest.getUsername(),
//                encoder.encode(signUpRequest.getPassword()),
//                signUpRequest.getFirstname(),
//                signUpRequest.getMiddlename(),
//                signUpRequest.getLastname());
//
//        Set<String> strRoles = signUpRequest.getRole();
//        Set<Role> roles = new HashSet<>();
//
//        if (strRoles == null) {
//            Role userRole = roleRepository
//                    .findByName("ROLE_USER")
//                    .orElseThrow(() -> new RuntimeException("Роль не найдена"));
//            roles.add(userRole);
//        } else {
//            strRoles.forEach(role -> {
//                switch (role) {
//                    case "admin":
//                        Role adminRole = roleRepository
//                                .findByName(ERole.ROLE_ADMIN)
//                                .orElseThrow(() -> new RuntimeException("Роль не найдена"));
//                        roles.add(adminRole);
//
//                        break;
//                    case "mod":
//                        Role modRole = roleRepository
//                                .findByName(ERole.ROLE_SUPERUSER)
//                                .orElseThrow(() -> new RuntimeException("Роль не найдена"));
//                        roles.add(modRole);
//
//                        break;
//                    default:
//                        Role userRole = roleRepository
//                                .findByName(ERole.ROLE_USER)
//                                .orElseThrow(() -> new RuntimeException("Роль не найдена"));
//                        roles.add(userRole);
//                }
//            });
//        }
//
//        user.setRoles(roles);
//        userRepository.save(user);
//
//        signUpRequest.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
//
//        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
//    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        Object principle =
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principle.toString() != "anonymousUser") {
            Long userId = ((UserDetailsImpl) principle).getId();
            refreshTokenService.deleteByUserId(userId);
        }

        ResponseCookie jwtCookie = accessTokenFacade.getCleanJwtCookie();
        ResponseCookie jwtRefreshCookie = accessTokenFacade.getCleanJwtRefreshCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(HttpServletRequest request) {
        String refreshToken = accessTokenFacade.getJwtRefreshFromCookies(request);

        if ((refreshToken != null) && (!refreshToken.isEmpty())) {
            return refreshTokenService
                    .findByToken(refreshToken)
                    .map(refreshTokenService::verifyExpiration)
                    .map(RefreshToken::getUserId)
                    .map(userId -> {
                        Optional<User> byId = userRepository.findById(userId);
                        UserDetailsImpl userDetails = UserDetailsImpl.build(byId.get());

                        ResponseCookie jwtCookie = accessTokenFacade.generateJwtCookie(userDetails);

                        return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                                .body(new MessageResponse("Token is refreshed successfully!"));
                    })
                    .orElseThrow(() -> new TokenRefreshException(refreshToken, "Refresh token is not in database!"));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Refresh Token is empty!"));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("verify-access-token")
    public void verifyAccessToken() {
        // Вызов перехватывается @link AccessTokenFilter, который определяет валидный токен или нет
    }
}
