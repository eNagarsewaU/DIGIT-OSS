package org.egov.win.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class TL {

	private List<Map<String, Object>> ulbCovered;

	private List<Map<String, Object>> newLicenseIssued;

	private List<Map<String, Object>> newLicense;

	private List<Map<String, Object>> renewalLicense;

	private List<Map<String, Object>> renewalLicenseIssued;

	private List<Map<String, Object>> revenueCollected;

	private List<Map<String, Object>> receiptCreated;
	
	private List<Map<String, Object>> onlineCollection;
}
