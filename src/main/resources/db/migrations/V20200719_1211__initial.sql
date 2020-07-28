create table agg
(
    id         bigserial primary key,
    uuid       uuid unique,
    version    int         not null,
    created_at timestamp   not null,
    updated_at timestamp,
    deleted_at timestamp,
    type       varchar(80) not null,
    data       jsonb       not null
);

create extension "uuid-ossp";

