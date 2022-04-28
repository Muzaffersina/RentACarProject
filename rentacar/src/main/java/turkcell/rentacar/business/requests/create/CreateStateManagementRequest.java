package turkcell.rentacar.business.requests.create;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStateManagementRequest {
	
	private CreateRentalRequest createRentalRequest;
	private CreatePaymentRequest createPaymentRequest;	

}
