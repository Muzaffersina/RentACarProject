package turkcell.rentacar.business.concretes;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import turkcell.rentacar.business.abstracts.AdditionalService;
import turkcell.rentacar.business.abstracts.OrderedAdditionalService;
import turkcell.rentacar.business.abstracts.RentalService;
import turkcell.rentacar.business.contants.Messages;
import turkcell.rentacar.business.dtos.ListOrderedAdditionalDto;
import turkcell.rentacar.business.requests.create.CreateOrderedAdditionalRequest;
import turkcell.rentacar.business.requests.delete.DeleteOrderedAdditionalRequest;
import turkcell.rentacar.business.requests.update.UpdateOrderedAdditionalRequest;
import turkcell.rentacar.core.concretes.BusinessException;
import turkcell.rentacar.core.utilities.mapping.ModelMapperService;
import turkcell.rentacar.core.utilities.results.DataResult;
import turkcell.rentacar.core.utilities.results.Result;
import turkcell.rentacar.core.utilities.results.SuccessDataResult;
import turkcell.rentacar.core.utilities.results.SuccessResult;
import turkcell.rentacar.dataAccess.abstracts.OrderedAdditionalDao;
import turkcell.rentacar.entities.concretes.OrderedAdditional;

@Service
public class OrderedAdditionalManager implements OrderedAdditionalService {

	private ModelMapperService modelMapperService;
	private OrderedAdditionalDao orderedAdditionalDao;
	private AdditionalService additionalService;
	private RentalService rentalService;

	@Autowired
	public OrderedAdditionalManager(ModelMapperService modelMapperService, OrderedAdditionalDao orderedAdditionalDao,
			AdditionalService additionalService, RentalService rentalService) {

		this.modelMapperService = modelMapperService;
		this.orderedAdditionalDao = orderedAdditionalDao;
		this.additionalService = additionalService;
		this.rentalService = rentalService;
	}

	@Override
	public Result add(CreateOrderedAdditionalRequest createOrderedAdditionalRequest) {

		this.rentalService.checkRentCarExist(createOrderedAdditionalRequest.getRentalId());
		this.additionalService.checkAdditionalExist(createOrderedAdditionalRequest.getAdditionalId());

		OrderedAdditional orderedAdditional = this.modelMapperService.forRequest().map(createOrderedAdditionalRequest,
				OrderedAdditional.class);

		orderedAdditional.setOrderedId(0);
		this.orderedAdditionalDao.save(orderedAdditional);

		return new SuccessResult(Messages.ORDEREDADDITIONALSERVICEADD);
	}

	@Override
	public Result delete(DeleteOrderedAdditionalRequest deleteOrderedAdditionalRequest) {

		checkAdditionalId(deleteOrderedAdditionalRequest.getOrderedId());

		OrderedAdditional orderedAdditional = this.modelMapperService.forRequest().map(deleteOrderedAdditionalRequest, OrderedAdditional.class);
		this.orderedAdditionalDao.deleteById(orderedAdditional.getOrderedId());

		return new SuccessResult(Messages.ORDEREDADDITIONALSERVICEDELETE);
	}

	@Override
	public Result update(UpdateOrderedAdditionalRequest updateOrderedAdditionalRequest) {

		this.rentalService.checkRentCarExist(updateOrderedAdditionalRequest.getRentalId());
		this.additionalService.checkAdditionalExist(updateOrderedAdditionalRequest.getAdditionalId());


		OrderedAdditional orderedAdditional = this.modelMapperService.forRequest().map(updateOrderedAdditionalRequest, OrderedAdditional.class);
		this.orderedAdditionalDao.save(orderedAdditional);

		return new SuccessResult(Messages.ORDEREDADDITIONALSERVICEUPDATE);
	}

	@Override
	public DataResult<List<ListOrderedAdditionalDto>> getAll() {

		List<OrderedAdditional> result = this.orderedAdditionalDao.findAll();

		List<ListOrderedAdditionalDto> response = result.stream().map(orderedAdditional -> this.modelMapperService
				.forDto().map(orderedAdditional, ListOrderedAdditionalDto.class)).collect(Collectors.toList());

		return new SuccessDataResult<List<ListOrderedAdditionalDto>>(response, Messages.ORDEREDADDITIONALSERVICELIST);
	}

	@Override
	public DataResult<List<ListOrderedAdditionalDto>> getByAdditional_AdditionalId(int additionalId) {

		checkAdditionalId(additionalId);

		List<OrderedAdditional> result = this.orderedAdditionalDao.getByAdditional_AdditionalId(additionalId);
		List<ListOrderedAdditionalDto> response = result.stream().map(orderedAdditional -> this.modelMapperService
				.forDto().map(orderedAdditional, ListOrderedAdditionalDto.class)).collect(Collectors.toList());

		return new SuccessDataResult<List<ListOrderedAdditionalDto>>(response, Messages.ORDEREDADDITIONALSERVICELIST);
	}

	private boolean checkAdditionalId(int additionalId) {

		if (this.additionalService.checkAdditionalExist(additionalId)) {
			return true;
		}
		throw new BusinessException(Messages.ORDEREDADDITIONALSERVICENOTFOUND);
	}

	@Override
	public boolean checkUsedAdditionalId(int additionalId) {

		List<OrderedAdditional> result = this.orderedAdditionalDao.getByAdditional_AdditionalId(additionalId);
		if (result == null) {
			return true;
		}
		throw new BusinessException(Messages.ORDEREDADDITIONALSERVICENOTDELETE);
	}
	
	
	@Override
	public void saveOrderedAdditional(List<Integer> additionalIds, int rentalId) {
		
		CreateOrderedAdditionalRequest createOrderedAdditionalRequest = new CreateOrderedAdditionalRequest();		
		for (int additionalId : additionalIds) {
			if(additionalId != 0) {
				createOrderedAdditionalRequest.setAdditionalId(additionalId);
				createOrderedAdditionalRequest.setRentalId(rentalId);
				add(createOrderedAdditionalRequest);
			}			
		}
	}
	
}
