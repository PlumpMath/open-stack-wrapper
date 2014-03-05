package test;



import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.json.JSONObject;
import org.testng.annotations.Test;

import com.enterpriseweb.openstack.OpenStackAPI;


public class OpenStackTest {
	
	
	
	@Test
	public void globalTest() throws IOException {

		Properties login = loadLoginProperties();
		JSONObject requestTokens = createJSONObject(
				login.getProperty("username"), 
				login.getProperty("password"),
				login.getProperty("url"));
		JSONObject tokens =OpenStackAPI.tokens(requestTokens);
		//System.out.println(tokens);
		// {"access":{"token":{"id":"MIIDCwYJKoZIhvcNAQcCoIIC-DCCAvgCAQExCTAHBgUrDgMCGjCCAWEGCSqGSIb3DQEHAaCCAVIEggFOeyJhY2Nlc3MiOiB7InRva2VuIjogeyJpc3N1ZWRfYXQiOiAiMjAxNC0wMy0wNFQwOTo1ODo1Mi41MDY3OTkiLCAiZXhwaXJlcyI6ICIyMDE0LTAzLTA1VDA5OjU4OjUyWiIsICJpZCI6ICJwbGFjZWhvbGRlciJ9LCAic2VydmljZUNhdGFsb2ciOiBbXSwgInVzZXIiOiB7InVzZXJuYW1lIjogImZhY2Vib29rMTQyODQ2Nzg1MCIsICJyb2xlc19saW5rcyI6IFtdLCAiaWQiOiAiMmVhZWRhYzBkY2MwNDM1Y2JmZWM2OWRjYmQzMzkxYjQiLCAicm9sZXMiOiBbXSwgIm5hbWUiOiAiZmFjZWJvb2sxNDI4NDY3ODUwIn0sICJtZXRhZGF0YSI6IHsiaXNfYWRtaW4iOiAwLCAicm9sZXMiOiBbXX19fTGCAYEwggF9AgEBMFwwVzELMAkGA1UEBhMCVVMxDjAMBgNVBAgMBVVuc2V0MQ4wDAYDVQQHDAVVbnNldDEOMAwGA1UECgwFVW5zZXQxGDAWBgNVBAMMD3d3dy5leGFtcGxlLmNvbQIBATAHBgUrDgMCGjANBgkqhkiG9w0BAQEFAASCAQC7jPVaPo0qvPrRjps2kQ+zYPcAwBOgGj90oAog2IMltVBsPCnIpKivcFx+1dNH3rmI1CdoKe2fEt8WUc3muiTTePsJbn4lmMdLz3jb86aWaE06VDxwoBFG0dYZ33H8gDkFM3kbb89k-PxqB7CGHKzQQ5D6uu5wkLFzn2DwUSwWBTJW80JRKSdMbblNnWSOjp4r9c1dLFXOtnQLpD9rlC4yVNVmdAocFRQbCPNLWYbMp+V3MkJtp90KVNWXyFcE9zVS6HqF9XLZTjwbDmP6Z8slBrORus70lGooUJL0DPbEl-+CJ2kFZq2U+7bdhLgLK92fJX5YimC4jQmB7XpySBUI","issued_at":"2014-03-04T09:58:52.506799","expires":"2014-03-05T09:58:52Z"},"serviceCatalog":[],"user":{"id":"2eaedac0dcc0435cbfec69dcbd3391b4","username":"facebook1428467850","name":"facebook1428467850","roles":[],"roles_links":[]},"metadata":{"is_admin":0,"roles":[]}},"success":true}
		
		String tokenId=tokens.getJSONObject("access").getJSONObject("token").getString("id");
		System.out.println("tokenid");
		System.out.println(tokenId);

		JSONObject tenants=OpenStackAPI.tenants(createJSONTenantsObject( tokenId, login.getProperty("url")));
		System.out.println("tenants");	
		System.out.println(tenants);

		JSONObject endpoints=OpenStackAPI.endpoints(createJSONEndPointsObject(login.getProperty("username"), 
				login.getProperty("password"),
				login.getProperty("url") ,tokenId, "admin"));
		System.out.println("endpoints");
		System.out.println(endpoints);
		JSONObject operation=OpenStackAPI.operation(createJSONOperationObject(login.getProperty("username"), 
				login.getProperty("password"),
				login.getProperty("url") ,login.getProperty("username"),  "compute","/images" ));
		System.out.println("operation");
		System.out.println(operation);
		String epsToken=endpoints.getString("token-id");
		String urlEps=endpoints.getJSONObject("eps").getJSONObject("compute").getString("publicURL");
		String path="/images";
		
		System.out.println(epsToken);
		System.out.println(urlEps);
		System.out.println(path);
		System.out.println(createJSONServiceCallObject(epsToken, urlEps, path));
		JSONObject serviceCall=OpenStackAPI.servicecall(createJSONServiceCallObject(epsToken, urlEps, path));
		System.out.println("service call");
		System.out.println(serviceCall);
		
		
		//assertTrue(true);
	}
	
	private JSONObject createJSONServiceCallObject(String epsToken, String urlEps, String path) {
		JSONObject jsonObject=new JSONObject();
		jsonObject.put("eps-token-id", epsToken);
		jsonObject.put("url", urlEps);
		jsonObject.put("path", path);
		return jsonObject;
	}

	
	private JSONObject createJSONOperationObject(String username, String password, String url, 
			String tenantName, String serviceType, String path) {
		JSONObject jsonObject=createJSONObject(username, password, url);
		jsonObject.put("tenant-name", tenantName);
		jsonObject.put("service-type", serviceType);
		jsonObject.put("path", path);
		return jsonObject;
	}

	
	private JSONObject createJSONEndPointsObject(String username, String password, String url, String tokenId,
	 String tenantName) {
		JSONObject jsonObject=createJSONObject(username, password, url);
		jsonObject.put("tenant-name", tenantName);		
		return jsonObject;
	}
	private JSONObject createJSONTenantsObject(String tokenId,
			String url) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("token-id", tokenId);
		
		jsonObject.put("url", url);
		return jsonObject;
	}
	
	private JSONObject createJSONObject(String username, String password,
			String url) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("username", username);
		jsonObject.put("password", password);
		jsonObject.put("url", url);
		return jsonObject;
	}
	
	private Properties loadLoginProperties() throws IOException {

		InputStream in = getClass().getResourceAsStream("login.properties");
		Properties prop = new Properties();
		prop.load(in);
		in.close();
		return prop;

	}


}
