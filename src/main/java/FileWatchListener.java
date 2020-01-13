import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wtwei .
 * @date 2020/1/9 .
 * @time 14:23 .
 */

public class FileWatchListener implements FileAlterationListener {
    private Map<String, String> dbFileMap = new HashMap<String, String>();

    public FileWatchListener(){
        String lineStr = null;
        try {
            File file = new File("E:\\01_Code\\CredWorkSpace\\duoduo\\src\\main\\resources\\fileDB.txt");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            while ((lineStr = bufferedReader.readLine()) != null) {
                if (StringUtils.isNotEmpty(lineStr) && lineStr.indexOf("#") != -1){
                    try{
                        String[] split = lineStr.split("#");
                        dbFileMap.put(split[0], split[1]);
                    }catch (Exception e){

                    }
                }
            }
            System.err.println("多多果园题库初始化完成，字典数量：" + dbFileMap.size());
        }catch(Exception e){
            System.out.println(lineStr);
            e.printStackTrace();
        }
    }
    
    @Override
    public void onStart(FileAlterationObserver fileAlterationObserver) {
        
    }

    @Override
    public void onDirectoryCreate(File file) {

    }

    @Override
    public void onDirectoryChange(File file) {

    }

    @Override
    public void onDirectoryDelete(File file) {

    }

    @Override
    public void onFileCreate(File file) {

    }

    @Override
    public void onFileChange(File file) {
        duoduoFileProcess(file);
    }

    @Override
    public void onFileDelete(File file) {

    }

    @Override
    public void onStop(FileAlterationObserver fileAlterationObserver) {

    }
    
    private boolean findInDBFile(String question){
        char[] questionChars = question.toCharArray();
        
        for (String source : dbFileMap.keySet()) {
            int eqCount = 0;
            int sLength = source.length();
            for (char questionChar : questionChars) {
                if (source.indexOf(questionChar) != -1){
                    eqCount++;
                }
            }
            
            if (eqCount / sLength > 0.7){
                System.err.println("-----在字典中找到问题-----");
                System.out.println("问题：" + source + "     答案：" + dbFileMap.get(source));
                return true;
            }
        }
        return false;
    }

    private void duoduoFileProcess(File file) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file),"GB2312"));

            StringBuffer fileBuffer = new StringBuffer();

            String fileContext = null;
            while ((fileContext = bufferedReader.readLine()) != null){
                fileBuffer.append(fileContext);
            }

            String messageStr = fileBuffer.toString().replaceAll("\t", "");
            if (StringUtils.isEmpty(messageStr)){
                return;
            }

            JSONObject jsonObject = JSON.parseObject(messageStr);
            if (jsonObject.getString("payload").equals("0") ){
                return;
            }
            
            if (jsonObject.getInteger("reserve") != 0){
                return;
            }

            String payloadBodyStr = jsonObject.getString("payload");

            JSONObject payloadBody = JSON.parseObject(payloadBodyStr);
            int actionId = payloadBody.getInteger("action_id");
            if (actionId != 7){
                return;
            }

            JSONObject payload = payloadBody.getJSONObject("payload");
            String questionStr = payload.getString("question");
            
            if (StringUtils.isEmpty(questionStr)){
                System.out.println("答题结束！！！！");
                return;
            }

            JSONArray optionsArray = payload.getJSONArray("options");
            List options = new ArrayList();
            for (Object o : optionsArray) {
                JSONObject op = (JSONObject) o;
                String opString = op.getString("item");
                options.add(opString);
            }

            System.out.println("#############################我是分割线##############################");
            System.out.println("问题： " + questionStr + "  ->>  " + JSONObject.toJSONString(options));
            if (!findInDBFile(questionStr)){
                SearchAnswer.search(questionStr, options);
            }
            System.out.println();
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
