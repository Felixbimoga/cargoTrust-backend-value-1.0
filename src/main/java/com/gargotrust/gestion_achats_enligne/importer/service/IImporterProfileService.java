package com.gargotrust.gestion_achats_enligne.importer.service;

import com.gargotrust.gestion_achats_enligne.importer.dto.request.UpdateImporterProfileRequest;
import com.gargotrust.gestion_achats_enligne.importer.dto.response.ImporterProfileResponse;

import java.util.UUID;

public interface IImporterProfileService {

    ImporterProfileResponse getMyProfile();

    ImporterProfileResponse updateMyProfile(UpdateImporterProfileRequest request);

    ImporterProfileResponse getByAccountId(UUID accountId);
}
