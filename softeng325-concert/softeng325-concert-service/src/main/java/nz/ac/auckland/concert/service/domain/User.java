package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.UserDTO;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USERS")
public class User {

    @Id
    @Column (name = "username", nullable = false, unique = true)
    private String _username;

    @Column(name = "password", nullable = false)
    private String _password;
    //lmao

    @ManyToOne
    @JoinColumn(name = "creditCard")
    private CreditCard _creditCard;

    @Column(name ="firstname", nullable = false)
    private String _firstname;

    @Column(name = "lastname", nullable = false)
    private String _lastname;

    @Column(name = "token")
    private String _token;

    @Column(name ="tokenTimeStamp")
    private LocalDateTime _tokenTimeStamp;


    @ManyToOne
    private NewsItem _lastMessage;

    public User(){

    }

    //should be able to create a user
    public User(UserDTO userDTO){
        _username= userDTO.getUsername();
        _password=userDTO.getPassword();
        _firstname=userDTO.getFirstname();
        _lastname=userDTO.getLastname();

    }

    public String get_username() {
        return _username;
    }

    public void set_username(String _username) {
        this._username = _username;
    }

    public String get_password() {
        return _password;
    }

    public void set_password(String _password) {
        this._password = _password;
    }

    public CreditCard get_creditCard() {
        return _creditCard;
    }

    public void set_creditCard(CreditCard _creditCard) {
        this._creditCard = _creditCard;
    }

    public UserDTO convertToDTO(){
        UserDTO dto = new UserDTO(_username,_password, _lastname, _firstname);

        return dto;
    }

    public String get_firstname() {
        return _firstname;
    }

    public void set_firstname(String _firstname) {
        this._firstname = _firstname;
    }

    public String get_lastname() {
        return _lastname;
    }

    public void set_lastname(String _lastname) {
        this._lastname = _lastname;
    }

    public String get_token() {
        return _token;
    }

    public void set_token(String _token) {
        this._token = _token;
    }

    public LocalDateTime get_tokenTimeStamp() {
        return _tokenTimeStamp;
    }

    public void set_tokenTimeStamp(LocalDateTime _tokenTimeStamp) {
        this._tokenTimeStamp = _tokenTimeStamp;
    }

    public NewsItem get_lastMessage() {
        return _lastMessage;
    }

    public void set_lastMessage(NewsItem _lastMessage) {
        this._lastMessage = _lastMessage;
    }
}
