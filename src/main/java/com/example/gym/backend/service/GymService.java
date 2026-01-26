package com.example.gym.backend.service;

import com.example.gym.backend.dto.GymDto;
import com.example.gym.backend.entity.Gym;
import com.example.gym.backend.exception.ResourceNotFoundException;
import com.example.gym.backend.repository.GymRepository;
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
public class GymService {

    private final GymRepository gymRepository;

    public GymDto createGym(GymDto gymDto) {
        log.info("Creating new gym: {}", gymDto.getName());

        Gym gym = new Gym();
        gym.setGymCode(gymDto.getGymCode());
        gym.setName(gymDto.getName());
        gym.setEmail(gymDto.getEmail());
        gym.setPhone(gymDto.getPhone());
        gym.setAddress(gymDto.getAddress());
        gym.setCity(gymDto.getCity());
        gym.setState(gymDto.getState());
        gym.setPincode(gymDto.getPincode());
        gym.setActive(true);

        Gym savedGym = gymRepository.save(gym);
        log.info("Gym created successfully with ID: {}", savedGym.getId());

        return convertToDto(savedGym);
    }

    public GymDto getGymById(Long id) {
        log.info("Fetching gym with ID: {}", id);
        Gym gym = gymRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found with ID: " + id));
        return convertToDto(gym);
    }

    public List<GymDto> getAllActiveGyms() {
        log.info("Fetching all active gyms");
        List<Gym> gyms = gymRepository.findActiveGyms();
        return gyms.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public GymDto updateGym(Long id, GymDto gymDto) {
        log.info("Updating gym with ID: {}", id);
        Gym gym = gymRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found with ID: " + id));

        gym.setGymCode(gymDto.getGymCode());
        gym.setName(gymDto.getName());
        gym.setEmail(gymDto.getEmail());
        gym.setPhone(gymDto.getPhone());
        gym.setAddress(gymDto.getAddress());
        gym.setCity(gymDto.getCity());
        gym.setState(gymDto.getState());
        gym.setPincode(gymDto.getPincode());
        gym.setActive(gymDto.isActive());

        Gym updatedGym = gymRepository.save(gym);
        log.info("Gym updated successfully with ID: {}", updatedGym.getId());

        return convertToDto(updatedGym);
    }

    public void deleteGym(Long id) {
        log.info("Deleting gym with ID: {}", id);
        if (!gymRepository.existsById(id)) {
            throw new ResourceNotFoundException("Gym not found with ID: " + id);
        }
        gymRepository.deleteById(id);
        log.info("Gym deleted successfully with ID: {}", id);
    }

    private GymDto convertToDto(Gym gym) {
        GymDto dto = new GymDto();
        dto.setId(gym.getId());
        dto.setGymCode(gym.getGymCode());
        dto.setName(gym.getName());
        dto.setEmail(gym.getEmail());
        dto.setPhone(gym.getPhone());
        dto.setAddress(gym.getAddress());
        dto.setCity(gym.getCity());
        dto.setState(gym.getState());
        dto.setPincode(gym.getPincode());
        dto.setActive(gym.isActive());
        dto.setCreatedAt(gym.getCreatedAt());
        dto.setUpdatedAt(gym.getUpdatedAt());
        return dto;
    }
}
