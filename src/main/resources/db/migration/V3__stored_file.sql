create table if not exists stored_file (
  file_id        bigserial primary key,
  original_name  varchar(255) not null,
  stored_name    varchar(255) not null,
  ext            varchar(16),
  mime_type      varchar(255) not null,
  size_bytes     bigint not null,
  sha256         varchar(64) not null,
  access_level   varchar(16) not null default 'PUBLIC',
  location       varchar(16) not null default 'LOCAL',
  relative_path  text not null,
  created_at     timestamp not null default now(),
  updated_at     timestamp not null default now()
);
create unique index if not exists uq_stored_file_sha256 on stored_file(sha256);
create index if not exists idx_stored_file_created_at on stored_file(created_at);

create table if not exists stored_file_variant (
  variant_id     bigserial primary key,
  file_id        bigint not null references stored_file(file_id) on delete cascade,
  variant_key    varchar(32) not null,
  mime_type      varchar(255) not null,
  size_bytes     bigint not null,
  width          int,
  height         int,
  relative_path  text not null,
  created_at     timestamp not null default now()
);
create unique index if not exists uq_file_variant_key on stored_file_variant(file_id, variant_key);


