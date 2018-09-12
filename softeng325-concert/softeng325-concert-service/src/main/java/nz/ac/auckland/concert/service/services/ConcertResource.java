package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.service.domain.*;
import org.hibernate.service.spi.ServiceException;
import org.hibernate.sql.Select;
import sun.net.www.content.text.Generic;

import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
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
@Produces({javax.ws.rs.core.MediaType.APPLICATION_XML})
@Consumes({javax.ws.rs.core.MediaType.APPLICATION_XML})
public class ConcertResource {

    @GET
    @Path("/concerts")
    public Response getAllConcerts(){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        try {
            TypedQuery<Concert> concertQuery = em.createQuery("SELECT c FROM Concert c", Concert.class);
            List<Concert> concerts = concertQuery.getResultList();

            //if not empty convert to DTO and build response
            if (!concerts.isEmpty()) {
                Set<ConcertDTO> concertDTOs = new HashSet<>();
                for (Concert c : concerts) {
                    concertDTOs.add(c.convertToDTO());
                }

                GenericEntity<Set<ConcertDTO>> wrappedDTO = new GenericEntity<Set<ConcertDTO>>(concertDTOs) {
                };

                Response.ResponseBuilder builder = Response.ok().entity(wrappedDTO);


                return builder.build();
            } else {
                return Response.noContent().build();
            }
        }finally {
            em.close();
        }
    }

