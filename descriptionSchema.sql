

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
ALTER TABLE patent_description_min_hash ADD COLUMN uid serial;
CREATE INDEX dm1_index ON patent_description_min_hash (m1);
CREATE INDEX dm2_index ON patent_description_min_hash (m2);
CREATE INDEX dm3_index ON patent_description_min_hash (m3);
CREATE INDEX dm4_index ON patent_description_min_hash (m4);
CREATE INDEX dm5_index ON patent_description_min_hash (m5);
CREATE INDEX dm6_index ON patent_description_min_hash (m6);
CREATE INDEX dm7_index ON patent_description_min_hash (m7);
CREATE INDEX dm8_index ON patent_description_min_hash (m8);
CREATE INDEX dm9_index ON patent_description_min_hash (m9);
CREATE INDEX dm10_index ON patent_description_min_hash (m10);
CREATE INDEX dm11_index ON patent_description_min_hash (m11);
CREATE INDEX dm12_index ON patent_description_min_hash (m12);
CREATE INDEX dm13_index ON patent_description_min_hash (m13);
CREATE INDEX dm14_index ON patent_description_min_hash (m14);
CREATE INDEX dm15_index ON patent_description_min_hash (m15);


