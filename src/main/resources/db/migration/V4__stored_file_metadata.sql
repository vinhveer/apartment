alter table if exists stored_file
	add column if not exists alt_text    varchar(255),
	add column if not exists title       varchar(255),
	add column if not exists description text,
	add column if not exists tags        text;


