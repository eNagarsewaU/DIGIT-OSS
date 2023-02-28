package org.egov.tl.web.controllers;


import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//import org.egov.user.persistence.repository;
import javax.validation.Valid;

import org.egov.tl.service.TradeLicenseService;
import org.egov.tl.service.UserService;
import org.egov.tl.util.ResponseInfoFactory;
import org.egov.tl.web.models.RequestInfoWrapper;
import org.egov.tl.web.models.TradeLicense;
import org.egov.tl.web.models.TradeLicenseRequest;
import org.egov.tl.web.models.TradeLicenseResponse;
import org.egov.tl.web.models.TradeLicenseSearchCriteria;
import org.egov.tl.web.models.User;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;


@RestController


    @RequestMapping("/v1")
    public class TradeLicenseController {
	
	@Autowired
    
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final ObjectMapper objectMapper;

        private final HttpServletRequest request;

        private final TradeLicenseService tradeLicenseService;

        private final ResponseInfoFactory responseInfoFactory;
        
        private final User user;
        
        
       
        
        

    @Autowired
    public TradeLicenseController(ObjectMapper objectMapper, HttpServletRequest request,
                                  TradeLicenseService tradeLicenseService ,ResponseInfoFactory responseInfoFactory) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.tradeLicenseService = tradeLicenseService;
        this.responseInfoFactory = responseInfoFactory;
        this.user = null;
    }


    @PostMapping({"/{servicename}/_create", "/_create"})
    public ResponseEntity<TradeLicenseResponse> create(@Valid @RequestBody TradeLicenseRequest tradeLicenseRequest,
                                                       @PathVariable(required = false) String servicename) {
        List<TradeLicense> licenses = tradeLicenseService.create(tradeLicenseRequest, servicename);
        TradeLicenseResponse response = TradeLicenseResponse.builder().licenses(licenses).responseInfo(
                responseInfoFactory.createResponseInfoFromRequestInfo(tradeLicenseRequest.getRequestInfo(), true))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{servicename}/_search", "/_search"}, method = RequestMethod.POST)
    public ResponseEntity<TradeLicenseResponse> search(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                                       @Valid @ModelAttribute TradeLicenseSearchCriteria criteria,
                                                       @PathVariable(required = false) String servicename
            , @RequestHeader HttpHeaders headers) {
        List<TradeLicense> licenses = tradeLicenseService.search(criteria, requestInfoWrapper.getRequestInfo(), servicename, headers);

        TradeLicenseResponse response = TradeLicenseResponse.builder().licenses(licenses).responseInfo(
                responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{servicename}/_update", "/_update"}, method = RequestMethod.POST)
    public ResponseEntity<TradeLicenseResponse> update(@Valid @RequestBody TradeLicenseRequest tradeLicenseRequest,
                                                       @PathVariable(required = false) String servicename) {
        List<TradeLicense> licenses = tradeLicenseService.update(tradeLicenseRequest, servicename);

        TradeLicenseResponse response = TradeLicenseResponse.builder().licenses(licenses).responseInfo(
                responseInfoFactory.createResponseInfoFromRequestInfo(tradeLicenseRequest.getRequestInfo(), true))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{servicename}/{jobname}/_batch", "/_batch"}, method = RequestMethod.POST)
    public ResponseEntity sendReminderSMS(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                          @PathVariable(required = false) String servicename,
                                          @PathVariable(required = true) String jobname) {

        tradeLicenseService.runJob(servicename, jobname, requestInfoWrapper.getRequestInfo());

        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/_plainsearch", method = RequestMethod.POST)
    public ResponseEntity<TradeLicenseResponse> plainsearch(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                                            @Valid @ModelAttribute TradeLicenseSearchCriteria criteria) {

        List<TradeLicense> licenses = tradeLicenseService.plainSearch(criteria, requestInfoWrapper.getRequestInfo());

        TradeLicenseResponse response = TradeLicenseResponse.builder().licenses(licenses).responseInfo(
                responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/external/_create", method = RequestMethod.POST)
    public ModelAndView signleSignOnForTradeLicense(@RequestHeader HttpHeaders header, @RequestBody Object object){
    	logger.info("Inside TradeLicenceController");
    	logger.info("Data Inside Request Header-> TradeLicenceController, Body Data:"+ header);
    	System.out.println(header);
    	logger.info("Data Inside Request Body-> TradeLicenceController, Body Data:"+ object);
    	System.out.println(object);
    	//userService.userExists(OwnerInfo owner,RequestInfo requestInfo);
		return new ModelAndView("https://nagarsewa-uat.uk.gov.in/citizen/tradelicense-citizen/home");
    	
    	
    }
    
    

}
