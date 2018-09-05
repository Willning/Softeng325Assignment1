package nz.ac.auckland.concert.service.services;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class ConcertApplication extends Application {

    private Set<Object> _singletons = new HashSet<>();
    private Set<Class<?>> _classes = new HashSet<>();

    public ConcertApplication(){
        _singletons.add(new ConcertResource());

        PersistenceManager pm = new PersistenceManager().instance();

        //soemthing is iffy with the manager
        //create the singleton manager here.
    }

    @Override
    public Set<Object> getSingletons(){
        return _singletons;
    }

    @Override
    public Set<Class<?>> getClasses(){
        return _classes;
    }
}
