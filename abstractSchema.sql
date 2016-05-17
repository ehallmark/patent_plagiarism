

\connect patentdb

DROP TABLE IF EXISTS patent_min_hash; 
DROP SEQUENCE IF EXISTS patent_min_hash_uid_seq;

DO language 'plpgsql'
$$
DECLARE patent_hash text :=  'CREATE TABLE patent_min_hash('
    || string_agg('m' || i::text || ' integer', ',') || ');'
    FROM generate_series(1,200) As i;
BEGIN
    EXECUTE patent_hash; 
END;
$$ ;


ALTER TABLE patent_min_hash ADD COLUMN pub_doc_number varchar(100) PRIMARY KEY;
ALTER TABLE patent_min_hash ADD COLUMN uid serial;
CREATE INDEX m1_index ON patent_min_hash (m1);
CREATE INDEX m2_index ON patent_min_hash (m2);
CREATE INDEX m3_index ON patent_min_hash (m3);
CREATE INDEX m4_index ON patent_min_hash (m4);
CREATE INDEX m5_index ON patent_min_hash (m5);
CREATE INDEX m6_index ON patent_min_hash (m6);
CREATE INDEX m7_index ON patent_min_hash (m7);
CREATE INDEX m8_index ON patent_min_hash (m8);
CREATE INDEX m9_index ON patent_min_hash (m9);
CREATE INDEX m10_index ON patent_min_hash (m10);
CREATE INDEX m11_index ON patent_min_hash (m11);
CREATE INDEX m12_index ON patent_min_hash (m12);
CREATE INDEX m13_index ON patent_min_hash (m13);
CREATE INDEX m14_index ON patent_min_hash (m14);
CREATE INDEX m15_index ON patent_min_hash (m15);
CREATE INDEX m16_index ON patent_min_hash (m16);
CREATE INDEX m17_index ON patent_min_hash (m17);
CREATE INDEX m18_index ON patent_min_hash (m18);
CREATE INDEX m19_index ON patent_min_hash (m19);
CREATE INDEX m20_index ON patent_min_hash (m20);

