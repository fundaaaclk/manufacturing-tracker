package com.ihrapanel.backend.user;

import com.ihrapanel.backend.user.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ihrapanel.backend.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;
import com.ihrapanel.backend.user.dto.CreateUserRequest;
import com.ihrapanel.backend.user.dto.UpdateUserRequest;
import java.util.UUID;
import com.ihrapanel.backend.user.dto.UpdateUserStatusRequest;
import jakarta.validation.Valid;
// import com.ihrapanel.backend.common.GlobalExceptionHandler;

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
                            user.getCompany().getId(),
                             user.isActive()
                    ))
                    .toList();

    return ResponseEntity.ok(response);
}

//Owner useri create ederken bu olucak
@PostMapping
public ResponseEntity<UserResponse> createUser(
        @Valid @RequestBody CreateUserRequest request,
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
            user.getCompany().getId(),
             user.isActive()
    );

    return ResponseEntity.ok(response);
}

//User_id ile donmesinin endpointi(company id de dikkat ediyoruz )
@GetMapping("/{id}")
public ResponseEntity<UserResponse> getUserById(
        @PathVariable UUID id,
        @AuthenticationPrincipal AuthenticatedUser currentUser
) {
    User user = userService.getUserById(
            id,
            currentUser.companyId()
    );

    UserResponse response = new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getCompany().getId(),
             user.isActive()
    );

    return ResponseEntity.ok(response);
}

//update the  user information 
@PutMapping("/{id}")
public ResponseEntity<UserResponse> updateUser(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateUserRequest request,
        @AuthenticationPrincipal AuthenticatedUser currentUser
) {
    User user = userService.updateUser(
            id,
            currentUser.companyId(),
            request.getName(),
            request.getEmail()
    );

    UserResponse response = new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getCompany().getId(),
             user.isActive()
    );

    return ResponseEntity.ok(response);
}
 
//owner useri aktif/inaktif yapıyor

@PatchMapping("/{id}/active")
public ResponseEntity<UserResponse> changeUserActiveStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateUserStatusRequest request,
        @AuthenticationPrincipal AuthenticatedUser currentUser
) {
    User user = userService.changeUserActiveStatus(
            id,
            currentUser.companyId(),
            request.isActive()
    );

    UserResponse response = new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getCompany().getId(),
             user.isActive()
    );

    return ResponseEntity.ok(response);
}

}