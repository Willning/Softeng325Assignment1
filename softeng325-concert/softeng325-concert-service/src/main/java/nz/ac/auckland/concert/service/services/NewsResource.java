package nz.ac.auckland.concert.service.services;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

public class NewsResource {

    @POST
    @Path("/subscribe")
    public Response subscribeUser(){
        return null;
    }


    @DELETE
    @Path("/unsubscribe")
    public Response unsubscribeUser(){
        return null;
    }

    @POST
    @Path("/send")
    public Response sendNews(){
        return null;
    }

}
