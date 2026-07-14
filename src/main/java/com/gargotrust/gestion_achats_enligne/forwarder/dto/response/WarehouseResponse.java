package com.gargotrust.gestion_achats_enligne.forwarder.dto.response;

import com.gargotrust.gestion_achats_enligne.location.AddressView;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WarehouseResponse {
    private Long        id;
    private String      label;
    private AddressView address;
}
