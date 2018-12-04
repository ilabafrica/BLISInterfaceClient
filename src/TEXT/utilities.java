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
package TEXT;

import configuration.configuration;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import log.DisplayMessageType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Stephen Adjei-Kyei <stephen.adjei.kyei@gmail.com>
 */
public class utilities {
    
    public String getFileName(String Format,String extension)
    {
       String name = "";
       if(!Format.contains("*"))
            name = system.utilities.getSystemDate(Format);
        if(null == extension || extension.isEmpty()) 
            return name;
        else
            return name +"."+extension;        
    }
    
    public String getFileName(String filename, String Format, String extension)
    {
        String name = "";
        if (filename != null){
            if(!Format.contains("*")){
                name =  filename + "-" + system.utilities.getSystemDate(Format);
            }
        }
        else {
            if(!Format.contains("*")){
                name =  system.utilities.getSystemDate(Format);
            }
        }
        if(extension ==  null || extension.isEmpty()) 
            return name;
        else
            return name +"."+extension;        
    }
    
    public Boolean createHumastarWorkList(String path, String filename )
    {
        String fullname = path + System.getProperty("file.separator") + filename;
        try{
            File f = new File(fullname);
            f.createNewFile();
        } catch (IOException e) {
            log.AddToDisplay.Display("Failed to create file!", DisplayMessageType.ERROR);
            Logger.getLogger(configuration.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
        return true;
    }
    
    public JSONObject loadJsonConfig()
    {
        String path = "configs/Humastar/HumastarMappings.json";
        JSONObject jsonObj = new JSONObject();
        JSONParser parser = new JSONParser();
        try {
            jsonObj = (JSONObject) parser.parse(new FileReader(path));
            return jsonObj;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(utilities.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(utilities.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(utilities.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jsonObj;
    }
    
}
