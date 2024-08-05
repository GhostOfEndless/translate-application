create schema if not exists translations;

create table translations.t_translation(
    id bigserial primary key,
    c_client_ip varchar(15) not null,
    c_source_language_code varchar(7) not null check (length(trim(c_source_language_code)) >= 2),
    c_target_language_code varchar(7) not null check (length(trim(c_target_language_code)) >= 2),
    c_source_text text not null,
    c_translated_text text not null,
    c_request_timestamp timestamp without time zone not null,
    c_response_timestamp timestamp without time zone not null
);