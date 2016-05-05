

\connect patentdb

DROP TABLE IF EXISTS patent_min_hash; 
DROP SEQUENCE IF EXISTS patent_min_hash_uid_seq;

DO language 'plpgsql'
$$
DECLARE var_sql text :=  'CREATE TABLE patent_min_hash('
    || string_agg('m' || i::text || ' integer', ',') || ');'
    FROM generate_series(1,400) As i;
BEGIN
    EXECUTE var_sql;
END;
$$ ;

ALTER TABLE patent_min_hash ADD COLUMN pub_doc_number varchar(100) PRIMARY KEY;
ALTER TABLE patent_min_hash ADD COLUMN uid serial;


