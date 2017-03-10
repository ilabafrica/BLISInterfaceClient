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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import log.DisplayMessageType;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import system.settings;

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
        try {
            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost("http://blis.dev/api/searchtests");
            
            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("key", "123456"));
            params.add(new BasicNameValuePair("datefrom", "2017-02-09 00:00:00"));
            params.add(new BasicNameValuePair("dateto", "2017-02-10 23:59:59"));
            params.add(new BasicNameValuePair("testtype", "LFTS"));
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
            HttpEntity entity = response.getEntity();
            
            ResponseHandler<String> responseHandler=new BasicResponseHandler();
            String responseString = new BasicResponseHandler().handleResponse(response);
            
            return responseString;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
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
    public static String saveResults(String specimenID, int measureID, String result)
    {
         String data="-1";
        try 
        {  
                String url = settings.BLIS_URL;
                url = url + "api/update_result.php?username="+settings.BLIS_USERNAME + "&password="+settings.BLIS_PASSWORD;           
                url = url + "&specimen_id="+URLEncoder.encode(specimenID,"UTF-8");
                url = url + "&measure_id="+measureID;
                url = url + "&result="+URLEncoder.encode(result,"UTF-8");
                url = url + "&dec=0"; 
                  
                 
                
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
            log.logger.PrintStackTrace(ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
        }
         return data.trim();
    }
    public static String saveResults(String specimenID, int measureID, float result,int dec)
    {
        
        String data="-1";
        try 
        {  
                String url = settings.BLIS_URL;
                url = url + "api/update_result.php?username="+settings.BLIS_USERNAME + "&password="+settings.BLIS_PASSWORD;           
                url = url + "&specimen_id="+URLEncoder.encode(specimenID,"UTF-8");
                url = url + "&measure_id="+measureID;
                url = url + "&result="+result;  
                url = url + "&dec="+dec;  
                 
                
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
            log.logger.PrintStackTrace(ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
        }
         return data.trim(); 
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
    
   
    
}
