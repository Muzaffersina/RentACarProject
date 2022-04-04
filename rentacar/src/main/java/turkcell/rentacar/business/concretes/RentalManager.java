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

import turkcell.rentacar.business.abstracts.AdditionalService;
import turkcell.rentacar.business.abstracts.CarMaintenanceService;
import turkcell.rentacar.business.abstracts.CarService;
import turkcell.rentacar.business.abstracts.CityService;
import turkcell.rentacar.business.abstracts.CustomerService;
import turkcell.rentacar.business.abstracts.InvoiceService;
import turkcell.rentacar.business.abstracts.OrderedAdditionalService;
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
	private OrderedAdditionalService orderedAdditionalService;
	private InvoiceService invoiceService;

	@Lazy
	@Autowired
	public RentalManager(RentalDao rentDao, ModelMapperService modelMapperService,
			CarMaintenanceService carMaintenanceService, CarService carService, AdditionalService additionalService,
			CityService cityService, CustomerService customerService, OrderedAdditionalService orderedAdditionalService,
			InvoiceService invoiceService) {

		this.rentDao = rentDao;
		this.modelMapperService = modelMapperService;
		this.carMaintenanceService = carMaintenanceService;
		this.carService = carService;
		this.additionalService = additionalService;
		this.cityService = cityService;
		this.customerService = customerService;
		this.orderedAdditionalService = orderedAdditionalService;
		this.invoiceService = invoiceService;

	}

	@Override
	public Result addIndividualCustomer(CreateRentalRequest createRentRequest) {

		this.carService.checkCarExists(createRentRequest.getCarId());
		this.customerService.checkCustomerExists(createRentRequest.getCustomerId());
		this.additionalService.checkAllAdditional(createRentRequest.getAdditionalIds());
		this.carMaintenanceService.checkCarMaintenceCar_CarIdReturnDate(createRentRequest.getCarId());
		this.cityService.checkCityExists(createRentRequest.getRentalCityId());
		this.cityService.checkCityExists(createRentRequest.getReturnCityId());
		checkDate(createRentRequest.getRentalDate(), createRentRequest.getReturnDate());
		checkRentCarDate(createRentRequest.getCarId());

		Rental rent = this.modelMapperService.forRequest().map(createRentRequest, Rental.class);
		

		rent.setRentalId(0);		
		this.rentDao.save(rent);
		rent.setTotalPrice(calculatorTotalPrice(rent.getRentalId(), createRentRequest.getAdditionalIds()));
		this.rentDao.save(rent);
		return new SuccessResult(Messages.RENTALADD);
	}

	@Override
	public Result addCorporateCustomer(CreateRentalRequest createRentRequest) {

		this.carService.checkCarExists(createRentRequest.getCarId());
		this.customerService.checkCustomerExists(createRentRequest.getCustomerId());
		this.additionalService.checkAllAdditional(createRentRequest.getAdditionalIds());
		this.carMaintenanceService.checkCarMaintenceCar_CarIdReturnDate(createRentRequest.getCarId());
		this.cityService.checkCityExists(createRentRequest.getRentalCityId());
		this.cityService.checkCityExists(createRentRequest.getReturnCityId());
		checkDate(createRentRequest.getRentalDate(), createRentRequest.getReturnDate());
		checkRentCarDate(createRentRequest.getCarId());

		Rental rent = this.modelMapperService.forRequest().map(createRentRequest, Rental.class);
		rent.setRentalId(0);
		rent.setTotalPrice(calculatorTotalPrice(rent.getRentalId(), createRentRequest.getAdditionalIds()));

		
		this.rentDao.save(rent);

		return new SuccessResult(Messages.RENTALADD);
	}

	@Override
	public Result delete(DeleteRentalRequest deleteRentRequest) {

		checkRentCarExists(deleteRentRequest.getRentalId());
		this.invoiceService.checkRentalIdExists(deleteRentRequest.getRentalId());
		// buraya iş kuralı eklenebilir. invoice-payment

		Rental rent = this.modelMapperService.forRequest().map(deleteRentRequest, Rental.class);
		this.rentDao.deleteById(rent.getRentalId());

		return new SuccessResult(Messages.RENTALDELETE);

	}

	@Override
	public Result update(UpdateRentalRequest updateRentRequest) {

		// sadeleştirilecek.

		checkRentCarExists(updateRentRequest.getRentalId());
		this.carService.checkCarExists(updateRentRequest.getCarId());
		this.cityService.checkCityExists(updateRentRequest.getRentalCityId());
		this.cityService.checkCityExists(updateRentRequest.getReturnCityId());
		this.additionalService.checkAllAdditional(updateRentRequest.getAdditionalIds());
		this.customerService.checkCustomerExists(updateRentRequest.getCustomerId());
		checkDate(updateRentRequest.getRentalDate(), updateRentRequest.getReturnDate());
		checkRentalCarCarId(updateRentRequest.getRentalId(),updateRentRequest.getCarId());
		Rental rent = this.modelMapperService.forRequest().map(updateRentRequest, Rental.class);

		rent.setTotalPrice(
				calculatorTotalPrice(updateRentRequest.getRentalId(), updateRentRequest.getAdditionalIds()));

		if (checkReturnedInTime(updateRentRequest.getRentalId(), updateRentRequest.getReturnDate())) {
			// donus tarıhı .now ile de olabilir.
			// Ek ödeme faturaya gidip ekleyecek mi ?
			extraPriceCal(updateRentRequest.getRentalId(), updateRentRequest.getAdditionalIds());
			System.out.println(extraPriceCal(updateRentRequest.getRentalId(), updateRentRequest.getAdditionalIds()));
		}

		updateCarKm(updateRentRequest.getCarId(), updateRentRequest.getReturnKm());
		/*addOrderedAdditional(updateRentRequest.getAdditionalIds(), updateRentRequest.getRentalId());		
		addInvoice(updateRentRequest.getRentalId());
		*/

		this.rentDao.save(rent);

		return new SuccessResult(Messages.RENTALUPDATE);
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

		checkRentCarExists(rentalId);

		var result = this.rentDao.getByRentalId(rentalId);
		GetListRentDto response = this.modelMapperService.forDto().map(result, GetListRentDto.class);

		return new SuccessDataResult<GetListRentDto>(response, Messages.RENTALLIST);
	}

	@Override
	public DataResult<List<ListRentDto>> getByCarCarId(int carId) {

		this.checkRentCarExists(carId);

		List<Rental> result = this.rentDao.getByCar_CarId(carId);
		List<ListRentDto> response = result.stream()
				.map(rent -> this.modelMapperService.forDto().map(rent, ListRentDto.class))
				.collect(Collectors.toList());

		return new SuccessDataResult<List<ListRentDto>>(response, Messages.RENTALLIST);
	}

	@Override
	public boolean checkRentCarExists(int rentalId) {

		var result = this.rentDao.getByRentalId(rentalId);
		if (result != null) {
			return true;
		}
		throw new BusinessException(Messages.RENTALNOTFOUND);
	}
	
	public boolean checkRentalCarCarId(int rentalId, int carId) {
		 checkRentCarExists(rentalId);		
		 
		 if(this.rentDao.getByRentalId(rentalId).getCar().getCarId() == carId) {
			 return true;
		 }
		 
		 throw new BusinessException(Messages.RENTALCARIDDOESNTEXİSTS);		
	}

	public boolean checkDate(LocalDate rentalDate, LocalDate returnDate) {

		long daysBetween = ChronoUnit.DAYS.between(rentalDate, returnDate);
		if (daysBetween < 0) {
			throw new BusinessException(Messages.RENTALDATESNOTVALİD);
		}
		return true;
	}

	public long calculatorDaysBetween(int rentalId) {

		checkRentCarExists(rentalId);
		var result = this.rentDao.getByRentalId(rentalId);
		long daysBetween = ChronoUnit.DAYS.between(result.getRentalDate(), result.getReturnDate());
		if (daysBetween == 0) {
			daysBetween = 1;
		}
		return daysBetween;

	}

	
	public double calculatorTotalPrice(int rentalId, List<Integer> additionalServiceId) {

		double totalPrice = 0;
		totalPrice = (this.additionalService.calculateAdditionalServicePrice(additionalServiceId)
				+ this.carService.calculateRentalPrice(this.rentDao.getByRentalId(rentalId).getCar().getCarId()))
				* calculatorDaysBetween(rentalId);		
		/// Bu kısım create de
		// double carDailyKm = this.carService.calculateRentalPrice(carId);
		// lastKm bilgisi olmadığı için eklenemiyor
		if ((this.rentDao.getByRentalId(rentalId).getRentalCity().getCityId() != this.rentDao.getByRentalId(rentalId)
				.getReturnCity().getCityId())) {

			double differentCityPrice = 100 * calculatorDaysBetween(rentalId);
			totalPrice += differentCityPrice;
		}
		
		return totalPrice;
	}

	public boolean checkReturnedInTime(int rentalId, LocalDate returnedTime) {

		checkRentCarExists(rentalId);
		var result = this.rentDao.getByRentalId(rentalId);

		long daysBetween = ChronoUnit.DAYS.between(result.getReturnDate() ,returnedTime);		
		if (daysBetween > 0) {
			return true;
		}
		return false;
	}

	public double extraPriceCal(int rentalId, List<Integer> additionalServiceId) {

		checkRentCarExists(rentalId);
		var result = this.rentDao.getByRentalId(rentalId);

		double extraPrice = (this.additionalService.calculateAdditionalServicePrice(additionalServiceId)
				+ this.carService.calculateRentalPrice(result.getCar().getCarId())) 
				* calculatorDaysBetween(rentalId);
		
		return extraPrice;
	}

	@Override
	public boolean checkCarUsed(int carId) {

		var result = checkRentCarDate(carId);
		if (!result) {
			throw new BusinessException(Messages.CARNOTDELETE);
		}
		return true;
	}

	@Override
	public boolean checkRentCarDate(int carId) {

		var result = this.rentDao.getByCar_CarId(carId);
		for (var rental : result) {
			long daysBetween = ChronoUnit.DAYS.between(rental.getReturnDate(), LocalDate.now());
			if (daysBetween <= 0) {
				throw new BusinessException(Messages.RENTALALREADYEXISTS);
			}
		}
		return true;
	}

	public void updateCarKm(int carId, int lastKm) {

		carService.updateCarKm(carId, lastKm);
		// Belki bir hata mesaji dondurelebilir.
	}

	@Override
	public Rental returnRental(int rentalId) {

		checkRentCarExists(rentalId);

		var returnedRental = this.rentDao.getByRentalId(rentalId);
		return returnedRental;
	}


	@Override
	public boolean checkCustomerUsed(int customerId) {

		var result = this.rentDao.getAllByCustomer_CustomerId(customerId);
		if (result == null) {
			return true;
		}
		throw new BusinessException(Messages.CUSTOMERNOTDELETE);
	}	

	
	/*public void addOrderedAdditional(List<Integer> additionalIds, int rentalId) {
		
		checkRentCarExists(rentalId);

		var result = this.rentDao.getByRentalId(rentalId);
		GetListRentDto response = this.modelMapperService.forDto().map(result, GetListRentDto.class);
		this.orderedAdditionalService.saveOrderedAdditional(additionalIds, response.getRentalId());
		/*
		// burada da iş kuralı gerek sanki Son araba gelmeyebilir burada
		List<ListRentDto> savedCar = getByCar_CarId(carId);
		for (ListRentDto listRentDto : savedCar) {
			this.orderedAdditionalService.saveOrderedAdditional(additionalIds, listRentDto.getRentalId());
		}
		
	}

	public void addInvoice(int rentalId) {

		checkRentCarExists(rentalId);

		var result = this.rentDao.getByRentalId(rentalId);
		GetListRentDto response = this.modelMapperService.forDto().map(result, GetListRentDto.class);
		
		this.invoiceService.saveInvoice(response.getCustomerId(), response.getRentalId(), response.getRentalDate(),
				response.getReturnDate(), response.getTotalPrice());		
	}*/
	
	
}
