package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.common.types.PriceBand;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "RESERVATIONS")
public class Reservation {
    @Id
    @Column(name = "r_id",nullable = false)
    private long _rid;

    @ManyToOne
    @JoinColumn(name = "concert", nullable = false)
    private Concert _concert;

    @ManyToOne
    @JoinColumn(name = "user", nullable = false)
    private User _user;

    @ElementCollection
    @CollectionTable(name = "RESERVED_SEATS", joinColumns = @JoinColumn(name = "r_id"))
    @Column(name = "seats", nullable = false)
    private Set<Seat> _seats;
    //change this to an integer or a string.

    @Column(name = "datetime")
    private LocalDateTime _dateTime;

    @Column(name = "priceband", nullable = false)
    @Enumerated(EnumType.STRING)
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
