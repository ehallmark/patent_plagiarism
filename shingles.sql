CREATE OR REPLACE FUNCTION words(t text) 
RETURNS text AS 
$$ BEGIN
    RETURN regexp_replace(coalesce(lower(t),''), '[^a-z]', '', 'g');
   END
$$ LANGUAGE plpgsql;

