

\connect patentdb

DROP TABLE IF EXISTS patent_claim_min_hash; 
DROP SEQUENCE IF EXISTS patent_claim_min_hash_uid_seq;

DO language 'plpgsql'
$$
DECLARE patent_claim_min_hash text :=  'CREATE TABLE patent_claim_min_hash('
    || string_agg('m' || i::text || ' integer', ',') || ');'
    FROM generate_series(1,100) As i;
BEGIN
    EXECUTE patent_claim_min_hash; 
END;
$$ ;


ALTER TABLE patent_claim_min_hash ADD COLUMN pub_doc_number varchar(25);
ALTER TABLE patent_claim_min_hash ADD COLUMN claim_number integer;

CREATE INDEX claim_number_index on patent_claim_min_hash(claim_number);
CREATE INDEX pub_doc_number_index on patent_claim_min_hash(pub_doc_number);
CREATE UNIQUE INDEX pub_doc_number_claim_number_index on patent_claim_min_hash(pub_doc_number,claim_number);


DO language 'plpgsql'
$$
DECLARE patent_claim_index text :=  string_agg('CREATE INDEX clm' || i::text || '_index ON patent_claim_min_hash (m' || i::text || ',m' || (i+1)::text || ',m' || (i+2)::text || ');', ' ') 
    FROM generate_series(1,60,3) As i;
BEGIN
    EXECUTE patent_claim_index; 
END;
$$ ;



