package com.gargotrust.gestion_achats_enligne.catalog.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductCategoryRequest {

    @Size(max = 150)
    private String name;

    @Size(max = 500)
    private String description;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal costCoefficient;

    private Boolean active;
}
