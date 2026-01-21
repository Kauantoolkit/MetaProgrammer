package com.metagen.backend.generated.service;

import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import com.metagen.backend.generated.entity.ClientUsage;
import com.metagen.backend.generated.repository.ClientUsageRepository;

@Service
public class ClientUsageService {

            private final ClientUsageRepository repository;

        public ClientUsageService(ClientUsageRepository repository) {
            this.repository = repository;
        }


    
}
