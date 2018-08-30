package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.Genre;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.Set;

@Entity
@XmlAccessorType(XmlAccessType.FIELD)
@Table(name  = "PERFORMER")
public class Performer {
    @Id
    @GeneratedValue
    @Column(name = "pid", nullable = false)
    private long id;

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



}
