package com.example.gym.backend.service;

import com.example.gym.backend.dto.TrainerDto;
import com.example.gym.backend.entity.Trainer;
import com.example.gym.backend.exception.ResourceNotFoundException;
import com.example.gym.backend.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TrainerService {

    private final TrainerRepository trainerRepository;

    public TrainerDto createTrainer(TrainerDto trainerDto) {
        log.info("Creating new trainer: {} {}", trainerDto.getFirstName(), trainerDto.getLastName());

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

    public List<TrainerDto> getTrainersBySpecialization(String specialization) {
        log.info("Fetching trainers by specialization: {}", specialization);
        List<Trainer> trainers = trainerRepository.findActiveTrainersBySpecialization(specialization);
        return trainers.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<TrainerDto> getTopRatedTrainers(Double minRating) {
        log.info("Fetching top rated trainers with minimum rating: {}", minRating);
        List<Trainer> trainers = trainerRepository.findTopRatedTrainers(minRating);
        return trainers.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public TrainerDto updateTrainer(Long id, TrainerDto trainerDto) {
        log.info("Updating trainer with ID: {}", id);
        Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with ID: " + id));

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
        return dto;
    }
}
