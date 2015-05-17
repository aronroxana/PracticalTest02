package ro.pub.cs.systems.pdsd.practicaltest02;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

public class ServerThread extends Thread {
	
	private int          port         = 0;
	private ServerSocket serverSocket = null;
	
	private HashMap<String, WeatherForecastInformation> data = null;
	
	public ServerThread(int port) {
		this.port = port;
		try {
			this.serverSocket = new ServerSocket(port);
		} catch (IOException ioException) {
			Log.e("DEBUG", "An exception has occurred: " + ioException.getMessage());			
			ioException.printStackTrace();
			
		}
		this.data = new HashMap<String, WeatherForecastInformation>();
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setServerSocker(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}
	
	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	public synchronized void setData(String city, WeatherForecastInformation weatherForecastInformation) {
		this.data.put(city, weatherForecastInformation);
	}
	
	public synchronized HashMap<String, WeatherForecastInformation> getData() {
		return data;
	}
	
	@Override
	public void run() {
		try {
			Thread.currentThread();
			while(!Thread.interrupted()) {
				Socket socket = serverSocket.accept();
				BufferedReader bufferedReader = Utilities.getReader(socket);
				PrintWriter    printWriter    = Utilities.getWriter(socket);
				String city            = bufferedReader.readLine();
				String informationType = bufferedReader.readLine();
				WeatherForecastInformation weatherForecastInformation = null;
				if (city != null && !city.isEmpty() && informationType != null && !informationType.isEmpty()) {
					if (data.containsKey(city)) { //get info from cache
						weatherForecastInformation = data.get(city);
					} else { //get info from web site
						HttpClient httpClient = new DefaultHttpClient();
						HttpGet httpGet = new HttpGet("http://www.wunderground.com/cgi-bin/findweather/getForecast?query="+city);
						ResponseHandler<String> responseHandler = new BasicResponseHandler();
						String pageSourceCode = httpClient.execute(httpGet, responseHandler);
						if(pageSourceCode != null) {
							Document document = Jsoup.parse(pageSourceCode);
							Element element = document.child(0);
							Elements scripts = element.getElementsByTag("script");
							for (Element script: scripts) {
								
								String scriptData = script.data();
								
								if (scriptData.contains("wui.api_data =\n")) {
									int position = scriptData.indexOf("wui.api_data =\n") + "wui.api_data =\n".length();
									scriptData = scriptData.substring(position);
									
									JSONObject content = new JSONObject(scriptData);
									
									JSONObject currentObservation = content.getJSONObject("current_observation");
									String temperature = currentObservation.getString("temperature");
									String windSpeed = currentObservation.getString("wind_speed");
									String condition = currentObservation.getString("condition");
									String pressure = currentObservation.getString("pressure");
									String humidity = currentObservation.getString("humidity");
									
									weatherForecastInformation = new WeatherForecastInformation(
											temperature,
											windSpeed,
											condition,
											pressure,
											humidity);

									setData(city, weatherForecastInformation);
									break;
								}
							}
						}
						
					}
					if (weatherForecastInformation != null) {
						String result = null;
						if ("all".equals(informationType)) {
							result = weatherForecastInformation.toString();
						} else if ("temperature".equals(informationType)) {
							result = weatherForecastInformation.getTemperature();
						} else if ("windSpeed".equals(informationType)) {
							result = weatherForecastInformation.getWindSpeed();
						} else if ("condition".equals(informationType)) {
							result = weatherForecastInformation.getCondition();
						} else if ("humidity".equals(informationType)) {
							result = weatherForecastInformation.getHumidity();
						} else if ("pressure".equals(informationType)) {
							result = weatherForecastInformation.getPressure();
						} else {
							result = "Wrong information type (all / temperature / wind_speed / condition / humidity / pressure)!";
						}
						printWriter.println(result);
						printWriter.flush();
					}
				}
				
				socket.close();
			}
			
		} catch (ClientProtocolException clientProtocolException) {
			Log.e("DEBUG", "An exception has occurred: " + clientProtocolException.getMessage());
			clientProtocolException.printStackTrace();		
		} catch (IOException ioException) {
			Log.e("DEBUG", "An exception has occurred: " + ioException.getMessage());
			ioException.printStackTrace();
		} catch (JSONException jsonException) {
			Log.e("DEBUG", "[COMMUNICATION THREAD] An exception has occurred: " + jsonException.getMessage());
			jsonException.printStackTrace();				
		}
	}
	
	public void stopThread() {
		if (serverSocket != null) {
			interrupt();
			try {
				serverSocket.close();
			} catch (IOException ioException) {
				Log.e("DEBUG", "An exception has occurred: " + ioException.getMessage());
				ioException.printStackTrace();				
			}
		}
	}
}
