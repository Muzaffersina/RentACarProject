package turkcell.rentacar.business.requests.update;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBrandRequest {
	@NotNull
	@Positive
	private int brandId;
	
	@NotNull
	@Size(min=1,max=15)
	private String brandName;
}
