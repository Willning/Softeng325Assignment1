package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.types.Genre;
import org.hibernate.annotations.Cascade;
import org.jboss.resteasy.spi.touri.MappedBy;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.HashSet;
import java.util.Set;

@Entity
@XmlAccessorType(XmlAccessType.FIELD)
@Table(name  = "PERFORMERS")
public class Performer {

    @Id
    @GeneratedValue
    @Column(name = "pid", nullable = false)
    private Long _id;

    @Column(name= "name", nullable = false)
    private String _name;

    @Column(name = "image_name")
    private String _imageName;

    @Column(name = "genre", nullable = false)
    @Enumerated(EnumType.STRING)
    private Genre _genre;

    //need someway to have cascading delete on both sides.
    @ManyToMany(mappedBy = "_performers")
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

    public String getName(){
        return _name;

    }

    public void setName(String name){
        _name = name;
    }

    public String getImageName(){
        return _imageName;
    }

    public void setImageName(String imageName){
        _imageName = imageName;
    }

    public Genre getGenre(){
        return _genre;
    }

    public void setGenre(Genre genre){
        _genre = genre;
    }

    public Set<Concert> getConcerts(){
        return  _concerts;
    }

    public void setConcerts(Set<Concert> concertSet){
        this._concerts=concertSet;
    }


}
