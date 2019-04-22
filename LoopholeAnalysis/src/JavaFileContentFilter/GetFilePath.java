package JavaFileContentFilter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class GetFilePath {

	static String outputPath = ""; //输出文件
	
	static int fileNum=0;
	static long fileSize=0;
	
	
	public static void main(String Dir , String outDir) {

		outputPath = outDir + "pom.xml_sourcePath.csv";
		
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
		System.out.println("Retrieving pom.xml file ...");
		func(file);
		System.out.println(fileNum);
		System.out.println("The result is stored in [" + outputPath.replace("\\\\", "\\") + "]");
	}
	
	private static void func(File file){
		try {
			File[] fs = file.listFiles();
			for(File f:fs){
				if(f.isDirectory())	{//若是目录，则递归打印该目录下的文件
					func(f);
				}
				if(f.isFile() && f.getName().equals("pom.xml")){	//若是文件名称匹配，直接打印
					String pathString = f.getPath();
					writeData(outputPath, pathString);//path
					fileNum++;
					if(fileNum%100==0 && fileNum>99){
			    		System.out.print("。");
			    		if(fileNum%1000==0 && fileNum>999){
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
