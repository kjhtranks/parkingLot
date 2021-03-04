package parkinglot;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Reserve_table")
public class Reserve {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String userId;
    private Long parkingLotId;
    private String status;

    @PostPersist
    public void onPostPersist(){
        Reserved reserved = new Reserved();
        BeanUtils.copyProperties(this, reserved);
        reserved.publishAfterCommit();


    }

    @PostUpdate
    public void onPostUpdate(){
        UserChecked userChecked = new UserChecked();
        BeanUtils.copyProperties(this, userChecked);
        userChecked.publishAfterCommit();


    }

    @PostRemove
    public void onPostRemove(){
        Canceled canceled = new Canceled();
        BeanUtils.copyProperties(this, canceled);
        canceled.publishAfterCommit();


    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public Long getParkingLotId() {
        return parkingLotId;
    }

    public void setParkingLotId(Long parkingLotId) {
        this.parkingLotId = parkingLotId;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




}
