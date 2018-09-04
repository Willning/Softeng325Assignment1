package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.CreditCardDTO;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "CREDIT_CARDS")
public class CreditCard {


    private CreditCardDTO.Type _type;
    private String _name;
    private String _number;
    private LocalDate _expiryDate;

    public CreditCard(){

    }

    public CreditCard(CreditCardDTO creditCardDTO){
        _type = creditCardDTO.getType();
        _name = creditCardDTO.getName();
        _number = creditCardDTO.getNumber(); //horrendous security flaw here
        _expiryDate = creditCardDTO.getExpiryDate();
    }

    private CreditCardDTO.Type getCardType(){
        return _type;
    }

    private String getName(){
        return _number;
    }

    private String getNumber(){
        return _number;
    }

    private LocalDate getExpiry(){
        return _expiryDate;
    }

}
