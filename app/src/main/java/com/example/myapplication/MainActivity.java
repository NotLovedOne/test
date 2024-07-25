package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myapplication.databinding.ActivityMainBinding;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'myapplication' library on application startup.
    static {
        System.loadLibrary("myapplication");
    }

    private native String getIpAddress();

    private ActivityMainBinding binding;

    private ProgressBar progressBar;
    private TextView ipAddressTextView;
    private TextView resultTextView;
    private ImageView resultIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressBar = findViewById(R.id.progress_bar);
        ipAddressTextView = findViewById(R.id.ip_address_text_view);
        resultTextView = findViewById(R.id.result_text_view);
        resultIcon = findViewById(R.id.result_icon);

        // Example of a call to a native method
        Button fetchIpButton = findViewById(R.id.fetch_ip_button);
        fetchIpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipAddress = getIpAddress();
                ipAddressTextView.setText(ipAddress);
                new SendIpTask().execute(ipAddress);
            }
        });
    }

    private class SendIpTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            ipAddressTextView.setVisibility(View.VISIBLE);
            resultTextView.setVisibility(View.GONE);
            resultIcon.setVisibility(View.GONE);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            String ipAddress = strings[0];
            try {
                URL url = new URL("https://s7om3fdgbt7lcvqdnxitjmtiim0uczux.lambda-url.us-east-2.on.aws/");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
//                urlConnection.setConnectTimeout(3000);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("address", ipAddress);

                OutputStream os = urlConnection.getOutputStream();
                os.write(jsonObject.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    JSONObject responseJson = new JSONObject(response.toString());
                    return responseJson.getBoolean("nat");
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE);
            resultTextView.setVisibility(View.VISIBLE);
            resultIcon.setVisibility(View.VISIBLE);

            if (result != null && result) {
                resultTextView.setText("Response is true");
                resultIcon.setImageResource(android.R.drawable.presence_online);
            } else {
                resultTextView.setText("Response is false");
                resultIcon.setImageResource(android.R.drawable.presence_offline);
            }
        }
    }
}