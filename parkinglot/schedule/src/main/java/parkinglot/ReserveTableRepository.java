package parkinglot;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReserveTableRepository extends CrudRepository<ReserveTable, Long> {
    // 추가
    ReserveTable findByParkingLotId(Long parkingLotId);
    ReserveTable findByReserveId(Long reserveId);
    void deleteByParkingLotId(Long parkingLotId);
    void deleteByReserveId(Long reserveId);
}