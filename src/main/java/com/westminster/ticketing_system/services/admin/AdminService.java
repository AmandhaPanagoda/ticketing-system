package com.westminster.ticketing_system.services.admin;

import java.util.List;
import com.westminster.ticketing_system.dtos.TicketDTO;
import com.westminster.ticketing_system.dtos.UserDTO;

public interface AdminService {
    List<TicketDTO> getAllTickets();

    List<UserDTO> getAllUsers();

    List<UserDTO> getUsersByRole(String role);

    boolean deleteUser(int userId);
}