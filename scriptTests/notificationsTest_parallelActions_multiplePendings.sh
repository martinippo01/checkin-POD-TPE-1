#!/bin/bash

BASE_PATH="../client/target/grpc-com-tpe1-client-2024.1Q"
ADDRESS='localhost:50058'
SECTOR='A'
COUNTERS='10'
COUNTER_FROM='41'
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

# Assign airlines ahead
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=assignCounters -Dsector="$SECTOR" -Dflights=AC987 -Dairline=AirCanada -DcounterCount=10
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=assignCounters -Dsector="$SECTOR" -Dflights=AC988 -Dairline=AirCanada -DcounterCount=10
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=assignCounters -Dsector="$SECTOR" -Dflights=AC989 -Dairline=AirCanada -DcounterCount=10
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=assignCounters -Dsector="$SECTOR" -Dflights=AA123 -Dairline=AmericanAirlines -DcounterCount=10


# Try to assign counters to Flybondi
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=assignCounters -Dsector="$SECTOR" -Dflights="$FLIGHT" -Dairline="$AIRLINE" -DcounterCount="$COUNTERS"

# Assign airlines behind
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=assignCounters -Dsector="$SECTOR" -Dflights=AA124 -Dairline=AmericanAirlines -DcounterCount=10
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=assignCounters -Dsector="$SECTOR" -Dflights=AA125 -Dairline=AmericanAirlines -DcounterCount=10

# List counters and pending
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=listCounters -Dsector="$SECTOR" -DcounterFrom=1 -DcounterTo=100
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=listPendingAssignments -Dsector="$SECTOR"


# Step by step, add counters
bash adminClient.sh -DserverAddress="$ADDRESS" -Daction=addCounters -Dsector="$SECTOR" -Dcounters="$COUNTERS"
bash adminClient.sh -DserverAddress="$ADDRESS" -Daction=addCounters -Dsector="$SECTOR" -Dcounters="$COUNTERS"
bash adminClient.sh -DserverAddress="$ADDRESS" -Daction=addCounters -Dsector="$SECTOR" -Dcounters="$COUNTERS"
bash adminClient.sh -DserverAddress="$ADDRESS" -Daction=addCounters -Dsector="$SECTOR" -Dcounters="$COUNTERS"
bash adminClient.sh -DserverAddress="$ADDRESS" -Daction=addCounters -Dsector="$SECTOR" -Dcounters="$COUNTERS"

# Print final state of counters
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=listCounters -Dsector="$SECTOR" -DcounterFrom=1 -DcounterTo=100

# Free the counters for the airline
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=freeCounters -Dsector="$SECTOR" -DcounterFrom="$COUNTER_FROM" -Dairline="$AIRLINE"

# Unregister Airline from notifications
bash eventsClient.sh -DserverAddress="$ADDRESS" -Daction=unregister -Dairline="$AIRLINE"

