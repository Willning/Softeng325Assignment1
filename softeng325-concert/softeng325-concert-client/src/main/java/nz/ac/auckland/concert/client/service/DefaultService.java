package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.User;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DefaultService implements ConcertService {

    private static String WEB_SERVICE_URI = "http://localhost:10000/services/resource";

    private Set<PerformerDTO> _performerCache;

    private Cookie cookie; // held by the service to authenticate.


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

            }else if (response.getStatus() == Response.Status.NOT_MODIFIED.getStatusCode()){
                concerts = response.readEntity(new GenericType<Set<ConcertDTO>>() {
                });

            }else{
                concerts = new HashSet<>();
            }
            client.close();
            return concerts;

        }catch(Exception e){
            throw new ServiceException(e.getMessage());
            //throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
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

            }else if (response.getStatus() == Response.Status.NOT_MODIFIED.getStatusCode()){
               performers =_performerCache;
            }else{
               performers = new HashSet<>();
            }
            client.close();
            return performers;

        }catch (Exception e){
            throw new ServiceException(e.getMessage());
            //throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }finally {
            client.close();
        }
    }

    @Override
    public UserDTO createUser(UserDTO newUser) throws ServiceException {

        Client client = ClientBuilder.newClient();
        try{
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/user").request().accept(MediaType.APPLICATION_XML);

            Response response = builder.post(Entity.entity(newUser, MediaType.APPLICATION_XML));
            //post the xml to the server

            if (response.getStatus() == Response.Status.CREATED.getStatusCode()){

                //Mission accomplished
            }else if(response.getStatus() == Response.Status.BAD_REQUEST.getStatusCode()) {
                throw new ServiceException(Messages.CREATE_USER_WITH_MISSING_FIELDS);

            }else if(response.getStatus() == Response.Status.CONFLICT.getStatusCode()){
                throw new ServiceException((Messages.CREATE_USER_WITH_NON_UNIQUE_NAME));

            }else{
                throw new ServiceException("Unexpected HTTP code");
            }
        }catch (Exception e){
            throw new ServiceException(e.getMessage());
            //throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }finally {
            client.close();
        }


        return null;
    }

    @Override
    public UserDTO authenticateUser(UserDTO user) throws ServiceException {
        Client client = ClientBuilder.newClient();

        try{
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/authenticate").request().accept(MediaType.APPLICATION_XML);

            Response response = builder.post(Entity.entity(user,MediaType.APPLICATION_XML));
            if(response.getStatus() == Response.Status.ACCEPTED.getStatusCode()){

                return response.readEntity(new GenericType<UserDTO>(){
                });

            }else if (response.getStatus() == Response.Status.BAD_REQUEST.getStatusCode()) {

                throw new ServiceException(Messages.AUTHENTICATE_USER_WITH_MISSING_FIELDS);
                //if either username or password is invalid
            }else if(response.getStatus()==Response.Status.NOT_FOUND.getStatusCode()){

                throw new ServiceException(Messages.AUTHENTICATE_NON_EXISTENT_USER);
                //no user found
            }else if (response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()){
                //password is wrong
                throw new ServiceException(Messages.AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD);

            }else{
                throw new ServiceException("Failed with error code" + + response.getStatus());
            }

        }catch (Exception e){
            //throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            throw new ServiceException(e.getMessage());
        }finally {
            client.close();
        }
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
        Client client = ClientBuilder.newClient();

        try {
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/register_credit_card").request().accept(MediaType.APPLICATION_XML);

            Response response = builder.cookie("authenticationToken", cookie.getValue())
                    .post(Entity.entity(creditCard, MediaType.APPLICATION_XML));
            //actually will need to post information to the server


            if (response.getStatus() == Response.Status.ACCEPTED.getStatusCode()) {
                //we can throw errors when something goes wrong? But what to do when something goes right?

            } else if (response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
            } else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                //no token, i.e. not authenticated
                throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);

            } else {
                throw new ServiceException("Failed with error code " + response.getStatus());
            }
        }catch (Exception e){
            throw new ServiceException(e.getMessage());
        }finally {
            client.close();
        }
    }

    @Override
    public Set<BookingDTO> getBookings() throws ServiceException {
        Client client = ClientBuilder.newClient();
        try {
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/booking").request().accept(MediaType.APPLICATION_XML);
            Response response = builder.get();


            if (response.getStatus()== Response.Status.OK.getStatusCode()){
                return response.readEntity(new GenericType<Set<BookingDTO>>(){
                });
            }else if(response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()){

                throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);

            }else if(response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()){
                throw new ServiceException((Messages.BAD_AUTHENTICATON_TOKEN));
            }else{
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }


        }catch (Exception e){
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }finally {
            client.close();
        }

    }
}
