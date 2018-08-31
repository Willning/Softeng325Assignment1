package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.types.PriceBand;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Table(name = "CONCERTS")
@Entity

public class Concert {
    @Id
    @GeneratedValue
    @Column(name = "cid", nullable = false, unique = true)
    private long _id;

    @Column(name = "Title", nullable = false)
    private String _title;

    @ElementCollection
    @JoinTable(name ="CONCERT_TARIFS", joinColumns = @JoinColumn(name = "cid"))
    @MapKeyColumn(name = "price_band")
    @Column(name = "tariff", nullable = false)
    @MapKeyEnumerated(EnumType.STRING)
    private Map<PriceBand, BigDecimal> _tariff; //Use big decimal because

    @ElementCollection
    @JoinTable(name = "CONCERT_DATES", joinColumns = @JoinColumn(name = "cid"))
    @Column (name = "dateTime", nullable = false)
    private Set<LocalDateTime> _dates;


    @ManyToMany
    @JoinTable(name = "CONCERT_PERFORMANCE",
            joinColumns = @JoinColumn(name = "cid"),
            inverseJoinColumns =@JoinColumn(name = "pid"))
    @Column(name = "performer", nullable = false)
    private Set<Performer> _performers;

    //make a concert object to a DTO object
    public ConcertDTO convertToDTO(){
        Set<Long> performerIDs = new HashSet<Long>();

        for (Performer performer: _performers){
            performerIDs.add(performer.getID());
        }

        return new ConcertDTO(_id, _title, _dates, _tariff,performerIDs);
    }

    public long getID(){
        return _id;
    }

    public void setID(long id){
        _id = id;
    }

    public String getTitle(){
        return _title;
    }

    public void setTitle(String title){
        _title = title;
    }

    public Set<LocalDateTime> getDates(){
        return _dates;
    }

    public void setDates(Set<LocalDateTime> dates){
        _dates  = dates;
    }

    public Map<PriceBand, BigDecimal> getTariff(){
        return _tariff;
    }

    public void setTariff(Map<PriceBand,BigDecimal> tariff){
        _tariff = tariff;

    }

    public Set<Performer> getPerformers(){
        return _performers;
    }

    public void setPerformers(Set<Performer> performers){
        _performers = performers;
    }




}
