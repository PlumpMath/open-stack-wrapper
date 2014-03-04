package test;
import java.lang.reflect.Method;

import org.json.JSONObject;
import org.junit.Test;




import com.enterpriseweb.openstack.OpenStackAPI;

import static org.junit.Assert.*;



public class OpenStackTest {


	
	@Test
	public void testPrintHelloWorld() {
		JSONObject jsonV2Object = createJSONObject("tcp", 261);
        System.out.println("jsonObject: "+jsonV2Object);
        System.out.print(OpenStackAPI.json(jsonV2Object));
        
        assertTrue(true);

	}

	private static JSONObject createJSONObject(String protocol, int port) {
		JSONObject jsonObject= new JSONObject();
        jsonObject.put("host", "localhost");
        jsonObject.put("port", port);
        jsonObject.put("protocol", protocol);
      //  jsonObject.put("oids", new String[] {"1.3.6.1.2.1.1.6.0","1.3.6.1.2.1.1.5.0"});
		return jsonObject;
	}

}
