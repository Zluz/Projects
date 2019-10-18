
SELECT 
    TIME( FROM_UNIXTIME( e.time / 1000 ) ) as 'time',
    CONCAT( "", TIMEDIFF( CURRENT_TIMESTAMP(), 
    				FROM_UNIXTIME( e.time / 1000 ) ) ) as 'age',
    seq_session as 'session',
    type,
    subject,
    # value,
    IF( ISNULL( data->'$."value-irl"' ), 
		value,
		CONCAT( "= ", ROUND( data->'$."value-irl"', 1 ), " ", 
				TRIM( BOTH '"' FROM data->'$."value-unit"' ) )
	) as "value",
    data as 'json_data'
#    0, e.* 
FROM 
	s2db.event e 
# where subject like "%VEH%"
WHERE 
	TRUE
	AND ( NOT subject LIKE "HEARTBEAT_HOUR" )
#	and data like "%true%"
ORDER BY 
	seq DESC
LIMIT 100;

