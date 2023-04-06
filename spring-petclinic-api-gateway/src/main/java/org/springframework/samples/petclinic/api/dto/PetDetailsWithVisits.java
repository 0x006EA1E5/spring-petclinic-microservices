package org.springframework.samples.petclinic.api.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import org.springframework.samples.petclinic.common.model.PetDetails;
import org.springframework.samples.petclinic.common.model.PetType;
import org.springframework.samples.petclinic.common.model.Visit;

import java.time.Instant;
import java.util.List;

public record PetDetailsWithVisits(long id, String name, String owner, Instant birthDate, PetType type,
                                   @JsonSetter(nulls = Nulls.AS_EMPTY) List<Visit> visits) {
    PetDetailsWithVisits(PetDetails petDetails, List<Visit> visits) {
        this(petDetails.id(), petDetails.name(), petDetails.owner(), petDetails.birthDate(), petDetails.type(), visits);
    }
}
