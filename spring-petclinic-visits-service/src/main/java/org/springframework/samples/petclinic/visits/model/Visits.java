package org.springframework.samples.petclinic.visits.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


public record Visits(List<Visit> items) {
//    private List<Visit> items;
}
