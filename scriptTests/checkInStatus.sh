#!/bin/bash

BOLD='\e[1m'
BLUE='\e[34m'
RED='\e[31m'
NC='\e[0m'

BASE_PATH="../client/target/grpc-com-tpe1-client-2024.1Q"
ADDRESS='localhost:50058'
SECTOR='A'
COUNTERS='10'
COUNTER_FROM='1'
AIRLINE_AIRCANADA='AirCanada'
AIRLINE_FLYBONDI='FlyBondi'
BOOKING_AIRCANADA_1='ABC123'
BOOKING_AIRCANADA_2='ABC125'
BOOKING_FLYBONDI='FLY213'
BOOKING_AMERICANAIRLINES='XYZ234'
FLIGHT_AIRCANADA='AC987'
FLIGHT_FLYBONDI='FO523'

MANIFEST_PATH="./manifest.csv"

# Take the manifest to the scripts directory
cp "$MANIFEST_PATH" "$BASE_PATH"

if ! pushd "$BASE_PATH"; then
	echo -e "${BOLD}${RED}Base path not found${NC}"
	exit 1
fi

# Add sector, counters and bookings
bash adminClient.sh -DserverAddress="$ADDRESS" -Daction=addSector -Dsector="$SECTOR"
bash adminClient.sh -DserverAddress="$ADDRESS" -Daction=addCounters -Dsector="$SECTOR" -Dcounters="$COUNTERS"
bash adminClient.sh -DserverAddress="$ADDRESS" -Daction=manifest -DinPath="$MANIFEST_PATH"

# Assign counters to AirCanada and Flybondi
echo -e "${BOLD}${BLUE}=> Should add 5 counters to AirCanada${NC}"
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=assignCounters -Dsector="$SECTOR" -Dflights="$FLIGHT_AIRCANADA" -Dairline="$AIRLINE_AIRCANADA" -DcounterCount="5"
echo -e "${BOLD}${BLUE}=> Should add 3 counters to FlyBondi${NC}"
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=assignCounters -Dsector="$SECTOR" -Dflights="$FLIGHT_FLYBONDI" -Dairline="$AIRLINE_FLYBONDI" -DcounterCount="3"
echo -e "${BOLD}${BLUE}=> Should list all sectors${NC}"
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=listSectors

# Try to do a check in when there are no passengers in queue
echo -e "${BOLD}${RED}=> Should fail due to no checkins having been made for AirCanada${NC}"
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=checkinCounters -Dsector="$SECTOR" -DcounterFrom="$COUNTER_FROM" -Dairline="$AIRLINE_AIRCANADA"

# Add a passenger in queue
echo -e "${BOLD}${BLUE}=> Should add two passengers to AirCanada queue${NC}"
bash passengerClient.sh -DserverAddress="$ADDRESS" -Daction=passengerCheckin -Dbooking="$BOOKING_AIRCANADA_1" -Dsector="$SECTOR" -Dcounter="$COUNTER_FROM"
bash passengerClient.sh -DserverAddress="$ADDRESS" -Daction=passengerCheckin -Dbooking="$BOOKING_AIRCANADA_2" -Dsector="$SECTOR" -Dcounter="$COUNTER_FROM"

# Status
echo -e "${BOLD}${BLUE}=> Should appear in queue${NC}"
bash passengerClient.sh -DserverAddress="$ADDRESS" -Daction=passengerStatus -Dbooking="$BOOKING_AIRCANADA_1"
bash passengerClient.sh -DserverAddress="$ADDRESS" -Daction=passengerStatus -Dbooking="$BOOKING_AIRCANADA_2"

echo -e "${BOLD}${RED}=> Should fail due to not being in queue${NC}"
bash passengerClient.sh -DserverAddress="$ADDRESS" -Daction=passengerStatus -Dbooking="$BOOKING_FLYBONDI"
echo -e "${BOLD}${RED}=> Should fail due to not having counters assigned${NC}"
bash passengerClient.sh -DserverAddress="$ADDRESS" -Daction=passengerStatus -Dbooking="$BOOKING_AMERICANAIRLINES"

# Try a new check in when there are two in queue
echo -e "${BOLD}${BLUE}=> Should check in two people in AirCanada${NC}"
bash counterClient.sh -DserverAddress="$ADDRESS" -Daction=checkinCounters -Dsector="$SECTOR" -DcounterFrom="$COUNTER_FROM" -Dairline="$AIRLINE_AIRCANADA"

echo -e "${BOLD}${BLUE}=> Should appear as already checked in${NC}"
bash passengerClient.sh -DserverAddress="$ADDRESS" -Daction=passengerStatus -Dbooking="$BOOKING_AIRCANADA_1"
bash passengerClient.sh -DserverAddress="$ADDRESS" -Daction=passengerStatus -Dbooking="$BOOKING_AIRCANADA_2"

echo -e "${BOLD}${RED}=> Should fail due to invalid booking${NC}"
bash passengerClient.sh -DserverAddress="$ADDRESS" -Daction=passengerStatus -Dbooking="NOT888"
 
# # Query the check ins that have been made
# bash queryClient.sh -DserverAddress="$ADDRESS" -Daction=checkins -DoutPath=./proper_check_in.txt

# Remove the manifest
rm "$MANIFEST_PATH"
