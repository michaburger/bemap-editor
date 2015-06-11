/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcan2;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import org.json.JSONObject;
import javax.xml.parsers.*;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author Micha
 */
public class OsmAPI {
    
    //0.0001 is a good value (about 5m)
    private static final double NODE_SEARCH_RADIUS = 0.0003;
    private final String USER_AGENT = "Mozilla/5.0";
    private final String DOMAIN = "http://api.openstreetmap.org";
    private final boolean SERVER_DEBUG = false;
        
    public OsmAPI(){
        
    }
    
    public ArrayList<OsmNode> getOSMSegments(double lat, double lon) throws Exception {

		ArrayList<OsmNode> OsmNodeList = new ArrayList<>();
               
                double left = lon - NODE_SEARCH_RADIUS;
                double bottom = lat - NODE_SEARCH_RADIUS;
                double right = lon + NODE_SEARCH_RADIUS;
                double top = lat + NODE_SEARCH_RADIUS;
                
                
                
                String url = DOMAIN + "/api/0.6/map?bbox="
                        +String.format( "%.10f", left )+","+String.format( "%.10f", bottom )+
                        ","+String.format( "%.10f", right )+","+String.format( "%.10f", top );

                
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		if(SERVER_DEBUG) BeMapEditor.mainWindow.append("Sending 'GET' request to URL : " + url + "\n");
   
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
                String xml = response.toString();

                //if(SERVER_DEBUG) BeMapEditor.mainWindow.append(xml);
                
                Document doc = loadXMLFromString(xml);
                NodeList nList = doc.getElementsByTagName("node");
                
                for (int temp = 0; temp < nList.getLength(); temp++) {
 
		Node nNode = nList.item(temp);
 
		//System.out.println("\nCurrent Element :" + nNode.getNodeName());
 
		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 
			Element eElement = (Element) nNode;
                        
                        
			//System.out.println(eElement.getAttribute("id"));
                        //System.out.println(eElement.getAttribute("lat"));
                        //System.out.println(eElement.getAttribute("lon"));
                        
                        NodeList childList = eElement.getChildNodes();
                        
                        for(int i = 0; i < childList.getLength();i++){
                           Node childNode = childList.item(i);
                        
                            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element child = (Element) childNode;
                                
                                if("highway".equals(child.getAttribute("k"))){
                                    double lat1 = Double.parseDouble(eElement.getAttribute("lat"));
                                    double lon1 = Double.parseDouble(eElement.getAttribute("lon"));
                                    long id = Long.parseLong(eElement.getAttribute("id"));
                                    
                                    OsmNode s = new OsmNode(id,lat1,lon1);
                                    OsmNodeList.add(s);
                                    
                                    //for debug
                                    /*
                                    MapMarkerDot m = new MapMarkerDot(lat1,lon1);
                                    BeMapEditor.mainWindow.getMap().addMapMarker(m);
                                    m.setVisible(true);
                                    */
                                }
                            }
                        }     
		}
            }
            
            return OsmNodeList;    
	}
    
    public static Document loadXMLFromString(String xml) throws Exception
    {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    InputSource is = new InputSource(new StringReader(xml));
    return builder.parse(is);
    }
    
   
    
    
            
    
}
