package turkcell.rentacar.business.concretes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import turkcell.rentacar.business.abstracts.AdditionalService;
import turkcell.rentacar.business.abstracts.CarDamageService;
import turkcell.rentacar.business.abstracts.CarMaintenanceService;
import turkcell.rentacar.business.abstracts.CarService;
import turkcell.rentacar.business.abstracts.CityService;
import turkcell.rentacar.business.abstracts.CustomerService;
import turkcell.rentacar.business.abstracts.InvoiceService;
import turkcell.rentacar.business.abstracts.RentalService;
import turkcell.rentacar.business.contants.Messages;
import turkcell.rentacar.business.dtos.GetListRentDto;
import turkcell.rentacar.business.dtos.ListRentDto;
import turkcell.rentacar.business.requests.create.CreateRentalRequest;
import turkcell.rentacar.business.requests.delete.DeleteRentalRequest;
import turkcell.rentacar.business.requests.update.UpdateRentalRequest;
import turkcell.rentacar.core.concretes.BusinessException;
import turkcell.rentacar.core.utilities.mapping.ModelMapperService;
import turkcell.rentacar.core.utilities.results.DataResult;
import turkcell.rentacar.core.utilities.results.Result;
import turkcell.rentacar.core.utilities.results.SuccessDataResult;
import turkcell.rentacar.core.utilities.results.SuccessResult;
import turkcell.rentacar.dataAccess.abstracts.RentalDao;
import turkcell.rentacar.entities.concretes.Rental;

@Service
public class RentalManager implements RentalService {

	private RentalDao rentDao;
	private ModelMapperService modelMapperService;
	private CarMaintenanceService carMaintenanceService;
	private CarService carService;
	private AdditionalService additionalService;
	private CityService cityService;
	private CustomerService customerService;
	private InvoiceService invoiceService;
	private CarDamageService carDamageService;

	@Lazy
	@Autowired
	public RentalManager(RentalDao rentDao, ModelMapperService modelMapperService,
			CarMaintenanceService carMaintenanceService, CarService carService, AdditionalService additionalService,
			CityService cityService, CustomerService customerService,
			InvoiceService invoiceService,CarDamageService carDamageService) {

		this.rentDao = rentDao;
		this.modelMapperService = modelMapperService;
		this.carMaintenanceService = carMaintenanceService;
		this.carService = carService;
		this.additionalService = additionalService;
		this.cityService = cityService;
		this.customerService = customerService;		
		this.invoiceService = invoiceService;
		this.carDamageService = carDamageService;
	}

	@Override
	public DataResult<Integer> addIndividualCustomer(CreateRentalRequest createRentRequest) {

		this.carService.checkCarExist(createRentRequest.getCarId());
		this.customerService.checkCustomerExist(createRentRequest.getCustomerId());
		this.additionalService.checkAllAdditional(createRentRequest.getAdditionalIds());
		this.carMaintenanceService.checkCarMaintenceCar_CarIdReturnDate(createRentRequest.getCarId());
		this.cityService.checkCityExist(createRentRequest.getRentalCityId());
		this.cityService.checkCityExist(createRentRequest.getReturnCityId());
		this.carDamageService.checkCarDamageExistForRental(createRentRequest.getCarId() , createRentRequest.getRentalDate());
		checkDate(createRentRequest.getRentalDate(), createRentRequest.getReturnDate());
		checkRentCarDate(createRentRequest.getCarId());

		Rental rent = this.modelMapperService.forRequest().map(createRentRequest, Rental.class);
		rent.setRentalId(0);		
		rent.setTotalPrice(
				calculatorTotalPrice(createRentRequest.getCarId(),createRentRequest.getRentalDate(),createRentRequest.getReturnDate()
						,createRentRequest.getAdditionalIds(),createRentRequest.getRentalCityId(),createRentRequest.getReturnCityId()));
		this.rentDao.save(rent);
		
		return new SuccessDataResult<Integer>(rent.getRentalId(),Messages.RENTALADD);
		
	}

