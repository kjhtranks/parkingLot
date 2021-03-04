package parkinglot;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="ReserveTable_table")
public class ReserveTable {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long parkingLotId;
    private Long reserveId;
    private String userId;
    private String status;
    private Integer floor;


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
    public Long getReserveId() {
        return reserveId;
    }

    public void setReserveId(Long reserveId) {
        this.reserveId = reserveId;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

}
