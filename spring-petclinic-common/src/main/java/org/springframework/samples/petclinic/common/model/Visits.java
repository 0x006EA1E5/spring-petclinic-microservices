package org.springframework.samples.petclinic.common.model;

import java.util.List;

// TODO remove in favour of List<Visit> items
public record Visits(List<Visit> items) {
}
