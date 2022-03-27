package turkcell.rentacar.business.requests.create;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateInvoiceRequest {	
	
	@NotNull
	@Positive
	private int invoiceNo;
	@NotNull
	@Positive
	private int customerId;
	@NotNull
	@Positive
	private int rentalId;	
	@NotNull
	private LocalDate rentalDate;
	@NotNull
	private LocalDate returnDate;	
}