	@Override
	public DataResult<Integer> addCorporateCustomer(CreateRentalRequest createRentRequest) {

		this.carService.checkCarExist(createRentRequest.getCarId());
		this.customerService.checkCustomerExist(createRentRequest.getCustomerId());
		this.additionalService.checkAllAdditional(createRentRequest.getAdditionalIds());
		this.carMaintenanceService.checkCarMaintenceCar_CarIdReturnDate(createRentRequest.getCarId());
		this.cityService.checkCityExist(createRentRequest.getRentalCityId());
		this.cityService.checkCityExist(createRentRequest.getReturnCityId());
		checkDate(createRentRequest.getRentalDate(), createRentRequest.getReturnDate());
		checkRentCarDate(createRentRequest.getCarId());

		Rental rent = this.modelMapperService.forRequest().map(createRentRequest, Rental.class);
		rent.setRentalId(0);		
		rent.setTotalPrice(
				calculatorTotalPrice(createRentRequest.getCarId(),createRentRequest.getRentalDate(),createRentRequest.getReturnDate()
						,createRentRequest.getAdditionalIds(),createRentRequest.getRentalCityId(),createRentRequest.getReturnCityId()));
		this.rentDao.save(rent);
		return new SuccessDataResult<Integer>(rent.getRentalId(),Messages.RENTALADD);
		
	}

	@Override
	public Result delete(DeleteRentalRequest deleteRentRequest) {
		
		checkRentCarExist(deleteRentRequest.getRentalId());
		this.invoiceService.checkRentalIdExist(deleteRentRequest.getRentalId());		

		Rental rent = this.modelMapperService.forRequest().map(deleteRentRequest, Rental.class);
		this.rentDao.deleteById(rent.getRentalId());

		return new SuccessResult(Messages.RENTALDELETE);

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor = BusinessException.class)
	public DataResult<Integer> update(UpdateRentalRequest updateRentRequest) {


		checkRentCarExist(updateRentRequest.getRentalId());
		this.carService.checkCarExist(updateRentRequest.getCarId());
		this.cityService.checkCityExist(updateRentRequest.getRentalCityId());
		this.cityService.checkCityExist(updateRentRequest.getReturnCityId());
		this.additionalService.checkAllAdditional(updateRentRequest.getAdditionalIds());
		this.customerService.checkCustomerExist(updateRentRequest.getCustomerId());
		checkDate(updateRentRequest.getRentalDate(), updateRentRequest.getReturnDate());
		checkRentalCarCarId(updateRentRequest.getRentalId(),updateRentRequest.getCarId());
		Rental rent = this.modelMapperService.forRequest().map(updateRentRequest, Rental.class);

		rent.setTotalPrice(
				calculatorTotalPrice(updateRentRequest.getCarId(),updateRentRequest.getRentalDate(),updateRentRequest.getReturnDate()
						,updateRentRequest.getAdditionalIds(),updateRentRequest.getRentalCityId(),updateRentRequest.getReturnCityId()));

		if (checkReturnedInTime(updateRentRequest.getRentalId(), updateRentRequest.getReturnDate())) {
			extraPriceCal(updateRentRequest.getRentalId(), updateRentRequest.getAdditionalIds());
		}
		
		updateCarKm(updateRentRequest.getCarId(), updateRentRequest.getReturnKm());
		this.rentDao.save(rent);

		return new SuccessDataResult<Integer>(rent.getRentalId(),Messages.RENTALUPDATE);		
	}

	@Override
	public DataResult<List<ListRentDto>> getAll() {

		List<Rental> result = this.rentDao.findAll();
		List<ListRentDto> response = result.stream()
				.map(rent -> this.modelMapperService.forDto().map(rent, ListRentDto.class))
				.collect(Collectors.toList());

		return new SuccessDataResult<List<ListRentDto>>(response, Messages.RENTALLIST);
	}

	@Override
	public DataResult<List<ListRentDto>> getAllPaged(int pageNo, int pageSize) {

		Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
		List<Rental> result = this.rentDao.findAll(pageable).getContent();
		List<ListRentDto> response = result.stream()
				.map(rent -> this.modelMapperService.forDto().map(rent, ListRentDto.class))
				.collect(Collectors.toList());

		return new SuccessDataResult<List<ListRentDto>>(response, Messages.RENTALLIST);
	}

	@Override
	public DataResult<List<ListRentDto>> getAllSorted(Direction direction) {

		Sort sort = Sort.by(direction, "returnDate");

		List<Rental> result = this.rentDao.findAll(sort);
		List<ListRentDto> response = result.stream()
				.map(rent -> this.modelMapperService.forDto().map(rent, ListRentDto.class))
				.collect(Collectors.toList());

		return new SuccessDataResult<List<ListRentDto>>(response, direction + Messages.RENTALLIST);
	}

	@Override
	public DataResult<GetListRentDto> getByRentId(int rentalId) {

		checkRentCarExist(rentalId);

		Rental result = this.rentDao.getByRentalId(rentalId);
		GetListRentDto response = this.modelMapperService.forDto().map(result, GetListRentDto.class);

		return new SuccessDataResult<GetListRentDto>(response, Messages.RENTALLIST);
	}

