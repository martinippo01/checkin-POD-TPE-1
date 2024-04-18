package ar.edu.itba.pod.tpe1.data;

import airport.CounterServiceOuterClass;
import airport.CounterServiceOuterClass.*;
import checkin.CheckinServiceOuterClass;
import checkin.CheckinServiceOuterClass.*;
import commons.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static checkin.CheckinServiceOuterClass.PassengerStatus.PASSENGER_STATUS_BOOKING_CODE_WITHOUT_AWAITING_PASSENGERS;
import static checkin.CheckinServiceOuterClass.PassengerStatus.PASSENGER_STATUS_CHECKIN_ALREADY_DONE;

public final class CheckIn {
    private final List<CounterServiceOuterClass.CheckInRecord> checkIns = Collections.synchronizedList(new ArrayList<>());

    private static CheckIn instance = null;

    private CheckIn() {
    }

    public static synchronized CheckIn getInstance() {
        if (instance == null) {
            instance = new CheckIn();
        }

        return instance;
    }

    public List<CounterServiceOuterClass.CheckInRecord> querygit(String sector, String airline) {
        return checkIns.stream()
                .filter(c -> (sector == null || c.getSector().equals(sector)) && (airline == null || c.getAirline().equals(airline)))
                .collect(Collectors.toList());
    }

    public List<CounterServiceOuterClass.CheckInRecord> queryCheckIns(String sector, String airline) {
        return new ArrayList<>();
    }

    public PassengerStatusResponse getPassengerStatus(String bookingCode) {
        BookingInformation.Builder bookingInformationBuilder = BookingInformation.newBuilder().setBookingCode(bookingCode);

        Optional<CheckInRecord> record = checkIns.stream().filter(c -> c.getBookingCode().equals(bookingCode)).findFirst();

        if (record.isEmpty()) {
            return PassengerStatusResponse.newBuilder()
                    .setStatus(PASSENGER_STATUS_BOOKING_CODE_WITHOUT_AWAITING_PASSENGERS)
                    .setBooking(bookingInformationBuilder.build())
                    .setEmpty(Service.Empty.newBuilder().build())
                    .build();
        }

        CounterInformation counterInformation = CounterInformation.newBuilder()
                .setCounter(record.get().getCounter())
                .setSectorName(record.get().getSector())
                .build();

        PassengerStatusResponse response = PassengerStatusResponse.newBuilder()
                .setStatus(PASSENGER_STATUS_CHECKIN_ALREADY_DONE)
                .setBooking(bookingInformationBuilder.build())
                .setCheckedInCounter(counterInformation)
                .build();

        return response;
    }
}
