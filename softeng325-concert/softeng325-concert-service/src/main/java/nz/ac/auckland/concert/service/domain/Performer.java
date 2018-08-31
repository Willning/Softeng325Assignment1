package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.types.Genre;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.HashSet;
import java.util.Set;

@Entity
@XmlAccessorType(XmlAccessType.FIELD)
@Table(name  = "PERFORMER")
public class Performer {
    @Id
    @GeneratedValue
    @Column(name = "pid", nullable = false)
    private long _id;

    @Column(name= "name", nullable = false)
    private String _name;

    @Column(name = "image_name")
    private String _imageName;

    @Column(name = "genre", nullable = false)
    @Enumerated(EnumType.STRING)
    private Genre _genre;

    //map many to many relationship between performer and concert.
    @ManyToMany
    @JoinTable(name = "CONCERT_PERFORMER",
            joinColumns = @JoinColumn(name = "pid"),
            inverseJoinColumns = @JoinColumn(name = "cid"))
    @Column(name = "concert", nullable = false)
    private Set<Concert> _concerts;

    public PerformerDTO convertToDTO(){
        Set<Long> concertIDs= new HashSet<Long>();

        for (Concert concert: _concerts){
            concertIDs.add(concert.getID());
        }

        return new PerformerDTO(_id, _name, _imageName, _genre, concertIDs);
    }

    public long getID(){
        return _id;
    }

    public void setId(long id){
        _id = id;
    }





}
