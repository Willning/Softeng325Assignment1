package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.NewsDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.service.domain.NewsItem;
import nz.ac.auckland.concert.service.domain.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/newsresource")
@Produces({javax.ws.rs.core.MediaType.APPLICATION_XML})
@Consumes({javax.ws.rs.core.MediaType.APPLICATION_XML})
public class NewsResource {

    private Map<User,AsyncResponse> _subscriptions = new HashMap<>();

    private Map<AsyncResponse, User> _reverseMap = new HashMap<>();
    //surely theres a better way?

    @POST
    @Path("/subscribe")
    public Response subscribeUser(@Suspended AsyncResponse response, @CookieParam("authenticationToken") Cookie token){

        if  (token == null){
            //no token
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        try {

            TypedQuery<User> userQuery = em.createQuery("SELECT u FROM User u WHERE u._token=:token", User.class)
                    .setParameter("token", token);

            User user = userQuery.getSingleResult();

            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            TypedQuery<NewsItem> newsQuery = em.createQuery("SELECT n FROM News n", NewsItem.class);

            List<NewsItem> newsList = newsQuery.getResultList();
            //maybe check if the news item's timestamp is after the latest one.

            for (NewsItem item: newsList){
                if (user.get_lastMessage() == null || item.get_date().isAfter(user.get_lastMessage().get_date())){
                    response.resume(item.get_message());
                    //check every news item, if timestamp is bigger than the time of the latest message,
                    // resume response
                }
            }

            _subscriptions.put(user, response);
            _reverseMap.put(response,user);

        }finally {
            em.close();
        }

        return null;
    }


    @DELETE
    @Path("/unsubscribe")
    public Response unsubscribeUser(@CookieParam("authenticationToken") Cookie token){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        try {
            TypedQuery<User> userQuery = em.createQuery("SELECT u FROM User u WHERE u._token=:token", User.class)
                    .setParameter("token", token);

            User user = userQuery.getSingleResult();

            _reverseMap.remove(_subscriptions.get(user));
            _subscriptions.remove(user);

            return Response.ok().build();
        }finally {
            em.close();
        }
    }

    @POST
    @Path("/send")
    public Response sendNews(NewsDTO newsDTO){
        //send a message to all users
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();
        try {
            for (AsyncResponse response : _subscriptions.values()) {
                response.resume(newsDTO.get_newsMessage());

                User user = _reverseMap.get(response);
                //create a new news here?

                NewsItem newsItem = new NewsItem(newsDTO);
                user.set_lastMessage(newsItem);

                em.persist(newsItem);
                em.merge(user);
                em.getTransaction().commit();

                //then need to persist the last message to the database.
            }
        }finally {
            em.close();
        }
        return null;
    }

}
