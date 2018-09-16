package nz.ac.auckland.concert.service.services;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/services")
public class ConcertApplication extends Application {


    public ConcertApplication(){

    }

    @Override
    public Set<Object> getSingletons(){
        Set<Object> singleTons= new HashSet<>();
        PersistenceManager manager = new PersistenceManager();
        singleTons.add(manager);

        //News resrouce maintains state and will be a singleton.

        singleTons.add(new NewsResource());

        return singleTons;
    }

    @Override
    public Set<Class<?>> getClasses(){
        Set<Class<?>> classSet = new HashSet<>();
        //ConcertResource uses resource per request.
        classSet.add(ConcertResource.class);
        return classSet;

    }
}
