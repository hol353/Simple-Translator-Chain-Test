/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java;

/**
 *
 * @author Dean Holzworth
 */


import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.zip.ZipInputStream;
import org.agmip.core.types.TranslatorOutput;
import org.agmip.translators.apsim.ApsimOutput;
import org.agmip.translators.dssat.DssatControllerInput;
import org.agmip.util.JSONAdapter;
import org.agmip.util.MapUtil;

public class Test {

    /**
     * MAIN program
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // Convert DSSAT files to JSON
        DssatControllerInput dssatTranslator = new DssatControllerInput();
        String filePath = "src/main/resources/UFGA7801_SBX.ZIP";
        String agmipJson = runDssatTranslator(dssatTranslator, filePath);
        
        // Override the AgMIP JSon and get an up-to-date json string.
        agmipJson = new Scanner(new File("./simulation.json"), "UTF-8").useDelimiter("\\A").next();
            
        // Convert JSON string to APSIM format
        TranslatorOutput apsimTranslator = new ApsimOutput();
        ZipInputStream zip = runOutputTranslator(apsimTranslator, agmipJson);
        
        // Zip file now contains all APSIM files.
    }
        
    
    
    
    
    /**
      * This method hides the ArrayList<LinkedHashMap> interface of the DSSAT translator.
     * @param translator - the translator to run
     * @param filePath - the filePath of the DSSAT .zip file
     */
    public static String runDssatTranslator(DssatControllerInput translator, String filePath) throws IOException { 
        ArrayList<LinkedHashMap> dssatMap = (ArrayList<LinkedHashMap>) translator.readFiles(filePath);
        return JSONAdapter.toJSON(dssatMap); 
    }

    
    
     /**
      * This method hides the messy interface of a translator.
     * @param translator - the translator to run
     * @param json - the json string to convert
     */
    public static ZipInputStream runOutputTranslator(TranslatorOutput translator, String json) throws IOException {
        LinkedHashMap<String, Object> compressedMap = JSONAdapter.fromJSON(json);
        LinkedHashMap<String, Object> uncompressedMap = MapUtil.decompressAll(compressedMap);

        TranslatorOutput apsimConverter = new ApsimOutput();
        apsimConverter.writeFile("./", uncompressedMap);

        // Unzip files
        File dir = new File("./");

        // Filter for .zip files.
        FilenameFilter filter = new FilenameFilter() {
        @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".zip");
            }
        };        


        String[] children = dir.list(filter);
        if (children == null) {
            // Either dir does not exist or is not a directory
        } else if (children.length == 1) {
            File inFile = new File(children[0]);
            FileInputStream inStream = new FileInputStream(inFile);
            BufferedInputStream in = new BufferedInputStream(inStream);
            return  new ZipInputStream(in);
        }
        return null;
    }

}


