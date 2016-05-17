

\connect patentdb

DROP TABLE IF EXISTS patent_claim_min_hash; 
DROP SEQUENCE IF EXISTS patent_claim_min_hash_uid_seq;

DO language 'plpgsql'
$$
DECLARE patent_claim_min_hash text :=  'CREATE TABLE patent_claim_min_hash('
    || string_agg('m' || i::text || ' integer', ',') || ');'
    FROM generate_series(1,200) As i;
BEGIN
    EXECUTE patent_claim_min_hash; 
END;
$$ ;


ALTER TABLE patent_claim_min_hash ADD COLUMN pub_doc_number varchar(100);
ALTER TABLE patent_claim_min_hash ADD COLUMN claim_number integer;
ALTER TABLE patent_claim_min_hash ADD COLUMN uid serial;

CREATE INDEX claim_number_index on patent_claim_min_hash(claim_number);
CREATE INDEX pub_doc_number_claim_number_index on patent_claim_min_hash(pub_doc_number,claim_number);
CREATE INDEX cm1_index ON patent_claim_min_hash (m1);
CREATE INDEX cm2_index ON patent_claim_min_hash (m2);
CREATE INDEX cm3_index ON patent_claim_min_hash (m3);
CREATE INDEX cm4_index ON patent_claim_min_hash (m4);
CREATE INDEX cm5_index ON patent_claim_min_hash (m5);
CREATE INDEX cm6_index ON patent_claim_min_hash (m6);
CREATE INDEX cm7_index ON patent_claim_min_hash (m7);
CREATE INDEX cm8_index ON patent_claim_min_hash (m8);
CREATE INDEX cm9_index ON patent_claim_min_hash (m9);
CREATE INDEX cm10_index ON patent_claim_min_hash (m10);
CREATE INDEX cm11_index ON patent_claim_min_hash (m11);
CREATE INDEX cm12_index ON patent_claim_min_hash (m12);
CREATE INDEX cm13_index ON patent_claim_min_hash (m13);
CREATE INDEX cm14_index ON patent_claim_min_hash (m14);
CREATE INDEX cm15_index ON patent_claim_min_hash (m15);
CREATE INDEX cm16_index ON patent_claim_min_hash (m16);
CREATE INDEX cm17_index ON patent_claim_min_hash (m17);
CREATE INDEX cm18_index ON patent_claim_min_hash (m18);
CREATE INDEX cm19_index ON patent_claim_min_hash (m19);
CREATE INDEX cm20_index ON patent_claim_min_hash (m20);


