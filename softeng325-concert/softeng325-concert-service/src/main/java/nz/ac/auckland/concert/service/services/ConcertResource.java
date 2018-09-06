package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.service.domain.*;
import org.hibernate.service.spi.ServiceException;

import javax.persistence.EntityManager;
import javax.persistence.PostLoad;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Using this to implement a simple REST web service.
 */

@Path("/resource")
@Produces({MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_XML})
public class ConcertResource {

    @GET
    @Path("/concerts")
    public Response getAllConcerts(){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        TypedQuery<Concert> concertQuery = em.createQuery("SELECT c FROM Concert c", Concert.class);
        List<Concert> concerts = concertQuery.getResultList();
        //if not empty convert to DTOs and build response

        if (concerts.isEmpty()){

            return Response.noContent().build();
        }else{
            Set<ConcertDTO> concertDTOs = new HashSet<>();
            for (Concert c : concerts) {
                concertDTOs.add(c.convertToDTO());
            }

            em.close();
            GenericEntity<Set<ConcertDTO>> wrappedDTO = new GenericEntity<Set<ConcertDTO>>(concertDTOs) {
            };

            Response.ResponseBuilder builder = Response.ok(wrappedDTO);
            //might need to wrap in a generic entity for client

            return builder.build();
        }

    }

    @GET
    @Path("/performers")
    public Response getAllPerformers(){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        TypedQuery<Performer> performerQuery = em.createQuery("SELECT p FROM Performer p", Performer.class);
        List<Performer> performers = performerQuery.getResultList();

        if(performers.isEmpty()){
            return Response.noContent().build();
        }else{
            Set<PerformerDTO> performerDTOS = new HashSet<>();

            for (Performer p : performers){
                performerDTOS.add(p.convertToDTO());
            }

            em.close();

            GenericEntity<Set<PerformerDTO>> wrappedDTOs = new GenericEntity<Set<PerformerDTO>>(performerDTOS) {
            };


            Response.ResponseBuilder builder = Response.ok(wrappedDTOs);
            return builder.build();
        }
    }

    @GET
    @Path("/bookings")
    public Response getAllBookings(@CookieParam("authenticationToken") Cookie token){
        if (token == null){
            return Response.status(Response.Status.NOT_FOUND).build();
            //if cookies is not presented, give a not found error, user not found.
        }

        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();
        TypedQuery<User> userQuery =  em.createQuery("SELECT u FROM User u WHERE u._token = :token", User.class).setParameter("token",token.getValue());

        User user = userQuery.getSingleResult();

        if (user == null){
            //if no user, then invalid authentication token
            em.close();
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }else{
            //find all BookingDTOS associated with the tokenID
            TypedQuery<Reservation> reservationQuery = em.createQuery("SELECT r FROM Reservation r WHERE r._user._token =:token", Reservation.class)
                    .setParameter("token", token.getValue());

            List<Reservation> reservations = reservationQuery.getResultList();

            Set<BookingDTO>bookingDTOS = new HashSet<>();

            for(Reservation r: reservations){
                bookingDTOS.add(r.makeBookingDTO());
            }
            em.close();
            //may need to wrap this to get it to work.
            GenericEntity<Set<BookingDTO>> wrappedDTOs = new GenericEntity<Set<BookingDTO>>(bookingDTOS) {
            };


            return Response.ok(wrappedDTOs).build();
        }

    }


