package com.example.gym.backend.service;

import com.example.gym.backend.dto.TrainerDto;
import com.example.gym.backend.entity.Gym;
import com.example.gym.backend.entity.Trainer;
import com.example.gym.backend.entity.User;
import com.example.gym.backend.exception.ResourceNotFoundException;
import com.example.gym.backend.exception.ValidationException;
import com.example.gym.backend.repository.GymRepository;
import com.example.gym.backend.repository.TrainerRepository;
import com.example.gym.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final GymRepository gymRepository;

    public TrainerDto createTrainer(TrainerDto trainerDto) {
        log.info("Creating new trainer: {} {}", trainerDto.getFirstName(), trainerDto.getLastName());

        // ========== VALIDATION 1: Check user_id uniqueness (1-1 relationship) ==========
        if (trainerDto.getUserId() != null) {
            if (trainerRepository.existsByUserId(trainerDto.getUserId())) {
                throw new ValidationException("A trainer with user_id " + trainerDto.getUserId() + " already exists");
            }
            
            // Also validate that user exists and is of role TRAINER
            User user = userRepository.findById(trainerDto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + trainerDto.getUserId()));
            
            if (user.getRole() != User.UserRole.TRAINER) {
                throw new ValidationException("User with ID " + trainerDto.getUserId() + " is not a TRAINER. " +
                        "Only users with TRAINER role can be linked to trainers.");
            }
        }

        // ========== VALIDATION 2: Check email uniqueness ==========
        if (trainerDto.getEmail() != null && !trainerDto.getEmail().isBlank()) {
            if (trainerRepository.existsByEmail(trainerDto.getEmail())) {
                throw new ValidationException("Email '" + trainerDto.getEmail() + "' already exists for another trainer");
            }
        }

        Trainer trainer = new Trainer();
        trainer.setFirstName(trainerDto.getFirstName());
        trainer.setLastName(trainerDto.getLastName());
        trainer.setEmail(trainerDto.getEmail());
        trainer.setPhone(trainerDto.getPhone());
        trainer.setSpecialization(trainerDto.getSpecialization());
        trainer.setExperienceYears(trainerDto.getExperienceYears());
        trainer.setHourlyRate(trainerDto.getHourlyRate());
        trainer.setCertifications(trainerDto.getCertifications());
        trainer.setBio(trainerDto.getBio());
        trainer.setSchedule(trainerDto.getSchedule());
        trainer.setLocation(trainerDto.getLocation());
        trainer.setActive(true);

        // Set user if userId is provided
        if (trainerDto.getUserId() != null) {
            User user = userRepository.findById(trainerDto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + trainerDto.getUserId()));
            trainer.setUser(user);
            
            // ========== VALIDATION 3: Ensure gym match between user and trainer ==========
            if (user.getGym() != null && trainerDto.getGymId() != null) {
                if (!user.getGym().getId().equals(trainerDto.getGymId())) {
                    throw new ValidationException("User belongs to gym " + user.getGym().getId() + 
                            " but trainer is being created for gym " + trainerDto.getGymId());
                }
            }
            
            // If user has gym, set same gym for trainer
            if (user.getGym() != null) {
                trainer.setGym(user.getGym());
            }
        }

        // Set gym if gymId is provided (and user doesn't have gym)
        if (trainerDto.getGymId() != null && trainer.getGym() == null) {
            Gym gym = gymRepository.findById(trainerDto.getGymId())
                    .orElseThrow(() -> new ResourceNotFoundException("Gym not found with ID: " + trainerDto.getGymId()));
            trainer.setGym(gym);
        }

        Trainer savedTrainer = trainerRepository.save(trainer);
        log.info("Trainer created successfully with ID: {}", savedTrainer.getId());

        return convertToDto(savedTrainer);
    }

    public TrainerDto getTrainerById(Long id) {
        log.info("Fetching trainer with ID: {}", id);
        Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with ID: " + id));
        return convertToDto(trainer);
    }

    public List<TrainerDto> getAllActiveTrainers() {
        log.info("Fetching all active trainers");
        List<Trainer> trainers = trainerRepository.findByIsActiveTrue();
        return trainers.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<TrainerDto> getAllTrainers() {
        log.info("Fetching all trainers");
        List<Trainer> trainers = trainerRepository.findAll();
        return trainers.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<TrainerDto> getTrainersBySpecialization(String specialization) {
        log.info("Fetching trainers by specialization: {}", specialization);
        List<Trainer> trainers = trainerRepository.findActiveTrainersBySpecialization(specialization);
        return trainers.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<TrainerDto> getTopRatedTrainers(BigDecimal minRating) {
        log.info("Fetching top rated trainers with minimum rating: {}", minRating);
        List<Trainer> trainers = trainerRepository.findTopRatedTrainers(minRating);
        return trainers.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public TrainerDto updateTrainer(Long id, TrainerDto trainerDto) {
        log.info("Updating trainer with ID: {}", id);
        Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with ID: " + id));

        // ========== VALIDATION: Check user_id uniqueness (for update) ==========
        if (trainerDto.getUserId() != null && !trainerDto.getUserId().equals(trainer.getUser() != null ? trainer.getUser().getId() : null)) {
            if (trainerRepository.existsByUserId(trainerDto.getUserId())) {
                throw new ValidationException("A trainer with user_id " + trainerDto.getUserId() + " already exists");
            }
            
            // Validate user exists and is TRAINER
            User user = userRepository.findById(trainerDto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + trainerDto.getUserId()));
            
            if (user.getRole() != User.UserRole.TRAINER) {
                throw new ValidationException("User with ID " + trainerDto.getUserId() + " is not a TRAINER");
            }
        }

        trainer.setFirstName(trainerDto.getFirstName());
        trainer.setLastName(trainerDto.getLastName());
        trainer.setEmail(trainerDto.getEmail());
        trainer.setPhone(trainerDto.getPhone());
        trainer.setSpecialization(trainerDto.getSpecialization());
        trainer.setExperienceYears(trainerDto.getExperienceYears());
        trainer.setHourlyRate(trainerDto.getHourlyRate());
        trainer.setCertifications(trainerDto.getCertifications());
        trainer.setBio(trainerDto.getBio());
        trainer.setSchedule(trainerDto.getSchedule());
        trainer.setLocation(trainerDto.getLocation());
        trainer.setActive(trainerDto.isActive());

        // Update user if provided
        if (trainerDto.getUserId() != null) {
            User user = userRepository.findById(trainerDto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + trainerDto.getUserId()));
            trainer.setUser(user);
        }

        Trainer updatedTrainer = trainerRepository.save(trainer);
        log.info("Trainer updated successfully with ID: {}", updatedTrainer.getId());

        return convertToDto(updatedTrainer);
    }

    public void deleteTrainer(Long id) {
        log.info("Deleting trainer with ID: {}", id);
        if (!trainerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trainer not found with ID: " + id);
        }
        trainerRepository.deleteById(id);
        log.info("Trainer deleted successfully with ID: {}", id);
    }

    private TrainerDto convertToDto(Trainer trainer) {
        TrainerDto dto = new TrainerDto();
        dto.setId(trainer.getId());
        dto.setFirstName(trainer.getFirstName());
        dto.setLastName(trainer.getLastName());
        dto.setEmail(trainer.getEmail());
        dto.setPhone(trainer.getPhone());
        dto.setSpecialization(trainer.getSpecialization());
        dto.setExperienceYears(trainer.getExperienceYears());
        dto.setHourlyRate(trainer.getHourlyRate());
        dto.setCertifications(trainer.getCertifications());
        dto.setBio(trainer.getBio());
        dto.setSchedule(trainer.getSchedule());
        dto.setLocation(trainer.getLocation());
        dto.setRating(trainer.getRating());
        dto.setTotalRatings(trainer.getTotalRatings());
        dto.setActive(trainer.isActive());
        dto.setCreatedAt(trainer.getCreatedAt());
        dto.setUpdatedAt(trainer.getUpdatedAt());
        if (trainer.getUser() != null) {
            dto.setUserId(trainer.getUser().getId());
        }
        if (trainer.getGym() != null) {
            dto.setGymId(trainer.getGym().getId());
        }
        return dto;
    }
}

