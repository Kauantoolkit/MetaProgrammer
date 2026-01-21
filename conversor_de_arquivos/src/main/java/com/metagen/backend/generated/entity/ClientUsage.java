package com.metagen.backend.generated.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.*;

@Entity
@Table(name = "clientusage")
public class ClientUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String clientIp;

    @NotNull
    private Date date;

    @NotNull
    private String conversions_today;

}
