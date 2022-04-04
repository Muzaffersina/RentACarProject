package turkcell.rentacar.business.concretes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import turkcell.rentacar.business.abstracts.CustomerService;
import turkcell.rentacar.business.abstracts.InvoiceService;
import turkcell.rentacar.business.abstracts.RentalService;
import turkcell.rentacar.business.contants.Messages;
import turkcell.rentacar.business.dtos.ListInvoiceDto;
import turkcell.rentacar.business.requests.create.CreateInvoiceRequest;
import turkcell.rentacar.business.requests.delete.DeleteInvoiceRequest;
import turkcell.rentacar.core.concretes.BusinessException;
import turkcell.rentacar.core.utilities.mapping.ModelMapperService;
import turkcell.rentacar.core.utilities.results.DataResult;
import turkcell.rentacar.core.utilities.results.Result;
import turkcell.rentacar.core.utilities.results.SuccessDataResult;
import turkcell.rentacar.core.utilities.results.SuccessResult;
import turkcell.rentacar.dataAccess.abstracts.InvoiceDao;
import turkcell.rentacar.entities.concretes.Invoice;

@Service
public class InvoiceManager implements InvoiceService {

	private ModelMapperService modelMapperService;
	private InvoiceDao invoiceDao;
	private CustomerService customerService;
	private RentalService rentalService;

	@Autowired
	public InvoiceManager(ModelMapperService modelMapperService, InvoiceDao invoiceDao, CustomerService customerService,
			RentalService rentalService) {
		super();
		this.modelMapperService = modelMapperService;
		this.invoiceDao = invoiceDao;
		this.customerService = customerService;
		this.rentalService = rentalService;
	}

	@Override
	public Result add(CreateInvoiceRequest createInvoiceRequest) {
		//iş kuralları
		
		Invoice invoice = this.modelMapperService.forRequest().map(createInvoiceRequest, Invoice.class);

		invoice.setInvoiceId(0);
		invoice.setTotalPrice(calculatorTotalPrice(createInvoiceRequest.getRentalId()));
		invoice.setCreatedDate(LocalDate.now());
		invoice.setRentalDaysValue(calculatorRentalDays(createInvoiceRequest.getRentalId()));
	
		this.invoiceDao.save(invoice);

		return new SuccessResult(Messages.INVOICEADD);
	}

	@Override
	public Result delete(DeleteInvoiceRequest deleteInvoiceRequest) {

		checkInvoiceExists(deleteInvoiceRequest.getInvoiceId());		
		Invoice invoice = this.modelMapperService.forRequest().map(deleteInvoiceRequest, Invoice.class);
		this.invoiceDao.deleteById(invoice.getInvoiceId());

		return new SuccessResult(Messages.INVOICEDELETE);
	}

	@Override
	public DataResult<List<ListInvoiceDto>> getAll() {

		var result = this.invoiceDao.findAll();
		List<ListInvoiceDto> response = result.stream()
				.map(invoice -> this.modelMapperService.forDto().map(invoice, ListInvoiceDto.class))
				.collect(Collectors.toList());

		return new SuccessDataResult<List<ListInvoiceDto>>(response,Messages.INVOICEUPDATE);
	}

	@Override
	public DataResult<List<ListInvoiceDto>> getByDate(LocalDate startDate, LocalDate endDate) {
		
		List<Invoice> invoices = this.invoiceDao.getByCreatedDateBetween(startDate, endDate);
		List<ListInvoiceDto> response = invoices.stream()
				.map(invoice -> this.modelMapperService.forDto().map(invoice, ListInvoiceDto.class))
				.collect(Collectors.toList());
		
		return new SuccessDataResult<List<ListInvoiceDto>>(response,Messages.INVOICELISTFORPRICE);
	}

	@Override
	public DataResult<List<ListInvoiceDto>> getByCustomerId(int customerId) {

		this.customerService.checkCustomerExists(customerId);

		List<Invoice> result = this.invoiceDao.getByCustomer_CustomerId(customerId);
		List<ListInvoiceDto> response = result.stream()
				.map(invoice -> this.modelMapperService.forDto().map(invoice, ListInvoiceDto.class))
				.collect(Collectors.toList());

		return new SuccessDataResult<List<ListInvoiceDto>>(response,Messages.INVOICEFOUND);
	}

	@Override
	public boolean checkInvoiceExists(int invoiceId) {

		var result = this.invoiceDao.existsById(invoiceId);
		if (result) {
			return true;
		}
		throw new BusinessException(Messages.INVOICENOTFOUND);
	}

	@Override
	public double calculatorTotalPrice(int rentalId) {

		var returnedRental = this.rentalService.returnRental(rentalId);

		return returnedRental.getTotalPrice();
	}

	@Override
	public double calculatorRentalDays(int rentalId) {
		
		var returnedRental = this.rentalService.returnRental(rentalId);
		
		int daysBetween = (int) ChronoUnit.DAYS.between(returnedRental.getRentalDate(), returnedRental.getReturnDate());
		return daysBetween;
	}

	@Override
	public boolean checkRentalIdExists(int rentalId) {
		
		this.rentalService.checkRentCarExists(rentalId);

		List<Invoice> result = this.invoiceDao.getAllByRental_RentalId(rentalId);
		
		if(result.isEmpty()) {
			return true;
		}
		throw new BusinessException(Messages.RENTALEXİSTSININVOICE);
		
	}	
	/*@Override
	public void saveInvoice(int customerId, int rentalId, LocalDate rentalDate, LocalDate returnDate,
			double totalPrice) {


		CreateInvoiceRequest createInvoiceRequest = new CreateInvoiceRequest();
		int invoiceNumber = (int) (Math.random()*10000);
		long rentalDaysValue = ChronoUnit.DAYS.between(rentalDate ,  returnDate);
		
		createInvoiceRequest.setInvoiceNo(invoiceNumber);
		createInvoiceRequest.setRentalDaysValue(rentalDaysValue);
		createInvoiceRequest.setCustomerId(customerId);
		createInvoiceRequest.setCreatedDate(LocalDate.now());	
		createInvoiceRequest.setRentalDate(rentalDate);
		createInvoiceRequest.setRentalId(rentalId);
		createInvoiceRequest.setReturnDate(returnDate);
		createInvoiceRequest.setTotalPrice(totalPrice);	
		
		add(createInvoiceRequest);
	}*/


	
}
