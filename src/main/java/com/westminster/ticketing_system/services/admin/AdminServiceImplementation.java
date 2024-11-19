package com.westminster.ticketing_system.services.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.westminster.ticketing_system.dtos.TicketDTO;
import com.westminster.ticketing_system.dtos.UserDTO;
import com.westminster.ticketing_system.entity.Ticket;
import com.westminster.ticketing_system.entity.User;
import com.westminster.ticketing_system.enums.UserRole;
import com.westminster.ticketing_system.repository.TicketRepository;
import com.westminster.ticketing_system.repository.UserRepository;
import com.westminster.ticketing_system.repository.SystemConfigurationRepository;
import com.westminster.ticketing_system.entity.SystemConfiguration;
import com.westminster.ticketing_system.dtos.SystemConfigurationDTO;

@Service
public class AdminServiceImplementation implements AdminService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SystemConfigurationRepository systemConfigurationRepository;

    @Override
    public List<TicketDTO> getAllTickets() {
        List<Ticket> tickets = ticketRepository.findAll();
        return tickets.stream()
                .map(Ticket::getDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .filter(user -> user.getRole() != UserRole.ADMIN)
                .map(User::getDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getUsersByRole(String role) {
        List<User> users = userRepository.findByRole(UserRole.valueOf(role.toUpperCase()));
        return users.stream()
                .map(User::getDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteUser(int userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent() && optionalUser.get().getRole() != UserRole.ADMIN) {
            User user = optionalUser.get();
            user.setIsDeleted(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateSystemConfiguration(int userId, SystemConfigurationDTO configurationDTO) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (!optionalUser.isPresent()) {
                return false;
            }

            // Validate new configuration
            if (configurationDTO.getTotalTickets() > configurationDTO.getMaxTicketCapacity()) {
                return false;
            }

            SystemConfiguration newConfig = new SystemConfiguration();
            newConfig.setTotalTickets(configurationDTO.getTotalTickets());
            newConfig.setTicketReleaseRate(configurationDTO.getTicketReleaseRate());
            newConfig.setCustomerRetrievalRate(configurationDTO.getCustomerRetrievalRate());
            newConfig.setMaxTicketCapacity(configurationDTO.getMaxTicketCapacity());
            newConfig.setCreatedDateTime(LocalDateTime.now());
            newConfig.setUpdatedDateTime(LocalDateTime.now());
            newConfig.setUpdatedBy(optionalUser.get());

            systemConfigurationRepository.save(newConfig);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public SystemConfigurationDTO getSystemConfiguration() {
        Optional<SystemConfiguration> config = systemConfigurationRepository.findFirstByOrderByCreatedDateTimeDesc();

        if (config.isPresent()) {
            SystemConfiguration currentConfig = config.get();
            return new SystemConfigurationDTO(
                    currentConfig.getTotalTickets(),
                    currentConfig.getTicketReleaseRate(),
                    currentConfig.getCustomerRetrievalRate(),
                    currentConfig.getMaxTicketCapacity());
        }

        // Return default values if no configuration exists
        return new SystemConfigurationDTO(100, 6000, 3000, 500);
    }
}