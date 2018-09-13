package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.service.domain.*;
import nz.ac.auckland.concert.utility.TheatreLayout;
import org.hibernate.service.spi.ServiceException;
import org.hibernate.sql.Select;
import sun.net.www.content.text.Generic;

import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.lang.reflect.Array;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Using this to implement a simple REST web service.
 */

@Path("/resource")
@Produces({javax.ws.rs.core.MediaType.APPLICATION_XML})
@Consumes({javax.ws.rs.core.MediaType.APPLICATION_XML})
public class ConcertResource {

    @GET
    @Path("/concerts")
    public Response getAllConcerts() {
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
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/performers")
    public Response getAllPerformers() {
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
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/bookings")
    public Response getAllBookings(@CookieParam("authenticationToken") Cookie token) {
        if (token == null) {
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
        } finally {
            em.close();
        }
    }


    @POST
    @Path("/user")
    public Response createNewUser(UserDTO userDTO) {
        //need to check if the DTO has all the fields
        if (userDTO.getFirstname() == null || userDTO.getLastname() == null
                || userDTO.getPassword() == null || userDTO.getUsername() == null) {
            //fail if any of the 4 fields are null
            //server should not throw an exception, should send a message to the user. User should throw exception.
            Response.ResponseBuilder errorResponse = Response.status(Response.Status.BAD_REQUEST);

            return errorResponse.build();
        }

        if (userDTO.getFirstname() == "" || userDTO.getLastname() == ""
                || userDTO.getPassword() == "" || userDTO.getUsername() == "") {
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
                    .entity(new GenericEntity<UserDTO>(user.convertToDTO()) {
                    })
                    .cookie(new NewCookie("authenticationToken", token.toString()));


            return builder.build();
        } finally {
            em.close();
        }

    }

    @POST
    @Path("/authenticate")
    public Response authenticateUser(UserDTO userDTO) {

        if (userDTO.getUsername() == null || userDTO.getUsername() == "") {
            //don't allow empty usernames
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (userDTO.getPassword() == null || userDTO.getPassword() == "") {
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
        } finally {
            em.close();
        }
    }


    @POST
    @Path("/make_request")
    public Response requestReservation(ReservationRequestDTO requestDTO, @CookieParam("authenticationToken") Cookie token) {
        if (token == null) {
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

            boolean transactionPass = true; //used to keep trying if no success.

            TypedQuery<Concert> concertQuery = em.createQuery("SELECT c FROM Concert c WHERE c._id =:cid", Concert.class)
                    .setParameter("cid", requestDTO.getConcertId());

            Concert concert = concertQuery.getSingleResult();

            //First user will end up with no seats, have to allocate here, ideally would initilize when concert is created
            if (concert.getDates().contains(requestDTO.getDate())) {
                while (transactionPass) {
                    try {
                        TypedQuery<Seat> seatQuery = em
                                .createQuery("SELECT s FROM Seat s WHERE s._concert._id=:cid AND s._datetime=:dateTime", Seat.class)
                                .setParameter("cid", concert.getID())
                                .setParameter("dateTime", requestDTO.getDate())
                                .setLockMode(LockModeType.OPTIMISTIC);

                        List<Seat> seatList = seatQuery.getResultList();

                        //what we want with optimistic lock is to keep trying until go through.

                        if (seatList.isEmpty()) {
                            //if the seatList is empty, i.e. we are the first users to book a  seat. then allocate all the seats as free
                            PriceBand priceBand;
                            for (SeatRow row : SeatRow.values()) {
                                if (TheatreLayout.getRowsForPriceBand(PriceBand.PriceBandA).contains(row)) {
                                    priceBand = PriceBand.PriceBandA;
                                } else if (TheatreLayout.getRowsForPriceBand(PriceBand.PriceBandB).contains(row)) {
                                    priceBand = PriceBand.PriceBandB;
                                } else {
                                    priceBand = PriceBand.PriceBandC;
                                }

                                for (int i = 1; i <= TheatreLayout.getNumberOfSeatsForRow(row); i++) {
                                    String seatCode = row.toString() + i;
                                    Seat seat = new Seat(seatCode);
                                    seat.set_status(Seat.Status.FREE);
                                    seat.set_concert(concert);
                                    seat.set_priceband(priceBand);
                                    seat.set_datetime(LocalDateTime.now());
                                    seatList.add(seat);
                                }
                            }
                        }

                        List<Seat> seatsToBook = new ArrayList<>();

                        for (Seat seat : seatList) {
                            if (seat.get_status().equals(Seat.Status.FREE) && seat.get_priceband().equals(requestDTO.getSeatType())) {
                                //do the thing with expired pending requests
                                //go through every seat and if it is free book it until we've reached the desired number.
                                seat.set_status(Seat.Status.PENDING);
                                seatsToBook.add(seat);
                            }
                            if (seatsToBook.size() == requestDTO.getNumberOfSeats()) {
                                break;
                            }
                        }

                        if (seatsToBook.size() < requestDTO.getNumberOfSeats()){
                            return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
                        }

                        Reservation reservation = new Reservation();

                        Set<SeatDTO> seatDTOS = new HashSet<>();

                        Set<String> seatCodes = new HashSet<>();
                        for (Seat seat : seatsToBook) {
                            seat.set_reservation(reservation);

                            seatDTOS.add(seat.convertToDTO());
                            em.persist(seat);

                            seatCodes.add(seat.getSeatCode());
                        }

                        reservation.set_concert(concert);
                        reservation.set_dateTime(requestDTO.getDate());
                        reservation.set_user(user);
                        reservation.set_priceBand(requestDTO.getSeatType());
                        reservation.set_rid(UUID.randomUUID().getMostSignificantBits());
                        reservation.set_seats(seatCodes);

                        ReservationDTO completeReservation = new ReservationDTO(reservation.get_rid(), requestDTO, seatDTOS);

                        em.persist(reservation);
                        em.getTransaction().commit();
                        transactionPass = false; //transaction has passed.
                        //Clear to respond as long as service encounters no errors.
                        return Response.ok(new GenericEntity<ReservationDTO>(completeReservation) {
                        }).build();
                    } catch (OptimisticLockException e) {
                        transactionPass = true;
                    }
                }
                return null;
                //code should not be able to reach here.
            } else {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } finally {
            em.close();
        }
    }


    @POST
    @Path("/confirm")
    public Response confirmReservation(ReservationDTO reservationDTO, @CookieParam("authenticationToken") Cookie token) {
        if (token == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        //find the user by token
        TypedQuery<User> userQuery = em.createQuery("SELECT u FROM User u WHERE u._token = :token", User.class)
                .setParameter("token", token.getValue());

        User user = userQuery.getSingleResult();

        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (user.get_creditCard() == null) {
            return Response.status(Response.Status.LENGTH_REQUIRED).build();
        }

        //find the reservation in database
        TypedQuery<Reservation> reservationQuery = em.createQuery("SELECT r FROM Reservation r WHERE r._rid=:rid", Reservation.class)
                .setParameter("rid", reservationDTO.getId());

        Reservation reservation = reservationQuery.getSingleResult();

        Set<SeatDTO> dtoSet = new HashSet<>();

        for (SeatDTO s : reservationDTO.getSeats()) {
            //get all the seats in the reservation, then find all those seats and book them.
            dtoSet.add(s);
        }

        TypedQuery<Seat> seatQuery = em.createQuery("SELECT s FROM Seat s WHERE s._concert._id =:cid AND s._datetime =:dateTime AND s._seatCode IN (:seatCodes)", Seat.class)
                .setParameter("cid", reservationDTO.getReservationRequest().getConcertId())
                .setParameter("dateTime", reservationDTO.getReservationRequest().getDate())
                .setParameter("seatCodes", dtoSet)
                .setLockMode(LockModeType.OPTIMISTIC);

        //optimistic lock to provide scalability / no clashing

        List<Seat> seats = seatQuery.getResultList();
        em.close();

        //TODO the thing here
        return null;

    }

    @POST
    @Path("/register_credit_card")
    public Response registerCreditCard(CreditCardDTO creditCardDTO, @CookieParam("authenticationToken") Cookie token) {
        //need to be able to catch errors
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        try {
            if (token == null) {
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

        } finally {
            em.close();
        }

    }

}
