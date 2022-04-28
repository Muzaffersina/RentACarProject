package turkcell.rentacar.business.abstracts;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;

import turkcell.rentacar.business.dtos.GetListRentDto;
import turkcell.rentacar.business.dtos.ListRentDto;
import turkcell.rentacar.business.requests.create.CreateRentalRequest;
import turkcell.rentacar.business.requests.delete.DeleteRentalRequest;
import turkcell.rentacar.business.requests.update.UpdateRentalRequest;
import turkcell.rentacar.core.utilities.results.DataResult;
import turkcell.rentacar.core.utilities.results.Result;
import turkcell.rentacar.entities.concretes.Rental;

public interface RentalService {
	DataResult<Integer> addIndividualCustomer(CreateRentalRequest createRentRequest);
	DataResult<Integer> addCorporateCustomer(CreateRentalRequest createRentRequest);

	Result delete(DeleteRentalRequest deleteRentRequest) ;
	DataResult<Integer> update(UpdateRentalRequest updateRentRequest);
	
		
	DataResult<List<ListRentDto>> getAll();
	DataResult<List<ListRentDto>> getAllPaged(int pageNo , int pageSize);
	DataResult<List<ListRentDto>> getAllSorted(Sort.Direction direction);	
	
	DataResult<GetListRentDto> getByRentId(int rentalId) ;
	DataResult<List<ListRentDto>> getByCarCarId(int carId) ;
	
	
	boolean checkRentCarExist(int rentalId);
	boolean checkRentCarDate(int carId);
	boolean checkCarUsed(int carId);	
	boolean checkCustomerUsed(int customerId);	
	Rental returnRental(int rentalId);
	double extraPriceCal(int rentalId, List<Integer> additionalServiceId);
	boolean checkReturnedInTime(int rentalId, LocalDate returnedTime);
	
}

