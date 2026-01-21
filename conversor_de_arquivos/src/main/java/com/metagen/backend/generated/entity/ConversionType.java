package com.metagen.backend.generated.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.*;

@Entity
@Table(name = "conversiontype")
public class ConversionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String input_format;

    @NotNull
    private String output_format;

    @NotNull
    private Boolean enabled;

}
