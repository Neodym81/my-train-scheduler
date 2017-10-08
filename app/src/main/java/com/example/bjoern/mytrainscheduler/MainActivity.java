package com.example.bjoern.mytrainscheduler;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
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
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;




public class MainActivity extends AppCompatActivity {

    public class Zuglauf {
        public String Zugnummer;
        public String id;
        public String wegliste;
        public String Zugtyp;
        public String Abfahrt;
    }

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

        protected java.util.List<java.util.Map.Entry<String,Zuglauf>> getdestinationfromplan(String xml)
        {
            java.util.List<java.util.Map.Entry<String,Zuglauf>> pairList= new java.util.ArrayList<>();
            try {
                XPathFactory factory = XPathFactory.newInstance();

                XPath xPath = factory.newXPath();
                ;
                NodeList shows = (NodeList) xPath.evaluate("/timetable/s", new InputSource(new StringReader(xml)), XPathConstants.NODESET);
                Log.v("aha", "parse4");
                for (int i = 0; i < shows.getLength(); i++) {
                    Element show = (Element) shows.item(i);
                    String id = show.getAttribute("id");

                    NodeList listzug = show.getElementsByTagName("tl");
                    String Zugnummer="";
                    String ZugTyp="";
for(int j=0; j < listzug.getLength(); j++)
{
    Zugnummer = ((Element) listzug.item(j)).getAttribute("n");
    ZugTyp = ((Element) listzug.item(j)).getAttribute("c");
}

                    NodeList innerlist = show.getElementsByTagName("dp");

                    for (int j = 0; j < innerlist.getLength();j++)
                    {
                        String innerste = ((Element) innerlist.item(j)).getAttribute("ppth");
                        String Abfahrt = ((Element) innerlist.item(j)).getAttribute("pt");
                        if (innerste.toLowerCase().indexOf("frankfurt") >=0) {
                            Log.v("inner", innerste + " " + id);
                            Zuglauf myZuglauf = new Zuglauf();
                            myZuglauf.id=id;
                            myZuglauf.wegliste=innerste;
                            myZuglauf.Zugnummer=Zugnummer;
                            myZuglauf.Zugtyp=ZugTyp;
                            myZuglauf.Abfahrt=Abfahrt;
                            pairList.add(new java.util.AbstractMap.SimpleEntry<>(id, myZuglauf));
                        }
                    }

                    Log.v("aha", "parse5");
                    //Element show = (Element) shows.item(i);
                    //String destination = show.getAttribute("ppth");
                    //Log.v("dest",destination );

                }
            } catch (Exception e) {
                Log.v("aha", "exception in xml parse");
            }
            return pairList;
        }


    }


    private class makedbrequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            // do above Server call here

            try {
                URL url = new URL("https://api.deutschebahn.com/timetables/v1/station/"+params[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", "Bearer 9c3f51196a91fe8c98fb8536f3733c4c");
                urlConnection.setRequestProperty("Accept", "application/xml");
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                String test = readStream(in);

                fetch_from_xml mytest = new fetch_from_xml();
                String eva = mytest.getelement(test);
                Log.e("ahaneu",test);
                return eva;
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

            return "";
        }

        @Override
        protected void onPostExecute(String message) {
            //process message
        }
    }


    private class requestchanges extends AsyncTask<Object, Void, String> {

        java.util.List<java.util.Map.Entry<String, Zuglauf>> regularplan = null;

        @Override
        protected String doInBackground(Object[] params) {
            // do above Server call here
            regularplan= (List<Map.Entry<String, Zuglauf>>) params[1];
            Log.v("aha", "wirkloch");
            java.util.List<java.util.Map.Entry<String, Zuglauf>> pairList = null;
            try {
                Log.v("aha", "wirkloch10");
                // URL url = new URL("https://api.deutschebahn.com/timetables/v1/fchg/8000667");
                URL url = new URL("https://api.deutschebahn.com/timetables/v1/fchg/"+params[0]);


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

                fetch_from_xml mytest = new fetch_from_xml();

                correct_static_list(regularplan,test);


                pairList = mytest.getdestinationfromplan(test);
Log.v("number of changes found", String.valueOf(pairList.size()));
for(int i=0; i < pairList.size();i++)
{
    Log.e("ausgabe",pairList.get(i).getValue().Zugnummer);
    Log.e("ausgabe",pairList.get(i).getValue().Abfahrt);
    Log.e("ausgabe key",pairList.get(i).getKey());
}

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

        private void correct_static_list(java.util.List<java.util.Map.Entry<String,Zuglauf>> regularplan, String xml) {
            for(int i=0; i < regularplan.size();i++)
            {
                NodeList shows=null;
                Log.v("correctlist",regularplan.get(i).getKey() );
                XPathFactory factory = XPathFactory.newInstance();
                Log.v("aha", "parse2");
                XPath xPath = factory.newXPath();
                Log.v("aha", "parse3");
                try {


                    shows = (NodeList) xPath.evaluate("/timetable/s", new InputSource(new StringReader(xml)), XPathConstants.NODESET);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                    Log.v("aha", "parse4");
                for (int j = 0; j < shows.getLength(); j++) {
                    Element show = (Element) shows.item(j);
                    String id = show.getAttribute("id");
                    Log.v("id inner",id);
if (id.equals(regularplan.get(i).getKey()))
{
    Log.v("Found match",id);
    NodeList dp= show.getElementsByTagName("dp");
    for (int k=0; k <dp.getLength();k++)
    {
        String newdepart=((Element) dp.item(k)).getAttribute("ct");
        Log.v("newdepart",newdepart);

    }
}

                }
            }
        }

        @Override
        protected void onPostExecute(String message) {
            //process message
            Log.v("after changes","after changes");
        }

    }


    private class requesttimetable extends AsyncTask<String, Void, String> {

        private String Station;
        java.util.List<java.util.Map.Entry<String, Zuglauf>> pairList = null;
        @Override
        protected String doInBackground(String[] params) {
            // do above Server call here
            Station=params[0];

            try {
                Date dt = new Date();

                SimpleDateFormat df1 = new SimpleDateFormat( "YYMMdd" );
                Log.v("Datum",df1.format( dt ));
                SimpleDateFormat df2 = new SimpleDateFormat( "HH" );
                Log.v("Datum",df2.format( dt ));


             //   String sourceDate = "2012-02-29 21";
              //  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
               // Date myDate = format.parse(sourceDate);
//                Calendar c = Calendar.getInstance();
  //              c.setTime(dt);

    //            int i=0;
      //          while (i < 24)
        //        {
          //          c.add(Calendar.HOUR, 1);
            //        myDate=c.getTime();
              //      System.out.println(format.format(myDate));
                //    ++i;
               // }




                int inc = 0;
                int found =0;
                String test="";
                Calendar c = Calendar.getInstance();
                             c.setTime(dt);
                while ((inc < 24 ) && (found == 0))
                {
                    URL url = new URL("https://api.deutschebahn.com/timetables/v1/plan/" + params[0] + "/" + df1.format(dt) + "/" + df2.format(dt));

                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestProperty("Authorization", "Bearer 9c3f51196a91fe8c98fb8536f3733c4c");
                    urlConnection.setRequestProperty("Accept", "application/xml");

                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    test = readStream(in);
                    fetch_from_xml mytest = new fetch_from_xml();
                    pairList = mytest.getdestinationfromplan(test);
                    if (pairList.size()>0)
                    {
                        found = 1;
                    }
                    else {
                        inc++;
                        c.add(Calendar.HOUR, 1);
                        dt=c.getTime();
                    }
                    Log.e("ahaneu", pairList.toString());
                }

                if (pairList != null && pairList.size()>0)
                {

                }

                for(int i=0; i < pairList.size();i++)
                    Log.v("id regulaer",pairList.get(i).getKey());

                return "";
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
            //now check the changed plan
            try {
                Log.v("now start changes","changes");
                String Test = new requestchanges().execute(Station,pairList).get();
                Log.v("postexecute","postexecute");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
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
                    Station = new makedbrequest().execute("Montabaur").get();
                    Log.v("resultat",Station);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if (Station != "") {
                    String res = null;
                    try {
                        res = new requesttimetable().execute(Station).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    Log.v("aha", "wirkloch1");
                    Log.e("final", res);
                }
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



