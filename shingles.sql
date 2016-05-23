CREATE OR REPLACE FUNCTION shingles(t text) 
RETURNS int[] AS 
$$ 
   DECLARE sentences text[]:= string_to_array(t, '.'); curr text; j int:= 0; num int:= array_length(sentences,1); int[] r = ARRAY[]::int[];
   BEGIN
	LOOP
	 EXIT WHEN j=num;
	 	curr:= sentences[j];
    	r:= r||ARRAY_AGG(DISTINCT hashtext(substring(curr FROM i FOR 6))::int) FROM generate_series(0,char_length(t)-6-1) i;
     j:= j+1;
    END LOOP;
    return r;
   END
$$ LANGUAGE plpgsql;