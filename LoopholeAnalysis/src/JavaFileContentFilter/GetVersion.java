package JavaFileContentFilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class GetVersion {

	
	static HashMap<String, String> VersionHashMap = new HashMap<String, String>();
	static HashMap<String, String> VersionOK = new HashMap<String, String>();
	static HashMap<String, String> VersionProblem = new HashMap<String, String>();
	
	static String sourceFilePath = "";
	static String outputPathOk = "";
	
	public static void main(String outDir) {  
        // TODO Auto-generated method stub  
		int xmlNum = 0;
		sourceFilePath = outDir + "pom.xml_sourcePath.csv";
		outputPathOk = outDir + "java_VersionOk.csv";
		
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
			
			System.out.println("Start filtering the pom.xml file ... ");
			
			while((filePath = br.readLine())!=null){    
				xmlNum++;
	        	if(xmlNum%100==0 && xmlNum>99){
	        		System.out.println(xmlNum);
	        	}
	        	VersionHashMap.clear();
	        	versionFilter(filePath);
	        }  
			System.out.println(xmlNum + " pom.xml files are filtered ! ");
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
		Iterator<Entry<String, String>> iteratorOk = VersionOK.entrySet().iterator();
	    while(iteratorOk.hasNext()){
	    	Entry<String, String> entry = iteratorOk.next();
	    	String resultString = entry.getKey() + "," + entry.getValue();
//	    	resultString = versionStandard(resultString);//对不符合规范的版本号进行简单的处理、删去不符合规范的软件名称，可以注释掉，查看其配置文件中原有版本
	    	if (!resultString.equals("")) {
	    		writeData(outputPathOk,resultString);
		    	okNum++;
		    	if(okNum%100==0 && okNum>99){
	        		System.out.print(".");
	        		if(okNum%1000==0 && okNum>999){
	            		System.out.println(okNum);
	            	}
	        	}
			}
	    	
	    }
	    System.out.println(okNum + " lines of version information have been written to [ "+outputPathOk.replace("\\\\", "\\"));
    }  
	

	//筛选该文件中包含的软件名及其版本号
	private static void versionFilter(String filePath) {
        try {
        	SAXReader reader = new SAXReader();
        	Document pomxmlPath = reader.read(new File(filePath));
        	 
        	//获取文档根节点
        	Element root = pomxmlPath.getRootElement();
        	 
        	//获取根节点下面的所有子节点（不包过子节点的子节点）
        	List<Element> list = root.elements() ;
        	//遍历List的方法
        	for (Element e:list){
        		if (e.getName().equals("version")) {
        			VersionHashMap.put("${project.version}", e.getText());
				}
        		
        		if (e.getName().contains(".version")) {
        			VersionHashMap.put(e.getName(), e.getText());
				}
        		
        		if (e.getName().equals("properties")) {
        			List<Element> contactList = e.elements();
                	for (Element dependency:contactList){
                		if (dependency.getName().contains(".version")||dependency.getName().contains("Version")) {
                			String projectName = "${" + dependency.getName() + "}";
                			String projectVersion = dependency.getText();
                			if (projectVersion.contains("${")) {
                				projectVersion = VersionHashMap.get(projectVersion);
							}
                			VersionHashMap.put(projectName, projectVersion);
        				}
                	} 
				}
        		
        		
        		if (e.getName().equals("dependencies")) {
        			List<Element> contactList = e.elements();
                	for (Element dependency:contactList){
                		ChildsAndContent(dependency,filePath);
                	} 
				}
        		
        		if (e.getName().equals("dependencyManagement")) {
        			//获取根节点下面的所有子节点（不包过子节点的子节点）
                	List<Element> dependencyManagementElList = e.elements() ;
                	for (Element dependencyManagement:dependencyManagementElList){
                		if (dependencyManagement.getName().equals("dependencies")) {
                			List<Element> contactList = dependencyManagement.elements();
                        	for (Element dependency:contactList){
                        		ChildsAndContent(dependency,filePath);
                        	} 
        				}
                	}
				}
        		
        		
        	}

        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
	}
	
	
	//获得特定节点下面的子节点
	private static void  ChildsAndContent(Element e,String filePath){
		//获得指定节点下面的子节点
		Element groupIdElem = e.element("groupId");//首先要知道自己要操作的节点。 
		Element artifactIdElem = e.element("artifactId");
		Element versionElem = e.element("version");

		String resultOk = "";
		String resultProblem = "";
		if (versionElem!=null) {
			String groupId = groupIdElem.getText();
			String artifactId = artifactIdElem.getText();
			String version = versionElem.getText();

			if (version.contains("${")) {
				if (VersionHashMap.containsKey(version)) {
					version = VersionHashMap.get(version);
					resultOk = groupId + "," + artifactId + "," + version;
					resultOk = resultOk.replaceAll("[\\t\\n\\r]", "");
					if (VersionOK.containsKey(resultOk)) {
						VersionOK.put(resultOk, VersionOK.get(resultOk)+"|"+filePath);
					}else {
						VersionOK.put(resultOk, filePath);
					}
				}else {
					resultProblem = groupId + "," + artifactId;
					resultProblem = resultProblem.replaceAll("[\\t\\n\\r]", "");
					if (VersionProblem.containsKey(resultProblem)) {
						VersionProblem.put(resultProblem, VersionProblem.get(resultProblem) + "|" + filePath);
					}else {
						VersionProblem.put(resultProblem, filePath);
					}
				}
			}else {
				resultOk = groupId + "," + artifactId + "," + version;
				resultOk = resultOk.replaceAll("[\\t\\n\\r]", "");
				if (VersionOK.containsKey(resultOk)) {
					VersionOK.put(resultOk, VersionOK.get(resultOk)+"|"+filePath);
				}else {
					VersionOK.put(resultOk, filePath);
				}
			}

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
	
	//将版本号标准化处理、删去不符合规范的软件名称
	private static String versionStandard(String resultString){
		String[] resultStrings = resultString.split(",");
		String version = resultStrings[2];
		version = version.replaceAll("[a-zA-Z]", "-");	
		if (version.contains("-")) {
			int index = version.indexOf("-");
			if (index == 0) {
				return resultString;
			}
			if (version.substring(index-1, index).equals(".")) {
				version = version.substring(0, index-1);
			}else {
				version = version.substring(0, index);
			}
		}
		if (resultStrings[1].contains("$")) {//删去不符合规范的软件名称
			resultString = "";
		}else {
			resultString = resultStrings[0]+","+resultStrings[1]+","+version+","+resultStrings[3];
		}
		return resultString;
	}
	
	
	//对结果进行去重，同时删去不符合规范的版本
	public static void deleteRepetition(HashMap<String, String> hashMap){
			HashMap<String, String> versionTmpHm = new HashMap<String, String>();
//			System.out.println("  开始对Version去重 ———— ");
			Iterator<Entry<String, String>> iteratorOk = VersionOK.entrySet().iterator();
		    while(iteratorOk.hasNext()){
		    	Entry<String, String> entry = iteratorOk.next();
		    	String keyString = entry.getKey();
		    	String[] messages = keyString.split(",");
		    	String name = messages[1];
		    	if (name.contains("$")) {
					continue;
				}
		    	String[] versions = messages[2].split("\\|");
		    	if (name.contains("MITLicense")) {
//		    		System.out.println("  开始对Version去重 ———— ");
				}
		    	
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
