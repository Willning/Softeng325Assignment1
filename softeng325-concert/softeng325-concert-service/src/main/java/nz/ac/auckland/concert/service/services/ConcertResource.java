package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.Performer;
import nz.ac.auckland.concert.service.domain.Reservation;
import nz.ac.auckland.concert.service.domain.User;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        TypedQuery<Concert> concertQuery = em.createQuery("SELECT c FROM CONCERTS c", Concert.class);
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

            Response.ResponseBuilder builder = Response.ok(concertDTOs);

            return builder.build();
        }

    }

    @GET
    @Path("/performers")
    public Response getPerformers(){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        TypedQuery<Performer> performerQuery = em.createQuery("SELECT p FROM PERFORMERS p", Performer.class);
        List<Performer> performers = performerQuery.getResultList();

        if(performers.isEmpty()){
            return Response.noContent().build();
        }else{
            Set<PerformerDTO> performerDTOS = new HashSet<>();

            for (Performer p : performers){
                performerDTOS.add(p.convertToDTO());
            }

            em.close();

            Response.ResponseBuilder builder = Response.ok(performerDTOS);
            return builder.build();
        }
    }

    @GET
    @Path("/bookings")
    public Response getAllBookings(@CookieParam("authenitcationToken") Cookie token){
        if (token == null){
            return Response.status(Response.Status.NOT_FOUND).build();
            //if cookies is not presented, give a not found error, user not found.
        }

        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();
        TypedQuery<User> userQuery =  em.createQuery("SELECT u FROM USERS u WHERE u.token = :token", User.class).setParameter("token",token.getValue());

        User user = userQuery.getSingleResult();

        if (user == null){
            //if no user, then invalid authentication token
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }else{
            //find all BookingDTOS associated with the tokenID
            return null;
        }

    }


    @POST
    @Path("/user")
    public Response createNewUser(UserDTO newUser){
        return null;

    }

}
