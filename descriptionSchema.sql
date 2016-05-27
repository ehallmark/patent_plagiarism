

\connect patentdb

DROP TABLE IF EXISTS patent_description_min_hash; 
DROP SEQUENCE IF EXISTS patent_description_min_hash_uid_seq;

DO language 'plpgsql'
$$
DECLARE patent_description_min_hash text :=  'CREATE TABLE patent_description_min_hash('
    || string_agg('m' || i::text || ' integer', ',') || ');'
    FROM generate_series(1,400) As i;
BEGIN
    EXECUTE patent_description_min_hash; 
END;
$$ ;


ALTER TABLE patent_description_min_hash ADD COLUMN pub_doc_number varchar(25) PRIMARY KEY;

DO language 'plpgsql'
$$
DECLARE patent_description_index text :=  string_agg('CREATE INDEX desc' || i::text || '_index ON patent_description_min_hash (m' || i::text || ',m' || (i+1)::text || ',m' || (i+2)::text || ',m'||(i+3)::text||');', ' ') 
    FROM generate_series(1,80,4) As i;
BEGIN
    EXECUTE patent_description_index; 
END;
$$ ;
