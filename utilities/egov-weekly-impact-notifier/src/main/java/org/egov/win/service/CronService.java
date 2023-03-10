package org.egov.win.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.egov.common.contract.request.RequestInfo;
import org.egov.win.model.Body;
import org.egov.win.model.Email;
import org.egov.win.model.EmailRequest;
import org.egov.win.model.Firenoc;
import org.egov.win.model.MiscCollections;
import org.egov.win.model.PGR;
import org.egov.win.model.PGRChannelBreakup;
import org.egov.win.model.PT;
import org.egov.win.model.StateWide;
import org.egov.win.model.TL;
import org.egov.win.model.WaterAndSewerage;
import org.egov.win.producer.Producer;
import org.egov.win.utils.CronConstants;
import org.egov.win.utils.CronUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CronService {

	@Autowired
	private EmailService emailService;

	@Autowired
	private CronUtils utils;

	@Autowired
	private ExternalAPIService externalAPIService;

	@Autowired
	private Producer producer;

	@Value("${egov.core.notification.email.topic}")
	private String emailTopic;

	@Value("${egov.impact.emailer.interval.in.secs}")
	private Long timeInterval;

	@Value("${egov.impact.emailer.email.to.address}")
	private String toAddress;

	@Value("${egov.impact.emailer.email.subject}")
	private String subject;

	public void fetchData() {
		try {
			Email email = getDataFromDb();

			if (email != null & email.getData() != null && email.getData().getStateWide() != null
					&& email.getData().getPt() != null && email.getData().getTl() != null
					&& email.getData().getMiscCollections() != null) {
				String content = emailService.formatEmail(email);
				send(email, content);
			}else {
				log.info("Email will not be sent, ERROR: data issue");
			}
		} catch (Exception e) {
			log.info("Email will not be sent, ERROR: ", e);
		}
	}

	private Email getDataFromDb() {
		Body body = new Body();
		/*
		 * List<Map<String, Object>> wsData = externalAPIService.getWSData();
		 * if(CollectionUtils.isEmpty(wsData)) throw new
		 * CustomException("EMAILER_DATA_RETREIVAL_FAILED",
		 * "Failed to retrieve data from WS module");
		 */
		enrichHeadersOfTheTable(body);
		enrichBodyWithStateWideData(body);
		// enrichBodyWithPGRData(body);
		enrichBodyWithPTData(body);
		enrichBodyWithTLData(body);
		enrichBodyWithMiscCollData(body);
		// enrichBodyWithWSData(body, wsData);
		// enrichBodyWithFirenocData(body);
		return Email.builder().body(body.toString()).data(body).build();
	}

	private void enrichHeadersOfTheTable(Body body) {
		String prefix = "Week";
		Integer noOfWeeks = 6;
		List<Map<String, Object>> header = new ArrayList<>();
		for (int week = 0; week < noOfWeeks; week++) {
			Map<String, Object> date = new HashMap<>();
			date.put(prefix + week,
					utils.getDayAndMonth((System.currentTimeMillis() - ((timeInterval * 1000) * week))));
			header.add(date);
		}
		body.setHeader(header);
	}

	private void enrichBodyWithStateWideData(Body body) {
		List<Map<String, Object>> data = externalAPIService.getRainmakerData(CronConstants.SEARCHER_SW);
		List<Map<String, Object>> ulbCovered = new ArrayList<>();
		List<Map<String, Object>> revenueCollected = new ArrayList<>();
		List<Map<String, Object>> servicesApplied = new ArrayList<>();
		List<Map<String, Object>> noOfCitizensResgistered = new ArrayList<>();
		List<Map<String, Object>> amountCollectedOnline = new ArrayList<>();
		Map<String, Object> map = utils.getWeekWiseRevenue(data);
		for (Map<String, Object> record : data) {
			Map<String, Object> ulbCoveredPerWeek = new HashMap<>();
			Map<String, Object> revenueCollectedPerWeek = new HashMap<>();
			Map<String, Object> servicesAppliedPerWeek = new HashMap<>();
			Map<String, Object> noOfCitizensResgisteredPerWeek = new HashMap<>();
			Map<String, Object> amountCollectedOnlinePerWeek = new HashMap<>();
			String prefix = "Week";
			Integer noOfWeeks = 6;
			Integer wsIndex = 0;
			for (int week = 0; week < noOfWeeks; week++) {
				if (record.get("day").equals(prefix + week)) {
					ulbCoveredPerWeek.put("w" + week + "ulbc", String.format("%.0f", record.get("ulbcovered"))); // ws
																													// not
																													// added
																													// because
																													// we
					// need a union logic.
					revenueCollectedPerWeek
							.put("w" + week + "revcoll",
									(new BigDecimal(record.get("revenuecollected") != null
											? record.get("revenuecollected").toString()
											: BigDecimal.ZERO.toString()).add(new BigDecimal(
													((Map) (map.get(prefix + week))).get("revenueCollected") != null
															? ((Map) (map.get(prefix + week))).get("revenueCollected")
																	.toString()
															: BigDecimal.ZERO.toString()))));
	
					servicesAppliedPerWeek.put("w" + week + "serapp", record.get("servicesapplied"));

					noOfCitizensResgisteredPerWeek.put("w" + week + "citreg", record.get("noofusersregistered"));
					
					amountCollectedOnlinePerWeek
						.put("w" + week + "onlinecoll",
								(new BigDecimal(record.get("onlinecollection") != null
										? record.get("onlinecollection").toString()
										: BigDecimal.ZERO.toString()).add(new BigDecimal(
												((Map) (map.get(prefix + week))).get("onlineCollection") != null
														? ((Map) (map.get(prefix + week))).get("onlineCollection")
																.toString()
														: BigDecimal.ZERO.toString()))));
					wsIndex++;
				}
			}
			ulbCovered.add(ulbCoveredPerWeek);
			revenueCollected.add(revenueCollectedPerWeek);
			servicesApplied.add(servicesAppliedPerWeek);
			noOfCitizensResgistered.add(noOfCitizensResgisteredPerWeek);
			amountCollectedOnline.add(amountCollectedOnlinePerWeek);
		}

		if (!data.isEmpty()) {
			StateWide stateWide = StateWide.builder().noOfCitizensResgistered(noOfCitizensResgistered)
					.revenueCollected(revenueCollected).onlineCollection(amountCollectedOnline).servicesApplied(servicesApplied).ulbCovered(ulbCovered).build();
			body.setStateWide(stateWide);
		}

	}

	private void enrichBodyWithPGRData(Body body) {
		List<Map<String, Object>> data = externalAPIService.getRainmakerData(CronConstants.SEARCHER_PGR);
		List<Map<String, Object>> ulbCovered = new ArrayList<>();
		List<Map<String, Object>> totalComplaints = new ArrayList<>();
		List<Map<String, Object>> redressal = new ArrayList<>();
		for (Map<String, Object> record : data) {
			Map<String, Object> ulbCoveredPerWeek = new HashMap<>();
			Map<String, Object> totalComplaintsPerWeek = new HashMap<>();
			Map<String, Object> redressalPerWeek = new HashMap<>();
			String prefix = "Week";
			Integer noOfWeeks = 6;
			for (int week = 0; week < noOfWeeks; week++) {
				if (record.get("day").equals(prefix + week)) {
					ulbCoveredPerWeek.put("w" + week + "pgrulbc", record.get("ulbcovered"));
					totalComplaintsPerWeek.put("w" + week + "pgrtcmp", record.get("totalcomplaints"));
					redressalPerWeek.put("w" + week + "pgrredd", record.get("redressal"));
				}
			}
			ulbCovered.add(ulbCoveredPerWeek);
			totalComplaints.add(totalComplaintsPerWeek);
			redressal.add(redressalPerWeek);
		}
		PGR pgr = PGR.builder().redressal(redressal).totalComplaints(totalComplaints).ulbCovered(ulbCovered).build();
		enrichBodyWithPGRChannelData(body, pgr);
		body.setPgr(pgr);
	}

	private void enrichBodyWithPGRChannelData(Body body, PGR pgr) {
		List<Map<String, Object>> data = externalAPIService.getRainmakerData(CronConstants.SEARCHER_PGR_CHANNEL);
		List<Map<String, Object>> ivr = new ArrayList<>();
		List<Map<String, Object>> mobiileApp = new ArrayList<>();
		List<Map<String, Object>> webApp = new ArrayList<>();
		for (Map<String, Object> record : data) {
			Map<String, Object> ivrPerWeek = new HashMap<>();
			Map<String, Object> mobileAppPerWeek = new HashMap<>();
			Map<String, Object> webAppPerWeek = new HashMap<>();
			String prefix = "Week";
			Integer noOfWeeks = 6;
			for (int week = 0; week < noOfWeeks; week++) {
				if (record.get("day").equals(prefix + week)) {
					ivrPerWeek.put("w" + week + "pgrchnlivr", record.get("ivr"));
					mobileAppPerWeek.put("w" + week + "pgrchnlmapp", record.get("mobileapp"));
					webAppPerWeek.put("w" + week + "pgrchnlweb", record.get("webapp"));
				}
			}
			ivr.add(ivrPerWeek);
			mobiileApp.add(mobileAppPerWeek);
			webApp.add(webAppPerWeek);
		}

		PGRChannelBreakup channel = PGRChannelBreakup.builder().ivr(ivr).mobileApp(mobiileApp).webApp(webApp).build();
		pgr.setChannelBreakup(channel);
	}

	private void enrichBodyWithPTData(Body body) {
		List<Map<String, Object>> data = externalAPIService.getRainmakerData(CronConstants.SEARCHER_PT);
		List<Map<String, Object>> ulbCovered = new ArrayList<>();
		List<Map<String, Object>> revenueCollected = new ArrayList<>();
		List<Map<String, Object>> noOfProperties = new ArrayList<>();
		List<Map<String, Object>> receiptGenerated = new ArrayList<>();
		List<Map<String, Object>> onlineCollection = new ArrayList<>();
		List<Map<String, Object>> updatedCollection = new ArrayList<>();
		List<Map<String, Object>> bbpsCollection = new ArrayList<>();		

		for (Map<String, Object> record : data) {
			Map<String, Object> ulbCoveredPerWeek = new HashMap<>();
			Map<String, Object> revenueCollectedPerWeek = new HashMap<>();
			Map<String, Object> noOfPropertiesPerWeek = new HashMap<>();
			Map<String, Object> receiptGeneratedPerWeek = new HashMap<>();
			Map<String, Object> onlineCollectionPerWeek = new HashMap<>();
			Map<String, Object> updatedCollectionPerWeek = new HashMap<>();
			Map<String, Object> bbpsCollectionPerWeek = new HashMap<>();

			String prefix = "Week";
			Integer noOfWeeks = 6;
			for (int week = 0; week < noOfWeeks; week++) {
				if (record.get("day").equals(prefix + week)) {
					ulbCoveredPerWeek.put("w" + week + "ptulbc",
							record.get("ulbcovered") != null ? String.format("%.0f", record.get("ulbcovered"))
									: BigDecimal.ZERO);
					revenueCollectedPerWeek.put("w" + week + "ptrevcoll",
							record.get("revenuecollected") != null ? record.get("revenuecollected") : BigDecimal.ZERO);
					noOfPropertiesPerWeek.put("w" + week + "ptnoofprp",
							record.get("noofpropertiescreated") != null ? record.get("noofpropertiescreated")
									: BigDecimal.ZERO);
					receiptGeneratedPerWeek.put("w" + week + "ptrcptgen",
							record.get("receiptscreated") != null ? record.get("receiptscreated") : BigDecimal.ZERO);
					onlineCollectionPerWeek.put("w" + week + "ptonlinecoll",
							record.get("onlinecollection") != null ? record.get("onlinecollection") : BigDecimal.ZERO);
					updatedCollectionPerWeek.put("w" + week + "ptupdatedcoll",
							record.get("updatedcollection") != null ? record.get("updatedcollection") : BigDecimal.ZERO);
					bbpsCollectionPerWeek.put("w" + week + "ptbbpscoll",
							record.get("bbpscollection") != null ? record.get("bbpscollection") : BigDecimal.ZERO);
				}
			}
			ulbCovered.add(ulbCoveredPerWeek);
			revenueCollected.add(revenueCollectedPerWeek);
			noOfProperties.add(noOfPropertiesPerWeek);
			receiptGenerated.add(receiptGeneratedPerWeek);
			onlineCollection.add(onlineCollectionPerWeek);
			updatedCollection.add(updatedCollectionPerWeek);
			bbpsCollection.add(bbpsCollectionPerWeek);
		}
		if (!data.isEmpty()) {
			PT pt = PT.builder().noOfProperties(noOfProperties).ulbCovered(ulbCovered)
					.revenueCollected(revenueCollected).receiptsGenerated(receiptGenerated).onlineCollection(onlineCollection).updatedCollection(updatedCollection).bbpsCollection(bbpsCollection).build();
			body.setPt(pt);
		}
	}

	private void enrichBodyWithTLData(Body body) {
		List<Map<String, Object>> data = externalAPIService.getRainmakerData(CronConstants.SEARCHER_TL);
		List<Map<String, Object>> ulbCovered = new ArrayList<>();
		List<Map<String, Object>> newLicense = new ArrayList<>();
		List<Map<String, Object>> newLicenseIssued = new ArrayList<>();
		List<Map<String, Object>> newRenewalLicense = new ArrayList<>();
		List<Map<String, Object>> newRenewalLicenseIssued = new ArrayList<>();
		List<Map<String, Object>> revenueCollected = new ArrayList<>();
		List<Map<String, Object>> receiptGenerated = new ArrayList<>();
		List<Map<String, Object>> onlineCollection = new ArrayList<>();
		for (Map<String, Object> record : data) {
			Map<String, Object> ulbCoveredPerWeek = new HashMap<>();
			Map<String, Object> newLicenseIssuedPerWeek = new HashMap<>();
			Map<String, Object> newLicensePerWeek = new HashMap<>();
			Map<String, Object> renewalLicensePerWeek = new HashMap<>();
			Map<String, Object> renewalLicenseIssuedPerWeek = new HashMap<>();
			Map<String, Object> revenueCollectedPerWeek = new HashMap<>();
			Map<String, Object> receiptGeneratedPerWeek = new HashMap<>();
			Map<String, Object> onlineCollectionPerWeek = new HashMap<>();
			String prefix = "Week";
			Integer noOfWeeks = 6;
			for (int week = 0; week < noOfWeeks; week++) {
				if (record.get("day").equals(prefix + week)) {
					ulbCoveredPerWeek.put("w" + week + "tlulbc",
							record.get("ulbcovered") != null ? String.format("%.0f", record.get("ulbcovered"))
									: BigDecimal.ZERO);
					newLicenseIssuedPerWeek.put("w" + week + "newtllicissued",
							record.get("newlicenseissued") != null ? record.get("newlicenseissued") : BigDecimal.ZERO);
					newLicensePerWeek.put("w" + week + "newtllic",
							record.get("newlicense") != null ? record.get("newlicense") : BigDecimal.ZERO);
					renewalLicensePerWeek.put("w" + week + "newtlrlic",
							record.get("renewallicense") != null ? record.get("renewallicense") : BigDecimal.ZERO);
					renewalLicenseIssuedPerWeek.put("w" + week + "newtlrlicissued",
							record.get("renewallicenseissued") != null ? record.get("renewallicenseissued")
									: BigDecimal.ZERO);
					revenueCollectedPerWeek.put("w" + week + "tlrevcoll",
							record.get("revenuecollected") != null ? record.get("revenuecollected") : BigDecimal.ZERO);
					receiptGeneratedPerWeek.put("w" + week + "tlrctcrt",
							record.get("receiptscreated") != null ? record.get("receiptscreated") : BigDecimal.ZERO);
					onlineCollectionPerWeek.put("w" + week + "tlonlinecoll",
							record.get("onlinecollection") != null ? record.get("onlinecollection") : BigDecimal.ZERO);
				}
			}
			ulbCovered.add(ulbCoveredPerWeek);
			newLicenseIssued.add(newLicenseIssuedPerWeek);
			newLicense.add(newLicensePerWeek);
			newRenewalLicense.add(renewalLicensePerWeek);
			newRenewalLicenseIssued.add(renewalLicenseIssuedPerWeek);
			revenueCollected.add(revenueCollectedPerWeek);
			receiptGenerated.add(receiptGeneratedPerWeek);
			onlineCollection.add(onlineCollectionPerWeek);
		}

		if (!data.isEmpty()) {
			TL tl = TL.builder().ulbCovered(ulbCovered).newLicenseIssued(newLicenseIssued).newLicense(newLicense)
					.renewalLicense(newRenewalLicense).renewalLicenseIssued(newRenewalLicenseIssued)
					.revenueCollected(revenueCollected).receiptCreated(receiptGenerated).onlineCollection(onlineCollection).build();
			body.setTl(tl);
		}
	}

	private void enrichBodyWithFirenocData(Body body) {
		List<Map<String, Object>> data = externalAPIService.getRainmakerData(CronConstants.SEARCHER_FIRENOC);
		List<Map<String, Object>> ulbCovered = new ArrayList<>();
		List<Map<String, Object>> certificatesIssued = new ArrayList<>();
		List<Map<String, Object>> revenueCollected = new ArrayList<>();
		for (Map<String, Object> record : data) {
			Map<String, Object> ulbCoveredPerWeek = new HashMap<>();
			Map<String, Object> certificatesIssuedPerWeek = new HashMap<>();
			Map<String, Object> revenueCollectedPerWeek = new HashMap<>();
			String prefix = "Week";
			Integer noOfWeeks = 6;
			for (int week = 0; week < noOfWeeks; week++) {
				if (record.get("day").equals(prefix + week)) {
					ulbCoveredPerWeek.put("w" + week + "fnulbc", record.get("ulbcovered"));
					certificatesIssuedPerWeek.put("w" + week + "fncertissued", record.get("certificatesissued"));
					revenueCollectedPerWeek.put("w" + week + "fnrevcoll", record.get("revenuecollected"));

				}
			}
			ulbCovered.add(ulbCoveredPerWeek);
			certificatesIssued.add(certificatesIssuedPerWeek);
			revenueCollected.add(revenueCollectedPerWeek);
		}

		Firenoc firenoc = Firenoc.builder().ulbCovered(ulbCovered).certificatesIssued(certificatesIssued)
				.revenueCollected(revenueCollected).build();
		body.setFirenoc(firenoc);
	}

	private void enrichBodyWithWSData(Body body, List<Map<String, Object>> data) {
		List<Map<String, Object>> ulbCovered = new ArrayList<>();
		List<Map<String, Object>> revenueCollected = new ArrayList<>();
		List<Map<String, Object>> servicesApplied = new ArrayList<>();
		Integer week = 0;
		for (Map<String, Object> record : data) {
			Map<String, Object> ulbCoveredPerWeek = new HashMap<>();
			Map<String, Object> revenueCollectedPerWeek = new HashMap<>();
			Map<String, Object> servicesAppliedPerWeek = new HashMap<>();
			ulbCoveredPerWeek.put("w" + week + "wsulbc", record.get("ulbsCovered"));
			revenueCollectedPerWeek.put("w" + week + "wsrevcoll", record.get("revenueCollected"));
			servicesAppliedPerWeek.put("w" + week + "wsserapp", record.get("servicesApplied"));
			ulbCovered.add(ulbCoveredPerWeek);
			revenueCollected.add(revenueCollectedPerWeek);
			servicesApplied.add(servicesAppliedPerWeek);

			week++;
		}

		WaterAndSewerage waterAndSewerage = WaterAndSewerage.builder().revenueCollected(revenueCollected)
				.serviceApplied(servicesApplied).ulbCovered(ulbCovered).build();
		body.setWaterAndSewerage(waterAndSewerage);

	}

	private void enrichBodyWithMiscCollData(Body body) {
		List<Map<String, Object>> data = externalAPIService.getRainmakerData(CronConstants.SEARCHER_MC);
		List<Map<String, Object>> ulbCovered = new ArrayList<>();
		List<Map<String, Object>> receiptsGenerated = new ArrayList<>();
		List<Map<String, Object>> revenueCollected = new ArrayList<>();
		for (Map<String, Object> record : data) {
			Map<String, Object> ulbCoveredPerWeek = new HashMap<>();
			Map<String, Object> receiptsGeneratedPerWeek = new HashMap<>();
			Map<String, Object> revenueCollectedPerWeek = new HashMap<>();
			String prefix = "Week";
			Integer noOfWeeks = 6;
			for (int week = 0; week < noOfWeeks; week++) {
				if (record.get("day").equals(prefix + week)) {
					ulbCoveredPerWeek.put("w" + week + "mculbc",
							record.get("ulbcovered") != null ? String.format("%.0f", record.get("ulbcovered"))
									: BigDecimal.ZERO);
					receiptsGeneratedPerWeek.put("w" + week + "mcrecgen",
							record.get("receiptscreated") != null ? record.get("receiptscreated") : BigDecimal.ZERO);
					revenueCollectedPerWeek.put("w" + week + "mcrevcoll",
							record.get("revenuecollected") != null ? record.get("revenuecollected") : BigDecimal.ZERO);
				}
			}
			ulbCovered.add(ulbCoveredPerWeek);
			receiptsGenerated.add(receiptsGeneratedPerWeek);
			revenueCollected.add(revenueCollectedPerWeek);
		}
		if (!data.isEmpty()) {
			MiscCollections miscCollections = MiscCollections.builder().ulbCovered(ulbCovered)
					.receiptsGenerated(receiptsGenerated).revenueCollected(revenueCollected).build();
			body.setMiscCollections(miscCollections);
		}
	}

	private void send(Email email, String content) {
		String[] addresses = toAddress.split(",");
		for (String address : Arrays.asList(addresses)) {
			Email request = Email.builder().emailTo(address).subject(subject).isHTML(true).body(content).build();
			EmailRequest emailRequest = EmailRequest.builder().requestInfo(new RequestInfo()).email(request).build();
			log.info("Sending email.......");
			producer.push(emailTopic, emailRequest);
		}
	}

	public Template getVelocityTemplate() {
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		ve.init();
		Template t = ve.getTemplate("velocity/weeklyimpactflasher.vm");
		return t;
	}

}
