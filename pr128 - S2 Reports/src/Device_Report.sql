
SELECT 
    d.mac,
    
    CONCAT( TIMEDIFF( current_timestamp(), max_start ), "" ) as 'age',
    ip_address, 
    class, 
    IF( LENGTH( d.name ) = 42, 
    			concat( substring(d.name,1,22), "\n", substring(d.name,23,100) ), 
    			d.name ) as 'name',
    replace( replace( replace( replace( replace( 
    			trim( trim( both '\n' from d.options ) ), 
    			"\n ", "\n" ), " \n", "\n" ), "\t", " " ), 
    			"    ", " " ), "  ", " " ) as 'options'
    # s.*
FROM 
	device d,
    `session` s,
    
	( 	SELECT 
			MAX( start ) max_start,
            seq_Device
		FROM 
			session 
		GROUP BY 
            seq_Device 
	) recent_session
    
WHERE 
	TRUE 
    AND ( recent_session.max_start = s.start ) 
    AND ( recent_session.seq_Device = d.seq ) 
	AND ( s.seq_device = d.seq ) 
    
	AND ( max_start > NOW() - INTERVAL 100 DAY )
    
    AND ( 17 = LENGTH( d.mac ) )
    AND ( ip_address like "192.168.%" )

GROUP BY
	d.seq

ORDER BY 
    d.mac ASC;
