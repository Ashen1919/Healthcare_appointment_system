package com.h_a_s.doctor_service.Services;

import com.h_a_s.doctor_service.Model.DatabaseSequence;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultationNoteIdGenerator {
    private final MongoTemplate mongoTemplate;

    public Long nextId() {
        Query query = new Query(
                Criteria.where("_id").is("consultation_note_sequence")
        );

        Update update = new Update().inc("sequence", 1L);

        FindAndModifyOptions options = FindAndModifyOptions.options()
                .upsert(true)
                .returnNew(true);

        DatabaseSequence counter = mongoTemplate.findAndModify(
                query,
                update,
                options,
                DatabaseSequence.class
        );

        if (counter == null) {
            throw new IllegalStateException("Failed to generate consultation note ID");
        }

        return 1000L + counter.getSequence();
    }
}