    @GET
    @Path("/performers")
    public Response getAllPerformers(){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        try {

            TypedQuery<Performer> performerQuery = em.createQuery("SELECT p FROM Performer p", Performer.class);
            List<Performer> performers = performerQuery.getResultList();

            if (performers.isEmpty()) {
                em.close();
                return Response.noContent().build();
            } else {
                Set<PerformerDTO> performerDTOS = new HashSet<>();

                for (Performer p : performers) {
                    performerDTOS.add(p.convertToDTO());
                }

                GenericEntity<Set<PerformerDTO>> wrappedDTOs = new GenericEntity<Set<PerformerDTO>>(performerDTOS) {
                };


                Response.ResponseBuilder builder = Response.ok(wrappedDTOs);
                return builder.build();
            }
        }finally {
            em.close();
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

        try {
            TypedQuery<User> userQuery = em.createQuery("SELECT u FROM User u WHERE u._token = :token", User.class).setParameter("token", token.getValue());

            User user = userQuery.getSingleResult();

            if (user == null) {
                //if no user, then invalid authentication token
                em.close();
                return Response.status(Response.Status.UNAUTHORIZED).build();
            } else {
                //find all BookingDTOS associated with the tokenID
                TypedQuery<Reservation> reservationQuery = em.createQuery("SELECT r FROM Reservation r WHERE r._user._token =:token", Reservation.class)
                        .setParameter("token", token.getValue());

                List<Reservation> reservations = reservationQuery.getResultList();

                Set<BookingDTO> bookingDTOS = new HashSet<>();

                for (Reservation r : reservations) {
                    bookingDTOS.add(r.makeBookingDTO());
                }
                em.close();
                //may need to wrap this to get it to work.
                GenericEntity<Set<BookingDTO>> wrappedDTOs = new GenericEntity<Set<BookingDTO>>(bookingDTOS) {
                };


                return Response.ok(wrappedDTOs).build();
            }
        }finally {
            em.close();
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
            Response.ResponseBuilder errorResponse = Response.status(Response.Status.BAD_REQUEST);

            return errorResponse.build();
        }

        if (userDTO.getFirstname() == "" || userDTO.getLastname() == ""
                ||userDTO.getPassword()==""||userDTO.getUsername()==""){
            //fail if any of the 4 fields are empty
            Response.ResponseBuilder errorResponse = Response.status(Response.Status.BAD_REQUEST);

            return errorResponse.build();
        }

        //need to also check if new user is unique

        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();
        try {

            User u = em.find(User.class, userDTO.getUsername());
            if (u != null) {
                //if non-unique user name, i.e. username already exists
                return Response.status(Response.Status.CONFLICT).build();
            }

            UUID token = UUID.randomUUID(); //generate a UUID,randomly should be fine?

            User user = new User(userDTO);
            user.set_token(token.toString()); //make this the token of the user
            user.set_tokenTimeStamp(LocalDateTime.now());
            em.persist(user);

            em.getTransaction().commit();

            Response.ResponseBuilder builder = Response.created(URI.create("/user/" + user.get_username()))
                    .entity(new GenericEntity<UserDTO> (user.convertToDTO()){
            })
                    .cookie(new NewCookie("authenticationToken", token.toString()));


            return builder.build();
        }finally {
            em.close();
        }

    }

    @POST
    @Path("/authenticate")
    public Response authenticateUser(UserDTO userDTO){

        if (userDTO.getUsername() == null || userDTO.getUsername() == ""){
            //don't allow empty usernames
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (userDTO.getPassword() == null || userDTO.getPassword() == ""){
            //don't allow empty passwords
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        try {

            User u = em.find(User.class, userDTO.getUsername());
            //attempt to find the user to see if the password/ username is empty

            if (u == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            if (u.get_password().equals(userDTO.getPassword())) {

                //if passwords don't match, UNAUTHORIZED.
                if (u.get_token() == null) {
                    //if the password matches but the user has no token
                    UUID userToken = UUID.randomUUID();
                    u.set_token(userToken.toString());

                    //merge because user already exists
                    em.merge(u);
                    em.getTransaction().commit();
                }

                GenericEntity<UserDTO> wrappedDTO = new GenericEntity<UserDTO>(u.convertToDTO()) {
                };

                return Response.accepted().entity(wrappedDTO).build();

            } else {
                em.close();
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        }finally {
            em.close();
        }
    }

    @POST
    @Path("/reservations")
    public Response reserveSeats(ReservationRequestDTO reservationRequestDTO, @CookieParam("authenticationToken") Cookie token){

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

        em.close();
        if (concert.getDates().contains(reservationRequestDTO.getDate())){
           //TODO do the thing here


        }else{
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        return null;
    }

    @POST
    @Path("/request")
    public Response requestReservation(ReservationRequestDTO requestDTO, @CookieParam("authenticationToken") Cookie token){
        if (token == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        try {
            //check if the user is valid
            TypedQuery<User> userQuery = em.createQuery("SELECT u FROM User u WHERE u._token = :token", User.class)
                    .setParameter("token", token.getValue());

            User user = userQuery.getSingleResult();

            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            TypedQuery<Concert> concertQuery = em.createQuery("SELECT c FROM Concert c WHERE c._id =:cid", Concert.class)
                    .setParameter("cid",requestDTO.getConcertId());

            Concert concert = concertQuery.getSingleResult();


            if (concert.getDates().contains(requestDTO.getDate())){
                TypedQuery<Seat> seatQuery = em
                        .createQuery("SELECT s FROM Seat s WHERE s._concert._id=:cid AND s._datetime=:date",Seat.class)
                        .setParameter("cid", concert.getID())
                        .setParameter("date",requestDTO.getDate())
                        .setLockMode(LockModeType.OPTIMISTIC);

                List<Seat> seatList = seatQuery.getResultList();

                //go through every seat and if it is free book it, if not free throw an error.


                return Response.ok().build();
            }else{
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

        }finally {
            em.close();
        }

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

        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (user.get_creditCard() == null){
            return Response.status(Response.Status.LENGTH_REQUIRED).build();
        }

        //find the reservation in database
        TypedQuery<Reservation> reservationQuery = em.createQuery("SELECT r FROM Reservation r WHERE r._rid=:rid", Reservation.class)
                .setParameter("rid", reservationDTO.getId());

        Reservation reservation= reservationQuery.getSingleResult();

        Set<SeatDTO> dtoSet = new HashSet<>();

        for (SeatDTO s:reservationDTO.getSeats()){
            //get all the seats in the reservation, then find all those seats and book them.
            dtoSet.add(s);
        }

        TypedQuery<Seat> seatQuery = em.createQuery("SELECT s FROM Seat s WHERE s._concert._id =:cid AND s._datetime =:dateTime AND s._seatCode IN (:seatCodes)", Seat.class)
                .setParameter("cid", reservationDTO.getReservationRequest().getConcertId())
                .setParameter("dateTime", reservationDTO.getReservationRequest().getDate())
                .setParameter("seatCodes",dtoSet )
                .setLockMode(LockModeType.OPTIMISTIC);

        //optimistic lock to provide scalability / no clashing

        List<Seat> seats = seatQuery.getResultList();
        em.close();

        //TODO the thing here
        return null;

    }

    @POST
    @Path("/register_credit_card")
    public Response registerCreditCard(CreditCardDTO creditCardDTO, @CookieParam("authenticationToken") Cookie token){
        //need to be able to catch errors
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        try {
            if (token == null){
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            //get user with the supplied token.
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u._token = :token", User.class)
                    .setParameter("token", token.getValue());

            User user = query.getSingleResult();

            if (user != null) {
                user.set_creditCard(new CreditCard(creditCardDTO));

                em.persist(user.get_creditCard()); //persist the credit card to the credit card table
                em.merge(user); //merge the user

                em.getTransaction().commit();
                return Response.accepted().build();

            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

        }finally{
            em.close();
        }

    }

}
