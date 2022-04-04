package turkcell.rentacar.business.abstracts;

import java.time.LocalDate;
import java.util.List;

import turkcell.rentacar.business.dtos.ListInvoiceDto;
import turkcell.rentacar.business.requests.create.CreateInvoiceRequest;
import turkcell.rentacar.business.requests.delete.DeleteInvoiceRequest;
import turkcell.rentacar.core.utilities.results.DataResult;
import turkcell.rentacar.core.utilities.results.Result;

public interface InvoiceService {
	
	Result add(CreateInvoiceRequest createInvoiceRequest);
	Result delete(DeleteInvoiceRequest deleteInvoiceRequest);

	
	DataResult<List<ListInvoiceDto>> getAll();
	DataResult<List<ListInvoiceDto>> getByDate(LocalDate rentalDate , LocalDate returnDate);
	DataResult<List<ListInvoiceDto>> getByCustomerId(int customerId);	

	
	boolean checkInvoiceExists(int invoiceId);
	double calculatorTotalPrice(int rentalId);
	double calculatorRentalDays(int rentalId);
	boolean checkRentalIdExists(int rentalId);
	
	
	
	
	
	//void saveInvoice(int customerId, int rentalId, LocalDate rentalDate, LocalDate returnDate,double totalPrice);	
}
