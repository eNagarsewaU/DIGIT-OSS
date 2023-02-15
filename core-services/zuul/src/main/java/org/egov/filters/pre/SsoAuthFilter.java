package org.egov.filters.pre;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import org.egov.Utils.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.netflix.zuul.http.HttpServletRequestWrapper;

public class SsoAuthFilter extends ZuulFilter {
	
	private HashSet<String> SSO_ENDPOINT;
	private final ObjectMapper objectMapper;	
	private UserUtils userUtils;
	private static final String RECEIVED_RESPONSE_MESSAGE = "SsoAuthFilter: Received response code: {} from upstream URI {}";
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
public SsoAuthFilter(HashSet<String> SSO_ENDPOINT,UserUtils userUtils) {
this.userUtils = userUtils;
this.SSO_ENDPOINT = SSO_ENDPOINT;
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
	//TODO
	   // Read the sso auth token
    // Validate the sso auth token by calling third party API
	
	logger.info(RECEIVED_RESPONSE_MESSAGE,
            ctx.getResponse().getStatus(), ctx.getRequest().getRequestURI());

	return null;

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