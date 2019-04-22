package FileTypeAnalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class FileTypeAnalyze {


	
//	static String Dir = "G:\\WorkSpace\\OpenStack_early\\OpenStack_early";		//要遍历的文件路径
//	static String outputPath = "G:\\WorkSpace\\OpenStack_early\\OpenStack_early_FileType.csv";  //结果输出路径
//	static List<String> searchTypeList = Arrays.asList("java","h","c","py");		//要检测的文件的类型
//	static HashMap<String, String> hmTypeNumSize = new HashMap<String, String>();
	static String Dir = "";		//要遍历的文件路径
	static String outputPath = "";  //结果输出路径
	static ArrayList<String> searchTypeList = new ArrayList<String>(); 		//要检测的文件的类型
	static HashMap<String, String> hmTypeNumSize = new HashMap<String, String>();
	static int fileNum = 0;
	
	
	public static void main(String dir,String outputpath,List searchTypes) {
		Dir = dir;
		outputPath = outputpath;
		searchTypeList.addAll(searchTypes);

		//输出文件检查
        File outputFile = new File(outputPath);
        
        if (outputFile.exists()){
        	outputFile.delete();
        }
        try {
			outputFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		File file = new File(Dir);		//获取其file对象
//		System.out.println("正在检索" + Dir.replace("\\\\", "\\") + " 中的所有文件");
		System.out.println("Retrieving all files in the [" + Dir.replace("\\\\", "\\") + "] ...");
		
		func(file);
		System.out.println(fileNum);

//		System.out.println("开始写搜索结果");
		Iterator<Entry<String, String>> iteratorTypeNumSize = hmTypeNumSize.entrySet().iterator();
	    while(iteratorTypeNumSize.hasNext()){
	    	Entry<String, String> entry = iteratorTypeNumSize.next();
	    	writeData(outputPath,entry.getKey() + "," + entry.getValue());
	    	
	    	String[] resultStrings = entry.getValue().split(",");
//	    	System.out.println(entry.getKey() + " 文件共 " + resultStrings[0] + " 个 ，文件总大小：" + resultStrings[1] + " Bit");
	    	System.out.println("There are " + resultStrings[0] +" "+ entry.getKey() + " files, the total file size is "+resultStrings[1]+" Bit");
	    }
		System.out.println("The result is stored in [" + outputPath.replace("\\\\", "\\")+ "]");
	}
	
	
	
	private static void func(File file){
		try {
			File[] fs = file.listFiles();
			for(File f:fs){
				if(f.isDirectory())	{//若是目录，则递归打印该目录下的文件
					func(f);
				}
				if(f.isFile()){	//若是文件后缀匹配，直接打印					
					String nameString = f.getName();
					String typeString = nameString.substring(nameString.lastIndexOf(".") + 1);
					long size = f.length();
					
					if (searchTypeList.contains(typeString)) {
						if (hmTypeNumSize.containsKey(typeString)) {
							String valueString = hmTypeNumSize.get(typeString);
							String[] valueStrings = valueString.split(",");
							long typeNum = Long.parseLong(valueStrings[0]) + 1;
							long totalSize = Long.parseLong(valueStrings[1]) + size;
							hmTypeNumSize.put(typeString, typeNum+","+totalSize);
						}else {
							hmTypeNumSize.put(typeString, "1,"+size);
						}
					}
					
					fileNum++;
					if(fileNum%1000==0 && fileNum>999){
			    		System.out.print("。");
			    		if(fileNum%10000==0 && fileNum>9999){
				    		System.out.println(fileNum);
				    	}
			    	}
					
				}
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}

	
	
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
