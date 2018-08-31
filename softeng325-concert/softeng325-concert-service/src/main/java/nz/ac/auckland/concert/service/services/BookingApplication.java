package nz.ac.auckland.concert.service.services;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class BookingApplication extends Application {

    private Set<Object> _singletons = new HashSet<>();
    private Set<Class<?>> _classes = new HashSet<>();

    public BookingApplication(){
        _singletons.add(new ConcertResource());

        PersistenceManager manager = new PersistenceManager().instance();
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
