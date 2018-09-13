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
    private Set<String> _seats;
    /*reservation has valuetype of seats, seats that a reservation have should not persist to other reservations
    until decision commited
     */
    //format is ROW +NUMBER

    @Column(name = "datetime")
    private LocalDateTime _dateTime;

    @Column(name = "priceband", nullable = false)
    @Enumerated(EnumType.STRING)
    private PriceBand _priceBand;

    @Column(name = "booked")
    private boolean booked = false;

    public BookingDTO makeBookingDTO(){
        if (booked){

            Set<SeatDTO> seatDTOs = new HashSet<>();

            for (String seatCode : _seats) {
                seatDTOs.add(new Seat(seatCode).convertToDTO());
            }

            return new BookingDTO(_concert.getID(), _concert.getTitle(), _dateTime, seatDTOs, _priceBand);
        }
        return null;

    }

    public long get_rid() {
        return _rid;
    }

    public void set_rid(long _rid) {
        this._rid = _rid;
    }

    public Concert get_concert() {
        return _concert;
    }

    public void set_concert(Concert _concert) {
        this._concert = _concert;
    }

    public User get_user() {
        return _user;
    }

    public void set_user(User _user) {
        this._user = _user;
    }

    public Set<String> get_seats() {
        return _seats;
    }

    public void set_seats(Set<String> _seats) {
        this._seats = _seats;
    }

    public LocalDateTime get_dateTime() {
        return _dateTime;
    }

    public void set_dateTime(LocalDateTime _dateTime) {
        this._dateTime = _dateTime;
    }

    public PriceBand get_priceBand() {
        return _priceBand;
    }

    public void set_priceBand(PriceBand _priceBand) {
        this._priceBand = _priceBand;
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }
}
