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
    private CreditCard _creditCard;

    @Column(name ="firstname", nullable = false)
    private String _firstname;

    @Column(name = "lastname", nullable = false)
    private String _lastname;

    @Column(name = "token")
    private String _token;

    @Column(name ="tokenTimeStamp")
    private LocalDateTime _tokenTimeStamp;

    public User(){

    }

    public User(UserDTO userDTO){
        _username= userDTO.getUsername();
        _password=userDTO.getPassword();
        _firstname=userDTO.getFirstname();
        _lastname=userDTO.getLastname();

    }



}
