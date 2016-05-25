package at.ac.tuwien.dsg.hcu.cloud.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MachineTaskClient {
    
    private final static String USER_AGENT = "Mozilla/5.0";

    public static void sendTask(String proxy, String server, int load) {
        
        try {
            String url = "http://" + proxy + "/rest/api/proxy/forward";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
     
            //add request header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
     
            String urlParameters = "url=http://" + server + "/rest/api/calculator/pi/" + load;
     
            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
     
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Post parameters : " + urlParameters);
            System.out.println("Response Code : " + responseCode);
     
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
     
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
     
            //print result
            System.out.println(response.toString());
        } catch (Exception e) {
            System.out.println(e);
        }
     }
    
}
