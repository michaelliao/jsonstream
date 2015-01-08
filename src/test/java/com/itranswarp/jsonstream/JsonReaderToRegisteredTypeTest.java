package com.itranswarp.jsonstream;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.Test;

import com.itranswarp.jsonstream.adapter.DateTypeAdapter;
import com.itranswarp.jsonstream.adapter.TimeTypeAdapter;
import com.itranswarp.jsonstream.annotation.Pattern;

public class JsonReaderToRegisteredTypeTest {

    @Test
    public void testUseTypeAdapter() throws Exception {
        String s = "{ \"rooms\": 1, \"start\": \"2015-01-01\", \"end\": \"2015-01-05\", \"breakfast\": \"07:10\" }";
        JsonReader js = new JsonBuilder()
                .registerTypeAdapter(LocalDate.class, new DateTypeAdapter())
                .registerTypeAdapter(LocalTime.class, new TimeTypeAdapter())
                .createReader(s);
        HotelBooking hotel = js.parse(HotelBooking.class);
        assertEquals(1, hotel.rooms);
        assertEquals(LocalDate.of(2015, 1, 1), hotel.start);
        assertEquals(LocalDate.of(2015, 1, 5), hotel.end);
        assertEquals(LocalTime.of(7, 10), hotel.breakfast);
    }

    @Test(expected=ValidateException.class)
    public void testUseTypeAdapterWithValidation() throws Exception {
        String s = "{ \"rooms\": 1, \"start\": \"2015-A1-01\", \"end\": \"2015-01-05\", \"breakfast\": \"07:10\" }";
        JsonReader js = new JsonBuilder()
                .registerTypeAdapter(LocalDate.class, new DateTypeAdapter())
                .registerTypeAdapter(LocalTime.class, new TimeTypeAdapter())
                .createReader(s);
        js.parse(HotelBooking.class);
    }
}

class HotelBooking {
    int rooms;
    @Pattern("^\\d\\d\\d\\d\\-\\d\\d\\-\\d\\d$")
    LocalDate start;
    LocalDate end;
    LocalTime breakfast;
}
