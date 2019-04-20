
SELECT 
    concat( timediff( current_timestamp(), max_start ), "" ) as 'age',
    ip_address, class, d.name 
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
	AND ( s.seq_device = d.seq ) 

ORDER BY 
	( recent_session.max_start ) DESC;
