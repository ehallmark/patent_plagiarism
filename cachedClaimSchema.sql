

\connect patentdb

DROP TABLE IF EXISTS patent_claim_cache_min_hash; 
DROP SEQUENCE IF EXISTS patent_claim_cache_min_hash_uid_seq;

DO language 'plpgsql'
$$
DECLARE patent_claim_cache_min_hash text :=  'CREATE TABLE patent_claim_cache_min_hash('
    || string_agg('m' || i::text || ' integer', ',') || ');'
    FROM generate_series(1,100) As i;
BEGIN
    EXECUTE patent_claim_cache_min_hash; 
END;
$$ ;


ALTER TABLE patent_claim_cache_min_hash ADD COLUMN pub_doc_number varchar(25) PRIMARY KEY;

