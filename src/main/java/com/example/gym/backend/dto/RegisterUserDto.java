package com.example.gym.backend.dto;

import com.example.gym.backend.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserDto {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String passwordHash;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String phone;

    private User.UserRole role;

    // This field will receive the gym ID as a number from the client
    // The frontend sends "gym": 1, so we use "gym" as the field name
    private Long gym;

    // Getter and setter for gym
    public Long getGym() {
        return gym;
    }

    public void setGym(Long gym) {
        this.gym = gym;
    }
}

