package turkcell.rentacar.dataAccess.abstracts;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import turkcell.rentacar.entities.concretes.Payment;

@Repository
public interface PaymentDao extends JpaRepository<Payment, Integer>{
	
	List<Payment> getAllByRental_RentalId(int rentalId); //
}
