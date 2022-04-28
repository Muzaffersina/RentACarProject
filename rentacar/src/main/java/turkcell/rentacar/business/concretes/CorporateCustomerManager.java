package turkcell.rentacar.business.concretes;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import turkcell.rentacar.business.abstracts.CorporateCustomerService;
import turkcell.rentacar.business.abstracts.RentalService;
import turkcell.rentacar.business.contants.Messages;
import turkcell.rentacar.business.dtos.ListCorporateCustomerDto;
import turkcell.rentacar.business.requests.create.CreateCorporateCustomerRequest;
import turkcell.rentacar.business.requests.delete.DeleteCorporateCustomerRequest;
import turkcell.rentacar.business.requests.update.UpdateCorporateCustomerRequest;
import turkcell.rentacar.core.concretes.BusinessException;
import turkcell.rentacar.core.utilities.mapping.ModelMapperService;
import turkcell.rentacar.core.utilities.results.DataResult;
import turkcell.rentacar.core.utilities.results.Result;
import turkcell.rentacar.core.utilities.results.SuccessDataResult;
import turkcell.rentacar.core.utilities.results.SuccessResult;
import turkcell.rentacar.dataAccess.abstracts.CorporateCustomerDao;
import turkcell.rentacar.entities.concretes.CorporateCustomer;

@Service
public class CorporateCustomerManager implements CorporateCustomerService {
	private CorporateCustomerDao corporateCustomerDao;
	private ModelMapperService modelMapperService;
	private RentalService rentalService;
	
	@Autowired
	public CorporateCustomerManager(CorporateCustomerDao corporateCustomerDao, ModelMapperService modelMapperService,
			RentalService rentalService) {
		this.corporateCustomerDao = corporateCustomerDao;
		this.modelMapperService = modelMapperService;
		this.rentalService = rentalService;
	}

	@Override
	public Result add(CreateCorporateCustomerRequest createCorporateCustomerRequest)  {		
		
		checkCorporateCustomerTaxNumber(createCorporateCustomerRequest.getTaxNumber());
		
		CorporateCustomer corporateCustomer = this.modelMapperService.forRequest()
				.map(createCorporateCustomerRequest, CorporateCustomer.class);
		corporateCustomer.setCustomerId(0);
		this.corporateCustomerDao.save(corporateCustomer);
		
		return new SuccessResult(Messages.CORPORATECUSTOMERADD);	
		
	}
	
	
	@Override
	public Result delete(DeleteCorporateCustomerRequest deleteCorporateCustomerRequest) {
		
		checkCorporateCustomerExist(deleteCorporateCustomerRequest.getCustomerId());
		this.rentalService.checkCustomerUsed(deleteCorporateCustomerRequest.getCustomerId());
		
		CorporateCustomer corporateCustomer = this.modelMapperService.forRequest()
				.map(deleteCorporateCustomerRequest, CorporateCustomer.class);
		this.corporateCustomerDao.deleteById(corporateCustomer.getCustomerId());

		return new SuccessResult(Messages.CORPORATECUSTOMERDELETE);
	}

	
	@Override
	public Result update(UpdateCorporateCustomerRequest updateCorporateCustomerRequest) {
		
		checkCorporateCustomerExist(updateCorporateCustomerRequest.getCustomerId());
		
		CorporateCustomer corporateCustomer = this.modelMapperService.forRequest()
				.map(updateCorporateCustomerRequest, CorporateCustomer.class);
		this.corporateCustomerDao.save(corporateCustomer);

		return new SuccessResult(Messages.CORPORATECUSTOMERUPDATE);
	}

	@Override
	public DataResult<List<ListCorporateCustomerDto>> getAll() {
		
		List<CorporateCustomer> result = this.corporateCustomerDao.findAll();
		
		List<ListCorporateCustomerDto> response = result.stream()
				.map(corporateCustomer -> this.modelMapperService.forDto().map(corporateCustomer, ListCorporateCustomerDto.class))
				.collect(Collectors.toList());
		
		return new SuccessDataResult<List<ListCorporateCustomerDto>>(response,Messages.CORPORATECUSTOMERLIST);
	}

	@Override
	public DataResult<ListCorporateCustomerDto> getById(int id) {

		checkCorporateCustomerExist(id);
		
		CorporateCustomer result = this.corporateCustomerDao.getById(id);		
	
		ListCorporateCustomerDto response = this.modelMapperService.forDto().map(result, ListCorporateCustomerDto.class);		
		
		return new SuccessDataResult<ListCorporateCustomerDto>(response,Messages.CORPORATECUSTOMERLIST);
	}

	private boolean checkCorporateCustomerExist(int id) {
		
		boolean result = this.corporateCustomerDao.existsById(id);
		if (result) {
			return true;
		}
		throw new BusinessException(Messages.CORPORATECUSTOMERNOTFOUND);
	}

	
	private boolean checkCorporateCustomerTaxNumber(int taxNumber) {
		
		CorporateCustomer result = this.corporateCustomerDao.getByTaxNumber(taxNumber);
		
		if (result == null) {
			return true; 
		}
		throw new BusinessException(Messages.CORPORATECUSTOMERTAXNUMBERRROR);
	}		
	
}
