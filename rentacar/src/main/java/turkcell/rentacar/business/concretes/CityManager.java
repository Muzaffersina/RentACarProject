package turkcell.rentacar.business.concretes;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import turkcell.rentacar.business.abstracts.CityService;
import turkcell.rentacar.business.abstracts.RentalService;
import turkcell.rentacar.business.contants.Messages;
import turkcell.rentacar.business.dtos.ListCityDto;
import turkcell.rentacar.business.requests.create.CreateCityRequest;
import turkcell.rentacar.business.requests.delete.DeleteCityRequest;
import turkcell.rentacar.business.requests.update.UpdateCityRequest;
import turkcell.rentacar.core.concretes.BusinessException;
import turkcell.rentacar.core.utilities.mapping.ModelMapperService;
import turkcell.rentacar.core.utilities.results.DataResult;
import turkcell.rentacar.core.utilities.results.Result;
import turkcell.rentacar.core.utilities.results.SuccessDataResult;
import turkcell.rentacar.core.utilities.results.SuccessResult;
import turkcell.rentacar.dataAccess.abstracts.CityDao;
import turkcell.rentacar.entities.concretes.City;

@Service
public class CityManager implements CityService {

	private ModelMapperService modelMapperService;
	private CityDao cityDao;
	private RentalService rentalService;

	@Autowired
	public CityManager(ModelMapperService modelMapperService, CityDao cityDao,
			RentalService rentalService) {
		this.modelMapperService = modelMapperService;
		this.cityDao = cityDao;
		this.rentalService = rentalService;
	}

	@Override
	public Result add(CreateCityRequest createCityRequest) {

		checkCityNameExist(createCityRequest.getCityName());

		City city = this.modelMapperService.forRequest().map(createCityRequest, City.class);
		this.cityDao.save(city);

		return new SuccessResult(Messages.CITYADD);
	}

	@Override
	public Result delete(DeleteCityRequest deleteCityRequest)  {

		checkCityExist(deleteCityRequest.getCityId());
			
		
		City city= this.modelMapperService.forRequest().map(deleteCityRequest, City.class);
		this.cityDao.deleteById(city.getCityId());
		
		return new SuccessResult(Messages.CITYDELETE);
	}

	@Override
	public Result update(UpdateCityRequest updateCityRequest){
		
		checkCityExist(updateCityRequest.getCityId());
		City city = this.modelMapperService.forRequest().map(updateCityRequest, City.class);
		this.cityDao.save(city);

		
		return new SuccessResult(Messages.CITYUPDATE);
	}

	@Override
	public DataResult<List<ListCityDto>> getAll() {

		List<City> result = this.cityDao.findAll();
		List<ListCityDto> response = result.stream()
				.map(city -> this.modelMapperService.forDto().map(city, ListCityDto.class))
				.collect(Collectors.toList());

		return new SuccessDataResult<List<ListCityDto>>(response,Messages.CITYLIST);
	}

	@Override
	public DataResult<List<ListCityDto>> getAllPaged(int pageNo, int pageSize) {

		Pageable pageable = PageRequest.of(pageNo - 1, pageSize);

		List<City> result = this.cityDao.findAll(pageable).getContent();
		List<ListCityDto> response = result.stream()
				.map(city -> this.modelMapperService.forDto().map(city, ListCityDto.class))
				.collect(Collectors.toList());

		return new SuccessDataResult<List<ListCityDto>>(response, Messages.CITYLIST);
	}

	@Override
	public DataResult<List<ListCityDto>> getAllSorted(Direction direction) {

		Sort sort = Sort.by(direction, "cityId");

		List<City> result = this.cityDao.findAll(sort);
		List<ListCityDto> response = result.stream()
				.map(city -> this.modelMapperService.forDto().map(city, ListCityDto.class))
				.collect(Collectors.toList());

		return new SuccessDataResult<List<ListCityDto>>(response,Messages.CITYLIST);
	}

	@Override
	public DataResult<ListCityDto> getById(int cityId){
		checkCityExist(cityId);

		City result = this.cityDao.getById(cityId);
		ListCityDto response = this.modelMapperService.forDto().map(result, ListCityDto.class);

		return new SuccessDataResult<ListCityDto>(response,Messages.CITYFOUND);
	}

	@Override
	public boolean checkCityExist(int cityId)  {

		if (!this.cityDao.existsById(cityId)) {
			throw new BusinessException(Messages.CITYNOTFOUND);
		}
		return true;
	}

	@Override
	public boolean checkCityNameExist(String cityName) {

		City result = this.cityDao.getByCityName(cityName);
		if (result == null) {
			return true;
		}
		throw new BusinessException(Messages.CITYNAMERROR);
	}

}
