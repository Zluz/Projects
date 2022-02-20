#!/bin/bash

export POST_BODY="
  {
    \"size\": 1,
    \"sort\": { \"updated\": \"desc\" },
    \"query\": {
      \"match_all\": {}
    }
  }"

export PREVIOUS=`curl -XPOST http://192.168.6.20:9200/status-weather/_search -H 'Content-Type: application/json' -d "$POST_BODY" 2>/dev/null`
export TIME_PREV=`echo $PREVIOUS | jq ".hits.hits[0]._source.updated"`
echo "TIME_PREV: $TIME_PREV"

# curl -XGET https://api.weather.gov/gridpoints/LWX/95,87/forecast | jq .
export WEATHER=`curl -XGET https://api.weather.gov/gridpoints/LWX/95,87/forecast 2>/dev/null`

# echo $WEATHER | jq '.properties' 

export TIME_NEW=`echo $WEATHER | jq '.properties.updated'`
echo "TIME_NEW:  $TIME_NEW"

if [ "$TIME_PREV" == "$TIME_NEW" ]; then
	echo "Weather forecast has not been updated since $TIME_NEW"
else
	echo "Weather forecast updated. POSTing new data for $TIME_NEW"
	
	rm /tmp/weather.json
	echo $WEATHER | jq '.properties' > /tmp/weather.json
	curl -X POST 'http://192.168.6.20:9200/status-weather/_doc/?pretty' -H 'Content-Type: application/json' -d @/tmp/weather.json
fi


