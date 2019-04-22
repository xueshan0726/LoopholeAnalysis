package PythonFileContentFilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class SupplyContent {

	static String sourcePath = "";
	static String outputPath = "";
	static String describeFilePath = "";
	static String scoreFilePath = "";
	
	static HashMap<String, String> describeHm = new HashMap<String, String>();
	static HashMap<String, String> scoreHm = new HashMap<String, String>();
	
	public static void main(String outDir , String describePath , String scorePath) {  
        // TODO Auto-generated method stub  
		sourcePath = outDir + "Loophole_python.csv";
		outputPath = outDir + "FinalResult_python.xlsx";
		describeFilePath = describePath;
		scoreFilePath = scorePath;

    	//输出文件检查
        File outputFileOk = new File(outputPath);
        if (outputFileOk.exists()){
        	outputFileOk.delete();
        }
        try {
        	outputFileOk.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		FileInputStream fis;
		
		//定义CVE描述hashMap
		String describe = "";
		try {
			fis = new FileInputStream(describeFilePath);
			InputStreamReader isr =new InputStreamReader(fis,"GBK");
			BufferedReader br = new BufferedReader(isr);
			while((describe = br.readLine())!=null){   
				String describeTmp = describe.replaceAll("[\\t\\n\\r]", " ");
				int index = describeTmp.indexOf(" ");
				String keyString = describe.substring(0,index);
				describeHm.put(keyString, describe.substring(index+1));
	        }
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//定义CVE描述hashMap
		String score = "";
		try {
			fis = new FileInputStream(scoreFilePath);
			InputStreamReader isr =new InputStreamReader(fis,"GBK");
			BufferedReader br = new BufferedReader(isr);
			while((score = br.readLine())!=null){   
				String scoreTmp = score.replaceAll("[\\t\\n\\r]", " ");
				int index = scoreTmp.indexOf(" ");
				String keyString = score.substring(0,index);
				scoreHm.put(keyString, score.substring(index+1));
	        }
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
				
				
		int addNum = 0;
		try {
			fis = new FileInputStream(sourcePath);
			InputStreamReader isr =new InputStreamReader(fis,"GBK");
			BufferedReader br = new BufferedReader(isr);
			String result = "";
			while((result = br.readLine())!=null){
				String[] results = result.split(",");
				String cveNum = results[0];
				result = result.replaceAll(",", "\t");
				
				result += describeHm.containsKey(cveNum)?"\t" + describeHm.get(cveNum):"\tLack of description";
				result += scoreHm.containsKey(cveNum)?"\t" + scoreHm.get(cveNum):"\tLack of score\tLack of score";
				addNum++;
				writeData(outputPath, result);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(addNum + " lines of vulnerability score have been written to [" + outputPath.replace("\\\\", "\\")+"]");
		
		
    }  
	
	//将结果写入文件 
	public static void writeData(String filePath,String content){		
		try {    
		    File newFile = new File(filePath); // CSV数据文件  
		    BufferedWriter bw = new BufferedWriter(new FileWriter(newFile, true)); // 附加   
		    // 添加新的数据行 
		    bw.write(content);
		    bw.newLine();    
		    bw.close();    
		   
		    }catch (FileNotFoundException e) {    
		      // File对象的创建过程中的异常捕获   
		    	e.printStackTrace();    
		    }catch (IOException e) {    
		      // BufferedWriter在关闭对象捕捉异常   
		    	e.printStackTrace();    
		    }    
		}

}
