package com.gargotrust.gestion_achats_enligne.forwarder.dto.request;

import com.gargotrust.gestion_achats_enligne.location.AddressCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Ajout d'un entrepôt / dépôt à l'origine.")
public class WarehouseRequest {

    @Size(max = 150)
    @Schema(description = "Libellé du dépôt", example = "Dépôt Guangzhou")
    private String label;

    @NotNull
    @Schema(description = "Adresse du dépôt (pour l'envoi des colis des importateurs)")
    private AddressCommand address;
}
