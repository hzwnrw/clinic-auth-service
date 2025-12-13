package com.clinicsystem.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

//    @PutMapping("/profile")
//    public void updateProfile(@RequestBody UserProfileDTO profileDTO) {
//        // Get the authenticated user's username or ID
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String username = auth.getName(); // Username from the JWT token
//
//        // Ensure the user can only update their own profile
//        // (Compare username with the profile being updated, or fetch the user's ID)
//        // Example: Call a service method to update the profile, passing the username
//        userService.updateProfile(username, profileDTO);
//    }
}