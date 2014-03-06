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
		String urlCompute=endpoints.getJSONObject("eps").getJSONObject("compute").getString("publicURL");
		String urlQuantum=endpoints.getJSONObject("eps").getJSONObject("network").getString("publicURL");

		System.out.println(epsToken);
		System.out.println(urlCompute);
		System.out.println(urlQuantum);

		JSONObject serviceCallImages=OpenStackAPI.serviceCall(createJSONServiceCallObject(epsToken, urlCompute, "/images"));
		System.out.println("service call:images");
		System.out.println(serviceCallImages);
                JSONObject imageSelected=(JSONObject)serviceCallImages.getJSONArray("images").get(0);
                String imageLink=((JSONObject)imageSelected.getJSONArray("links").get(0)).getString("href");
		System.out.println( imageLink);

		JSONObject serviceCallFlavors=OpenStackAPI.serviceCall(createJSONServiceCallObject(epsToken, urlCompute, "/flavors"));
		System.out.println("service call:flavors");
		System.out.println(serviceCallFlavors);
                JSONObject flavorSelected=(JSONObject)serviceCallFlavors.getJSONArray("flavors").get(0);

                String flavorLink=((JSONObject)flavorSelected.getJSONArray("links").get(0)).getString("href");
		System.out.println( flavorLink);

                //JSONObject createNetworkResponse=OpenStackAPI.createNetwork(createJSONCreateNetworkObject(epsToken, urlQuantum, "my-network-name"));
		//System.out.println("create network!!!");
		//System.out.println(createNetworkResponse);


		JSONObject serviceCallNetworks=OpenStackAPI.serviceCall(createJSONServiceCallObject(epsToken, urlQuantum, "v2.0/networks"));
		System.out.println("service call:networks");
		System.out.println(serviceCallNetworks);
                String networkId=((JSONObject)serviceCallNetworks.getJSONArray("networks").get(0)).getString("id");

		System.out.println("first network id");
		System.out.println(networkId);

                JSONObject createSubnetResponse=OpenStackAPI.createSubnet(createJSONCreateSubnetObject(epsToken, urlQuantum,networkId, "192.168.198.0/24","192.168.198.40", "192.168.198.50"  ));
		System.out.println(createSubnetResponse);




		//assertTrue(true);
	}

    private JSONObject createJSONCreateSubnetObject(String epsToken, String urlQuantum, String networkId, String cidr, String start, String end) {
		JSONObject jsonObject=new JSONObject();
		jsonObject.put("token-id", epsToken);
		jsonObject.put("quantum-url", urlQuantum);
		jsonObject.put("network-id", networkId);
		jsonObject.put("cidr", cidr);
		jsonObject.put("start", start);
		jsonObject.put("end", end);
		return jsonObject;
    }



    private JSONObject createJSONCreateNetworkObject(String epsToken, String urlQuantum, String networkName) {
		JSONObject jsonObject=new JSONObject();
		jsonObject.put("token-id", epsToken);
		jsonObject.put("quantum-url", urlQuantum);
		jsonObject.put("network-name", networkName);
		return jsonObject;
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
