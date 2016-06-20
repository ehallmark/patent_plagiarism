

\connect patentdb

DROP TABLE IF EXISTS last_min_hash_ingest;


CREATE TABLE last_min_hash_ingest(
	last_uid integer,
	table_name varchar(100)
);

INSERT INTO last_min_hash_ingest(last_uid,table_name) VALUES ('20010000','patent_grant');
