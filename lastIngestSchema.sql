

\connect patentdb

DROP TABLE IF EXISTS last_min_hash_ingest;


CREATE TABLE last_min_hash_ingest(
	last_uid bigint,
	table_name varchar(100)
);

