package com.ihrapanel.backend.user;

import com.ihrapanel.backend.user.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ihrapanel.backend.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;
import com.ihrapanel.backend.user.dto.CreateUserRequest;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    //JWT denemesi
@GetMapping("/me")
public ResponseEntity<String> me(
        @AuthenticationPrincipal AuthenticatedUser currentUser
) {
    return ResponseEntity.ok(
            "User ID: " + currentUser.userId()
            + "\nCompany ID: " + currentUser.companyId()
            + "\nRole: " + currentUser.role()
    );
}
//Company_id ye görede userları döndürür sadece owner yapablir configsecurityde öyle verdik

@GetMapping
public ResponseEntity<List<UserResponse>> getUsers(
        @AuthenticationPrincipal AuthenticatedUser currentUser
) {

    List<User> users =
            userService.getUsersByCompany(
                    currentUser.companyId()
            );

    List<UserResponse> response =
            users.stream()
                    .map(user -> new UserResponse(
                            user.getId(),
                            user.getName(),
                            user.getEmail(),
                            user.getRole(),
                            user.getCompany().getId()
                    ))
                    .toList();

    return ResponseEntity.ok(response);
}

//Owner useri create ederken bu olucak
@PostMapping
public ResponseEntity<UserResponse> createUser(
        @RequestBody CreateUserRequest request,
        @AuthenticationPrincipal AuthenticatedUser currentUser
) {

    User user = userService.createEmployee(
        currentUser.companyId(),
        request.getName(),
        request.getEmail(),
        request.getPassword(),
        request.getRole()
);

    UserResponse response = new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getCompany().getId()
    );

    return ResponseEntity.ok(response);
}

}