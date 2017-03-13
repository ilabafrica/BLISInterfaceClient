/* 
 *  C4G BLIS Equipment Interface Client
 * 
 *  Project funded by PEPFAR
 * 
 *  Emmanuel Kweyu      - Team Lead  
 *  Brian Maiyo  - Software Developer
 *  Emmanuel Kitsao - Software Developer
 * 
 */
package TEXT;




import BLIS.sampledata;
import configuration.configuration;
import configuration.xmlparser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import log.DisplayMessageType;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author Brian Maiyo <bmaiyo@strathmore.edu>
 */
public class Humastar100 extends Thread {
	
	 private static List<String> testIDs = new ArrayList<String>();
	 static final char Start_Block = (char)2;
	 static final char End_Block = (char)3;
	 static final char CARRIAGE_RETURN = 13; 
	 private static StringBuilder datarecieved = new StringBuilder();
	 private boolean stopped = false;
	 private static FileTime  ReadTime;
	 private static long ReadLine = 1;   
	 BufferedReader in=null;   
	
	
	private static String getFileName(){
		 return new utilities().getFileName(settings.FILE_NAME, settings.FILE_NAME_FORMAT,settings.FILE_EXTENSION);
	}
	 
	@Override
	public void run() {
            log.AddToDisplay.Display("Humastar 100 handler started...", DisplayMessageType.TITLE);
            log.AddToDisplay.Display("Checking file availability  on this system...", DisplayMessageType.INFORMATION);
            if(openFile()){
                log.AddToDisplay.Display("File Available and accessible...", DisplayMessageType.INFORMATION);
                setTestIDs();
                if(system.settings.ENABLE_AUTO_POOL)
                {
                    while(!stopped)
                    {             
                        try {
                            getBLISTests("",false);
                                manageResults();
                            Thread.sleep(system.settings.POOL_INTERVAL * 1000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Humastar100.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    log.AddToDisplay.Display("Humastar 100 Handler Stopped",log.DisplayMessageType.TITLE);
                }
                else{
                    log.AddToDisplay.Display("Auto Pull Disabled. Only manual activity can be performed",log.DisplayMessageType.INFORMATION);
                }
            }
            else
            {
                log.AddToDisplay.Display("Could not open file", DisplayMessageType.ERROR);
            }
	}
	
	private boolean openFile()
	{
            boolean flag = false;
            String path = settings.BASE_DIRECTORY 
                             + System.getProperty("file.separator")
                             + getFileName();

            File config_file = new File(path);
            Scanner scanner = null;
            try {
                scanner = new Scanner(config_file);
                flag = true;
            } catch (FileNotFoundException ex) {
                flag = false;
                Logger.getLogger(configuration.class.getName()).log(Level.SEVERE, null, ex);
                log.AddToDisplay.Display("File not found ... creating new file", DisplayMessageType.ERROR);
            }

            if(new utilities().createHumastarWorkList(settings.BASE_DIRECTORY, getFileName()))
            {
                flag = true;
            }
            return  flag;
	}
        
        private void getBLISTests(String aux_id, boolean flag){
        try {
            String data = BLIS.blis.getTestData(getSpecimenFilter(2), "",aux_id,system.settings.POOL_DAY);
            JSONParser parser = new JSONParser();
            JSONArray sampleList = (JSONArray) parser.parse(data);
            
            if(sampleList.isEmpty()){
                log.AddToDisplay.Display("No data found",DisplayMessageType.INFORMATION);
                return;
            }
            
            log.AddToDisplay.Display(sampleList.size()+" result(s) test found in BLIS!",DisplayMessageType.INFORMATION);
            generateWorklist(sampleList);
//                    if(sendTesttoAnalyzer(sampleList.get(i)))
//                    {
//                        addToQueue(sampleList.get(i));
                        log.AddToDisplay.Display("Test sent sucessfully",DisplayMessageType.INFORMATION);
//                    }
//                } 
//                else
//                {
//                    if(flag)                         
//                        log.AddToDisplay.Display("Sample with code: "+aux_id +" already exist in Analyzer",DisplayMessageType.INFORMATION);
//                }
//            }
//            else{
//                if(flag)                         
//                   log.AddToDisplay.Display("Sample with code: "+aux_id +" does not exist in BLIS",DisplayMessageType.INFORMATION);
//            }
        }catch(Exception ex){
                log.logger.PrintStackTrace(ex);
        }
    }
        
    private static String generateWorklist(JSONArray sampleList)
    {
        List<String> wrklst = new ArrayList<>();
        String hheader = "H|\\^&|||HSX00^V1.0|||||Host||P|1|20140117";
        wrklst.add(hheader);
        //Loops through test list
        for (int i=0; i < sampleList.size(); i++) 
        {
            JSONObject sample = (JSONObject)sampleList.get(i);
            JSONObject visit =  (JSONObject)sample.get("visit");
            JSONObject patient =  (JSONObject)visit.get("patient");
            
            
            JSONObject ttype =  (JSONObject)sample.get("test_type");
            JSONArray jarr =  (JSONArray)ttype.get("measures");
            
            String pdetails =  "P|1||2-FIDO|"+patient.get("name") +"|FIDO||20050000|MALE|||||||||||||||||||||||||";
            wrklst.add(pdetails);
            String mheader = "C|1|||";
            wrklst.add(mheader);

            for (int j=0; j < jarr.size(); j++) 
            {
                //Loop through measures
                JSONObject measure = (JSONObject)jarr.get(j);
                String mdetails = "O|1|2458||"+ measure.get("name") +"|False||||||||||Serum|||||||||||||||";
                wrklst.add(mdetails);
            }
            log.AddToDisplay.Display("Sending test with CODE: "+sample.get("id") + " to Analyzer Humastar 100",DisplayMessageType.INFORMATION);
        }
        String hfooter =  "L||N";
        wrklst.add(hfooter);
        
        //Write data to file
        writeToFile(wrklst);
        
        return "";
    }
        
        public static boolean sendTesttoAnalyzer(String data)
        {
            return true;
        }
        
        public static void writeToFile(List content)
        {
            String path = settings.BASE_DIRECTORY 
                             + System.getProperty("file.separator")
                             + getFileName();
            try {
                PrintWriter hworklist = new PrintWriter(path);
                
                // iterate over the array
                for( Object str : content ) {
                    hworklist.println(str);
                }
                hworklist.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Humastar100.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }
	
	public static void HandleDataInput(String data){
            String[] DataParts = data.trim().split(String.valueOf("\\"+settings.SEPERATOR_CHAR));
            //If first part is R means its a results string
            if (DataParts[0].equals("R")){
                if( DataParts.length > 1){
                    String methodName = DataParts[2];
                    String result = DataParts[8];
                    if(true){
                        //Get mesure id
                        //Get testid
                        getMeasureID("s");
                        String testId = "";
                        String MeasureID = "";
                        SaveResult(testId, MeasureID, result);
                        log.AddToDisplay.Display
                            ("Results with Code: "+ methodName +" and result "+result+" sent to BLIS sucessfully",DisplayMessageType.INFORMATION);
                    }
                    else{
                        log.AddToDisplay.Display
                            ("Test with Code: "+methodName +" not Found on BLIS ",DisplayMessageType.WARNING);
                    }
                }
            }	   
	}
	
	private void manageResults() 
	{
            if(shouldRead())
            {
                String path = settings.BASE_DIRECTORY 
                        + System.getProperty("file.separator")
                        + settings.OUTPUT_DIRECTORY 
                        + System.getProperty("file.separator")
                        + getFileName();         

                File in_file = new File(path);
                String line="";
                try {
                    in=new BufferedReader(new InputStreamReader(new FileInputStream(in_file)));
                    while((line = in.readLine()) != null){
                        HandleDataInput(line);
                    }
               } catch (FileNotFoundException ex) {
                       Logger.getLogger(BDFACSCalibur.class.getName()).log(Level.SEVERE, null, ex);
               } catch (IOException ex) {
                       Logger.getLogger(BDFACSCalibur.class.getName()).log(Level.SEVERE, null, ex);
               }    		
            }
	}
	
	private boolean shouldRead(){
            boolean flag = false;
//             String path = settings.BASE_DIRECTORY 
//                             + System.getProperty("file.separator")
//                             + getFileName();         
//
//            Path file = Paths.get(path);
//             try {           
//                BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
//                if(null == ReadTime || (attr.lastModifiedTime().compareTo(ReadTime) > 0))
//                {
//                        flag = true;
//
//                }            
//                else
//                {
//                        flag = false;
//                }
//             } catch (IOException ex) {
//                     Logger.getLogger(BDFACSCalibur.class.getName()).log(Level.SEVERE, null, ex);
//             }

            return true;
	}   
   
	
	public void Stop()
	{
	
		 log.AddToDisplay.Display("Stoping handler", log.DisplayMessageType.TITLE);
		 
		 stopped = true;           
		 this.interrupt();
		/*if(Manager.closeOpenedPort())
		{
			log.AddToDisplay.Display("Port Closed sucessfully", log.DisplayMessageType.INFORMATION);
		}*/
	}
	
    private void setTestIDs()
     {
             String equipmentid = getSpecimenFilter(3);
             String blismeasureid = getSpecimenFilter(4);

             String[] equipmentids = equipmentid.split(",");
             String[] blismeasureids = blismeasureid.split(",");
             for(int i=0;i<equipmentids.length;i++)
             {
                     testIDs.add(equipmentids[i]+";"+blismeasureids[i]);             
             }

     }

    private static String getSpecimenFilter(int whichdata)
    {
            String data = "";
            xmlparser p = new xmlparser("configs/BDFACSCalibur/bdfacscalibur.xml");
            try {
                    data = p.getMicros60Filter(whichdata);           
            } catch (Exception ex) {
                    Logger.getLogger(BDFACSCalibur.class.getName()).log(Level.SEVERE, null, ex);
            }        
            return data;        
    }

     private static int getMeasureID(String humastarMeasure)
     {
            int measureid = 0;
            JSONObject mappings = new utilities().loadJsonConfig();
            //Loop through all tests
            //
            JSONObject tests = (JSONObject)mappings.get("LFTS");
            JSONObject visit =  (JSONObject)mappings.get("visit");
             
             for(int i=0;i<testIDs.size();i++)
             {
                     if(testIDs.get(i).split(";")[0].equalsIgnoreCase(humastarMeasure))
                     {
                             measureid = Integer.parseInt(testIDs.get(i).split(";")[1]);
                             break;
                     }
             }
             return measureid;
     }

    private static boolean SaveResult(String testId, String MeasureID, String result)
    {


              boolean flag = false;       
              if("1".equals(BLIS.blis.saveResult(testId, MeasureID, result,0)))
               {
                      flag = true;
                    }

             return flag;

    }
}
