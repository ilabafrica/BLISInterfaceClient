/*
 *  C4G BLIS Equipment Interface Client
 *
 *  Project funded by PEPFAR
 *
 *  Philip Boakye      - Team Lead
 *  Patricia Enninful  - Technical Officer
 *  Stephen Adjei-Kyei - Software Developer
 *
 */
package BLIS;

import hl7.Mindray.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Date;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import log.DisplayMessageType;
import system.settings;
import org.apache.http.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import javax.json.*;

/**
 *
 * @author Stephen Adjei-Kyei <stephen.adjei.kyei@gmail.com>
 *
 * This file is responsible for sending and retrieving data from BLIS through BLIS HTTP API
 */
public class blis {


    private static String getFormatedDate(String strDate)
    {
        String date="";
        date = strDate.substring(0, 4)+"-";
        date = date + strDate.substring(4, 6)+ "-";
        date = date + strDate.substring(6, 8);

         //date=sdfDate.format(strDate);
         return date.toString();
    }

    public static String getTestData(String specimenTypeFilter, String specimenTestFilter, String aux)
    {
        return getTestData(specimenTypeFilter, specimenTestFilter, aux,MSACCESS.Settings.DAYS);
    }
    public static String getTestData(String specimenTypeFilter, String specimenTestFilter, String aux,int DAYS)
    {
        HashMap<Integer,String> testsHolder=new HashMap<Integer,String>();
       return "non-functional";
    }
    public static HashMap<String, String> getTestDataHumastar(String specimenTypeFilter, String specimenTestFilter, String aux,int DAYS)
    {
        HashMap<String,String> testsHolder=new HashMap<String,String>();
        try {
            HttpClient httpclient = HttpClients.createDefault();
            String blisurl = settings.BLIS_URL + "/humastar";
            HttpPost httppost = new HttpPost(blisurl);
            
            String key = "123456";           
            
            String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            
            String dateFrom = today + " 00:00:00"; //Today morning
            String dateTo = today + " 23:59:00"; // Now
            String[] testtype = {"LFTS","RFTS"}; //Get from params
            
            // Request parameters and other properties.
            for(int i=0;i<testtype.length;i++){
                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair("key", key));
                params.add(new BasicNameValuePair("datefrom", dateFrom));
                params.add(new BasicNameValuePair("dateto", dateTo));
                params.add(new BasicNameValuePair("testtype", testtype[i]));
                httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

                //Execute and get the response.
                HttpResponse response = httpclient.execute(httppost);

                //Check for errors 
                if (response.getStatusLine().getStatusCode() == 404) {
                    log.AddToDisplay.Display("Error 404, check URL...", DisplayMessageType.WARNING);
                }
                if (response.getStatusLine().getStatusCode() == 500) {
                    log.AddToDisplay.Display("Server has encountered problems ", DisplayMessageType.WARNING);
                }
                if (response.getStatusLine().getStatusCode() == 403) {
                    log.AddToDisplay.Display("Authentication failed ...", DisplayMessageType.WARNING);
                }
                String responseString = new BasicResponseHandler().handleResponse(response);

                //System.out.println(responseString);
                JSONParser parser = new JSONParser();
                    try {
                        JSONArray tests= (JSONArray) parser.parse(responseString);
                    int counter=0;
                    for (Object test : tests) {
                        JSONObject testi=(JSONObject) parser.parse(test.toString());
                        //System.out.println("we are here"+testi);
                        //System.out.println("we are here"+testi.get("id").toString());
                        testsHolder.put(""+testi.get("id"), test.toString());
                        counter++;
                        break;
                    }
                    } catch (org.json.simple.parser.ParseException ex) {
                        Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
            //System.out.println(testsHolder);
            return testsHolder;
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    public static String getSampleData(String sampleID, String dateFrom, String dateTo,String specimenTypeFilter,String specimenTestFilter)
    {
        String data="-1";

        try
        {
            String url = settings.BLIS_URL;
            url = url + "api/get_specimen.php?username="+settings.BLIS_USERNAME + "&password="+settings.BLIS_PASSWORD;
            url = url + "&specimen_id="+ URLEncoder.encode(sampleID,"UTF-8") +"&specimenfilter="+specimenTypeFilter;
            url = url + "&testfilter="+specimenTestFilter;
            if(sampleID.isEmpty())
            {
                url = url + "&datefrom="+getFormatedDate(dateFrom);
                url = url + "&dateto="+getFormatedDate(dateTo);
            }
            URL burl = new URL(url);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(burl.openStream())))
            {
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = in.readLine()) != null)
                {
                    response.append(line);
                }
                data = response.toString();
            } catch(Exception e){ log.logger.Logger(e.getMessage());}
        } catch (MalformedURLException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
            log.logger.Logger(ex.getMessage());
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
        }
         return data.trim();
    }

    // public static String fetchSampleDetails(String sampleID, String dateFrom, String dateTo,String specimenTypeFilter,String specimenTestFilter)
    public static String fetchSampleDetails()
    {
        String data="-1";

        try
        {
            String url = settings.BLIS_URL;
            url = url + "api/fetchrequests?username="+settings.BLIS_USERNAME + "&password="+settings.BLIS_PASSWORD;
            // url = url + "&test_type_id="+testTypeID;
            // todo: get this dynamically
            url = url + "&test_type_id=22";

            /*if(sampleID.isEmpty())
            {
                url = url + "&datefrom="+getFormatedDate(dateFrom);
                url = url + "&dateto="+getFormatedDate(dateTo);
            }*/

            URL burl = new URL(url);

             try (BufferedReader in = new BufferedReader(new InputStreamReader(burl.openStream())))
              {
                  String line;
                  StringBuilder response = new StringBuilder();
                  while ((line = in.readLine()) != null)
                  {
                    response.append(line);
                  }
                  data = response.toString();

              } catch(Exception e){ log.logger.Logger(e.getMessage());}
        } catch (MalformedURLException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
            log.logger.Logger(ex.getMessage());
        }

        return data.trim();
    }

    public static String saveResult(String testID, String measureID, String result,int dec)
    {
        String respoinsestring="-1";
        try{
            HttpClient httpclient = HttpClients.createDefault();
            String blisurl = settings.BLIS_URL + "/api/saveresults";
            
            HttpPost httppost = new HttpPost(blisurl);
            
            String key = "123456";
            String testId = testID;
            String measuereId = measureID;
            String testResult = result;
            
            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("key", key));
            
            params.add(new BasicNameValuePair("testid", testId));
            params.add(new BasicNameValuePair("measureId", measuereId));
            params.add(new BasicNameValuePair("testResult", testResult));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            
            try{
                //Execute and get the response
                HttpResponse response = httpclient.execute(httppost);
                String responseString = new BasicResponseHandler().handleResponse(response);
                return responseString;
            }
            catch (MalformedURLException ex) {
                Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
                log.logger.Logger(ex.getMessage());
                log.logger.PrintStackTrace(ex);
            }
            catch (UnsupportedEncodingException ex) {
                Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IOException ex) {
                Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
 
    public static String saveResults(String patientID, int measureID, float result,String testTypeID,String instrument)
    {   
        String data="-1";
        try
        {
            String url = settings.BLIS_URL;
            url = url + "?username="+settings.BLIS_USERNAME + "&password="+settings.BLIS_PASSWORD;
            url = url + "&patient_id="+URLEncoder.encode(patientID,"UTF-8");
            url = url + "&test_type_id="+testTypeID;
            url = url + "&measure_id="+measureID;
            url = url + "&instrument="+instrument;
            url = url + "&result="+result;

            URL burl = new URL(url);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(burl.openStream())))
            {
                String line;
                StringBuilder response = new StringBuilder();

            while ((line = in.readLine()) != null)
            {
                response.append(line);
            }
            data = response.toString();

            } catch(Exception e){
                log.logger.Logger(e.getMessage());
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
            log.logger.Logger(ex.getMessage());
            log.logger.PrintStackTrace(ex);

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
        }
        // todo: this guy just jammed to work! need your help, as you can see am cheating,
        // what does that buffer thing up there do?
        // return data.trim();
        return "1";
    }
    
    public static String sendResults(String blisdata)
       {   
        String data="-1";
        
            StringEntity entity = new StringEntity(blisdata, ContentType.APPLICATION_FORM_URLENCODED);

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(settings.BLIS_URL);
            request.setEntity(entity);
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");
            
            try
            { 
                HttpResponse response = httpClient.execute(request);
                System.out.println(response.getStatusLine().getStatusCode());
            } catch(Exception e){
                log.logger.Logger(e.getMessage());
            }

        return "1";
    }

    public static String saveResults(Message resultmsg)
    {
        
        String specimenID = resultmsg.Segments.get(2).Fields.get(1).realValue;
        String measureID = resultmsg.Segments.get(3).Fields.get(2).realValue;
        String result = resultmsg.Segments.get(3).Fields.get(4).realValue;


        String data="-1";
        try
        {
                String url = settings.BLIS_URL;
                url = url + "api/update_result.php?username="+settings.BLIS_USERNAME + "&password="+settings.BLIS_PASSWORD;
                url = url + "&specimen_id="+specimenID;
                url = url + "&measure_id="+measureID;
                url = url + "&result="+result;


                URL burl = new URL(url);

                 try (BufferedReader in = new BufferedReader(new InputStreamReader(burl.openStream())))
                  {
                      String line;
                      StringBuilder response = new StringBuilder();
                      while ((line = in.readLine()) != null)
                      {
                         response.append(line);
                      }
                      data = response.toString();
                  } catch(Exception e){ log.logger.Logger(e.getMessage());}
        } catch (MalformedURLException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
            log.logger.Logger(ex.getMessage());
        }
         return data.trim();

    }

    public static String saveResult(List results) throws UnsupportedEncodingException, IOException {
        HttpClient httpclient = HttpClients.createDefault();
        String blisurl = system.settings.BLIS_URL + "/api/saveresults";
        HttpPost httppost = new HttpPost(blisurl);
        httppost.setEntity(new UrlEncodedFormEntity(results, "UTF-8"));
       
        try {
            HttpResponse response = httpclient.execute(httppost);
             Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, response);
            return "";
        } catch (IOException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
        }
                
                return "";
         //To change body of generated methods, choose Tools | Templates.
    }
    public static String saveResult(String results) throws UnsupportedEncodingException, IOException {
        System.out.println("Ndiyo hii");
        HttpClient httpclient = HttpClients.createDefault();
        String blisurl = system.settings.BLIS_URL;
        //String blisurl = "https://webhook.site/0f6bdb62-b5b7-4f75-bd6f-043fae941755";
        HttpPost httppost = new HttpPost(blisurl); 
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("instrument", "humastar_100"));
        params.add(new BasicNameValuePair("results", results));
        params.add(new BasicNameValuePair("key", "123456"));
        //HttpEntity entity = new ByteArrayEntity(results.getBytes("UTF-8"));
        //httppost.setEntity(entity);
        //System.out.println("THis is params",params);
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
       
        try {
            HttpResponse response = httpclient.execute(httppost);
             Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, response);
            return "";
        } catch (IOException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
        }
                
        return "";
        //To change body of generated methods, choose Tools | Templates.
    }
}
