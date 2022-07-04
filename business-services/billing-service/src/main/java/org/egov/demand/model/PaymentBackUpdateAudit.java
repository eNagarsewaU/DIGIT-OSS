package org.egov.demand.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PaymentBackUpdateAudit {

	private String paymentId;
	
	private Boolean isBackUpdateSucces;
	
	private Boolean isReceiptCancellation;
	
	private String errorMessage;
	
}
