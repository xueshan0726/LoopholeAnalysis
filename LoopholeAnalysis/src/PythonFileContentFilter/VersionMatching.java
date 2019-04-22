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
		sourcePath = outDir + "selected_python.txt";
		outputPath = outDir + "Loophole_python.csv";
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
//				System.out.println(lineNum);//输出检测的行号
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
					String loopholeVersion = "";
					//将漏洞中的版本号依次规范化
					for (int i = 3; i < messagesStrings.length; i++) {
						//包含出x以外的字母，进行版本号规范化
						String judgeStr = messagesStrings[i].replaceAll("x", "?");
						judgeStr = judgeStr.replaceAll("prior to", "before");
						judgeStr = judgeStr.replaceAll("_", ".");
						judgeStr = judgeStr.replaceAll("\\[", "");
						if (judgeContainsStr(judgeStr)) {
							loopholeVersion += "|" + loopholeVersionStandardization(judgeStr);
						}else {	//不包含除了x以外的字母，判定为规范版本号
							loopholeVersion += "|" + judgeStr;
						}
					}
					if (!loopholeVersion.equals("")) {
						loopholeVersion = loopholeVersion.substring(1);//已经标准化的 CVE中的软件版本号
					}
					
					String matchingResult = "";
					String[]  jarVersions = jarVersion.split("\\|");
					String[]  cveVersions = loopholeVersion.split("\\|");
					for (String i:jarVersions) {
//						System.out.println(i);
						for (String j:cveVersions) {
							if (!j.contains("#")) {//如果为##，无效版本，略过
								//jarVersions为范围
								if (i.contains("~")) {
									if (j.contains("~")) {//cveVersions为范围
										//范围 与 范围  相比较
										boolean success = rangeCompareRange(i,j);
										if (success) {
											matchingResult += "|" + j;
										}
									}else {
										//范围 与 点  相比较
										boolean success = rangeComparePoint(j, i);
										if (success) {
											matchingResult += "|" + j;
										}
									}
								}
								//jarVersions为点
								else if (!i.contains("!=")) {
									if (j.contains("~")) {//cveVersions为范围
										//点 与 范围  相比较
										boolean success = pointCompareRange(i, j);
										if (success) {
											matchingResult += "|" + j;
										}
									}else {
										//点 与 点  相比较
										if (versionCompare(i, j) == 0) {
											matchingResult += "|" + j;
										}
									}
								}
								
							}
						}
					}
					if (!matchingResult.equals("")) {
						matchingResult = matchingResult.substring(1);
						String key = loopholeName + "," + jarName;
						if (ResultHm.containsKey(key)) {
							resultMerge(key,jarVersion,loopholeVersion,matchingResult);
						}else {
							ResultHm.put(key, jarVersion + "," + loopholeVersion + "," + matchingResult);
						}
					}
				}else {
//		        	System.out.println(lineNum + "  " +messages);//输出有问题无法检测的行号 + 内容
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
	
	//将漏洞版本与软件版本相比较，返回比较结果,版本范围与固定版本比较
	public static boolean rangeComparePoint(String jarVersion,String loopholeVersion){
		boolean result = pointCompareRange(loopholeVersion , jarVersion);
		return result;
	}
	
	
	//将漏洞版本与软件版本相比较，返回比较结果,固定版本与版本范围比较
	public static boolean pointCompareRange(String jarVersion,String loopholeVersion){		
		
		if (loopholeVersion.contains("~")) {
			String[] parts = loopholeVersion.split("~");
			String min = parts[0].substring(1);
			String max = parts[1].substring(0,parts[1].length()-1);
			
			if (CompareMin(jarVersion, min) == 1 && CompareMax(jarVersion, max) == -1) {
				return true;
			}
			
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
		
//		System.out.println(jarVersion +"  "+ min +"|"+ strJar +"  "+ strMin);
		
		Long jarNum = Long.parseLong(strJar);
		if (strMin.contains("?")) {
//			System.out.println(jarVersion +"|"+ min +"|");
			strMin =  strMin.replaceAll("\\?", "0");
//			System.out.print(jarNum + "|" + Long.parseLong(strMin));
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
		
		Long jarNum = Long.parseLong(strJar);
		if (strMax.contains("?")) {
			strMax = strMax.replaceAll("\\?", "9");
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
	
    //将漏洞版本与软件版本相比较，返回比较结果,范围与范围比较
  	public static boolean rangeCompareRange(String jarVersion,String loopholeVersion){		
  		
  		String jarStart = jarVersion.substring(1,jarVersion.indexOf("~"));
  		String jarEnd = jarVersion.substring(jarVersion.indexOf("~")+1,jarVersion.length()-1);
  		String cveStart = loopholeVersion.substring(1,loopholeVersion.indexOf("~"));
  		String cveEnd = loopholeVersion.substring(loopholeVersion.indexOf("~")+1,loopholeVersion.length()-1);
  		if (jarStart.contains("?")) {
  			jarStart = jarEnd.replaceAll("\\d", "0");
		}
  		if (jarEnd.contains("?")) {
  			jarEnd = jarStart.replaceAll("\\d", "0");
		}
  		if (cveStart.contains("?")) {
  			cveStart = cveEnd.replaceAll("\\d", "0");
		}
  		if (cveEnd.contains("?")) {
  			cveEnd = cveStart.replaceAll("\\d", "0");
		}

  		
  		if (versionCompare(jarEnd,cveStart) == -1) {
			return  false;
		}
  		if (versionCompare(jarEnd,cveStart) == 0) {
  			String jarEndContain = jarVersion.substring(jarVersion.length()-1);
  			String cveStartContain = loopholeVersion.substring(0,1);
  			if (jarEndContain.equals("]") && cveStartContain.equals("[")) {
  				return true;
			}
			return  false;
		}
  		if (versionCompare(jarEnd,cveStart) == 1) {
			if (versionCompare(jarStart,cveEnd) == -1) {
				return true;
			}
			if (versionCompare(jarStart,cveEnd) == 0) {
				String jarStartContain = jarVersion.substring(0,1);
	  			String cveEndContain = loopholeVersion.substring(jarVersion.length()-1);
	  			if (jarStartContain.equals("]") && cveEndContain.equals("[")) {
	  				return true;
				}
				return  false;
			}
			return false;
		}
  		return false;
  	}
  	
	
	
	//比较结果，版本号v1与v2相比，相同返回0，大于返回1，小于返回0，格式不同返回2
	public static int versionCompare(String version1,String version2) {
		version1 = version1.replaceAll("\\?", "");
		version1 = version1.replaceAll("\\s*", "");
		version2 = version2.replaceAll("\\?", "");
		version2 = version2.replaceAll("\\s*", "");
		
		if (version1.equals(version2)) {
			return 0;
		}
		
		String[] version1s = version1.split("\\.");
		String[] version2s = version2.split("\\.");
		int totalLen1 = version1s.length;
		int totalLen2 = version2s.length;
		
		//9和9.0相比，判断结果为不相同
		if (totalLen2 != totalLen1){
			return 2;
		}
		
		String strV1 = "";
		String strV2 = "";
		
		//9和9.0相比，判断结果为相同
//		if (totalLen1 > totalLen2) {
//			for (int i = 0; i < totalLen1; i++) {
//				int len1 = version1s[i].length();
//				int len2 = 0;
//				String tmpV1 = version1s[i];
//				String tmpV2 = "";
//				if (i < totalLen2 ) {
//					len2 = version2s[i].length();
//					tmpV2 = version2s[i];
//				}
//				if (len1 == len2) {
//					strV1 += tmpV1;
//					strV2 += tmpV2;
//				}else if (len1 > len2) {
//					strV1 += tmpV1;
//					for (int j = len2; j < len1; j++) {
//						tmpV2 = "0" + tmpV2;
//					}
//					strV2 += tmpV2;
//				}else {
//					strV2 += tmpV2;
//					for (int j = len1; j < len2; j++) {
//						tmpV1 = "0" + tmpV1;
//					}
//					strV1 += tmpV1;
//				}
//			}
//		}
//		
//		
//		if (totalLen2 > totalLen1) {
//			for (int i = 0; i < totalLen2; i++) {
//				int len2 = version2s[i].length();
//				int len1 = 0;
//				String tmpV2 = version2s[i];
//				String tmpV1 = "";
//				if (i < totalLen1 ) {
//					len1 = version1s[i].length();
//					tmpV1 = version1s[i];
//				}
//				if (len2 == len1) {
//					strV2 += tmpV2;
//					strV1 += tmpV1;
//				}else if (len2 > len1) {
//					strV2 += tmpV2;
//					for (int j = len1; j < len2; j++) {
//						tmpV1 = "0" + tmpV1;
//					}
//					strV1 += tmpV1;
//				}else {
//					strV1 += tmpV1;
//					for (int j = len2; j < len1; j++) {
//						tmpV2 = "0" + tmpV2;
//					}
//					strV2 += tmpV2;
//				}
//			}
//		}
		
		if (totalLen2 == totalLen1) {
			for (int i = 0; i < totalLen2; i++) {
				int len2 = version2s[i].length();
				int len1 = version1s[i].length();;
				String tmpV2 = version2s[i];
				String tmpV1 = version1s[i];
				
				if (len2 == len1) {
					strV2 += tmpV2;
					strV1 += tmpV1;
				}else if (len2 > len1) {
					strV2 += tmpV2;
					for (int j = len1; j < len2; j++) {
						tmpV1 = "0" + tmpV1;
					}
					strV1 += tmpV1;
				}else {
					strV1 += tmpV1;
					for (int j = len2; j < len1; j++) {
						tmpV2 = "0" + tmpV2;
					}
					strV2 += tmpV2;
				}
			}
		}
		
		
		Long v1Num = Long.parseLong(strV1);
		Long v2Num = Long.parseLong(strV2);
		
		if (v1Num == v2Num) {
			return 0;
		}else if (v1Num > v2Num) {
			return 1;
		}else {
			return -1;
		}
		
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
