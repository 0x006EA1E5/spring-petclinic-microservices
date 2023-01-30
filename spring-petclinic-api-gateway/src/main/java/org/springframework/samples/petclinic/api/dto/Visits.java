package org.springframework.samples.petclinic.api.dto;

import java.util.List;


public record Visits(List<Visit> items) {
//    private List<Visit> items;
}
