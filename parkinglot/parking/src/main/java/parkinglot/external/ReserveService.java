
package parkinglot.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

// @FeignClient(name="reserve", url="http://reserve:8080")
@FeignClient(name="reserve", url="${api.reserve.url}")
public interface ReserveService {

    // @RequestMapping(method= RequestMethod.GET, path="/reserves")
    // public void userCheck(@RequestBody Reserve reserve);
    @RequestMapping(method= RequestMethod.GET, path="/reserves/check")
    public String userCheck(@RequestBody Reserve reserve);
}