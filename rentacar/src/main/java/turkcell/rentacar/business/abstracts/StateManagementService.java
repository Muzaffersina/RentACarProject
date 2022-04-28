package turkcell.rentacar.business.abstracts;


import turkcell.rentacar.business.requests.create.CreateStateManagementRequest;
import turkcell.rentacar.core.utilities.results.Result;

public interface StateManagementService {
	
	Result createForIndividualCustomer(CreateStateManagementRequest createStateManagementRequest );	
	Result createForCorporateCustomer(CreateStateManagementRequest createStateManagementRequest);

}
