

\connect patentdb

DROP TABLE IF EXISTS patent_min_hash; 
DROP SEQUENCE IF EXISTS patent_min_hash_uid_seq;
DROP TABLE IF EXISTS technology_min_hash; 
DROP SEQUENCE IF EXISTS technology_min_hash_uid_seq;
DROP TABLE IF EXISTS last_min_hash_ingest;

DO language 'plpgsql'
$$
DECLARE patent_hash text :=  'CREATE TABLE patent_min_hash('
    || string_agg('m' || i::text || ' integer', ',') || ');'
    FROM generate_series(1,100) As i; tech_hash text :=  'CREATE TABLE technology_min_hash('
    || string_agg('m' || i::text || ' integer', ',') || ');'
    FROM generate_series(1,100) As i;
BEGIN
    EXECUTE patent_hash; EXECUTE tech_hash;
END;
$$ ;

ALTER TABLE patent_min_hash ADD COLUMN pub_doc_number varchar(100) PRIMARY KEY;
ALTER TABLE patent_min_hash ADD COLUMN uid serial;

ALTER TABLE technology_min_hash ADD COLUMN name varchar(250) NOT NULL;
ALTER TABLE technology_min_hash ADD COLUMN uid serial;
CREATE INDEX technology_name_index ON technology_min_hash (name);


CREATE TABLE last_min_hash_ingest(
	last_uid bigint,
	table_name varchar(100)
);

INSERT INTO last_min_hash_ingest(last_uid,table_name) VALUES (20130101,'patent_grant');