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
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of AdminService that handles administrative operations
 * including user management, ticket management, and system configuration.
 */
@Slf4j
@Service
public class AdminServiceImplementation implements AdminService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SystemConfigurationRepository systemConfigurationRepository;

    /**
     * Retrieves all tickets from the system.
     * 
     * @return List of TicketDTO containing all ticket information
     */
    @Override
    public List<TicketDTO> getAllTickets() {
        log.info("Fetching all tickets from the system");
        List<Ticket> tickets = ticketRepository.findAll();
        log.debug("Found {} tickets", tickets.size());
        return tickets.stream()
                .map(Ticket::getDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all non-admin users from the system.
     * 
     * @return List of UserDTO containing user information
     */
    @Override
    public List<UserDTO> getAllUsers() {
        log.info("Fetching all non-admin users");
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOs = users.stream()
                .filter(user -> user.getRole() != UserRole.ADMIN)
                .map(User::getDto)
                .collect(Collectors.toList());
        log.debug("Found {} non-admin users", userDTOs.size());
        return userDTOs;
    }

    /**
     * Retrieves users by their role.
     * 
     * @param role The role to filter users by
     * @return List of UserDTO matching the specified role
     */
    @Override
    public List<UserDTO> getUsersByRole(String role) {
        log.info("Fetching users with role: {}", role);
        List<User> users = userRepository.findByRole(UserRole.valueOf(role.toUpperCase()));
        log.debug("Found {} users with role {}", users.size(), role);
        return users.stream()
                .map(User::getDto)
                .collect(Collectors.toList());
    }

    /**
     * Soft deletes a user by setting their isDeleted flag to true.
     * 
     * @param userId The ID of the user to delete
     * @return true if user was successfully deleted, false otherwise
     */
    @Override
    public boolean deleteUser(int userId) {
        log.info("Attempting to delete user with ID: {}", userId);
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent() && optionalUser.get().getRole() != UserRole.ADMIN) {
            User user = optionalUser.get();
            user.setIsDeleted(true);
            userRepository.save(user);
            log.info("Successfully deleted user with ID: {}", userId);
            return true;
        }

        log.warn("Failed to delete user with ID: {}. User not found or is admin", userId);
        return false;
    }

    /**
     * Reactivates a previously deleted user.
     * 
     * @param userId The ID of the user to activate
     * @return true if user was successfully activated, false otherwise
     */
    @Override
    public boolean activateUser(int userId) {
        log.info("Attempting to activate user with ID: {}", userId);
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent() && optionalUser.get().getIsDeleted()) {
            User user = optionalUser.get();
            user.setIsDeleted(false);
            userRepository.save(user);
            log.info("Successfully activated user with ID: {}", userId);
            return true;
        }

        log.warn("Failed to activate user with ID: {}. User not found or is not deleted", userId);
        return false;
    }

    /**
     * Updates the system configuration with new settings.
     * 
     * @param userId           The ID of the admin user making the update
     * @param configurationDTO The new configuration settings
     * @return true if configuration was successfully updated, false otherwise
     */
    @Override
    public boolean updateSystemConfiguration(int userId, SystemConfigurationDTO configurationDTO) {
        log.info("Attempting to update system configuration by user ID: {}", userId);
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (!optionalUser.isPresent()) {
                log.error("User not found with ID: {}", userId);
                return false;
            }

            // Validate new configuration
            if (configurationDTO.getTotalTickets() > configurationDTO.getMaxTicketCapacity()) {
                log.error("Invalid configuration: Total tickets ({}) exceeds max capacity ({})",
                        configurationDTO.getTotalTickets(), configurationDTO.getMaxTicketCapacity());
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
            log.info("Successfully updated system configuration");
            return true;
        } catch (Exception e) {
            log.error("Error updating system configuration: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Retrieves the current system configuration or returns default values if none
     * exists.
     * 
     * @return SystemConfigurationDTO containing the current configuration
     */
    @Override
    public SystemConfigurationDTO getSystemConfiguration() {
        log.info("Fetching current system configuration");
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