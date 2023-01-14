module spring.petclinic.visits.service {
    opens org.springframework.samples.petclinic.visits.model;
    opens org.springframework.samples.petclinic.visits;
    exports org.springframework.samples.petclinic.visits.web;
    exports org.springframework.samples.petclinic.visits.model;
    requires lombok;
    requires jakarta.persistence;
    requires com.fasterxml.jackson.annotation;
    requires jakarta.validation;
    requires spring.data.jpa;
    requires spring.web;
    requires org.slf4j;
    requires io.opentelemetry.api;
    requires spring.boot.autoconfigure;
    requires spring.boot;

}

