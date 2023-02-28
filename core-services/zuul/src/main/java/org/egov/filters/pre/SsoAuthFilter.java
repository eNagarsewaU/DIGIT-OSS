package org.egov.filters.pre;

import static org.egov.constants.RequestContextConstants.USER_INFO_KEY;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.egov.Utils.ExceptionUtils;
import org.egov.Utils.UserUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

public class SsoAuthFilter extends ZuulFilter {
	private HashSet<String> SSO_ENDPOINT;
	private String ukswcs_auth_url_api;
	private String ukswcs_return_url;
	private final ObjectMapper objectMapper;
	private UserUtils userUtils;
	private static final String RECEIVED_RESPONSE_MESSAGE = "SsoAuthFilter: Received response code: {} from upstream URI {}";
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String RETRIEVING_USER_FAILED_MESSAGE = "Retrieving user failed";
	private static final String PASS_THROUGH_GATEWAY_HEADER_NAME = "x-pass-through-gateway";

	public SsoAuthFilter(HashSet<String> SSO_ENDPOINT, String ukswcs_auth_url_api, String ukswcs_return_url,
			UserUtils userUtils) {
		this.userUtils = userUtils;
		this.SSO_ENDPOINT = SSO_ENDPOINT;
		this.ukswcs_auth_url_api = ukswcs_auth_url_api;
		this.ukswcs_return_url = ukswcs_return_url;
		objectMapper = new ObjectMapper();
	}

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return 1;
	}

	@Override
	public boolean shouldFilter() {
		return getRequestURI().contains("/tl-services/v1/external/_create");
	}

	@Override
	public Object run() {

		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();
		logger.info(RECEIVED_RESPONSE_MESSAGE, ctx.getResponse().getStatus(), ctx.getRequest().getRequestURI());
		final HttpHeaders headers = new HttpHeaders();
		BufferedReader reader;
		String token = "";
		JSONObject jsonObject;
		String mobile = "";
		String iuid = "";
		String user_id = "";
		String fname = "";
		String lname = "";
		String name = fname + " " + lname;
		String userObject = "";
		Map<String, String> userSearchResponseAfterUserCreation = new HashMap<String, String>();
		Map<String, String> userSearchResponse = new HashMap<String, String>();

		try {
			reader = request.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
				String[] splited = line.split(",");

				for (String part : splited) {
					if (part.contains("sso_token")) {
						String[] splitData = part.split(":");
						token = splitData[1].toString();
						token = token.replaceAll("\"", "");
						System.out.println(token);
						break;
					}
				}
			}

		} catch (IOException e2) {
			e2.printStackTrace();
		}

		if (!token.equals("")) {
			logger.info("Recieved Token: ", token.toString());
			// token available in request
			String url = ukswcs_auth_url_api + token;
			RestTemplate restTemplate = new RestTemplate();
			int requestStatus;
			try {
				// consume third party auth api
				String result = restTemplate.getForObject(url, String.class);

				jsonObject = new JSONObject(result);
				requestStatus = jsonObject.getInt("STATUS");

			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}

			if (requestStatus == 200) {
				logger.info("Third Party Api Pass, Response Status for their side: ", 200);
				// if api call is successful
				try {
					String getResponse = jsonObject.getJSONObject("RESPONSE").toString();
					JSONObject jsonObjects = new JSONObject(getResponse);
					logger.info("Third Party Api Data Recieved: ", jsonObjects);
					mobile = jsonObjects.get("mobile_number").toString();
					iuid = jsonObjects.get("iuid").toString();
					user_id = jsonObjects.get("user_id").toString();
					fname = jsonObjects.get("first_name").toString();
					lname = jsonObjects.get("last_name").toString();
					name = fname + " " + lname;

				} catch (JSONException e) {
					e.printStackTrace();
					return null;
				}

				// take mobile and user from auth api and get username and mobile.
				if (!user_id.equals("") && !mobile.equals("")) {
					
					// search user form db
					try {
						userSearchResponse = searchUser(user_id, iuid, mobile);
						logger.info("User Search Api Data Recieved: ", userSearchResponse.toString());
					} catch (HttpClientErrorException ex) {
						logger.error(RETRIEVING_USER_FAILED_MESSAGE, ex);
						ExceptionUtils.RaiseException(ex);
					}
					if (userSearchResponse.isEmpty()) {
						// create a user
						Boolean userCreated = createUser(user_id, iuid, mobile, name);
						logger.info("Create Api Data status: ", userCreated);
						if (userCreated) {
							try {
								userSearchResponseAfterUserCreation = searchUser(user_id, iuid, mobile);
								logger.info("Search Api Data: ", userSearchResponseAfterUserCreation.toString());
							} catch (HttpClientErrorException ex) {
								logger.error(RETRIEVING_USER_FAILED_MESSAGE, ex);
								ExceptionUtils.RaiseException(ex);
							}

							// here we can get the userObject and send it to header
							if (!userSearchResponseAfterUserCreation.get("user").isEmpty()) {
								userObject = userSearchResponseAfterUserCreation.get("user");
								// put it into header
								logger.info("User Data Exist in DB, Block: ", userObject.toString());
								ctx.setResponseStatusCode(200);
								ctx.set("status", "pass");
								ctx.set(USER_INFO_KEY, userObject);
								return null;
							}
							return null;
						}
					} else {
						// user already exist: get userObject and put into the header
						logger.info("User Data Already Exist in DB, Block: ", userObject.toString());
						userObject = userSearchResponse.get("user");
						ctx.setResponseStatusCode(200);
						ctx.set("status", "pass");
						ctx.set(USER_INFO_KEY, userObject);
						return null;

					}
				} else {
					ctx.setResponseStatusCode(401);
					return null;
				}

			} else {
				logger.info("Data Not found in Request");
				// token not available in request
				ctx.setResponseStatusCode(401);
				return null;
			}
		}
		logger.info("Request Data is not valid");
		ctx.setResponseStatusCode(401);
		return null;
	}

	private Map<String, String> searchUser(String user_id, String iuid, String mobile) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		JSONObject searchRequest = new JSONObject();
		JSONObject searchRequestInfo = new JSONObject();
		RestTemplate restTemplate = new RestTemplate();
		String fetchUserDetailsUrl = "https://nagarsewa-uat.uk.gov.in/user/_search";
		Map<String, String> userExistanceInDb = new HashMap<String, String>();

		try {
			searchRequestInfo.put("api_id", "Rainmaker");
			searchRequestInfo.put("ver", ".01");
			searchRequestInfo.put("ts", null);
			searchRequestInfo.put("res_msg_id", null);
			searchRequestInfo.put("msg_Id", "20170310130900|en_IN");
			searchRequestInfo.put("status", null);
			searchRequestInfo.put("authToken", "");

			searchRequest.put("RequestInfo", searchRequestInfo);
			// username is combination of deptartment.(swcs)+ userid+iuid.
			searchRequest.put("userName", "swcs-" + user_id + "-" + iuid);
			searchRequest.put("mobileNumber", mobile);
			searchRequest.put("type", "CITIZEN");
			searchRequest.put("tenantId", "uk");
			
			System.out.println("User Search Details Api, Json Request in SSO Filter:  " + searchRequest);
			
			HttpEntity<String> searchRequesthead = new HttpEntity<String>(searchRequest.toString(), headers);
			ResponseEntity<String> response = restTemplate.postForEntity(fetchUserDetailsUrl, searchRequesthead,
					String.class);
			System.out.println("User Search Details Api, Response in SSO Filter:  " + response);
			if (response.getBody().toString().contains(mobile)) {
				String[] object = response.toString().split("},");
				String userObjectString = object[1].replace("\"user\":", "");

				userExistanceInDb.put("status", "true");
				userExistanceInDb.put("data", userObjectString);
				return userExistanceInDb;
			} else {
				return userExistanceInDb;
			}

		} catch (JSONException e) {
			e.printStackTrace();
			return userExistanceInDb;
		}
	}

	private Boolean createUser(String user_id, String iuid, String mobile, String name) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		RestTemplate restTemplate = new RestTemplate();
		JSONObject createRequest = new JSONObject();
		JSONObject createRequestInfo = new JSONObject();
		JSONObject userInfo = new JSONObject();
		JSONObject roleInfo = new JSONObject();
		JSONArray roleArrayObject = new JSONArray();

		String createUserUrl = "https://nagarsewa-uat.uk.gov.in/user/users/_createnovalidate";
		Boolean created = false;

		try {
			createRequestInfo.put("apiId", "Rainmaker");
			createRequestInfo.put("ver", ".01");
			createRequestInfo.put("ts", null);
			createRequestInfo.put("action", "_update");
			createRequestInfo.put("did", "1");
			createRequestInfo.put("key", "");
			createRequestInfo.put("msgId", "20170310130900|en_IN");
			createRequestInfo.put("authToken", "");

			userInfo.put("userName", "swcs-" + user_id + "-" + iuid);
			userInfo.put("password", "Swcs@12345");
			userInfo.put("name", name);
			userInfo.put("mobileNumber", mobile);
			userInfo.put("type", "CITIZEN");
			userInfo.put("active", true);

			roleInfo.put("name", "Citizen");
			roleInfo.put("code", "CITIZEN");
			roleInfo.put("tenantId", "uk");
			roleArrayObject.put(roleInfo);

			userInfo.put("roles", roleArrayObject);
			userInfo.put("tenantId", "uk");

			createRequest.put("RequestInfo", createRequestInfo);
			createRequest.put("user", userInfo);
			System.out.println("User Create Api Request in SSO Filter:  " + createRequest);

			HttpEntity<String> requests = new HttpEntity<String>(createRequest.toString(), headers);

			ResponseEntity<String> createUserResponse = restTemplate.postForEntity(createUserUrl, requests,
					String.class);

			if (createUserResponse.getBody().toString().contains(mobile)) {
				created = true;
				return created;
			} else {
				created = false;
				return created;
			}

		} catch (JSONException e) {
			e.printStackTrace();
			return created;
		}
	}

	private String getRequestURI() {
		return getRequest().getRequestURI();
	}

	private HttpServletRequest getRequest() {
		RequestContext ctx = RequestContext.getCurrentContext();
		return ctx.getRequest();
	}

	private String getRequestMethod() {
		return getRequest().getMethod();
	}
}