package turkcell.rentacar.business.concretes;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import turkcell.rentacar.business.abstracts.InvoiceService;
import turkcell.rentacar.business.abstracts.OrderedAdditionalService;
import turkcell.rentacar.business.abstracts.PaymentService;
import turkcell.rentacar.business.abstracts.RentalService;
import turkcell.rentacar.business.abstracts.StateManagementService;
import turkcell.rentacar.business.requests.create.CreateInvoiceRequest;
import turkcell.rentacar.business.requests.create.CreatePaymentRequest;
import turkcell.rentacar.business.requests.create.CreateRentalRequest;
import turkcell.rentacar.business.requests.create.CreateStateManagementRequest;
import turkcell.rentacar.core.concretes.BusinessException;
import turkcell.rentacar.core.utilities.results.DataResult;
import turkcell.rentacar.core.utilities.results.Result;
import turkcell.rentacar.core.utilities.results.SuccessResult;

@Service
public class StateManagementManager implements StateManagementService {

	private RentalService rentalService;
	private PaymentService paymentService;
	private InvoiceService invoiceService;
	private OrderedAdditionalService orderedAdditionalService;

	@Autowired
	public StateManagementManager(RentalService rentalService, PaymentService paymentService,
			InvoiceService invoiceService, OrderedAdditionalService orderedAdditionalService) {
		this.rentalService = rentalService;
		this.paymentService = paymentService;
		this.invoiceService = invoiceService;
		this.orderedAdditionalService = orderedAdditionalService;

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor = BusinessException.class)
	public Result createForIndividualCustomer(CreateStateManagementRequest createStateManagementRequest) {
		
		int rentalId = toSetRentalForIndividual(createStateManagementRequest.getCreateRentalRequest()).getData();
		
		toSetPayment(createStateManagementRequest.getCreatePaymentRequest(),rentalId);
		
		toSetInvoice(createStateManagementRequest.getCreateRentalRequest().getCustomerId(),rentalId
				,createStateManagementRequest.getCreateRentalRequest().getRentalDate()
				,createStateManagementRequest.getCreateRentalRequest().getReturnDate());
		
		toSetOrderedAdditional(createStateManagementRequest.getCreateRentalRequest().getAdditionalIds(),rentalId);
		
		return new SuccessResult("Created for IndividualCustomer");
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor = BusinessException.class)
	public Result createForCorporateCustomer(CreateStateManagementRequest createStateManagementRequest) {
		
		int rentalId = toSetRentalForCorporateCustomer(createStateManagementRequest.getCreateRentalRequest()).getData();
		
		toSetPayment(createStateManagementRequest.getCreatePaymentRequest(),rentalId);
		
		toSetInvoice(createStateManagementRequest.getCreateRentalRequest().getCustomerId(),rentalId
				,createStateManagementRequest.getCreateRentalRequest().getRentalDate()
				,createStateManagementRequest.getCreateRentalRequest().getReturnDate());
		
		toSetOrderedAdditional(createStateManagementRequest.getCreateRentalRequest().getAdditionalIds(),rentalId);
		
		return new SuccessResult("Created for CorporateCustomer");
	}
	

	private DataResult<Integer> toSetRentalForIndividual(CreateRentalRequest createRentalRequest) {

		DataResult<Integer> rentalId= this.rentalService.addIndividualCustomer(createRentalRequest);		
		return rentalId;
		 
	}
	private DataResult<Integer> toSetRentalForCorporateCustomer(CreateRentalRequest createRentalRequest) {

		DataResult<Integer> rentalId= this.rentalService.addCorporateCustomer(createRentalRequest);		
		return rentalId;
		 
	}
	
	private void toSetPayment(CreatePaymentRequest createPaymentRequest,int rentalId) {
		
		createPaymentRequest.setRentalId(rentalId);
		this.paymentService.add(createPaymentRequest);
	}
	
	private void toSetInvoice(int customerId,int rentalId ,LocalDate rentalDate,LocalDate returnDate) {
		
		CreateInvoiceRequest request = new CreateInvoiceRequest();
		request.setInvoiceNo(customerId+rentalId);
		request.setCustomerId(customerId);		
		request.setRentalId(rentalId);
		request.setRentalDate(rentalDate);
		request.setReturnDate(returnDate);
		this.invoiceService.add(request);
	}
	
	private void toSetOrderedAdditional(List<Integer> additionalIds, int rentalId ) {

		this.orderedAdditionalService.saveOrderedAdditional(additionalIds, rentalId);
	}

	
}
