package parkinglot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

@RestController
public class ReserveController {
    // 로직 추가
    @Autowired
    ReserveRepository reserveRepository;

    @RequestMapping(value="/reserves/check")
    public String userCheck(@RequestBody Reserve reserve){
    Optional<Reserve> result=reserveRepository.findById(reserve.getId());
    if(result.isPresent()){
        if(result.get().getUserId().equals(reserve.getUserId())){
            result.get().setStatus("Started");
            reserveRepository.save(result.get());

            return "valid";
        }
        
        return "invalid";
    }
    else{
        return "invalid";
    }
  }
}
