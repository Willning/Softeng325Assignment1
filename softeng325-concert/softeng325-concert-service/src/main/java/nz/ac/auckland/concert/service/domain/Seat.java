package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.service.domain.jpa.SeatNumberConverter;

import javax.persistence.*;

@Entity
@Table(name = "SEATS")
public class Seat {
    //

    //TODO make this work
    @Id
    @Column(name ="seatRow", nullable = false)
    private String _rowString;

    private SeatRow _row;

    @Id
    @Column(name = "seatNumber", nullable = false)
    private int _number;


    @Id
    @ManyToOne
    private Concert _concert;

    @ManyToOne
    private Reservation _reservation;

    private PriceBand _priceband;

    public Seat(SeatDTO seatDTO){
        _number =seatDTO.getNumber().intValue();
        _row = seatDTO.getRow();
        _rowString = _row.toString();
    }


    public SeatDTO convertToDTO(){

        return new SeatDTO(_row, new SeatNumber(_number));
    }

}
