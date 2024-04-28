#!/bin/bash

BASE_PATH="../client/target/grpc-com-tpe1-client-2024.1Q"
ADDRESS='localhost:50058'
SECTOR='A'
COUNTERS='10'
COUNTER_FROM='1'
AIRLINE='FlyBondi'
BOOKING='FLY213'
FLIGHT='FO523'

MANIFEST_PATH="./manifest.csv"

# Take the manifest to the scripts directory
cp "$MANIFEST_PATH" "$BASE_PATH"

if ! pushd "$BASE_PATH"; then
        echo "Base path not found"
        exit 1
fi

# Do some actions so the registered notifications recieves notifications

# Assign counters to Flybondi, list the counters
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=assignCounters -Dsector="$SECTOR" -Dflights="$FLIGHT" -Dairline="$AIRLINE" -DcounterCount="$COUNTERS"
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=listSectors
# Try to do a check in when there are no passengers in queue
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=checkinCounters -Dsector="$SECTOR" -DcounterFrom="$COUNTER_FROM" -Dairline="$AIRLINE"

# Query the check ins that have been made
bash queryClient.sh -DserverAddress="$ADDRESS" -Daction=checkins -DoutPath=./should_not_exist.txt

# Add a passenger in queue
bash passengerClient.sh -DserverAddress="$ADDRESS" -Daction=passengerCheckin -Dbooking="$BOOKING" -Dsector="A" -Dcounter="$COUNTER_FROM"

# Try a new check in when there's only one in queue
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=checkinCounters -Dsector="$SECTOR" -DcounterFrom="$COUNTER_FROM" -Dairline="$AIRLINE"

# Query the check ins that have been made
bash queryClient.sh -DserverAddress="$ADDRESS" -Daction=checkins -DoutPath=./proper_check_in.txt

# Query the check ins that have been made
bash queryClient.sh -DserverAddress="$ADDRESS" -Daction=checkins -DoutPath=./proper_check_in_filter_A.txt -Dsector="$SECTOR"

# Query the check ins that have been made
bash queryClient.sh -DserverAddress="$ADDRESS" -Daction=checkins -DoutPath=./proper_check_in_filter_C.txt -Dsector=C

# Unregister Airline from notifications
bash eventsClient.sh -DserverAddress="$ADDRESS" -Daction=unregister -Dairline="$AIRLINE"

