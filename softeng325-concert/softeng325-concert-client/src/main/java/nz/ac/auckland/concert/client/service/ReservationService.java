package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.common.dto.*;

import java.awt.*;
import java.util.Set;

public class ReservationService implements ConcertService {



    @Override
    public Set<ConcertDTO> getConcerts() throws ServiceException {
        return null;
    }

    @Override
    public Set<PerformerDTO> getPerformers() throws ServiceException {
        return null;
    }

    @Override
    public UserDTO createUser(UserDTO newUser) throws ServiceException {
        return null;
    }

    @Override
    public UserDTO authenticateUser(UserDTO user) throws ServiceException {
        return null;
    }

    @Override
    public Image getImageForPerformer(PerformerDTO performer) throws ServiceException {
        return null;
    }

    @Override
    public ReservationDTO reserveSeats(ReservationRequestDTO reservationRequest) throws ServiceException {
        return null;
    }

    @Override
    public void confirmReservation(ReservationDTO reservation) throws ServiceException {

    }

    @Override
    public void registerCreditCard(CreditCardDTO creditCard) throws ServiceException {

    }

    @Override
    public Set<BookingDTO> getBookings() throws ServiceException {
        return null;
    }
}
