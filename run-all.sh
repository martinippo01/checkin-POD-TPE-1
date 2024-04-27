#!/usr/bin/env bash
#
#
# Ignore SC2155: Declare and assign separately to avoid masking return values.
# shellcheck disable=SC2155
#

# Enter script directory before sourcing anything

readonly COMPILED_DIR="grpc-com-tpe1-client-2024.1Q"

readonly SCRIPT_DIR="$(dirname "${BASH_SOURCE[0]}")"
if ! pushd "${SCRIPT_DIR}" &> /dev/null; then
    >&2 echo "Script directory not found (??): '$SCRIPT_DIR'"
    exit 1
fi

if ! pushd "$(pwd)/client/target/$COMPILED_DIR" &> /dev/null; then
    >&2 echo "Client target directory not found."
    exit 1
fi

#1.1
sh adminClient.sh -DserverAddress=10.6.0.1:50051 -Daction=addSector -Dsector=C
#rta:
#  Sector C added successfully

#1.2
sh adminClient.sh -DserverAddress=10.6.0.1:50051 -Daction=addCounters -Dsector=C -Dcounters=3
#rta:
#  3 new counters (2-4) in Sector C added successfully

#1.3
sh adminClient.sh -DserverAddress=10.6.0.1:50051 -Daction=manifest -DinPath=../manifest.csv
#rta:
#  Booking ABC123 for AirCanada AC987 added successfully
#  ...

#2.1
sh counterClient.sh -DserverAddress=10.6.0.1:50051 -Daction=listSectors
#rta:
#   Sectors   Counters
#   ###################
#   A         (1-1)
#   C         (2-4)(7-8)
#   D         (5-6)
#   Z         -

#2.2
sh counterClient.sh -DserverAddress=10.6.0.1:50051 -Daction=listCounters -Dsector=C -DcounterFrom=2 -DcounterTo=5
#rta:
#   Counters  Airline          Flights             People
#   ##########################################################
#   (2-3)     AmericanAirlines AA123|AA124|AA125   6
#   (4-4)     -                -                   -

sh counterClient.sh -DserverAddress=10.6.0.1:50051 -Daction=listCounters -Dsector=C -DcounterFrom=10 -DcounterTo=20
#rta:
#   Counters  Airline          Flights             People
#   ##########################################################

#2.3
sh counterClient.sh -DserverAddress=10.6.0.1:50051 -Daction=assignCounters -Dsector=C -Dflights=AA123|AA124|AA125 -Dairline=AmericanAirlines -DcounterCount=2
#rta:
#  2 counters (3-4) in Sector C are now checking in passengers from AmericanAirlines AA123|AA124|AA125 flights

sh counterClient.sh -DserverAddress=10.6.0.1:50051 -Daction=assignCounters -Dsector=C -Dflights=AA123|AA124|AA125 -Dairline=AmericanAirlines -DcounterCount=2
#rta:
#  2 counters in Sector C is pending with 5 other pendings ahead

#2.4
sh counterClient.sh -DserverAddress=10.6.0.1:50051 -Daction=freeCounters -Dsector=C -DcounterFrom=3 -Dairline=AmericanAirlines
#rta:
#  Ended check-in for flights AA123|AA124|AA125 on 2 counters (3-4) in Sector C

#2.5
sh counterClient.sh -DserverAddress=10.6.0.1:50051 -Daction=checkinCounters -Dsector=C -DcounterFrom=3 -Dairline=AmericanAirlines
#rta:
#   Check-in successful of XYZ345 for flight AA123 at counter 3
#   Counter 4 is idle

#2.6
sh counterClient.sh -DserverAddress=10.6.0.1:50051 -Daction=listPendingAssignments -Dsector=C
#rta:
#   Counters  Airline          Flights
#   ##########################################################
#   2         AirCanada        AC003
#   5         AmericanAirlines AA987|AA988
#   2         AirCanada        AC001

#3.1
sh passengerClient.sh -DserverAddress=10.6.0.1:50051 -Daction=fetchCounter -Dbooking=XYZ345
#rta:
#  Flight AA123 from AmericanAirlines is now checking in at counters (3-4) in Sector C with 7 people in line

