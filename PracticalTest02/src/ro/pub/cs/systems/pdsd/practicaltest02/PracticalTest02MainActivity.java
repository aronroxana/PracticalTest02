package ro.pub.cs.systems.pdsd.practicaltest02;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class PracticalTest02MainActivity extends Activity {
	
	// Server widgets
	private EditText     serverPortEditText       = null;
	private Button       connectButton            = null;
		
	// Client widgets
	private EditText     clientAddressEditText    = null;
	private EditText     clientPortEditText       = null;
	private EditText     cityEditText             = null;
	private Spinner      informationTypeSpinner   = null;
	private Button       getWeatherForecastButton = null;
	private TextView     weatherForecastTextView  = null;
		
	private ServerThread serverThread             = null;
	private ClientThread clientThread             = null;
	
	private ConnectButtonClickListener connectButtonListener = new ConnectButtonClickListener();
		
	private class ConnectButtonClickListener implements Button.OnClickListener {

		@Override
		public void onClick(View v) {
			String serverPort = serverPortEditText.getText().toString();
			serverThread = new ServerThread(Integer.parseInt(serverPort));
			serverThread.start();			
		}
		
	}
	
	private WeatherButtonClickListener weatherButtonListener = new WeatherButtonClickListener();
	
	private class WeatherButtonClickListener implements Button.OnClickListener {

		@Override
		public void onClick(View v) {
			String clientAddress = clientAddressEditText.getText().toString();
			String clientPort    = clientPortEditText.getText().toString();
			String city = cityEditText.getText().toString();
			String informationType = informationTypeSpinner.getSelectedItem().toString();
			
			weatherForecastTextView.setText("");
			clientThread = new ClientThread(
				      clientAddress,
				      Integer.parseInt(clientPort),
				      city,
				      informationType,
				      weatherForecastTextView);
			clientThread.start();
		}
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_practical_test02_main);
		
		serverPortEditText = (EditText)findViewById(R.id.server_port_edit_text);
		connectButton = (Button)findViewById(R.id.connect_button);
		connectButton.setOnClickListener(connectButtonListener);
		
		clientAddressEditText = (EditText)findViewById(R.id.client_address_edit_text);
		clientPortEditText = (EditText)findViewById(R.id.client_port_edit_text);
		cityEditText = (EditText)findViewById(R.id.city_edit_text);
		informationTypeSpinner = (Spinner)findViewById(R.id.information_type_spinner);
		getWeatherForecastButton = (Button)findViewById(R.id.get_weather_forecast_button);
		getWeatherForecastButton.setOnClickListener(weatherButtonListener);
		weatherForecastTextView = (TextView)findViewById(R.id.weather_forecast_text_view);
	}

	@Override
	protected void onDestroy() {
		if (serverThread != null) {
			serverThread.stopThread();
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.practical_test02_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
