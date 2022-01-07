package com.moesif.servlet;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.lang.*;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.moesif.api.APIHelper;
import com.moesif.api.models.CompanyBuilder;
import com.moesif.api.models.CompanyModel;
import com.moesif.api.models.UserBuilder;
import junit.framework.TestCase;
import org.mockito.Mockito;
import com.moesif.api.models.UserModel;
import com.moesif.api.models.CampaignModel;
import com.moesif.api.models.CampaignBuilder;
import static org.mockito.Mockito.when;


public class MoesifServletTests extends TestCase {
	HttpServletRequest request;
	HttpServletResponse response;
	FilterConfig filterConfig;
	FilterChain chain;
	MoesifFilter filter;
	MoesifConfiguration config;
	ServletOutputStream servletOutputStream;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		request = Mockito.mock(HttpServletRequest.class);
		response = Mockito.mock(HttpServletResponse.class);
		filterConfig = Mockito.mock(FilterConfig.class);
		chain = Mockito.mock(FilterChain.class);
		config = new MoesifConfiguration();
		config.disableTransactionId = true;
		servletOutputStream = Mockito.mock(ServletOutputStream.class);
		filter = new MoesifFilter("Your Moesif Application Id", config);
	}

	public void testSendEvent() throws Exception {

		// Mock Request
		StringBuffer url = new StringBuffer("https://acmeinc.com/items/42752/reviews");
		when(request.getRequestURL()).thenReturn(url);
		when(request.getMethod()).thenReturn("POST");
		when(request.getQueryString()).thenReturn("?key=value");
		when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");

		// Mock Request headers
		Map<String, String> headers = new HashMap<String, String>();

		// create an Enumeration over the header keys
		final Iterator<String> iterator = headers.keySet().iterator();
		Enumeration headerNames = new Enumeration<String>() {
			@Override
			public boolean hasMoreElements() {
				return iterator.hasNext();
			}

			@Override
			public String nextElement() {
				return iterator.next();
			}
		};
		when(request.getHeaderNames()).thenReturn(headerNames);

		// Mock Request Body
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream ();
        final ByteArrayInputStream inputStream = new ByteArrayInputStream (outputStream.toByteArray());

        // Servlet Input Stream
        ServletInputStream s = new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() throws IOException {
                return inputStream.read();
            }
        };

        when(request.getInputStream()).thenReturn(s);

		// Mock Response Headers
		when(response.getStatus()).thenReturn(201);
		when(response.getContentType()).thenReturn("application/json");
		when(response.getHeaders("Content-Type")).thenReturn(Arrays.asList("application/json"));
		when(response.getHeaders("Cache-Control")).thenReturn(Arrays.asList("no-cache"));
		List<String> rspHeaders = Arrays.asList("Content-Type", "Cache-Control");
		when(response.getHeaderNames()).thenReturn(rspHeaders);

		// Mock Response body
		String resp = "{ \"key\": \"value\"}";
		byte[] b = resp.getBytes(StandardCharsets.UTF_8);
		servletOutputStream.write(b);
		when(response.getOutputStream()).thenReturn(servletOutputStream);

		// Init the servlet
		filter.init(filterConfig);
		filter.doFilter(request, response, chain);
	}

	public void testUpdateUser() throws Throwable {

		CampaignModel campaign = new CampaignBuilder()
				.utmSource("Newsletter")
				.utmMedium("Email")
				.build();

		UserModel user = new UserBuilder()
				.userId("12345")
				.companyId("67890")
				.modifiedTime(new Date())
				.ipAddress("29.80.250.240")
				.sessionToken("di3hd982h3fubv3yfd94egf")
				.userAgentString("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
				.metadata(APIHelper.deserialize("{" +
						"\"email\": \"johndoe@acmeinc.com\"," +
						"\"string_field\": \"value_1\"," +
						"\"number_field\": 0," +
						"\"object_field\": {" +
						"\"field_1\": \"value_1\"," +
						"\"field_2\": \"value_2\"" +
						"}" +
						"}"))
				.campaign(campaign)
				.build();
		filter.updateUser(user);
	}

	public void testUpdateUsersBatch() throws Throwable {

		List<UserModel> users = new ArrayList<UserModel>();

		HashMap<String, Object> metadata = new HashMap<String, Object>();
		metadata = APIHelper.deserialize("{" +
				"\"email\": \"johndoe@acmeinc.com\"," +
				"\"string_field\": \"value_1\"," +
				"\"number_field\": 0," +
				"\"object_field\": {" +
				"\"field_1\": \"value_1\"," +
				"\"field_2\": \"value_2\"" +
				"}" +
				"}");

		UserModel userA = new UserBuilder()
				.userId("12345")
				.companyId("67890")
				.modifiedTime(new Date())
				.ipAddress("29.80.250.240")
				.sessionToken("di3hd982h3fubv3yfd94egf")
				.userAgentString("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
				.metadata(metadata)
				.build();

		UserModel userB = new UserBuilder()
				.userId("1234")
				.companyId("6789")
				.modifiedTime(new Date())
				.ipAddress("29.80.250.240")
				.sessionToken("di3hd982h3fubv3yfd94egf")
				.userAgentString("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
				.metadata(metadata)
				.build();

		users.add(userA);
		users.add(userB);

		filter.updateUsersBatch(users);
	}

	public void testUpdateCompany() throws Throwable {

		CampaignModel campaign = new CampaignBuilder()
				.utmSource("Adwords")
				.utmMedium("Twitter")
				.build();

		CompanyModel company = new CompanyBuilder()
				.companyId("12345")
				.companyDomain("acmeinc.com")
				.metadata(APIHelper.deserialize("{" +
						"\"email\": \"johndoe@acmeinc.com\"," +
						"\"string_field\": \"value_1\"," +
						"\"number_field\": 0," +
						"\"object_field\": {" +
						"\"field_1\": \"value_1\"," +
						"\"field_2\": \"value_2\"" +
						"}" +
						"}"))
				.campaign(campaign)
				.build();
		filter.updateCompany(company);
	}

	public void testUpdateCompaniesBatch() throws Throwable {

		List<CompanyModel> companies = new ArrayList<CompanyModel>();

		HashMap<String, Object> metadata = new HashMap<String, Object>();
		metadata = APIHelper.deserialize("{" +
				"\"email\": \"johndoe@acmeinc.com\"," +
				"\"string_field\": \"value_1\"," +
				"\"number_field\": 1," +
				"\"object_field\": {" +
				"\"field_1\": \"value_1\"," +
				"\"field_2\": \"value_2\"" +
				"}" +
				"}");

		CompanyModel companyA = new CompanyBuilder()
				.companyId("12345")
				.companyDomain("acmeinc.com")
				.metadata(metadata)
				.build();

		CompanyModel companyB = new CompanyBuilder()
				.companyId("67890")
				.companyDomain("nowhere.com")
				.metadata(metadata)
				.build();

		companies.add(companyA);
		companies.add(companyB);

		filter.updateCompaniesBatch(companies);
	}

	public void testGetAppConfig() throws Throwable{
//		filter.init(filterConfig);
		int sampleRate = AppConfigManager.getInstance().getSampleRate(null, null);
		assertTrue("Sample Rate should be less than equal to 100", 100 >= sampleRate);
	}
}
