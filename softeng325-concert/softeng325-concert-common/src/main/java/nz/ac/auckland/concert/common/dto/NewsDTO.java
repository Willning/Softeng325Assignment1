package nz.ac.auckland.concert.common.dto;

import nz.ac.auckland.concert.common.jaxb.LocalDateAdapter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;


/**DTO class to represnt news
 * parameters
 * _id = id of the news
 * _date = date of thew news
 * _newsMessage = contents of the news
 */
@XmlRootElement(name = "News")
@XmlAccessorType(XmlAccessType.FIELD)
public class NewsDTO {

    @XmlAttribute(name = "ID")
    private Long _id;

    @XmlElement(name = "date")
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDateTime _time;

    @XmlElement(name = "message")
    private String _newsMessage;

    public NewsDTO(){}

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public LocalDateTime get_time() {
        return _time;
    }

    public void set_time(LocalDateTime _time) {
        this._time = _time;
    }

    public String get_newsMessage() {
        return _newsMessage;
    }

    public void set_newsMessage(String _newsMessage) {
        this._newsMessage = _newsMessage;
    }

    @Override
    public boolean equals(Object obj){
        if (!(obj instanceof ConcertDTO))
            return false;
        if (obj == this)
            return true;

        NewsDTO rhs = (NewsDTO) obj;
        return new EqualsBuilder()
                .append(_time,rhs.get_time())
                .append(_newsMessage,rhs.get_newsMessage())
                .isEquals();
    }

    @Override
    public int hashCode(){
        return new HashCodeBuilder(17,31)
                .append(_time)
                .append(_newsMessage)
                .hashCode();
    }
}
