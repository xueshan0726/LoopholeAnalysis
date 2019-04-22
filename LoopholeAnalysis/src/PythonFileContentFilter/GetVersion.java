package PythonFileContentFilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class GetVersion {


	static HashMap<String, String> VersionOK = new HashMap<String, String>();	
	static String sourceFilePath = "";
	static String outputPathOk = "";
	
	public static void main(String outDir) {  
        // TODO Auto-generated method stub  
		int requirementsNum = 0;
		sourceFilePath = outDir + "requirements.txt_sourcePath.csv";
		outputPathOk = outDir + "Python_VersionOk.csv";
		
    	//输出文件检查
        File outputFileOk = new File(outputPathOk);
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
		String filePath = "";
		try {
			fis = new FileInputStream(sourceFilePath);
			InputStreamReader isr =new InputStreamReader(fis,"GBK");
			BufferedReader br = new BufferedReader(isr);
			
			System.out.println("Start filtering the requirements.txt file ... ");
			
			while((filePath = br.readLine())!=null){    
				requirementsNum++;
	        	if(requirementsNum%100==0 && requirementsNum>99){
	        		System.out.println(requirementsNum);
	        	}
	        	
	        	if (filePath.length() > 4) {
		        	versionFilter(filePath);
				}else {
//					System.out.println(filePath.length() + "  ___  " + requirementsNum + " 行requirementsNum.txt文件路径为空！;");
				}
	        }  
			System.out.println(requirementsNum + " requirements.txt files are filtered !");
			br.close();
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//对VersionOK进行处理(去重、去不合规、整合)
		deleteRepetition(VersionOK);
		
		int okNum = 0;
		//开始输出Version
		Iterator<Entry<String, String>> iteratorOk = VersionOK.entrySet().iterator();
	    while(iteratorOk.hasNext()){
	    	Entry<String, String> entry = iteratorOk.next();
	    	
	    	String resultString = entry.getKey() + "," + entry.getValue();
	    	writeData(outputPathOk,resultString);
	    	okNum++;
	    	if(okNum%100==0 && okNum>99){
        		System.out.print(".");
        		if(okNum%1000==0 && okNum>999){
            		System.out.println(okNum);
            	}
        	}
	    }
	    System.out.println(okNum + " lines of version information have been written to [ "+outputPathOk.replace("\\\\", "\\"));

    }  
	

	//筛选软件名和版本号
	private static void versionFilter(String filePath) {
		FileInputStream fis;
		try {
			fis = new FileInputStream(filePath);
			InputStreamReader isr =new InputStreamReader(fis,"GBK");
			BufferedReader br = new BufferedReader(isr);
			
//			System.out.println("  开始对requirements.txt文件进行筛选 , 已筛选 ");
			int lineNum = 0;
			String versionContent = "";
			while((versionContent = br.readLine())!=null){    
				lineNum++;
	        	versionStandard(versionContent,filePath,lineNum);
	        }  
//			System.out.println(lineNum + " 行依赖筛选完成  " + filePath);
			br.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		        
	}
	
	
	//将每行依赖包及软件版本标准化处理
	public static void versionStandard(String versionContent,String filePath,int lineNum){
		String okKey = "";
		versionContent = versionContent.replaceAll("\\s*", "");//把所有空格、换行等删去
		if (!versionContent.equals("") && !versionContent.substring(0,1).equals("#") && isValid(versionContent) == true) {
			
			String[] contents = versionContent.split("#");
			if (contents.length == 2) {
				String namePart = getName(contents[0]);
				String versionPart = contents[0].substring(namePart.length());
				if (!versionPart.contains(",")) {
					versionPart = getVersion(versionPart);
					okKey = contents[1] + "," + namePart + "," + versionPart;
					hashmapAdd(VersionOK,okKey, filePath);
					
				}else {
					String[] versionParts = versionPart.split(",");
					versionPart = "";//清空对象
					for (String i:versionParts) {
						if (i.contains("!=")) {
							versionPart += "|" + i;
						}else {
							versionPart += "|" + getVersion(i);
						}
					}
					okKey = contents[1] + "," + namePart + "," + versionPart.substring(1);
					hashmapAdd(VersionOK,okKey, filePath);
					
				}
				
			}
			
			if (contents.length == 1) {
				String namePart = getName(contents[0]);
				String versionPart = contents[0].substring(namePart.length());
				if (!versionPart.contains(",")) {
					versionPart = getVersion(versionPart);

					okKey ="," + namePart + "," + versionPart;
					hashmapAdd(VersionOK,okKey, filePath);
				}else {
					
					String[] versionParts = versionPart.split(",");
					versionPart = "";//清空对象
					for (String i:versionParts) {
						if (i.contains("!=")) {
							versionPart += "|" + i;
						}else {
							versionPart += "|" + getVersion(i);
						}
					}
					okKey ="," + namePart + "," + versionPart.substring(1);
					hashmapAdd(VersionOK,okKey, filePath);
				}
			}
			
			
		}else {
			//输出版本信息筛选失败的信息
//			System.out.println(versionContent);
//			VersionProblem.put(filePath + "," + versionContent, "" );
		}
		
		
	}
	
	//截取名称
	public static String getName(String versionContent){
		String nameString = versionContent;
		int[] indexs={versionContent.indexOf(">"),versionContent.indexOf("<"),versionContent.indexOf("!"),versionContent.indexOf("=")};   
        Arrays.sort(indexs);  //进行排序
		for (int i : indexs) {
			if (i>0) {
				nameString = versionContent.substring(0,i);
				return nameString;
			}
		}
		return null;
	}
	
	//截取版本信息
	public static String getVersion(String versionPart){
		String versionString = "";
		String tmp = "";
		tmp = versionPart.replaceAll("\\d", "@");
		String compare = versionPart.substring(0,tmp.indexOf("@"));
		
		if (compare.equals("==")||compare.equals("=")) {
			versionString = versionPart.substring(tmp.indexOf("@"));
			return versionString;
		}
		if (compare.equals(">")) {
//			versionString = "(" + versionPart.substring(tmp.indexOf("@")) + "~?)";
//			versionString = "#" + versionPart + "#";
			return versionString;
		}
		if (compare.equals(">=")) {
//			versionString = "[" + versionPart.substring(tmp.indexOf("@")) + "~?)";
//			versionString = "#" + versionPart + "#|" + versionPart.substring(tmp.indexOf("@"));
			versionString =  versionPart.substring(tmp.indexOf("@"));
			return versionString;
		}
		if (compare.equals("<")) {
			versionString = "(?~" + versionPart.substring(tmp.indexOf("@")) +")";
			return versionString;
		}
		if (compare.equals("<=")) {
			versionString = "(?~" + versionPart.substring(2) + "]";
			return versionString;
		}
		if (compare.equals("!=")) {
			versionString = "#" + versionString + "#";
			return versionString;
		}
		versionString = "#" + versionString + "#";
		return versionString;
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
	
	
	//判断该行是否包含有版本信息
	public static boolean isValid(String content){
		if (content.indexOf("!")>0) {
			if (Pattern.compile("[0-9]").matcher(content.substring(content.indexOf("!"))).find()) {
				return true;
			}
			return false;
		};
		if (content.indexOf("=")>0) {
			if (Pattern.compile("[0-9]").matcher(content.substring(content.indexOf("="))).find()) {
				return true;
			}
			return false;
		};
		if (content.indexOf(">")>0) {
			if (Pattern.compile("[0-9]").matcher(content.substring(content.indexOf(">"))).find()) {
				return true;
			}
			return false;
		};
		if (content.indexOf("<")>0) {
			if (Pattern.compile("[0-9]").matcher(content.substring(content.indexOf("<"))).find()) {
				return true;
			}
			return false;
		};
		return false;
	}

	//向hashMap中添加记录
	public static void hashmapAdd(HashMap<String, String> hashMap ,String newKey,String newValue){
		if (hashMap.containsKey(newKey)) {
			hashMap.put(newKey, hashMap.get(newKey) + "|" + newValue);
		}else {
			hashMap.put(newKey, newValue);
		}
	}

	//对结果进行去重，同时删去不符合规范的版本
	public static void deleteRepetition(HashMap<String, String> hashMap){
		HashMap<String, String> versionTmpHm = new HashMap<String, String>();
//		System.out.println("  开始对Version去重 ———— ");
		Iterator<Entry<String, String>> iteratorOk = VersionOK.entrySet().iterator();
	    while(iteratorOk.hasNext()){
	    	Entry<String, String> entry = iteratorOk.next();
	    	String keyString = entry.getKey();
	    	String[] messages = keyString.split(",");
	    	if (messages.length<3) {
				continue;
			}
	    	String name = messages[1];
	    	String[] versions = messages[2].split("\\|");
	    		    	
	    	if (versionTmpHm.containsKey(name)) {
	    		for (String i:versions) {
					if (!versionTmpHm.get(name).contains(i) && !i.equals("") && containsEn(i)==false) {
						versionTmpHm.put(name, versionTmpHm.get(name) + "|" + i);
					}
				}
			}else {
				String versionString = "";
				for (String i:versions) {
					if (containsEn(i)==false) {
						if (!versionString.equals("")) {
							versionString += "|" + i;
						}else {
							versionString += i;
						}
					}
				}
				if (!versionString.equals("")) {
					versionTmpHm.put(name, versionString);
				}
			}
	    }
	    
	    //重新覆盖hashMap
	    hashMap.clear();
	    Iterator<Entry<String, String>> iteratorTmp = versionTmpHm.entrySet().iterator();
	    while(iteratorTmp.hasNext()){
	    	Entry<String, String> entry = iteratorTmp.next();
	    	hashMap.put("*," + entry.getKey(), entry.getValue() + ",*");
	    }
		
	}
	
	//判断该行是否包含有英文、#
	public static boolean containsEn(String version){
			version = version.replaceAll("[a-zA-Z]", "@");
			if (version.indexOf("@") != -1 || version.contains("#")) {
				return true;
			}else {
				return false;
			}
		}

}
