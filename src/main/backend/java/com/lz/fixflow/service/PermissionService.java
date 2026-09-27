package com.lz.fixflow.service;

import com.lz.fixflow.entity.Ticket;
import org.springframework.stereotype.Service;

@Service
public interface PermissionService {

    void requireAdmin(String userid);

    void requireStaff(String userid);

    void requireTicketAccess(String userid, Ticket ticket);
}
