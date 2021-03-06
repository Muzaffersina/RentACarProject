package turkcell.rentacar.business.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetListAdditionalDto {
	
	private int additionalId;
	private String additionalName;
	private double additionalPrice;
}
