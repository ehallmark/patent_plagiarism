CREATE OR REPLACE FUNCTION words(t text) 
RETURNS text AS 
$$ BEGIN
    RETURN ARRAY_TO_STRING(ARRAY_AGG(regexp_replace(coalesce(lower(temp.result),''), '[^a-z]', '', 'g')), '') FROM (SELECT * from UNNEST(STRING_TO_ARRAY(t,' ')) as result where char_length(regexp_replace(result, '[^a-z]', '', 'g')) > 2) as temp; 
   END
$$ LANGUAGE plpgsql;

