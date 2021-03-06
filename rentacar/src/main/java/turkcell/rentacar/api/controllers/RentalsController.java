package turkcell.rentacar.api.controllers;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import turkcell.rentacar.business.abstracts.RentalService;
import turkcell.rentacar.business.dtos.GetListRentDto;
import turkcell.rentacar.business.dtos.ListRentDto;
import turkcell.rentacar.business.requests.create.CreateRentalRequest;
import turkcell.rentacar.business.requests.delete.DeleteRentalRequest;
import turkcell.rentacar.business.requests.update.UpdateRentalRequest;
import turkcell.rentacar.core.utilities.results.DataResult;
import turkcell.rentacar.core.utilities.results.Result;

@RestController
@RequestMapping("/api/rents")
public class RentalsController {
	
	private RentalService rentService;

	@Autowired
	public RentalsController(RentalService rentService) {
		this.rentService = rentService;
	}	

	@PostMapping("/add/IndividualCustomer")
	public Result addIndividualCustomer(@RequestBody @Valid CreateRentalRequest createRentRequest){
		return this.rentService.addIndividualCustomer(createRentRequest);
	}
	
	@PostMapping("/add/CorporateCustomer")
	public Result addCorporateCustomer(@RequestBody @Valid CreateRentalRequest createRentRequest){
		return this.rentService.addCorporateCustomer(createRentRequest);
	}
	
	@DeleteMapping("/delete")
	public Result delete(@RequestBody @Valid DeleteRentalRequest deleteRentRequest) {
		return this.rentService.delete(deleteRentRequest);
	}
	
	@PutMapping("/update")
	public Result update(@RequestBody @Valid UpdateRentalRequest updateRentRequest) {
		return this.rentService.update(updateRentRequest);
	}
	
	@GetMapping("/getAll")
	public DataResult<List<ListRentDto>> getAll() {
		return this.rentService.getAll();
	}
	
	@GetMapping("/getAllPaged")
	public DataResult<List<ListRentDto>> getAllPaged(int pageNo, int pageSize) {
		return this.rentService.getAllPaged(pageNo, pageSize);
	}

	@GetMapping("/getAllSorted")
	public DataResult<List<ListRentDto>> getAllSorted(Sort.Direction direction) {
		return this.rentService.getAllSorted(direction);
	}

	@GetMapping("/getRentId")
	public DataResult<GetListRentDto> getByRentalId(int rentalId)  {
		return this.rentService.getByRentId(rentalId);
	}

	@GetMapping("/getByCarCarId")
	public DataResult<List<ListRentDto>> getByCar_CarId(int carId)  {
		return this.rentService.getByCarCarId(carId);
	}
 	
}
