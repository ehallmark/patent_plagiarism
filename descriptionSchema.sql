

\connect patentdb

DROP TABLE IF EXISTS patent_description_min_hash; 
DROP SEQUENCE IF EXISTS patent_description_min_hash_uid_seq;

DO language 'plpgsql'
$$
DECLARE patent_description_min_hash text :=  'CREATE TABLE patent_description_min_hash('
    || string_agg('m' || i::text || ' integer', ',') || ');'
    FROM generate_series(1,150) As i;
BEGIN
    EXECUTE patent_description_min_hash; 
END;
$$ ;


ALTER TABLE patent_description_min_hash ADD COLUMN pub_doc_number varchar(100) PRIMARY KEY;

DO language 'plpgsql'
$$
DECLARE patent_description_index text :=  string_agg('CREATE INDEX dm' || i::text || '_index ON patent_description_min_hash (m' || i::text || ');', ' ') 
    FROM generate_series(1,30) As i;
BEGIN
    EXECUTE patent_description_index; 
END;
$$ ;
