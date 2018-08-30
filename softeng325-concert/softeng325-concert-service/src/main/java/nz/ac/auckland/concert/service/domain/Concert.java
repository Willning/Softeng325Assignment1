package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.PriceBand;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private String title;

    @ElementCollection
    @JoinTable(name ="CONCERT_TARIFS", joinColumns = @JoinColumn(name = "cid"))
    @MapKeyColumn(name = "price_band")
    @Column(name = "tariff", nullable = false)
    @MapKeyEnumerated(EnumType.STRING)
    private Map<PriceBand, BigDecimal> _tariff;

    @ElementCollection
    @JoinTable(name = "CONCERT_DATES", joinColumns = @JoinColumn(name = "cid"))
    @Column (name = "dateTime", nullable = false)
    private Set<LocalDateTime> _dates;


}
