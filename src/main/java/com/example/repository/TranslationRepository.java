package com.example.repository;

import com.example.entity.Translation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranslationRepository extends CrudRepository<Translation, Long> {

    Page<Translation> findAll(Pageable pageable);
}
