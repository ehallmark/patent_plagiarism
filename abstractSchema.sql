

\connect patentdb

DROP TABLE IF EXISTS patent_abstract_min_hash; 
DROP SEQUENCE IF EXISTS patent_abstract_min_hash_uid_seq;

DO language 'plpgsql'
$$
DECLARE patent_abstract_min_hash text :=  'CREATE TABLE patent_abstract_min_hash('
    || string_agg('m' || i::text || ' integer', ',') || ');'
    FROM generate_series(1,200) As i;
BEGIN
    EXECUTE patent_abstract_min_hash; 
END;
$$ ;


ALTER TABLE patent_abstract_min_hash ADD COLUMN pub_doc_number varchar(25) PRIMARY KEY;

DO language 'plpgsql'
$$
DECLARE patent_abstract_index text :=  string_agg('CREATE INDEX abs' || i::text || '_index ON patent_abstract_min_hash (m' || i::text || ',m' || (i+1)::text ||');', ' ') 
    FROM generate_series(1,10,2) As i;
BEGIN
    EXECUTE patent_abstract_index; 
END;
$$ ;
