package parkinglot;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import java.util.List;

@Entity
@Table(name="Parking_table")
public class Parking {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long parkingLotId;
    private String userId;
    private Long reserveId;
 
    @PrePersist
    public void onPrePersist(){
        // Started started = new Started();
        // BeanUtils.copyProperties(this, started);
        // started.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        parkinglot.external.Reserve reserve = new parkinglot.external.Reserve();
        // mappings goes here
        // Application.applicationContext.getBean(parkinglot.external.ReserveService.class).userCheck(reserve);

        // Ended ended = new Ended();
        // BeanUtils.copyProperties(this, ended);
        // ended.publishAfterCommit();

        reserve.setId(reserveId);
        reserve.setUserId(userId);
        reserve.setParkingLotId(parkingLotId);
        String result = ParkingApplication.applicationContext.getBean(parkinglot.external.ReserveService.class).userCheck(reserve);

        if(result.equals("valid")){ 
            System.out.println("Success!");
        }
        else{   //  usercheck가 유효하지 않을 때 강제로 예외 발생            
            System.out.println("FAIL!! InCorrect User or Incorrect Resevation");
            Exception ex = new Exception();
            ex.notify();
        }
    }

    @PreRemove
    public void onPreRemove(){
        Ended ended = new Ended();
        BeanUtils.copyProperties(this, ended);
        ended.publishAfterCommit();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getParkingLotId() {
        return parkingLotId;
    }

    public void setParkingLotId(Long parkingLotId) {
        this.parkingLotId = parkingLotId;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public Long getReserveId() {
        return reserveId;
    }

    public void setReserveId(Long reserveId) {
        this.reserveId = reserveId;
    }




}
