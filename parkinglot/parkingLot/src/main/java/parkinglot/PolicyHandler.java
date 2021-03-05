package parkinglot;

import parkinglot.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PolicyHandler{
    @Autowired
    ParkingLotRepository parkingLotRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    // public void wheneverCanceled_UpdateReserve(@Payload Canceled canceled){
    public void wheneverCanceled_(@Payload Canceled canceled){

        if(canceled.isMe()){
            System.out.println("##### listener  : " + canceled.toJson());
            
            Optional<ParkingLot> parkingLot = parkingLotRepository.findById(canceled.getParkingLotId());
            if (parkingLot.isPresent()){
                parkingLot.get().setStatus("Available");    // 주차장 예약이 취소되어 예약이 가능해짐.
                parkingLotRepository.save(parkingLot.get());
            }
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    // public void wheneverReserved_UpdateReserve(@Payload Reserved reserved){
    public void wheneverReserved_(@Payload Reserved reserved){

        if(reserved.isMe()){
            System.out.println("##### listener  : " + reserved.toJson());

            Optional<ParkingLot> parkingLot = parkingLotRepository.findById(reserved.getParkingLotId());
            if (parkingLot.isPresent()){
                parkingLot.get().setStatus("Reserved");    // 주차장 예약됨.
                parkingLotRepository.save(parkingLot.get());
            }
        }
    }
    
    @StreamListener(KafkaProcessor.INPUT)
    // public void wheneverUserChecked_UpdateReserve(@Payload UserChecked userChecked){
    public void wheneverUserChecked_(@Payload UserChecked userChecked){

        if(userChecked.isMe()){
            System.out.println("##### listener  : " + userChecked.toJson());

            Optional<ParkingLot> parkingLot = parkingLotRepository.findById(userChecked.getParkingLotId());
            if (parkingLot.isPresent()){
                parkingLot.get().setStatus("Available");    // 주차 시작됨.
                parkingLotRepository.save(parkingLot.get());
            }            
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    // public void wheneverEnded_UpdateReserve(@Payload Ended ended){
    public void wheneverEnded_(@Payload Ended ended){

        if(ended.isMe()){
            System.out.println("##### listener  : " + ended.toJson());
            
            Optional<ParkingLot> parkingLot = parkingLotRepository.findById(ended.getParkingLotId());
            if (parkingLot.isPresent()){
                parkingLot.get().setStatus("Available");    // 주차 종료됨.
                parkingLotRepository.save(parkingLot.get());
            }            
        }
    }    
}
