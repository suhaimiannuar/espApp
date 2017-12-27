package com.example.suhaimi.esp8266;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PrivateKey;

public class MainActivity extends AppCompatActivity {

    EditText editIp;
    Button btnOn, btnOff;
    TextView textInfo1, textInfo2, textInfo4, textInfo6;
    String hasil ="";

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editIp = (EditText)findViewById(R.id.ip);
        btnOn = (Button)findViewById(R.id.bon);
        btnOff = (Button)findViewById(R.id.boff);
        textInfo1 = (TextView)findViewById(R.id.info1);
        textInfo2 = (TextView)findViewById(R.id.info2);
        textInfo4 = (TextView)findViewById(R.id.info4);
        textInfo6 = (TextView)findViewById(R.id.info6);


        btnOn.setOnClickListener(btnOnOffClickListener);
        btnOff.setOnClickListener(btnOnOffClickListener);

        doTheAutoRefresh();


    }

    private void doTheAutoRefresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Write code for your refresh logic
                String handlerSensor = "sensor";
                String serverIP = editIp.getText().toString()+":80";


                FireSensor fireSensor = new FireSensor(serverIP);
                fireSensor.execute(handlerSensor);
                doTheAutoRefresh();
            }
        }, 250);
    }

    View.OnClickListener btnOnOffClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String onoff;

            if(v==btnOn){
                onoff = "1";
            }
            else {
                onoff = "0";
            }

            btnOn.setEnabled(false);
            btnOff.setEnabled(false);

            String serverIP = editIp.getText().toString()+":80";

            TaskEsp taskEsp = new TaskEsp(serverIP);
            taskEsp.execute(onoff);
        }
    };

    public static String br2nl(String html) {
        if(html==null)
            return html;
        Document document = Jsoup.parse(html);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
        document.select("br").append("\\n");
        document.select("p").prepend("\\n\\n");
        String s = document.html().replaceAll("\\\\n", "\n");
        return Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
    }


    private class FireSensor extends AsyncTask<String, Void, String>{

        String server;

        FireSensor(String server){ this.server = server;}

        @Override
        protected String doInBackground(String... params) {
            String val = params[0];
            String serverResponse = "";

            String html = "http://"+server+"/"+val;

            try{

                Document doc = Jsoup.connect(html).get();
                serverResponse = doc.text();

            }catch (Exception e){
                e.printStackTrace();
            }

            return serverResponse;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            textInfo6.setText(s);
        }
    }



    private class TaskEsp extends AsyncTask<String,Void,String>{

        String server;

        TaskEsp(String server){
            this.server = server;
        }

        @Override
        protected String doInBackground(String... params) {
            String val = params[0];
            final String p = "http://"+server+"?led="+val;

            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    textInfo1.setText(p);
                }
            });

            String serverResponse = "";
            HttpClient httpclient = new DefaultHttpClient();
            try {
                HttpGet httpGet = new HttpGet();
                httpGet.setURI(new URI(p));
                HttpResponse httpResponse = httpclient.execute(httpGet);

                InputStream inputStream = null;
                inputStream = httpResponse.getEntity().getContent();
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(inputStream));
                serverResponse = bufferedReader.readLine();

                inputStream.close();
            } catch (URISyntaxException e) {
                e.printStackTrace();
                serverResponse = e.getMessage();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                serverResponse = e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
                serverResponse = e.getMessage();
            }

            return serverResponse;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            textInfo4.setText(s);

            btnOn.setEnabled(true);
            btnOff.setEnabled(true);

        }
    }
}


