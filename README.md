# parkingLot
Intensive Course 개별과제

# 서비스 시나리오
## 기능적 요구사항
1. 기본적으로 선착순으로 주차장 예약을 진행할 수 있도록 시나리오를 작성하였다.
2. 해당 주차장이 예약 중이거나 사용 중이라면 예약을 할 수 없고, 예약 취소나 사용 종료가 일어났을 때 예약을 할 수 있게 하였다.
3. 회원이 주차장에 대한 예약을 한다.
4. 회원이 예약한 주차장에 대해 입구에 차량 진입시 주차 시작 프로세스가 요청된다.
5. 예약한 사람이면 주차가 가능할 수 있도록 진입이 허용된다.
6. 예약한 사람이 아니면 차량 진입을 할 수 없어 주차를 시작할 수 없다.
7. 예약한 주차에 대해 예약 취소를 요청할 수 있다.
8. 차량이 주차장을 빠져나옴으로써 시작된 주차를 종료할 수 있다.

## 비기능적 요구사항
1. 트랜잭션
 - 주차 시작(차량 진입시)을 요청할 때, 주차를 예약한 사람이 아니라면 주차를 시작하지 못하게 한다.(Sync 호출)
2. 장애격리
 - 주차장 시스템이 수행되지 않더라도 예약 취소, 사용자 확인, 주차 종료는 아무때나 진행 가능하게 한다.(Async 호출)
3. 그 외
 - 주차장 현황에 대해 예약 상황을 별도로 확인할 수 있어야 한다.(CQRS)

# 체크포인트
https://workflowy.com/s/assessment/qJn45fBdVZn4atl3

