package com.metagen.backend.generated.service;

import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import com.metagen.backend.generated.entity.ConversionJob;
import com.metagen.backend.generated.repository.ConversionJobRepository;

@Service
public class ConversionJobService {

            private final ConversionJobRepository repository;

        public ConversionJobService(ConversionJobRepository repository) {
            this.repository = repository;
        }


    
}
