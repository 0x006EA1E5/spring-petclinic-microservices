package org.springframework.samples.petclinic.visits.model;

import java.util.List;

public record Visits(List<Visit> items) {
}
