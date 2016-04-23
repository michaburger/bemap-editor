

package jcan2;

/**
 *
 * @author Micha Burger
 * 
 * This class communicates with our webserver via HTTP requests
 * 
 */
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.json.JSONObject;

/**
 * Implements all the functions for the communication with the beMap webserver.
 * @author Micha Burger
 */
public class ServerCommunication {

	private final String USER_AGENT = "Mozilla/5.0";
        private final String DOMAIN = "http://bemap.lapijover.eu/";
        private final boolean SERVER_DEBUG = false;


        /**
         * HTTP GET request, the 2 corner points of the map have to be transmitted
         * as a JSON String, and returns a JSON object containing all the points or a 
         * String containing nothing.
         * @param data JSONObject with 2 corner points (top left + bottom right)
         * @return JSON objects with all the points in the current map section
         * @throws Exception 
         */
	public String getFromBeMapServer(String data) throws Exception {

		String url = DOMAIN + "JSON/get";
                URL sender = new URL(url);
                
		HttpURLConnection con = (HttpURLConnection) sender.openConnection();
                if(SERVER_DEBUG) BeMapEditor.mainWindow.append("sendPost called\n");
                
		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                //Main.mainWindow.append("Data sent: " + data + "\n");
		String params = "JSON=" + data;

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(params);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		if(SERVER_DEBUG) BeMapEditor.mainWindow.append("\nSending 'POST' request to URL : " + url + "\n");
		if(SERVER_DEBUG) BeMapEditor.mainWindow.append("Post parameters : " + params + "\n");
		if(SERVER_DEBUG) BeMapEditor.mainWindow.append("Response Code : " + responseCode +"\n");

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		if(SERVER_DEBUG) BeMapEditor.mainWindow.append(response.toString()+"\n");
                
                
                if(responseCode != 200) return "";
                else return response.toString();
                        
                //test values
                //return "{\"Track\":[{\"id\":0,\"s2\":0,\"time\":5221200,\"lon\":6.660289764404297,\"s1\":0,\"date\":260315,\"s3\":0,\"lat\":46.506427047278464,\"s4\":0},{\"id\":1,\"s2\":0,\"time\":5221200,\"lon\":6.656684875488281,\"s1\":0,\"date\":260315,\"s3\":0,\"lat\":46.50654519707596,\"s4\":0},{\"id\":2,\"s2\":57,\"time\":5221500,\"lon\":6.650848388671875,\"s1\":57,\"date\":260315,\"s3\":57,\"lat\":46.506427047278464,\"s4\":57},{\"id\":3,\"s2\":57,\"time\":5221500,\"lon\":6.646728515625,\"s1\":57,\"date\":260315,\"s3\":57,\"lat\":46.50559999150564,\"s4\":57},{\"id\":4,\"s2\":57,\"time\":5221600,\"lon\":6.641407012939453,\"s1\":57,\"date\":260315,\"s3\":57,\"lat\":46.50630889722416,\"s4\":57},{\"id\":5,\"s2\":57,\"time\":5221600,\"lon\":6.6364288330078125,\"s1\":57,\"date\":260315,\"s3\":57,\"lat\":46.50678149590053,\"s4\":57},{\"id\":6,\"s2\":117,\"time\":5221900,\"lon\":6.632823944091797,\"s1\":117,\"date\":260315,\"s3\":117,\"lat\":46.507490386211146,\"s4\":117},{\"id\":7,\"s2\":117,\"time\":5222000,\"lon\":6.629047393798828,\"s1\":117,\"date\":260315,\"s3\":117,\"lat\":46.50666334661664,\"s4\":117},{\"id\":8,\"s2\":117,\"time\":5222000,\"lon\":6.625785827636719,\"s1\":117,\"date\":260315,\"s3\":117,\"lat\":46.507254090468145,\"s4\":117},{\"id\":9,\"s2\":117,\"time\":5222100,\"lon\":6.6226959228515625,\"s1\":117,\"date\":260315,\"s3\":117,\"lat\":46.508435558911344,\"s4\":117},{\"id\":10,\"s2\":117,\"time\":5222200,\"lon\":6.618404388427734,\"s1\":117,\"date\":260315,\"s3\":117,\"lat\":46.51115283887624,\"s4\":117},{\"id\":11,\"s2\":117,\"time\":5222200,\"lon\":6.615486145019531,\"s1\":117,\"date\":260315,\"s3\":117,\"lat\":46.51209794789087,\"s4\":117},{\"id\":12,\"s2\":167,\"time\":5222500,\"lon\":6.612567901611328,\"s1\":167,\"date\":260315,\"s3\":167,\"lat\":46.51469691293868,\"s4\":167},{\"id\":13,\"s2\":167,\"time\":5222600,\"lon\":6.607246398925781,\"s1\":167,\"date\":260315,\"s3\":167,\"lat\":46.51587821960058,\"s4\":167},{\"id\":14,\"s2\":167,\"time\":5222600,\"lon\":6.602611541748047,\"s1\":167,\"date\":260315,\"s3\":167,\"lat\":46.516586991271616,\"s4\":167},{\"id\":15,\"s2\":167,\"time\":5222600,\"lon\":6.597461700439453,\"s1\":167,\"date\":260315,\"s3\":167,\"lat\":46.518240755886346,\"s4\":167},{\"id\":16,\"s2\":167,\"time\":5255500,\"lon\":7.4542236328125,\"s1\":167,\"date\":260315,\"s3\":167,\"lat\":46.96151050487311,\"s4\":167},{\"id\":17,\"s2\":167,\"time\":5255700,\"lon\":7.45697021484375,\"s1\":167,\"date\":260315,\"s3\":167,\"lat\":46.96244775337095,\"s4\":167},{\"id\":18,\"s2\":147,\"time\":5255900,\"lon\":7.4631500244140625,\"s1\":147,\"date\":260315,\"s3\":147,\"lat\":46.95237147154127,\"s4\":147},{\"id\":19,\"s2\":147,\"time\":5260000,\"lon\":7.45697021484375,\"s1\":147,\"date\":260315,\"s3\":147,\"lat\":46.952254294313356,\"s4\":147},{\"id\":20,\"s2\":147,\"time\":5260100,\"lon\":7.448387145996094,\"s1\":147,\"date\":260315,\"s3\":147,\"lat\":46.95184417199528,\"s4\":147},{\"id\":21,\"s2\":147,\"time\":5260100,\"lon\":7.446584701538086,\"s1\":147,\"date\":260315,\"s3\":147,\"lat\":46.95055519567804,\"s4\":147},{\"id\":22,\"s2\":147,\"time\":5260100,\"lon\":7.450532913208008,\"s1\":147,\"date\":260315,\"s3\":147,\"lat\":46.948504487557834,\"s4\":147},{\"id\":23,\"s2\":147,\"time\":5260200,\"lon\":7.453365325927734,\"s1\":147,\"date\":260315,\"s3\":147,\"lat\":46.94868026561828,\"s4\":147},{\"id\":24,\"s2\":147,\"time\":5260200,\"lon\":7.451648712158203,\"s1\":147,\"date\":260315,\"s3\":147,\"lat\":46.94744980707282,\"s4\":147},{\"id\":25,\"s2\":87,\"time\":5260400,\"lon\":7.443323135375977,\"s1\":87,\"date\":260315,\"s3\":87,\"lat\":46.95172699361285,\"s4\":87},{\"id\":26,\"s2\":87,\"time\":5260500,\"lon\":7.438688278198242,\"s1\":87,\"date\":260315,\"s3\":87,\"lat\":46.95119968771708,\"s4\":87},{\"id\":27,\"s2\":87,\"time\":5260500,\"lon\":7.436971664428711,\"s1\":87,\"date\":260315,\"s3\":87,\"lat\":46.94680526987945,\"s4\":87},{\"id\":28,\"s2\":87,\"time\":5260600,\"lon\":7.442121505737305,\"s1\":87,\"date\":260315,\"s3\":87,\"lat\":46.94498880512475,\"s4\":87},{\"id\":29,\"s2\":87,\"time\":5260600,\"lon\":7.44624137878418,\"s1\":87,\"date\":260315,\"s3\":87,\"lat\":46.94721543080955,\"s4\":87},{\"id\":30,\"s2\":27,\"time\":5260800,\"lon\":7.4494171142578125,\"s1\":27,\"date\":260315,\"s3\":27,\"lat\":46.94709824229308,\"s4\":27},{\"id\":31,\"s2\":27,\"time\":5260900,\"lon\":7.447957992553711,\"s1\":27,\"date\":260315,\"s3\":27,\"lat\":46.94703964793863,\"s4\":27},{\"id\":32,\"s2\":27,\"time\":5260900,\"lon\":7.446498870849609,\"s1\":27,\"date\":260315,\"s3\":27,\"lat\":46.946981053520055,\"s4\":27},{\"id\":33,\"s2\":27,\"time\":5261000,\"lon\":7.445383071899414,\"s1\":27,\"date\":260315,\"s3\":27,\"lat\":46.94680526987945,\"s4\":27},{\"id\":34,\"s2\":27,\"time\":5261000,\"lon\":7.442808151245117,\"s1\":27,\"date\":260315,\"s3\":27,\"lat\":46.94680526987945,\"s4\":27},{\"id\":35,\"s2\":27,\"time\":5261100,\"lon\":7.441263198852539,\"s1\":27,\"date\":260315,\"s3\":27,\"lat\":46.94721543080955,\"s4\":27},{\"id\":36,\"s2\":27,\"time\":5261600,\"lon\":8.5308837890625,\"s1\":27,\"date\":260315,\"s3\":27,\"lat\":47.36859434521338,\"s4\":27},{\"id\":37,\"s2\":27,\"time\":5261700,\"lon\":8.554229736328125,\"s1\":27,\"date\":260315,\"s3\":27,\"lat\":47.36952443865407,\"s4\":27},{\"id\":38,\"s2\":27,\"time\":5261800,\"lon\":8.53912353515625,\"s1\":27,\"date\":260315,\"s3\":27,\"lat\":47.3778945415552,\"s4\":27},{\"id\":39,\"s2\":27,\"time\":5261900,\"lon\":8.523330688476562,\"s1\":27,\"date\":260315,\"s3\":27,\"lat\":47.37766205663577,\"s4\":27},{\"id\":40,\"s2\":170,\"time\":5262200,\"lon\":8.532514572143555,\"s1\":170,\"date\":260315,\"s3\":170,\"lat\":47.37859199016309,\"s4\":170},{\"id\":41,\"s2\":170,\"time\":5262300,\"lon\":8.530540466308594,\"s1\":170,\"date\":260315,\"s3\":170,\"lat\":47.379056950776324,\"s4\":170},{\"id\":42,\"s2\":170,\"time\":5262300,\"lon\":8.52839469909668,\"s1\":170,\"date\":260315,\"s3\":170,\"lat\":47.37963814577685,\"s4\":170},{\"id\":43,\"s2\":170,\"time\":5262300,\"lon\":8.526506423950195,\"s1\":170,\"date\":260315,\"s3\":170,\"lat\":47.38004497846526,\"s4\":170},{\"id\":44,\"s2\":170,\"time\":5262400,\"lon\":8.524274826049805,\"s1\":170,\"date\":260315,\"s3\":170,\"lat\":47.38056804445183,\"s4\":170},{\"id\":45,\"s2\":170,\"time\":5262400,\"lon\":8.523416519165039,\"s1\":170,\"date\":260315,\"s3\":170,\"lat\":47.37958002656511,\"s4\":170},{\"id\":46,\"s2\":170,\"time\":5262500,\"lon\":8.521528244018555,\"s1\":170,\"date\":260315,\"s3\":170,\"lat\":47.3798125030277,\"s4\":170},{\"id\":47,\"s2\":170,\"time\":5262500,\"lon\":8.51963996887207,\"s1\":170,\"date\":260315,\"s3\":170,\"lat\":47.38027745287774,\"s4\":170},{\"id\":48,\"s2\":170,\"time\":5262500,\"lon\":8.517837524414062,\"s1\":170,\"date\":260315,\"s3\":170,\"lat\":47.38103298763894,\"s4\":170},{\"id\":49,\"s2\":170,\"time\":5262600,\"lon\":8.516206741333008,\"s1\":170,\"date\":260315,\"s3\":170,\"lat\":47.381614160856806,\"s4\":170}],\"Global 1\":[]}";
	}