    @POST
    @Path("/user")
    public Response createNewUser(UserDTO userDTO){
        //need to check if the DTO has all the fields
        if (userDTO.getFirstname() == null || userDTO.getLastname() == null
                ||userDTO.getPassword()==null||userDTO.getUsername()==null){
            //fail if any of the 4 fields are null
            //server should not throw an exception, should send a message to the user. User should throw exception.
            Response.ResponseBuilder errorRespsonse = Response.status(Response.Status.BAD_REQUEST);
            return errorRespsonse.build();
        }

        if (userDTO.getFirstname() == "" || userDTO.getLastname() == ""
                ||userDTO.getPassword()==""||userDTO.getUsername()==""){
            //fail if any of the 4 fields are empty
            Response.ResponseBuilder errorRespsonse = Response.status(Response.Status.BAD_REQUEST);
            return errorRespsonse.build();
        }

        //need to also check if new user is unique


        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        User u = em.find(User.class, userDTO.getUsername());
        if (u!= null){
            //if non-unique user name, i.e. username already exists
            return Response.status(Response.Status.CONFLICT).build();
        }

        UUID token = UUID.randomUUID(); //generate a UUID

        User user = new User(userDTO);
        user.set_token(token.toString()); //make this the token of the user
        user.set_tokenTimeStamp(LocalDateTime.now());

        em.persist(user);

        em.getTransaction().commit();

        Response.ResponseBuilder builder = Response.created(URI.create("/user/" + user.get_username()));
        return builder.build();

    }

    @POST
    @Path("/authenticate")
    public Response authenticateUser(UserDTO userDTO){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        if (userDTO.getUsername() == null || userDTO.getUsername() == ""){
            //don't allow empty usernames
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (userDTO.getPassword() == null || userDTO.getPassword() == ""){
            //don't allow empty passwords
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        User u = em.find(User.class, userDTO.getUsername());
        //attempt to find the user.

        if (u == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (u.get_password() == userDTO.getPassword()){
            //if passwords don't match, UNAUTHORIZED.
            if(u.get_token()==null){
                //if the password matches but the used has no token
                UUID userToken = UUID.randomUUID();
                u.set_token(userToken.toString());

                //merge because user already exists
                em.merge(u);
                em.getTransaction().commit();
                em.close();

                return Response.accepted().entity(u.convertToDTO()).build();

            }

        }else{
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        //code shouldn't reach here.
        return null;

    }

    @POST
    @Path("/reservations")
    public Response reserveSeats(ReservationRequestDTO reservationRequestDTO, @CookieParam("authenitcationToken") Cookie token){

        if (token == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        //search out the concert id

        TypedQuery<Concert> concertQuery = em.createQuery("SELECT c FROM Concert c WHERE c._id =:cid", Concert.class)
                .setParameter("cid",reservationRequestDTO.getConcertId());

        Concert concert = concertQuery.getSingleResult();
        //since cid is unique, should have single result

        if (concert.getDates().contains(reservationRequestDTO.getDate())){
           //TODO do the thing here


        }else{
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        return null;

    }

    @POST
    @Path("/booking")
    public Response confirmReservation(ReservationDTO reservationDTO, @CookieParam("authenticationToken") Cookie token){
        if (token == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        //find the user by token
        TypedQuery<User> userQuery = em.createQuery("SELECT u FROM User u WHERE u._token = :token",User.class)
                .setParameter("token",token.getValue());

        User user = userQuery.getSingleResult();

        //find the reservation in database
        TypedQuery<Reservation> reservationQuery = em.createQuery("SELECT r FROM Reservation r WHERE r._rid=:rid", Reservation.class)
                .setParameter("rid", reservationDTO.getId());

        Reservation reservation= reservationQuery.getSingleResult();

        //TODO the thing here
        return null;

    }

    @POST
    @Path("/register_credit_card")
    public Response registerCreditCard(CreditCardDTO creditCardDTO, @CookieParam("authenticationToken") Cookie token){
        //need to be able to catch errors

        if (token == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        //get user with the supplied token.
        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u._token = :token", User.class).setParameter("token", token.getValue());
        User user = query.getSingleResult();

        if (user != null) {
            user.set_creditCard(new CreditCard(creditCardDTO));
            em.persist(user.get_creditCard()); //persist the credit card to the crdit card table
            em.merge(user); //merge the user

            em.getTransaction().commit();

            return Response.accepted().build();
        }else{
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

    }

}