## EventStorming 결과
### 완성된 1차 모형
![image](https://user-images.githubusercontent.com/78134025/109919495-158b8500-7cfc-11eb-9b89-05495d5e31ca.png)

### 1차 완성본에 대한 기능적/비기능적 요구사항을 충족하는지 검증
![image](https://user-images.githubusercontent.com/78134025/109920139-37393c00-7cfd-11eb-8960-e1723acf30e7.png)
    
1. 주차장이 등록이 된다. (7)
2. 회원이 주차장 예약을 한다. (3 -> 6)
3. 회원이 예약한 주차장에 대해 주차 시작(차량 진입시)을 요청한다.
4. 예약한 회원이면 주차를 시작한다. (1 -> 5 -> 6)
5. 예약한 회원이 아니면 차량 진입을 할 수 없어 주차를 시작할 수 없다. (1)
6. 회원이 예약한 주차장을 예약 취소한다. (4 -> 6)
7. 차량이 주차장을 빠져나와 시작했던 주차가 종료된다. (2 -> 6)
8. schedule 메뉴에서 주차장에 대한 예약 정보를 알 수 있다.(ParkingLot Service + Reserve Service) (8)

### 헥사고날 아키텍쳐 다이어그램 도출 (Polyglot)
![image](https://user-images.githubusercontent.com/78134025/109924142-2d1a3c00-7d03-11eb-984f-55903640a96f.png)

# 구현
도출해낸 헥사고날 아키텍처에 맞게, 로컬에서 SpringBoot를 이용해 Maven 빌드 하였다. 각각의 포트넘버는 8081 ~ 8084, 8088 이다.
    
    cd gateway
    mvn spring-boot:run

    cd parking
    mvn spring-boot:run
    
    cd parkingLot
    mvn spring-boot:run
    
    cd reserve
    mvn spring-boot:run
    
    cd schedule
    mvn spring-boot:run
  
## DDD의 적용
**ParkingLot 서비스의 ParkingLot.java**

```java
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
```

**ParkingLot 서비스의 PolicyHandler.java**
```java
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
    public void wheneverCanceled_UpdateReserve(@Payload Canceled canceled){

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
    public void wheneverReserved_UpdateReserve(@Payload Reserved reserved){

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
    public void wheneverUserChecked_UpdateReserve(@Payload UserChecked userChecked){

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
    public void wheneverEnded_UpdateReserve(@Payload Ended ended){

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

```


- 적용 후 REST API의 테스트를 통해 정상적으로 작동함을 확인하였다.
- 주차장 등록(Added) 후 결과  

![image](https://user-images.githubusercontent.com/78134025/109990410-b4d86880-7d4c-11eb-980c-d4dbdaf894a0.png)


- 주차장 예약(Reserved) 후 결과

![image](https://user-images.githubusercontent.com/78134025/109995392-96c13700-7d51-11eb-9c88-3179c2133b66.png)


## Gateway 적용
API Gateway를 통해 마이크로 서비스들의 진입점을 하나로 진행하였다.
```yml
server:
  port: 8088

---

spring:
  profiles: default
  cloud:
    gateway:
      routes:
        - id: parking
          uri: http://localhost:8081
          predicates:
            - Path=/parkings/** 
        - id: reserve
          uri: http://localhost:8082
          predicates:
            - Path=/reserves/** 
        - id: parkingLot
          uri: http://localhost:8083
          predicates:
            - Path=/parkingLots/** 
        - id: schedule
          uri: http://localhost:8084
          predicates:
            - Path= /reserveTables/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true


---

spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: parking
          uri: http://parking:8080
          predicates:
            - Path=/parkings/** 
        - id: reserve
          uri: http://reserve:8080
          predicates:
            - Path=/reserves/** 
        - id: parkingLot
          uri: http://parkingLot:8080
          predicates:
            - Path=/parkingLots/** 
        - id: schedule
          uri: http://schedule:8080
          predicates:
            - Path= /reserveTables/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true

server:
  port: 8080

```

## Polyglot Persistence
- parking 서비스의 경우, 다른 서비스들이 h2 저장소를 이용한 것과는 다르게 hsql을 이용하였다. 
- 이 작업을 통해 서비스들이 각각 다른 데이터베이스를 사용하더라도 전체적인 기능엔 문제가 없음(Polyglot Persistence)를 확인하였다.

![image](https://user-images.githubusercontent.com/78134025/109927929-3c4fb880-7d08-11eb-89ee-acc013e1aae1.png)

## 동기식 호출(Req/Res 방식)과 Fallback 처리

- parking 서비스의 external/ReserveService.java 내에 예약한 사용자가 맞는지 확인하는 Service 대행 인터페이스(Proxy)를 FeignClient를 이용하여 구현하였다.

```java
@FeignClient(name="reserve", url="${api.reserve.url}")
public interface ReserveService {

    @RequestMapping(method= RequestMethod.GET, path="/reserves/check")
    public String userCheck(@RequestBody Reserve reserve);

}
```
- parking 서비스의 Parking.java 내에 사용자 확인 후 결과에 따라 주차시작(차량 진입 시)을 진행할지, 진행하지 않을지 결정.(@PrePersist)
```java
@PrePersist
    public void onPrePersist(){
        /*Started started = new Started();
        BeanUtils.copyProperties(this, started);
        started.publishAfterCommit();*/

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        meetingroom.external.Reserve reserve = new meetingroom.external.Reserve();
        // mappings goes here
        reserve.setId(reserveId);
        reserve.setUserId(userId);
        reserve.setRoomId(roomId);
        String result = ConferenceApplication.applicationContext.getBean(meetingroom.external.ReserveService.class).userCheck(reserve);

        if(result.equals("valid")){
            System.out.println("Success!");
        }
        else{
            /// usercheck가 유효하지 않을 때 강제로 예외 발생
                System.out.println("FAIL!! InCorrect User or Incorrect Resevation");
                Exception ex = new Exception();
                ex.notify();
        }
    }
```


## 비동기식 호출 (Pub/Sub 방식)

- reserve 서비스 내 Reserve.java에서 아래와 같이 서비스 Pub 구현

```java
@Entity
@Table(name="Reserve_table")
public class Reserve {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String userId;
    private Long roomId;
    private String status;

    //...

    @PostPersist
    public void onPostPersist(){
        Reserved reserved = new Reserved();
        BeanUtils.copyProperties(this, reserved);
        reserved.publishAfterCommit();
    }
}
```

- parkingLot 서비스 내 PolicyHandler.java 에서 아래와 같이 Sub 구현

```java
@Service
public class PolicyHandler{
    @Autowired
    ParkingLotRepository parkingLotRepository;

    //...
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReserved_UpdateReserve(@Payload Reserved reserved){

        if(reserved.isMe()){
            System.out.println("##### listener  : " + reserved.toJson());

            Optional<ParkingLot> parkingLot = parkingLotRepository.findById(reserved.getParkingLotId());
            if (parkingLot.isPresent()){
                parkingLot.get().setStatus("Reserved");    // 주차장 예약됨.
                parkingLotRepository.save(parkingLot.get());
            }
        }
    }
  }
```
- 비동기 호출은 다른 서비스 하나가 비정상이어도 해당 메세지를 다른 메시지 큐에서 보관하고 있기에, 서비스가 다시 정상으로 돌아오게 되면 그 메시지를 처리하게 된다.
  - reserve 서비스와 parkingLot 서비스가 둘 다 정상 작동을 하고 있을 경우, 이상이 없이 잘 된다.  
    ![image](https://user-images.githubusercontent.com/78134025/110062196-27c4fc00-7dac-11eb-8f78-6a9a7e192d66.png)

  - parkingLot 서비스를 내리고, reserve 서비스를 이용해 예약을 하여도 문제가 없이 동작한다.  
    ![image](https://user-images.githubusercontent.com/78134025/110062463-a326ad80-7dac-11eb-8862-01890ba0cc3b.png)

    
## CQRS

viewer인 schedule 서비스를 별도로 구현하여 아래와 같이 view를 출력한다.
- Reserved 수행 후 schedule (예약 진행)  
  ![image](https://user-images.githubusercontent.com/78134025/110062927-876fd700-7dad-11eb-9b4e-96503d8144ab.png)


# 운영
## CI/CD 설정
- git에서 소스를 가져온다.
```
git clone https://github.com/kjhtranks/parkingLot.git
```
- Dependency 를 적용한다.
```
cd project/parkingLot/parkinglot
cd gateway
mvn package

cd ..
cd parking
mvn package

cd ..
cd parkingLot
mvn package

cd ..
cd reserve
mvn package

cd ..
cd schedule
mvn package
```
- Dockerizing, ACR(Azure Container Registry 에 Docker Image Push) 등록을 진행한다.
```
cd project/parkingLot/parkinglot
cd gateway
az acr build --registry parkinglot --image parkinglot.azurecr.io/gateway:latest .

cd ..
cd parking
az acr build --registry parkinglot --image parkinglot.azurecr.io/parking:latest .

cd ..
cd parkinglot
az acr build --registry parkinglot --image parkinglot.azurecr.io/parkinglot:latest .

cd ..
cd reserve
az acr build --registry parkinglot --image parkinglot.azurecr.io/reserve:latest .

cd ..
cd schedule
az acr build --registry parkinglot --image parkinglot.azurecr.io/schedule:latest .
```
- ACR에서 이미지 가져와서 Kubernetes에서 Deploy 한다.
```
kubectl create deploy gateway --image=parkinglot.azurecr.io/gateway:latest 
kubectl create deploy parking --image=parkinglot.azurecr.io/parking:latest
kubectl create deploy parkinglot --image=parkinglot.azurecr.io/parkinglot:latest
kubectl create deploy reserve --image=parkinglot.azurecr.io/reserve:latest
kubectl create deploy schedule --image=parkinglot.azurecr.io/schedule:latest

kubectl get all
```
- Kubectl Deploy 결과 확인  

  ![image](https://user-images.githubusercontent.com/78134025/109984984-a3d92880-7d47-11eb-8a1c-6d92d213aefd.png) 

- Kubernetes에서 서비스 생성하기 (Docker 생성이기에 Port는 8080이며, Gateway는 LoadBalancer로 생성)
```
kubectl expose deploy gateway --type="LoadBalancer" --port=8080
kubectl expose deploy parking --type="ClusterIP" --port=8080
kubectl expose deploy parkinglot --type="ClusterIP" --port=8080
kubectl expose deploy reserve --type="ClusterIP" --port=8080
kubectl expose deploy schedule --type="ClusterIP" --port=8080

kubectl get all
```
- Kubectl Expose 결과 확인  

  ![image](https://user-images.githubusercontent.com/78134025/109985556-3974b800-7d48-11eb-99a6-4ec28aa0f622.png)

  
## 무정지 재배포
- 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 이나 CB 설정을 제거함
- siege 로 배포작업 직전에 워크로드를 모니터링 함
```
siege -c10 -t60S -r10 -v --content-type "application/json" 'http://52.231.184.252:8080/reserves POST {"userId":1", "parkingLotId":"3"}'
```
- Readiness가 설정되지 않은 yml 파일로 배포 진행  
  ![image](https://user-images.githubusercontent.com/78134025/110064117-d61e7080-7daf-11eb-81bd-0cf8fa57b331.png)

```
kubectl apply -f deployment.yml
```
- 아래 그림과 같이, Kubernetes가 Readiness 준비가 되지 않은 reserve pod에 요청을 보내서 siege의 Availability 가 100% 미만으로 떨어짐  
  ![image](https://user-images.githubusercontent.com/78134025/110064850-45e12b00-7db1-11eb-9041-5294903b2ae8.png)

- Readiness가 설정된 yml 파일로 배포 진행  
  ![image](https://user-images.githubusercontent.com/78134025/110067534-48468380-7db7-11eb-87bc-b1e479ed53c1.png)

```
kubectl apply -f deployment.yml
```
- 배포 중 pod가 2개가 뜨고, 새롭게 띄운 pod가 준비될 때까지, 기존 pod가 유지됨을 확인  
  ![image](https://user-images.githubusercontent.com/78134025/110067625-76c45e80-7db7-11eb-91b3-364954de5c7a.png)
  
- siege 가 중단되지 않고, Availability가 높아졌음을 확인하여 무정지 재배포가 됨을 확인함  
  ![image](https://user-images.githubusercontent.com/78134025/110068103-53e67a00-7db8-11eb-84cf-dbb6a749d3ca.png)


## 오토스케일 아웃
- 서킷 브레이커는 시스템을 안정되게 운영할 수 있게 해줬지만, 사용자의 요청이 급증하는 경우, 오토스케일 아웃이 필요하다.

  - 단, 부하가 제대로 걸리기 위해서, reserve 서비스의 리소스를 줄여서 재배포한다.  
    ![image](https://user-images.githubusercontent.com/78134025/110068733-a5433900-7db9-11eb-834f-460f43944b17.png)

- reserve 시스템에 replica를 자동으로 늘려줄 수 있도록 HPA를 설정한다. 설정은 CPU 사용량이 15%를 넘어서면 replica를 10개까지 늘려준다.
```
kubectl autoscale deploy reserve --min=1 --max=10 --cpu-percent=15
```

- hpa 설정 확인  
  ![image](https://user-images.githubusercontent.com/78134025/110068874-f7845a00-7db9-11eb-84a0-7c9bc9c9cf14.png)

- hpa 상세 설정 확인  
  ![image](https://user-images.githubusercontent.com/78134025/110069260-c35d6900-7dba-11eb-887b-91b6d9e1b0c6.png)
  ![image](https://user-images.githubusercontent.com/78134025/110069157-9446f780-7dba-11eb-8a45-ffd8eeb4fd14.png)
  
- siege를 활용해서 워크로드를 2분간 걸어준다. (Cloud 내 siege pod에서 부하줄 것)
```
kubectl exec -it (siege POD 이름) -- /bin/bash
siege -c1000 -t120S -r100 -v --content-type "application/json" 'http://52.231.184.252:8080/reserves POST {"userId":1, "parkingLotId":"3"}'
```

- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다.
```
watch kubectl get all
```
- 스케일 아웃이 자동으로 되었음을 확인  
  ![image](https://user-images.githubusercontent.com/78134025/110070367-2fd96780-7dbd-11eb-958e-4ec49fdad251.png)

- 오토스케일링에 따라 Siege 성공률이 높은 것을 확인 할 수 있다.  
  ![image](https://user-images.githubusercontent.com/78134025/110070415-47b0eb80-7dbd-11eb-88ea-4bdd8ce53aa3.png)


## Self-healing (Liveness Probe)
- reserve 서비스의 yml 파일에 liveness probe 설정을 바꾸어서, liveness probe 가 동작함을 확인

- liveness probe 옵션을 추가하되, 서비스 포트가 아닌 8090으로 설정, readiness probe 미적용  
  ![image](https://user-images.githubusercontent.com/78134025/110070680-d58cd680-7dbd-11eb-8a7c-dfa0bafd3238.png)

- reserve 서비스에 liveness가 적용된 것을 확인  
  ![image](https://user-images.githubusercontent.com/78134025/110070931-66fc4880-7dbe-11eb-8ff7-ba5139687825.png)

- reserve 서비스에 liveness가 발동되었고, 8090 포트에 응답이 없기에 Restart가 발생함   
  ![image](https://user-images.githubusercontent.com/78134025/110071012-9743e700-7dbe-11eb-85c6-bcbf993f9467.png)


## ConfigMap 적용
- reserve의 application.yaml에 ConfigMap 적용 대상 항목을 추가한다.

  <img width="558" alt="스크린샷 2021-02-28 오후 4 01 52" src="https://user-images.githubusercontent.com/33116855/109410475-4f981680-79de-11eb-8231-0679b6f5f55b.png">

- reserve의 service.yaml에 ConfigMap 적용 대상 항목을 추가한다.

  <img width="325" alt="스크린샷 2021-02-28 오후 4 05 07" src="https://user-images.githubusercontent.com/33116855/109410532-c03f3300-79de-11eb-8e61-71752818c41d.png">


- ConfigMap 생성하기
```
kubectl create configmap apiurl --from-literal=conferenceapiurl=http://conference:8080 --from-literal=roomapiurl=http://room:8080
```

- Configmap 생성 확인, url이 Configmap에 설정된 것처럼 잘 반영된 것을 확인할 수 있다.  
```
kubectl get configmap apiurl -o yaml
```
  <img width="447" alt="스크린샷 2021-02-28 오후 4 08 16" src="https://user-images.githubusercontent.com/33116855/109410590-33e14000-79df-11eb-93ed-bdfb04778cd8.png">
  <img width="625" alt="스크린샷 2021-02-28 오후 4 10 11" src="https://user-images.githubusercontent.com/33116855/109410630-8884bb00-79df-11eb-99d4-f6311cbe37bd.png">


## 동기식 호출 / 서킷 브레이킹 / 장애격리

* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함

- RestAPI 기반 Request/Response 요청이 과도할 경우 CB 를 통하여 장애격리 하도록 설정함.

- Hystrix 를 설정:  요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 설정

- conference의 Application.yaml 설정
```
feign:
  hystrix:
    enabled: true
    
hystrix:
  command:
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610

```

- reserve에 Thread 지연 코드 삽입
  <img width="702" alt="스크린샷 2021-03-01 오후 2 40 46" src="https://user-images.githubusercontent.com/33116855/109456415-22aa3900-7a9c-11eb-9a30-4e63323312c2.png">

* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 100명
- 60초 동안 실시

```
siege -c100 -t60S -r10 -v --content-type "application/json" 'http://52.141.56.203:8080/conferences POST {"roomId": "1", "userId":"1", reserveId:"1"}'
```
- 부하 발생하여 CB가 발동하여 요청 실패처리하였고, 밀린 부하가 reserve에서 처리되면서 다시 conference를 받기 시작 

  <img width="409" alt="스크린샷 2021-03-01 오후 2 32 14" src="https://user-images.githubusercontent.com/33116855/109455911-00fc8200-7a9b-11eb-8d95-f5df5ef249fd.png">

- CB 잘 적용됨을 확인



