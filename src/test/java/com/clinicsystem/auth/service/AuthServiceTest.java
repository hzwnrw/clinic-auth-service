//package com.clinicsystem.auth.service;
//
//import com.clinicsystem.auth.dto.AuthResponse;
//import com.clinicsystem.auth.dto.LoginRequest;
//import com.clinicsystem.auth.dto.RegisterRequest;
//import com.clinicsystem.auth.model.User;
//import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//@SpringBootTest
//@RunWith(SpringRunner.class)
//public class AuthServiceTest {
//
//    @Autowired
//    private AuthService authService; // Inject your AuthService
//
//    @Test
//    public void testRegisterUser() {
//        RegisterRequest request = new RegisterRequest("ADMIN", "password123", "admin@mail.com", "ADMIN");
//        AuthResponse user = authService.register(request);
//        assertNotNull(user);
//        //assertEquals("testuser", user.getUsername());
//    }
//
//
////    @Test
////    public void testLoginUser() {
////        LoginRequest request = new LoginRequest("testuser", "password123");
////        AuthResponse response = authService.login(request);
////        System.out.println("Generated Token: " + response.getToken());
////        assertNotNull(response.getToken());
////    }
//
//}
//
//
//
