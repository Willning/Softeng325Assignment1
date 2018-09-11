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
    //TODO make this be stored wih a number or something

    @Version
    private int _version;

    private boolean _confirmed;

    public int get_version() {
        return _version;
    }

    public Seat(){

    }

    private SeatRow _row;

    private Integer _number; //fix this using the convert thing

    //Probably use this instead of row and column
    @Id
    @Column(name = "seatCode",nullable = false)
    private String _seatCode; //string consisting of merged seat row and seat number

    @Id
    @ManyToOne
    private Concert _concert;

    @Id
    @Column(name = "datetime", nullable = false)
    private LocalDateTime _datetime;

    @ManyToOne
    private Reservation _reservation;

    @Column(name = "priceband", nullable = false)
    @Enumerated(EnumType.STRING)
    private PriceBand _priceband;


    public Seat(SeatDTO seatDTO){
        _number =seatDTO.getNumber().intValue();
        _row = seatDTO.getRow();

        _seatCode = _row.toString() + _number.toString();
    }

    public Seat(String seatCode){
        _row = SeatRow.valueOf(String.valueOf(seatCode.charAt(0)));

        if (seatCode.length() == 2){
            _number = Character.getNumericValue(seatCode.charAt(1));

        }else if(seatCode.length() ==3){
            _number = Integer.parseInt(seatCode.substring(1));
        }
    }


    public SeatDTO convertToDTO(){

        return new SeatDTO(_row, new SeatNumber(_number));
    }

    public SeatRow get_row(){
        return _row;
    }

    public void set_row(SeatRow _row) {
        this._row = _row;
    }

    public Integer get_number() {
        return _number;
    }

    public void set_number(int _number) {
        this._number = _number;
    }

    public Concert get_concert() {
        return _concert;
    }

    public void set_concert(Concert _concert) {
        this._concert = _concert;
    }

    public LocalDateTime get_datetime() {
        return _datetime;
    }

    public void set_datetime(LocalDateTime _datetime) {
        this._datetime = _datetime;
    }

    public Reservation get_reservation() {
        return _reservation;
    }

    public void set_reservation(Reservation _reservation) {
        this._reservation = _reservation;
    }

    public PriceBand get_priceband() {
        return _priceband;
    }

    public void set_priceband(PriceBand _priceband) {
        this._priceband = _priceband;
    }


    public boolean is_confirmed() {
        return _confirmed;
    }

    public void set_confirmed(boolean _confirmed) {
        this._confirmed = _confirmed;
    }

    public String getSeatCode() {
        return _seatCode;
    }

    public void setSeatCode(String seatCode) {
        this._seatCode = seatCode;
    }
}

class SeatId implements Serializable {
    //make this so we can use compound key?
    //primary key should be concert, date, row and number.
    Concert _concert;
    String _seatCode;
    LocalDateTime _datetime;

    public SeatId(){

    }

    public SeatId(Concert concert, String seatCode, LocalDateTime datetime){
        _concert=concert;
        _seatCode = seatCode;
        _datetime = datetime;
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = _concert.hashCode();
        result = result * prime + (_datetime != null ? _datetime.hashCode() :0);
        result = result * prime + _seatCode.hashCode();

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

        if (_datetime!=null ? !_datetime.equals(seatId._datetime) : seatId._datetime !=null){
            return false;
        }

        return (_seatCode !=null ? seatId._seatCode == null :_seatCode == seatId._seatCode);

    }

}
