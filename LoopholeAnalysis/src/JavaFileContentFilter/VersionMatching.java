package JavaFileContentFilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionMatching {
	
	static String sourcePath = "";
	static String outputPath = "";
	static HashMap<String, String> ResultHm = new HashMap<String, String>();
	
	
	public static void main(String outDir,String InvalidName) {  
        // TODO Auto-generated method stub  
		int lineNum = 0;
		sourcePath = outDir + "selected_java.txt";
		outputPath = outDir + "Loophole_java.csv";
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
		String messages = "";
		try {
			fis = new FileInputStream(sourcePath);
			InputStreamReader isr =new InputStreamReader(fis,"GBK");
			BufferedReader br = new BufferedReader(isr);

			while((messages = br.readLine())!=null){    
				lineNum++;
				if(lineNum%100==0 && lineNum>99){
		    		System.out.print("。");
		    		if(lineNum%1000==0 && lineNum>999){
			    		System.out.println(lineNum);
			    	}
		    	}
	        	String[] messagesStrings = messages.split(",");
	        	
	        	if (messagesStrings.length>3) {
					String loopholeName = messagesStrings[0];
					String jarName = messagesStrings[1];
					String jarVersion = messagesStrings[2];

					String resultString = loopholeName + "," + jarName + "," + jarVersion + ",";
					for (int i = 3; i < messagesStrings.length; i++) {
						//包含出x以外的字母，进行版本号规范化
						String judgeStr = messagesStrings[i].replaceAll("x", "?");
						judgeStr = judgeStr.replaceAll("prior to", "before");
						judgeStr = judgeStr.replaceAll("_", ".");
						judgeStr = judgeStr.replaceAll("\\[", "");
						if (judgeContainsStr(judgeStr)) {
							resultString += "|" + loopholeVersionStandardization(judgeStr);
						}else {	//不包含除了x以外的字母，判定为规范版本号
							resultString += "|" + judgeStr;
						}
					}
					
					String[] resultParts = resultString.split(",");
					String[] loopholeversions = resultParts[3].split("\\|");
					String successString = "";
					for (int i = 1; i < loopholeversions.length; i++) {
						if (!loopholeversions[i].contains("#")) {
							if (loopholeversions[i].contains("~")) {
								//点与范围相比
								boolean success = pointCompareRange(jarVersion, loopholeversions[i]);
								if (success) {
									successString += "|" + loopholeversions[i];	//版本号比较成功，输出比较成功的版本号
								}
							}else {
								//点与点相比
								if (Compare(jarVersion,loopholeversions[i])==0) {
									successString += "|" + loopholeversions[i];	//版本号比较成功，输出比较成功的版本号
								}
							}							
						}
					}
					if (!successString.equals("")) {
//						writeData(outputPath,resultString + "," + successString.substring(1));
						String matchingResult = successString.substring(1);
						String key = loopholeName + "," + jarName;
						String loopholeVersion = resultString.substring(key.length()+jarVersion.length()+3);
						if (ResultHm.containsKey(key)) {
							resultMerge(key,jarVersion,loopholeVersion,matchingResult);
						}else {
							ResultHm.put(key, jarVersion + "," + loopholeVersion + "," + matchingResult);
						}
					}
					
				}
	        }  
			br.close();
	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int resultNum = 0;
		
		Iterator<Entry<String, String>> resultIterator = ResultHm.entrySet().iterator();
	    while(resultIterator.hasNext()){
	    	Entry<String, String> entry = resultIterator.next();
	    	String resultString = entry.getKey() + "," + entry.getValue();
	    	if (invalidResult(entry.getKey(),InvalidName)==true) {
				continue;
			}
	    	writeData(outputPath,resultString);
	    	resultNum++;
	    	if(resultNum%100==0 && resultNum>99){
        		System.out.print(".");
        		if(resultNum%1000==0 && resultNum>999){
            		System.out.println(resultNum);
            	}
        	}
	    }
	    System.out.println(resultNum + " lines of vulnerability information have been written to [" + outputPath.replace("\\\\", "\\")+"]");
//		System.out.println("  软件成分版本匹配完成！  ");
    }  
	

	
	//将漏洞版本规范化——统一为数字表示
	public static String loopholeVersionStandardization(String loopholeVersion){	
		String[] versionPart = loopholeVersion.split(" ");
		//版本描述为四部分以上的直接标记#，并返回
		if (versionPart.length>3 || loopholeVersion.contains("-")) {
			loopholeVersion = "#"+loopholeVersion+"#";
			return loopholeVersion.replaceAll("\\s*", "");
		}
		
		if (loopholeVersion.contains("through")) {
			String[] range = loopholeVersion.split("through");
			if (range.length==2) {
				loopholeVersion ="["+ range[0]+"~"+range[1]+"]";
			}else {
				loopholeVersion = "#"+loopholeVersion+"#";
			}
			return loopholeVersion.replaceAll("\\s*", "");
		}
		
		
		if (loopholeVersion.contains("before")) {
			String[] range = loopholeVersion.split("before");
			if (range.length == 1) {
				loopholeVersion = "(?"+"~"+range[0]+")";
			}else if (range.length == 2) {
				if (range[0].equals("")) {
					loopholeVersion = "(?"+"~"+range[1]+")";
				}else {
					loopholeVersion ="["+ range[0]+"~"+range[1]+")";
				}
			}else {
				loopholeVersion = "#"+loopholeVersion+"#";
			}
			return loopholeVersion.replaceAll("\\s*", "");
		}
		
		
		if (loopholeVersion.contains("and earlier")) {
			String[] range = loopholeVersion.split(" and earlier");
			if (range.length==1) {
				loopholeVersion = "(?"+"~"+range[0] + "]";
			}else {
				loopholeVersion = "#"+loopholeVersion+"#";
			}
			return loopholeVersion.replaceAll("\\s*", "");
		}
		
		if (loopholeVersion.contains("and prior")) {
			String[] range = loopholeVersion.split(" and prior");
			if (range.length==1) {
				loopholeVersion = "(?"+"~"+range[0] + "]";
			}else {
				loopholeVersion = "#"+loopholeVersion+"#";
			}
			return loopholeVersion.replaceAll("\\s*", "");
		}
		
		if (loopholeVersion.contains(" and ")) {
			String[] range = loopholeVersion.split(" and ");
			loopholeVersion = range[0] + "|" + range[1];
			return loopholeVersion.replaceAll("\\s*", "");
		}
		
		if (loopholeVersion.contains(" or later")) {
			String[] range = loopholeVersion.split(" or later");
			loopholeVersion ="["+ range[0] + "~?)";
			return loopholeVersion.replaceAll("\\s*", "");
		}
		
		if (loopholeVersion.contains(" to ")) {
			String[] range = loopholeVersion.split(" to ");
			loopholeVersion = "["+ range[0] + "~" + range[1] + "]";
			return loopholeVersion.replaceAll("\\s*", "");
		}
		
		loopholeVersion = "#" + loopholeVersion + "#";
		return loopholeVersion.replaceAll("\\s*", "");
	}
	
	
	//将漏洞版本与软件版本相比较，返回比较结果
	public static boolean pointCompareRange(String jarVersion,String loopholeVersion){		
		String[] parts = loopholeVersion.split("~");
		String min = parts[0].substring(1);
		String max = parts[1].substring(0,parts[1].length()-1);
		
		int minResult = CompareMin(jarVersion, min);
		int maxResult = CompareMax(jarVersion, max);
		
		if (minResult == 1 && maxResult == -1) {
			return true;
		}
		if (minResult == 0 && parts[0].contains("[")) {
			return true;
		}
		if (maxResult == 0 && parts[1].contains("]")) {
			return true;
		}
		return false;
	}
	
	//比较结果，版本号软件jar版本与标准版本相比，相同0，大于1，小于0
	//jarVersion = Version ——→   0
	//jarVersion > Version ——→   1
	//jarVersion < Version ——→   -1
	//jarVersion 和 Version格式不统一    ——→   2 
	public static int Compare(String jarVersion,String Version) {
		if (jarVersion.equals(Version)) {
			return 0;
		}
		
		String[] jarParts = jarVersion.split("\\.");
		String[] versionParts = Version.split("\\.");
		int partLength = 0;
		if (jarParts.length == versionParts.length) {
			partLength = jarParts.length;
		}else {
			return 2;
		}
		
		String strJar = "";
		String strVer = "";
		for (int i = 0; i < partLength; i++) {
			if (jarParts[i].length() == versionParts[i].length()) {
				strJar += jarParts[i];
				strVer += versionParts[i];
			}else {
				return 2;
			}
		}
		
		Long jarNum = Long.parseLong(strJar);
		if (strVer.contains("?")) {
			strVer = strVer.replaceAll("\\?", "0");
		}
		Long verNum = Long.parseLong(strVer);
		
		if (jarNum == verNum) {
			return 0;
		}else if (jarNum > verNum) {
			return 1;
		}else {
			return -1;
		}
		
	}
	
	
	public static int CompareMin(String jarVersion,String min) {
		if (jarVersion.equals(min)) {
			return 0;
		}
		
		if (min.equals("?")) {
			return 1;
		}
		
		String[] jarParts = jarVersion.split("\\.");
		String[] minParts = min.split("\\.");
				
		int partLength = 0;
		if (jarParts.length == minParts.length) {
			partLength = jarParts.length;
		}else {
			return 2;
		}
		
		String strJar = "";
		String strMin = "";
		for (int i = 0; i < partLength; i++) {
			if (jarParts[i].length() == minParts[i].length()) {
				strJar += jarParts[i];
				strMin += minParts[i];
			}else {
				return 2;
			}
		}
		
		Long jarNum = Long.parseLong(strJar);
		if (strMin.contains("?")) {
			strMin =  strMin.replaceAll("\\?", "0");
		}
		Long minNum = Long.parseLong(strMin);
		
		if (jarNum == minNum) {
			return 0;
		}else if (jarNum > minNum) {
			return 1;
		}else {
			return -1;
		}
		
	}
	
	
	public static int CompareMax(String jarVersion,String max) {
		if (jarVersion.equals(max)) {
			return 0;
		}
		
		String[] jarParts = jarVersion.split("\\.");
		String[] maxParts = max.split("\\.");
		
		int partLength = 0;
		if (jarParts.length == maxParts.length) {
			partLength = jarParts.length;
		}else {
			return 2;
		}
		
		String strJar = "";
		String strMax = "";
		for (int i = 0; i < partLength; i++) {
			if (jarParts[i].length() == maxParts[i].length()) {
				strJar += jarParts[i];
				strMax += maxParts[i];
			}else {
				return 2;
			}
		}
		
//		System.out.println(jarVersion +"  "+ max +"|"+ strJar +"  "+ strMax);
		
		Long jarNum = Long.parseLong(strJar);
		if (strMax.contains("?")) {
//			System.out.println(jarVersion +"|"+ max +"|");
			strMax = strMax.replaceAll("\\?", "9");
//			System.out.println(jarNum + "|" + Long.parseLong(strMax));
		}
		Long maxNum = Long.parseLong(strMax);
		
		
		if (jarNum == maxNum) {
			return 0;
		}else if (jarNum > maxNum) {
			return 1;
		}else {
			return -1;
		}
		
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
			

	/**  
     * 使用正则表达式来判断字符串中是否包含字母  
     * @param str 待检验的字符串 
     * @return 返回是否包含  
     * true: 包含字母 ;false 不包含字母
     */  
    public static boolean judgeContainsStr(String str) {  
        String regex=".*[a-zA-Z]+.*";  
        Matcher m=Pattern.compile(regex).matcher(str);  
        return m.matches();  
    }  
	
    
    public static void resultMerge(String key,String jarVersion,String loopholeVersion,String matchingResult){
		String oldString = ResultHm.get(key);
		String[] oldStrings = oldString.split(",");
		jarVersion = oldStrings[0]+"|"+jarVersion;
		loopholeVersion = oldStrings[1]+"|"+loopholeVersion;
		matchingResult = oldStrings[1]+"|"+matchingResult;
		ResultHm.put(key, jarVersion + "," + loopholeVersion + "," + matchingResult);
		
	}
	
    
    //是否包含要删去的筛选结果
    public static boolean invalidResult(String jarName,String InvalidName){
    	if (InvalidName.equals("")) {
			return false;
		}
    	String[] InvalidNames = InvalidName.split(",");
    	for (String i:InvalidNames) {
    		if (jarName.contains(i)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    
}
