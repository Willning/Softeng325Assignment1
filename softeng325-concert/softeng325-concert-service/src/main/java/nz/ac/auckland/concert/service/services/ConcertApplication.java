package nz.ac.auckland.concert.service.services;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/services")
public class ConcertApplication extends Application {


    public ConcertApplication(){
        //soemthing is iffy with the manager
        //create the singleton manager here.
    }

    @Override
    public Set<Object> getSingletons(){
        Set<Object> persistenceManger = new HashSet<>();
        PersistenceManager manager = new PersistenceManager();
        persistenceManger.add(manager);

        return persistenceManger;
    }

    @Override
    public Set<Class<?>> getClasses(){
        Set<Class<?>> classSet = new HashSet<>();
        classSet.add(ConcertResource.class);
        return classSet;

    }
}
