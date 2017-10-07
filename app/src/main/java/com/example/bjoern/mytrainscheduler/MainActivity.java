package com.example.bjoern.mytrainscheduler;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;


public class MainActivity extends AppCompatActivity {

    private static String readStream(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            Log.e("aha", "IOException", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e("aha", "IOException", e);
            }
        }
        return sb.toString();
    }

    private class fetch_from_xml
    {


        protected String getelement(String xml)
        {
            try {
                Log.v("aha", "parse1");
                XPathFactory factory = XPathFactory.newInstance();
                Log.v("aha", "parse2");
                XPath xPath = factory.newXPath();
                Log.v("aha", "parse3");
                NodeList shows = (NodeList) xPath.evaluate("/stations/station", new InputSource(new StringReader(xml)), XPathConstants.NODESET);
                Log.v("aha", "parse4");
                for (int i = 0; i < shows.getLength(); i++) {
                    Log.v("aha", "parse5");
                    Element show = (Element) shows.item(i);
                    String eva = show.getAttribute("eva");
                    Log.v("aha", eva);
                    return eva;
                }
            } catch (Exception e) {
                Log.v("aha", "exception in xml parse");
            }
            return "";
        }


    }


    private class makedbrequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            // do above Server call here
            Log.v("aha", "wirkloch");

            try {
                Log.v("aha", "wirkloch10");
                URL url = new URL("https://api.deutschebahn.com/timetables/v1/station/Montabaur");
                Log.v("aha", "wirkloch11");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", "Bearer 9c3f51196a91fe8c98fb8536f3733c4c");
                urlConnection.setRequestProperty("Accept", "application/xml");


                Log.v("aha", "wirkloch12");
                Log.v("vorher",urlConnection.getResponseMessage());
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                Log.v("aha", "wirkloch13");
                String test = readStream(in);

                fetch_from_xml mytest = new fetch_from_xml();
              String eva = mytest.getelement(test);
                Log.e("ahaneu",test);
            }catch(Exception e)
            {
                Log.v("ecp",e.toString());
                String test = e.getMessage();
                if (test != null)
                    Log.v("aha", e.getMessage());
                else
                    Log.v("aha", "is null");

            }
            finally {
            }

            return "done";
        }

        @Override
        protected void onPostExecute(String message) {
            //process message
        }
    }

    private class requesttimetable extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            // do above Server call here
            Log.v("aha", "wirkloch");

            try {
                Log.v("aha", "wirkloch10");
                URL url = new URL("https://api.deutschebahn.com/timetables/v1/fchg/8000667");

                Log.v("aha", "wirkloch11");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", "Bearer 9c3f51196a91fe8c98fb8536f3733c4c");
                urlConnection.setRequestProperty("Accept", "application/xml");


                Log.v("aha", "wirkloch12");
                Log.v("vorher",urlConnection.getResponseMessage());
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                Log.v("aha", "wirkloch13");
                String test = readStream(in);


                Log.e("ahaneu",test);

                return test;
            }catch(Exception e)
            {
                Log.v("ecp",e.toString());
                String test = e.getMessage();
                if (test != null)
                    Log.v("aha", e.getMessage());
                else
                    Log.v("aha", "is null");

            }
            finally {
            }

            return "done";
        }

        @Override
        protected void onPostExecute(String message) {
            //process message
        }
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = (Button) findViewById(R.id.mybutton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                final TextView label = (TextView) findViewById(R.id.label);
                label.setText("Klappt");
                String Station = null;
                try {
                    Station = new makedbrequest().execute("").get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                String res= null;
                try {
                    res = new requesttimetable().execute(Station).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                Log.v("aha", "wirkloch1");
                Log.v("final",res);
            }
        });




        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(10000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final TextView label = (TextView) findViewById(R.id.label);
                                label.setText("Klapptwirklich");
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();
    }
    }



