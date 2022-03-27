package turkcell.rentacar.business.abstracts;

import java.util.List;

import turkcell.rentacar.business.dtos.ListOrderedAdditionalDto;
import turkcell.rentacar.business.requests.create.CreateOrderedAdditionalRequest;
import turkcell.rentacar.business.requests.delete.DeleteOrderedAdditionalRequest;
import turkcell.rentacar.business.requests.update.UpdateOrderedAdditionalRequest;
import turkcell.rentacar.core.utilities.results.DataResult;
import turkcell.rentacar.core.utilities.results.Result;

public interface OrderedAdditionalService {
	
	Result add(CreateOrderedAdditionalRequest createOrderedAdditionalRequest);
	Result delete(DeleteOrderedAdditionalRequest deleteOrderedAdditionalRequest);
	Result update(UpdateOrderedAdditionalRequest updateOrderedAdditionalRequest);
	DataResult<List<ListOrderedAdditionalDto>> getAll();
	DataResult<List<ListOrderedAdditionalDto>> getByAdditional_AdditionalId(int additionalId);
	
	boolean checkAdditionalId(int additionalId);
	boolean checkUsedAdditionalId(int additionalId);
	
	
	
	//void saveOrderedAdditional(List<Integer> additionalIds, int rentalId);
}
