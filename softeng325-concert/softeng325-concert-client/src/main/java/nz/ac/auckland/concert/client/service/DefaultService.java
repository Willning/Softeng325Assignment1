package nz.ac.auckland.concert.client.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.xspec.M;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.Performer;
import nz.ac.auckland.concert.service.domain.User;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DefaultService implements ConcertService {

    private static String WEB_SERVICE_URI = "http://localhost:10000/services/resource";

    // AWS S3 access credentials for concert images.
    private static final String AWS_ACCESS_KEY_ID = "AKIAJOG7SJ36SFVZNJMQ";
    private static final String AWS_SECRET_ACCESS_KEY = "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";

    // Name of the S3 bucket that stores images.
    private static final String AWS_BUCKET = "concert2.aucklanduni.ac.nz";

    private static final String FILE_SEPARATOR = System
            .getProperty("file.separator");
    private static final String USER_DIRECTORY = System
            .getProperty("user.home");
    private static final String DOWNLOAD_DIRECTORY = USER_DIRECTORY
            + FILE_SEPARATOR + "images";

    private Cookie cookie; // held by the client to authenticate for later

    private Set<ConcertDTO> _concertCache = new HashSet<>();
    private Set<PerformerDTO> _performerCache = new HashSet<>();



    @Override
    public Set<ConcertDTO> getConcerts() throws ServiceException {
        Client client = ClientBuilder.newClient();

        try{
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/concerts")
                    .request()
                    .accept(MediaType.APPLICATION_XML);
            Response response= builder.get();

            Set<ConcertDTO> concerts;

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                //if ok status.
                concerts = response.readEntity(new GenericType<Set<ConcertDTO>>() {
                });
                _concertCache = concerts;
            }else if(response.getStatus() == Response.Status.NOT_MODIFIED.getStatusCode()){
                //get value from cache.
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
            }else if(response.getStatus() ==Response.Status.NOT_MODIFIED.getStatusCode()){
                performers = _performerCache;

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
        try{
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/user").request().accept(MediaType.APPLICATION_XML);

            Response response = builder.post(Entity.entity(newUser, MediaType.APPLICATION_XML));
            //post the xml to the server

            if (response.getStatus() == Response.Status.CREATED.getStatusCode()){
                cookie = (Cookie) response.getCookies().values().toArray()[0];
                return response.readEntity(new GenericType<UserDTO>(){

                });

                //Mission accomplished
            }else if(response.getStatus() == Response.Status.BAD_REQUEST.getStatusCode()) {
                throw new ServiceException(Messages.CREATE_USER_WITH_MISSING_FIELDS);

            }else if(response.getStatus() == Response.Status.CONFLICT.getStatusCode()){
                throw new ServiceException((Messages.CREATE_USER_WITH_NON_UNIQUE_NAME));

            }else{
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        }catch (Exception e){
            throw new ServiceException(e.getMessage());
        }finally {
            client.close();
        }

    }

    @Override
    public UserDTO authenticateUser(UserDTO user) throws ServiceException {
        Client client = ClientBuilder.newClient();

        try{
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/authenticate").request().accept(MediaType.APPLICATION_XML);

            Response response = builder.post(Entity.entity(user,MediaType.APPLICATION_XML));
            if(response.getStatus() == Response.Status.ACCEPTED.getStatusCode()){

                //this part is broke
                return response.readEntity(new GenericType<UserDTO>(){});

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
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }

        }catch (Exception e){
            throw new ServiceException(e.getMessage());

        }finally {
            client.close();
        }
    }

    @Override
    public Image getImageForPerformer(PerformerDTO performer) throws ServiceException {
       try{
           //First check if we have the image in local files.
           String performerName =performer.getImageName();

           try{
               File filePath = new File(performerName);
               return ImageIO.read(filePath);
           }catch (Exception e){
               //file no existo.

           }

           File file = new File("Images");
           file.mkdir();

           //If not, download the picture from the server.
           BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                   AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
           AmazonS3 s3 = AmazonS3ClientBuilder
                   .standard()
                   .withRegion(Regions.AP_SOUTHEAST_2)
                   .withCredentials(
                           new AWSStaticCredentialsProvider(awsCredentials))
                   .build();

           File imageFile = new File(file, performerName);

           S3Object s3object = s3.getObject(AWS_BUCKET, performerName);
           S3ObjectInputStream inputStream = s3object.getObjectContent();
           FileUtils.copyInputStreamToFile(inputStream, imageFile);

           return ImageIO.read(imageFile);

       }catch (Exception e){
           throw new ServiceException(e.getMessage());
       }
    }

    @Override
    public ReservationDTO reserveSeats(ReservationRequestDTO reservationRequest) throws ServiceException {

        if (cookie == null){
            throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
        }

        Client client = ClientBuilder.newClient();
        try {
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/make_request")
                    .request()
                    .accept(MediaType.APPLICATION_XML);

            Response response = builder.cookie("authenticationToken", cookie.getValue())
                    .post(Entity.entity(reservationRequest, MediaType.APPLICATION_XML));

            if (response.getStatus() == Response.Status.OK.getStatusCode()){
                return response.readEntity(ReservationDTO.class);

            }else if(response.getStatus()== Response.Status.UNAUTHORIZED.getStatusCode()){

                throw new ServiceException((Messages.BAD_AUTHENTICATON_TOKEN));
            }else if (response.getStatus() == Response.Status.BAD_REQUEST.getStatusCode()){

                throw new ServiceException(Messages.CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE);
            }else if(response.getStatus() == Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE.getStatusCode()){

                throw new ServiceException(Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION);
            }else{
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }

        }finally {
            client.close();
        }
    }

    @Override
    public void confirmReservation(ReservationDTO reservation) throws ServiceException {
        Client client = ClientBuilder.newClient();

        if (cookie == null){
            throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
        }

        //do the booking
        try {
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/confirm")
                    .request()
                    .accept(MediaType.APPLICATION_XML);

            Response response = builder
                    .cookie("authenticationToken", cookie.getValue())
                    .post(Entity.entity(reservation,MediaType.APPLICATION_XML));

            if (response.getStatus() == Response.Status.OK.getStatusCode()){
                return;

            }else if(response.getStatus()==Response.Status.LENGTH_REQUIRED.getStatusCode()){
                throw new ServiceException(Messages.CREDIT_CARD_NOT_REGISTERED);

            }else if (response.getStatus()==Response.Status.REQUEST_TIMEOUT.getStatusCode()){
                throw new ServiceException(Messages.EXPIRED_RESERVATION);

            }else if(response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()){
                throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
            }else{
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }

        }finally{
            client.close();
        }


    }

    @Override
    public void registerCreditCard(CreditCardDTO creditCard) throws ServiceException {
        Client client = ClientBuilder.newClient();

        if (cookie == null){
            throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
        }

        try {
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/register_credit_card")
                    .request()
                    .accept(MediaType.APPLICATION_XML);

            Response response = builder.cookie("authenticationToken", cookie.getValue())
                    .post(Entity.entity(creditCard, MediaType.APPLICATION_XML));
            //actually will need to post information to the server


            if (response.getStatus() == Response.Status.ACCEPTED.getStatusCode()) {
                //we can throw errors when something goes wrong? But what to do when something goes right?

                return;

            } else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {

                throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);

            } else if (response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                //no token, i.e. not authenticated
                throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
            } else {
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
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
        if (cookie == null) {
            throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
        }

        try {
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/bookings")
                    .request().accept(MediaType.APPLICATION_XML);
            Response response = builder.cookie("authenticationToken", cookie.getValue()).get();


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
            throw new ServiceException(e.getMessage());
        }finally {
            client.close();
        }
    }

    public void subscribeToService(){
        if (cookie == null) {
            throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
        }

        Client client = ClientBuilder.newClient();



    }

    public void unsubscribeFromService(){
        if (cookie == null) {
            throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
        }
        Client client = ClientBuilder.newClient();

        try{
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/unsubscribe")
                    .request(MediaType.APPLICATION_XML);
            Response response = builder
                    .cookie("authenticationToken", cookie.getValue())
                    .delete();
            if (response.getStatus() == Response.Status.OK.getStatusCode()){
                return;
            }else{

            }

        }finally {
            client.close();
        }



    }


}