sh passengerClient.sh -DserverAddress=10.6.0.1:50051 -Daction=fetchCounter -Dbooking=XYZ345
#rta:
#  Flight AA123 from AmericanAirlines has no counters assigned yet

#3.2
sh passengerClient.sh -DserverAddress=10.6.0.1:50051 -Daction=passengerCheckin -Dbooking=ABC123 -Dsector=C -Dcounter=3
#rta:
#  Booking ABC123 for flight AA123 from AmericanAirlines is now waiting to check-in on counters (3-4) in Sector C with 6 people in line

#3.3
sh passengerClient.sh -DserverAddress=10.6.0.1:50051 -Daction=passengerStatus -Dbooking=ABC123
#rta:
#  Booking ABC123 for flight AA123 from AmericanAirlines checked in at counter 4 in Sector C
#      Si está esperando en la cola para hacer el check-in
#  Booking ABC123 for flight AA123 from AmericanAirlines is now waiting to check-in on counters (3-4) in Sector C with 6 people in line
#      Y si todavía no ingresó en la cola
#   Booking ABC123 for flight AA123 from AmericanAirlines can check-in on counters (3-4) in Sector C

#4.1
sh eventsClient.sh -DserverAddress=10.6.0.1:50051 -Daction=register -Dairline=AmericanAirlines
#rta:
#  AmericanAirlines registered successfully for events
#  ...

#4.2
sh eventsClient.sh -DserverAddress=10.6.0.1:50051 -Daction=unregister -Dairline=AmericanAirlines
#rta:
#  AmericanAirlines unregistered successfully for events

#5.1
sh queryClient.sh -DserverAddress=10.6.0.1:50051 -Daction=queryCounters -DoutPath=../query1.txt
#rta:
#  $> cat ../query1.txt
#     Sector  Counters  Airline          Flights             People
#     ###############################################################
#     A       (1-1)     AirCanada        AC989               0
#     C       (2-3)     AmericanAirlines AA123|AA124|AA125   6
#     C       (4-4)     -                -                   -
#     C       (7-8)     -                -                   -
#     D       (5-6)     -                -                   -

sh queryClient.sh -DserverAddress=10.6.0.1:50051 -Daction=queryCounters -DoutPath=../query1.txt -Dsector=C
#rta:
#  $> cat ../query1.txt
#     Sector  Counters  Airline          Flights             People
#     ###############################################################
#     C       (2-3)     AmericanAirlines AA123|AA124|AA125   6
#     C       (4-4)     -                -                   -
#     C       (7-8)     -                -                   -

sh queryClient.sh -DserverAddress=10.6.0.1:50051 -Daction=queryCounters -DoutPath=../query1.txt -Dsector=Z
#rta:
#  $> cat ../query1.txt
#     Sector  Counters  Airline          Flights             People
#     ###############################################################

#5.2
sh queryClient.sh -DserverAddress=10.6.0.1:50051 -Daction=checkins -DoutPath=../query2.txt
#rta:
#  $> cat ../query2.txt
#     Sector  Counter   Airline           Flight     Booking
#     ###############################################################
#     C       2         AmericanAirlines  AA123      ABC321
#     C       3         AmericanAirlines  AA123      XYZ999
#     A       1         AirCanada         AC989      XYZ123

sh queryClient.sh -DserverAddress=10.6.0.1:50051 -Daction=checkins -DoutPath=../query2.txt -Dsector=C
#rta:
#  $> cat ../query2.txt
#     Sector  Counter   Airline           Flight     Booking
#     ###############################################################
#     C       2         AmericanAirlines  AA123      ABC321
#     C       3         AmericanAirlines  AA123      XYZ999

sh queryClient.sh -DserverAddress=10.6.0.1:50051 -Daction=checkins -DoutPath=../query2.txt -Dairline=AirCanada
#rta:
#   Sector  Counter   Airline           Flight     Booking
#   ###############################################################
#   A       1         AirCanada         AC989      XYZ123

sh queryClient.sh -DserverAddress=10.6.0.1:50051 -Daction=checkins -DoutPath=../query2.txt -Dairline=AirCanada
#rta:
#   Sector  Counter   Airline           Flight     Booking
#   ###############################################################


echo "Finished"

# Exit COMPILED_DIR
popd &> /dev/null || exit 0

# Exit script directory
popd &> /dev/null || exit 0