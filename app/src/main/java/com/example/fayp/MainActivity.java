package com.example.fayp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> displayData = new ArrayList<String>();
    Button findBtn;
    EditText cityNameView;
    String city;
    ListView listWeather;
    ImageView weatherImage;
    String icon;

    public void findWeather(View view){
        city = cityNameView.getText().toString();
        city = city.trim();

        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=APP_ID";
        // http://openweathermap.org/img/w/10d.png

        weatherRetriever task = new weatherRetriever();
        task.execute(url);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findBtn = (Button) findViewById(R.id.findBtn);
        cityNameView = (EditText) findViewById(R.id.cityName);
        listWeather = (ListView) findViewById(R.id.listData);
        weatherImage = (ImageView) findViewById(R.id.weatherImage);
    }

    public class weatherRetriever extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            String weatherData = "";
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1){
                    weatherData += ((char) data);
                    data = reader.read();
                }

                Log.i("Data",weatherData);
                return weatherData;
            } catch (Exception e){
                e.printStackTrace();
                return "404";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                if (s.equals("404")) {
                    Toast.makeText(MainActivity.this,"Write Appropriate name of City", Toast.LENGTH_LONG).show();
                    displayData.clear();
                    weatherImage.setImageBitmap(null);
                } else{
                    displayData.clear();
                    JSONObject jsonObject = new JSONObject(s);
                    JSONObject coord = (JSONObject) jsonObject.get("coord");
                    String latitude = coord.getString("lat");
                    String longitude = coord.getString("lon");
                    displayData.add("Latitude: " + latitude);
                    displayData.add("Longitude: " + longitude);

                    String weatherInfo = jsonObject.getString("weather");
                    JSONArray weatherArray = new JSONArray(weatherInfo);
                    JSONObject weather = (JSONObject) weatherArray.get(0);
                    displayData.add("Description: " + weather.getString("description"));
                    icon = weather.getString("icon");

                    JSONObject main = jsonObject.getJSONObject("main");
                    displayData.add("Temperature (in F): " + main.getString("temp"));

                    JSONObject wind = jsonObject.getJSONObject("wind");
                    displayData.add("Wind speed in m/s: " + wind.getString("speed"));

                    imageDownloader imageDownloader = new imageDownloader();
                    Log.i("URL:", "https://openweathermap.org/img/w/" + icon + ".png");
                    imageDownloader.execute("https://openweathermap.org/img/w/" + icon + ".png");

                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, displayData);
                listWeather.setAdapter(arrayAdapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class imageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();
                Bitmap myBitMap = BitmapFactory.decodeStream(in);
                return myBitMap;
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            weatherImage.setImageBitmap(bitmap);
        }
    }
}