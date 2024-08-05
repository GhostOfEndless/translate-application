package com.example.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;

@Getter
@Setter
@Builder
@EqualsAndHashCode(exclude = "id")
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "translations", name = "t_translation")
public class Translation {

    @Id
    private Long id;

    @Column("c_client_ip")
    private String clientIP;

    @Column("c_source_language_code")
    private String sourceLanguageCode;

    @Column("c_target_language_code")
    private String targetLanguageCode;

    @Column("c_source_text")
    private String sourceText;

    @Column("c_translated_text")
    private String translatedText;

    @Column("c_request_timestamp")
    private Timestamp requestTimestamp;

    @Column("c_response_timestamp")
    private Timestamp responseTimestamp;
}
