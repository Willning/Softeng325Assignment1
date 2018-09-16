package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.NewsDTO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "NEWS")
public class NewsItem {

    @Id
    @Column(name ="nid",nullable = false,unique = true)
    private Long _id;

    @Column(name = "date", nullable = false)
    private LocalDateTime _date;

    @Column(name = "message")
    private String _message;

    public NewsItem(){

    }

    public NewsItem(NewsDTO newsDTO){
        _id = newsDTO.get_id();
        _date = newsDTO.get_time();
        _message = newsDTO.get_newsMessage();
    }


    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public LocalDateTime get_date() {
        return _date;
    }

    public void set_date(LocalDateTime _date) {
        this._date = _date;
    }

    public String get_message() {
        return _message;
    }

    public void set_message(String _message) {
        this._message = _message;
    }
}
