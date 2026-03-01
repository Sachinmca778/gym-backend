package com.example.gym.backend.dto;

import com.example.gym.backend.entity.Member;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MemberDto {

    private Long id;

    private String memberCode;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Member.Gender gender;

    private String address;
    private String city;
    private String state;
    private String pincode;

    @NotBlank(message = "Emergency contact name is required")
    private String emergencyContactName;

    @NotBlank(message = "Emergency contact phone is required")
    private String emergencyContactPhone;

    private String emergencyContactRelation;
    private String medicalConditions;
    private String allergies;
    private String fitnessGoals;
    private String profileImage;

    private Member.MemberStatus status;
    private LocalDate joinDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;
    private Long gymId;
}
