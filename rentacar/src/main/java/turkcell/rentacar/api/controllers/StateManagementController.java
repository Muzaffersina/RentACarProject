package turkcell.rentacar.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import turkcell.rentacar.business.abstracts.StateManagementService;
import turkcell.rentacar.business.requests.create.CreateStateManagementRequest;
import turkcell.rentacar.core.utilities.results.Result;

@RestController
@RequestMapping("/api/stateManagement")
public class StateManagementController {
	
	private StateManagementService stateManagementService;

	@Autowired
	public StateManagementController(StateManagementService stateManagementService) {
		this.stateManagementService = stateManagementService;
	}
	
	@PostMapping("/createForIndividualCustomer")
	public Result createForIndividualCustomer(@RequestBody CreateStateManagementRequest createStateManagementRequest){
		return this.stateManagementService.createForIndividualCustomer(createStateManagementRequest);
	}
	
	@PostMapping("/createForCorporateCustomer")
	public Result createForCorporateCustomer(@RequestBody CreateStateManagementRequest createStateManagementRequest){
		return this.stateManagementService.createForCorporateCustomer(createStateManagementRequest);
	}
	

}