	@Override
	public DataResult<List<ListRentDto>> getByCarCarId(int carId) {

		this.carService.checkCarExist(carId);

		List<Rental> result = this.rentDao.getByCar_CarId(carId);
		List<ListRentDto> response = result.stream()
				.map(rent -> this.modelMapperService.forDto().map(rent, ListRentDto.class))
				.collect(Collectors.toList());

		return new SuccessDataResult<List<ListRentDto>>(response, Messages.RENTALLISTFORCAR);
	}

	@Override
	public boolean checkRentCarExist(int rentalId) {

		Rental result = this.rentDao.getByRentalId(rentalId);
		if (result != null) {
			return true;
		}
		throw new BusinessException(Messages.RENTALNOTFOUND);
	}
	
	private boolean checkRentalCarCarId(int rentalId, int carId) {
		
		 checkRentCarExist(rentalId);		
		 
		 if(this.rentDao.getByRentalId(rentalId).getCar().getCarId() == carId) {
			 return true;
		 }
		 
		 throw new BusinessException(Messages.RENTALCARIDDOESNTEXİSTS);		
	}

	private boolean checkDate(LocalDate rentalDate, LocalDate returnDate) {

		long daysBetween = ChronoUnit.DAYS.between(rentalDate, returnDate);
		if (daysBetween < 0) {
			throw new BusinessException(Messages.RENTALDATESNOTVALİD);
		}
		return true;
	}

	private long calculatorDaysBetween( LocalDate rentalDate, LocalDate returnDate) {

		
		long daysBetween = ChronoUnit.DAYS.between(rentalDate, returnDate);
		if (daysBetween == 0) {
			daysBetween = 1;
		}
		return daysBetween;

	}

	
	private double calculatorTotalPrice( int carId , LocalDate rentalDate, LocalDate returnDate
			,List<Integer> additionalServiceId,int rentalCityId , int returnCityId ) {

		double totalPrice = 0;
		totalPrice = (this.additionalService.calculateAdditionalServicePrice(additionalServiceId)+ this.carService.calculateRentalPrice(carId))
				* calculatorDaysBetween(rentalDate,returnDate);		
	
		if ( rentalCityId  != returnCityId) {
			
			double differentCityPrice = 100 * calculatorDaysBetween(rentalDate,returnDate);
			totalPrice += differentCityPrice;
		}
		
		return totalPrice;
	}
	@Override
	public boolean checkReturnedInTime(int rentalId, LocalDate returnedTime) {

		checkRentCarExist(rentalId);
		Rental result = this.rentDao.getByRentalId(rentalId);

		long daysBetween = ChronoUnit.DAYS.between(result.getReturnDate() ,returnedTime);		
		if (daysBetween > 0) {
			return true;
		}
		return false;
	}
	@Override
	public double extraPriceCal(int rentalId, List<Integer> additionalServiceId) {

		checkRentCarExist(rentalId);
		Rental result = this.rentDao.getByRentalId(rentalId);

		double extraPrice = (this.additionalService.calculateAdditionalServicePrice(additionalServiceId)
				+ this.carService.calculateRentalPrice(result.getCar().getCarId())) 
				* calculatorDaysBetween(result.getRentalDate(),result.getReturnDate());
		
		return extraPrice;
	}

	@Override
	public boolean checkCarUsed(int carId) {
		
		if (!checkRentCarDate(carId)) {
			throw new BusinessException(Messages.CARNOTDELETE);
		}
		return true;
	}

	@Override
	public boolean checkRentCarDate(int carId) {

		List<Rental> result = this.rentDao.getByCar_CarId(carId);
		for (var rental : result) {
			long daysBetween = ChronoUnit.DAYS.between(rental.getReturnDate(), LocalDate.now());
			if (daysBetween <= 0) {
				throw new BusinessException(Messages.RENTALALREADYEXISTS);
			}
		}
		return true;
	}

	private void updateCarKm(int carId, int lastKm) {

		carService.updateCarKm(carId, lastKm);
	}

	@Override
	public Rental returnRental(int rentalId) {

		checkRentCarExist(rentalId);

		return this.rentDao.getByRentalId(rentalId);
	}


	@Override
	public boolean checkCustomerUsed(int customerId) {

		List<Rental> result = this.rentDao.getAllByCustomer_CustomerId(customerId);
		if (result == null) {
			return true;
		}
		throw new BusinessException(Messages.CUSTOMERNOTDELETE);
	}	
}