        /**
         * HTTP POST request that sends all the points of the current data layer
         * to the server. 
         * @param data JSONObject containing all the points
         * @return HTTP response code (200 if transmission ok)
         * @throws Exception 
         */
	public int sendToBeMapServer(String data) throws Exception {
                

		String url = DOMAIN +"JSON/send";
		URL sender = new URL(url);
                
		HttpURLConnection con = (HttpURLConnection) sender.openConnection();
                if(SERVER_DEBUG) BeMapEditor.mainWindow.append("sendPost called\n");
                
		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                //Main.mainWindow.append("Data sent: " + data + "\n");
		String params = "JSON=" + data;

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(params);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		if(SERVER_DEBUG) BeMapEditor.mainWindow.append("\nSending 'POST' request to URL : " + url + "\n");
		if(SERVER_DEBUG) BeMapEditor.mainWindow.append("Post parameters : " + params + "\n");
		if(SERVER_DEBUG) BeMapEditor.mainWindow.append("Response Code : " + responseCode +"\n");

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		if(SERVER_DEBUG) BeMapEditor.mainWindow.append(response.toString()+"\n");
                
                return responseCode;
	}
        
        public int sendJSONFileToBeMapServer(String filename) throws MalformedURLException, IOException{
            String urlToConnect = DOMAIN + "JSON/send";
            String paramToSend = "JSON";
            File fileToUpload = new File(filename);
            String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.

            URLConnection connection = new URL(urlToConnect).openConnection();
            connection.setDoOutput(true); // This sets request method to POST.
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));

                writer.println("--" + boundary);
                if(SERVER_DEBUG) BeMapEditor.mainWindow.append("--" + boundary+"\n");
                writer.println("Content-Disposition: form-data; name=JSON");
                if(SERVER_DEBUG) BeMapEditor.mainWindow.append("Content-Disposition: form-data; name=JSON"+"\n");
                writer.println("Content-Type: application/json; charset=UTF-8");
                if(SERVER_DEBUG) BeMapEditor.mainWindow.append("Content-Type: application/json; charset=UTF-8"+"\n");
                writer.println();
                if(SERVER_DEBUG) BeMapEditor.mainWindow.append("\n");
                writer.println(paramToSend);
                if(SERVER_DEBUG) BeMapEditor.mainWindow.append(paramToSend+"\n");

                writer.println("--" + boundary);
                if(SERVER_DEBUG) BeMapEditor.mainWindow.append("--" + boundary+"\n");
                writer.println("Content-Disposition: form-data; name=export.json; filename=export.json");
                if(SERVER_DEBUG) BeMapEditor.mainWindow.append("Content-Disposition: form-data; name=export.json; filename=export.json"+"\n");
                writer.println("Content-Type: application/json; charset=UTF-8");
                if(SERVER_DEBUG) BeMapEditor.mainWindow.append("Content-Type: application/json; charset=UTF-8"+"\n");
                writer.println();
                if(SERVER_DEBUG) BeMapEditor.mainWindow.append("\n");
                BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileToUpload), "UTF-8"));
                for (String line; (line = reader.readLine()) != null;) {
                    writer.println(line);
                }
            } finally {
                if (reader != null) try { reader.close(); } catch (IOException logOrIgnore) {}
                }

            writer.println("--" + boundary + "--");
            } finally {
            if (writer != null) writer.close();
            }      
            
            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                    connection.getInputStream()));
            String decodedString;
            while ((decodedString = in.readLine()) != null) {
                BeMapEditor.mainWindow.append("\n"+decodedString);
            }
            in.close();

            // Connection is lazily executed whenever you request any status.
            int responseCode = ((HttpURLConnection) connection).getResponseCode();
            if(SERVER_DEBUG) BeMapEditor.mainWindow.append("Server answer: "+responseCode+"\n"); // Should be 200
            
            return responseCode;
            }
        
        /**
         * Request for getting the road segment from the google servers and plot
         * the JSON object to the status area if SERVER_DEBUG is activated, used 
         * for testing purposes so far.
         * @param lat Latitude
         * @param lon Longitude
         * @throws Exception 
         * @return Google JSONObject
         */
        public JSONObject getGoogleRoadSegment(double lat, double lon) throws Exception {

		String google = "http://maps.googleapis.com/maps/api/geocode/json?latlng="
                        + Double.toString(lat) + "," + Double.toString(lon)
                        + "&sensor=false";
               
		URL obj = new URL(google);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		if(SERVER_DEBUG) BeMapEditor.mainWindow.append("Sending 'GET' request to URL : " + google + "\n");
   
		if(SERVER_DEBUG) BeMapEditor.mainWindow.append("Response Code : " + responseCode + "\n");

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
                String jsonString = response.toString();

                if(SERVER_DEBUG) BeMapEditor.mainWindow.append(jsonString);
                
                JSONObject googleData = new JSONObject(jsonString);
                return googleData;
                
                
	}
        
        
        
}
