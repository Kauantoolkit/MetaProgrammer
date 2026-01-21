package com.metagen.backend.generated.service;

import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import com.metagen.backend.generated.entity.ConversionType;
import com.metagen.backend.generated.repository.ConversionTypeRepository;

@Service
public class ConversionTypeService {

            private final ConversionTypeRepository repository;

        public ConversionTypeService(ConversionTypeRepository repository) {
            this.repository = repository;
        }


    
}
