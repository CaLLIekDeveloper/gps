package com.example.gps;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class Api extends AsyncTask<String,Void,String> {
    protected  void onPreExecute() { }

    //https://parshyn.000webhostapp.com/
    @Override
    protected String doInBackground(String... arg0) {
            try {
                String link = "https://parshyn.000webhostapp.com/add.php?q="+ URLEncoder.encode(arg0[1], "UTF-8");
                Log.e("URL", link);
                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                wr.flush();

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    Log.d("Answer ", line);
                    break;
                }

                Log.e("GetData", "FINISH GOOD:    ");
                return "";
            } catch (Exception ex) {
                Log.e("GetData", "FINISH BAD:    " + ex.getMessage());
                return new String("Exception: " + ex.getMessage());
            }
    }
    @Override
    protected void onPostExecute(String result) { }
}
