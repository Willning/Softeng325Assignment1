package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.service.domain.Concert;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DefaultService implements ConcertService {

    private static String WEB_SERVICE_URI = "http://localhost:10000/services/resource";

    private Set<ConcertDTO> _concertCache;
    private Set<PerformerDTO> _performerCache;

    @Override
    public Set<ConcertDTO> getConcerts() throws ServiceException {
        Client client = ClientBuilder.newClient();


        try{
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/concerts").request().accept(MediaType.APPLICATION_XML);

            Set<ConcertDTO> concerts;

            Response response= builder.get();
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                //if ok status.
                concerts = response.readEntity(new GenericType<Set<ConcertDTO>>() {

                });
                _concertCache = concerts;

                //set up caching of concerts too.
            }else if (response.getStatus() == Response.Status.NOT_MODIFIED.getStatusCode()){
                concerts = _concertCache;
            }else{
                concerts = new HashSet<>();
            }
            client.close();
            return concerts;

        }catch(Exception e){
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }finally {
            client.close();
        }

    }

    @Override
    public Set<PerformerDTO> getPerformers() throws ServiceException {
        Client client = ClientBuilder.newClient();

        try {
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/performers").request().accept(MediaType.APPLICATION_XML);

            Set<PerformerDTO> performers;
            Response response = builder.get();

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                //if ok status.
                performers = response.readEntity(new GenericType<Set<PerformerDTO>>() {
                });
                _performerCache = performers;

                //set up caching of concerts too.
            }else if (response.getStatus() == Response.Status.NOT_MODIFIED.getStatusCode()){
               performers =_performerCache;
            }else{
               performers = new HashSet<>();
            }
            client.close();
            return performers;

        }catch (Exception e){
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }finally {
            client.close();
        }
    }

    @Override
    public UserDTO createUser(UserDTO newUser) throws ServiceException {
        Client client = ClientBuilder.newClient();

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
