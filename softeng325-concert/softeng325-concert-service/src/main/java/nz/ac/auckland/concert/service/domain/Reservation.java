package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.common.types.PriceBand;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "RESERVATIONS")
public class Reservation {

    private long _rid;

    private Concert _concert;

    private User _user;

    private Set<Seat> _seats;

    private LocalDateTime _dateTime;

    private PriceBand _priceBand;

    public BookingDTO makeBooking(){
        //TODO make this only when confirmed

        Set<SeatDTO> seatDTOs = new HashSet<>();

        for (Seat seat:_seats){
            seatDTOs.add(seat.convertToDTO());
        }

        return new BookingDTO(_concert.getID(), _concert.getTitle(),_dateTime, seatDTOs, _priceBand);

    }


}
