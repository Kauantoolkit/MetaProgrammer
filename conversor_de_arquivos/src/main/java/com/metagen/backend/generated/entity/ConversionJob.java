package com.metagen.backend.generated.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.*;

@Entity
@Table(name = "conversionjob")
public class ConversionJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String cj_input_format;

    @NotNull
    private String cj_output_format;

    @NotNull
    private Boolean cj_sucess;

    @NotNull
    private String cj_client_ip;

}
