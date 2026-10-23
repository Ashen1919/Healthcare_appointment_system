package com.h_a_s.doctor_service.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "database_sequences")
public class DatabaseSequence {
    @Id
    private String id;

    private long sequence;
}
