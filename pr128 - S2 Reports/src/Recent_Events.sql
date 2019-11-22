
SELECT 
    # SUBSTRING( FROM_UNIXTIME( e.time / 1000 ), 1, 19 ) as 'time',
    SUBSTRING( CONVERT_TZ( FROM_UNIXTIME( e.time / 1000 ), '+00:00','+03:00' ), 1, 19 ) as 'time',
    SUBSTRING( CONCAT( "", TIMEDIFF( CURRENT_TIMESTAMP(), 
    				FROM_UNIXTIME( e.time / 1000 ) ) ), 1, 8 ) as 'age',
    seq_session as 'session',
    type,
    subject,
    # value,
    IF( ISNULL( data->'$."value-irl"' ), 
		value,
		CONCAT( "= ", ROUND( data->'$."value-irl"', 1 ), " ", 
				TRIM( BOTH '"' FROM data->'$."value-unit"' ) )
	) as "value",
    s.seq_device as 'Dx',
    data as 'json_data'
#    0, e.* 
FROM 
	s2db.event e
    LEFT JOIN s2db.session s ON ( e.seq_session = s.seq )
#    LEFT JOIN s2db.session s ON ( e.seq_session = s.seq ),
#    LEFT JOIN s2db.device d ON ( s.seq = d.seq_device )
    
# where subject like "%VEH%"
WHERE 
	TRUE
	AND ( NOT subject LIKE "HEARTBEAT_HOUR" )
#	and data like "%true%"
ORDER BY 
	e.seq DESC
LIMIT 100;
