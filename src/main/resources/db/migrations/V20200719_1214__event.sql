create table ev
(
    id             bigserial primary key,
    uuid           uuid unique,
    aggregate_uuid uuid        not null references agg (uuid),
    version        int         not null,
    created_at     timestamp   not null,
    updated_at     timestamp,
    deleted_at     timestamp,
    type           varchar(80) not null,
    data           jsonb       not null,
    published_as   bigint
);

create unique index idx_ev_published_as on ev (published_as);
