package parkinglot;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="ParkingLot_table")
public class ParkingLot {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String status;
    private Integer floor;

    @PostPersist
    public void onPostPersist(){
        Added added = new Added();
        BeanUtils.copyProperties(this, added);
        added.publishAfterCommit();


    }

    // @PrePersist
    // public void onPrePersist(){
    //     Searched searched = new Searched();
    //     BeanUtils.copyProperties(this, searched);
    //     searched.publishAfterCommit();


    //     Deleted deleted = new Deleted();
    //     BeanUtils.copyProperties(this, deleted);
    //     deleted.publishAfterCommit();


    // }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
