package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.service.domain.jpa.SeatNumberConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@IdClass(SeatId.class)
@Table(name = "SEATS")
public class Seat {

    public Seat(){

    }

    //TODO make this work
    @Id
    @Column(name ="seatRow", nullable = false)
    @Enumerated(EnumType.STRING)
    private SeatRow _row;

    @Id
    @Column(name = "seatNumber", nullable = false)
    private int _number;


    @Id
    @ManyToOne
    private Concert _concert;

    @Id
    @Column(name = "datetime", nullable = false)
    private LocalDateTime _datetime;

    @ManyToOne
    private Reservation _reservation;

    private PriceBand _priceband;

    public Seat(SeatDTO seatDTO){
        _number =seatDTO.getNumber().intValue();
        _row = seatDTO.getRow();
    }


    public SeatDTO convertToDTO(){

        return new SeatDTO(_row, new SeatNumber(_number));
    }

}

class SeatId implements Serializable {
    //make this so we can use compound key?
    //primary key should be concert, date, row and number.
    Concert _concert;
    SeatRow _row;
    Integer _number;
    LocalDateTime _datetime;

    public SeatId(){

    }

    public SeatId(Concert concert, SeatRow row, SeatNumber number, LocalDateTime datetime){
        _concert=concert;
        _row = row;
        _number = number.intValue();
        _datetime = datetime;

    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = _concert.hashCode();
        result = result * prime + (_datetime != null ? _datetime.hashCode() :0);
        result = result * prime + (_row != null ? _row.hashCode() :0);
        result = result * prime + _number;

        return result;
    }

    @Override
    public boolean equals(Object o){
        if (this == o){
            return true;
        }
        if (o == null || this.getClass() != o.getClass()){
            return false;
        }
        SeatId seatId = (SeatId) o;
        // need to consider for if any of the fields are null.

        if (_concert != null ? !_concert.equals(seatId._concert) : seatId._concert != null){
            return false;
        }

        if (_datetime!=null ? !_datetime.equals((seatId._datetime): seatId._datetime !=null){
            return false;
        }

        if (_datetime!=null ? !_row.equals(seatId._row): seatId._row !=null){
            return false;
        }

        return (_number !=null ? seatId._number == null :_number == seatId._number);

    }

}
