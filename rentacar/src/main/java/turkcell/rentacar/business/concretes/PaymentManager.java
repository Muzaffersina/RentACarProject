package turkcell.rentacar.business.concretes;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import turkcell.rentacar.business.abstracts.PaymentInfoService;
import turkcell.rentacar.business.abstracts.PaymentService;
import turkcell.rentacar.business.abstracts.RentalService;
import turkcell.rentacar.business.contants.Messages;
import turkcell.rentacar.business.dtos.ListPaymentDto;
import turkcell.rentacar.business.requests.create.CreatePaymentRequest;
import turkcell.rentacar.business.requests.delete.DeletePaymentRequest;
import turkcell.rentacar.core.adapters.abstracts.BankAdapterService;
import turkcell.rentacar.core.concretes.BusinessException;
import turkcell.rentacar.core.utilities.mapping.ModelMapperService;
import turkcell.rentacar.core.utilities.results.DataResult;
import turkcell.rentacar.core.utilities.results.Result;
import turkcell.rentacar.core.utilities.results.SuccessDataResult;
import turkcell.rentacar.core.utilities.results.SuccessResult;
import turkcell.rentacar.dataAccess.abstracts.PaymentDao;
import turkcell.rentacar.entities.concretes.Payment;

@Service

public class PaymentManager implements PaymentService {

	private ModelMapperService modelMapperService;
	private PaymentDao paymentDao;
	private RentalService rentalService;
	private BankAdapterService bankAdapterService;
	private PaymentInfoService paymentInfoService;

	@Autowired
	public PaymentManager(ModelMapperService modelMapperService, PaymentDao paymentDao, RentalService rentalService,
			BankAdapterService bankAdapterService, PaymentInfoService paymentInfoService) {
		this.modelMapperService = modelMapperService;
		this.paymentDao = paymentDao;
		this.rentalService = rentalService;
		this.bankAdapterService = bankAdapterService;
		this.paymentInfoService = paymentInfoService;		
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = BusinessException.class)
	public Result add(CreatePaymentRequest createPaymentRequest) {

		this.rentalService.checkRentCarExist(createPaymentRequest.getRentalId());
		
		this.bankAdapterService.checkIfLimitIsEnough(createPaymentRequest.getCreatePaymentInfoRequest(), true); // 
		//this.bankAdapterService.checkIfLimitIsEnough(createPaymentRequest.getCreatePaymentInfoRequest(), false);

		checkRememberMe(createPaymentRequest);

		Payment payment = this.modelMapperService.forRequest().map(createPaymentRequest, Payment.class);
		payment.setTotalPayment(calculatorTotalPrice(createPaymentRequest.getRentalId()));
		payment.setPaymentId(0);

		this.paymentDao.save(payment);

		return new SuccessResult(Messages.PAYMENTADD);
	}

	@Override
	public Result delete(DeletePaymentRequest deletePaymentRequest) {

		checkPaymentExist(deletePaymentRequest.getPaymentId());

		Payment payment = this.modelMapperService.forRequest().map(deletePaymentRequest, Payment.class);
		this.paymentDao.deleteById(payment.getPaymentId());

		return new SuccessResult(Messages.PAYMENTDELETE);
	}

	@Override
	public DataResult<List<ListPaymentDto>> getAll() {

		List<Payment> result = this.paymentDao.findAll();
		List<ListPaymentDto> response = result.stream()
				.map(payment -> this.modelMapperService.forDto().map(payment, ListPaymentDto.class))
				.collect(Collectors.toList());

		return new SuccessDataResult<List<ListPaymentDto>>(response, Messages.PAYMENTLIST);
	}

	@Override
	public DataResult<List<ListPaymentDto>> getAllPaged(int pageNo, int pageSize) {

		Pageable pageable = PageRequest.of(pageNo - 1, pageSize);

		List<Payment> result = this.paymentDao.findAll(pageable).getContent();
		List<ListPaymentDto> response = result.stream()
				.map(payment -> this.modelMapperService.forDto().map(payment, ListPaymentDto.class))
				.collect(Collectors.toList());

		return new SuccessDataResult<List<ListPaymentDto>>(response, Messages.PAYMENTLIST);
	}

	@Override
	public DataResult<List<ListPaymentDto>> getByRentalId(int rentalId) {

		this.rentalService.checkRentCarExist(rentalId);

		List<Payment> result = this.paymentDao.getAllByRental_RentalId(rentalId);
		List<ListPaymentDto> response = result.stream()
				.map(payment -> this.modelMapperService.forDto().map(payment, ListPaymentDto.class))
				.collect(Collectors.toList());

		return new SuccessDataResult<List<ListPaymentDto>>(response, Messages.PAYMENTLIST);
	}

	private boolean checkPaymentExist(int paymentId) {

		if (this.paymentDao.existsById(paymentId)) {
			return true;
		}
		throw new BusinessException(Messages.PAYMENTNOTFOUND);
	}

	private double calculatorTotalPrice(int rentalId) {

		return this.rentalService.returnRental(rentalId).getTotalPrice();
	}

	private boolean checkRememberMe(CreatePaymentRequest createPaymentRequest) {

		if (createPaymentRequest.isRememberMe()) {
			this.paymentInfoService.save(createPaymentRequest.getCreatePaymentInfoRequest());
		}
		return true;
	}

	public boolean checkPaymentForRentalId(int rentalId) {

		this.rentalService.checkRentCarExist(rentalId);

		if (this.paymentDao.getAllByRental_RentalId(rentalId).isEmpty()) {
			return true;
		}
		return false;
	}


}
