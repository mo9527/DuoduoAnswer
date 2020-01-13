import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author wtwei .
 * @date 2020/1/8 .
 * @time 14:37 .
 */
public class ProcessFile {
    private static String filePath = "E:\\01_Code\\CredWorkSpace\\duoduo\\src\\main\\resources\\result.txt";
    static Map<String, String> questionMap = new HashMap<String, String>();
    
    public static void main(String[] args) throws Exception {
        ProcessFile processFile = new ProcessFile();
        processFile.initDBFile();
        
    }
    
    private void initDBFile() throws Exception {
        Pattern pattern = Pattern.compile("\\d+[\\.]");
        File file = new File(filePath);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String lineStr = null;
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("fileDB.txt"), true));
        while ((lineStr = bufferedReader.readLine()) != null){
            String[] tmpStr = pattern.split(lineStr);
            for (String s : tmpStr) {
                if (StringUtils.isEmpty(s)){
                    continue;
                }
                String tmpLine = s.substring(1).trim().replaceAll("”", "").replaceAll("“", "").replaceAll("？", "#");
                bw.write(tmpLine + "\n");
                
//                if (!StringUtils.isEmpty(s)){
//                    String tmpLine = s.substring(1).trim().replaceAll("”", "").replaceAll("“", "");
//                    String[] tmpSplit = tmpLine.split("？");
//                    questionMap.put(tmpSplit[0], tmpSplit[1]);
//                }
            }
        }
        
        
    }
    
    
}
