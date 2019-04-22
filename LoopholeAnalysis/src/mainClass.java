
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.net.ftp.parser.NetwareFTPEntryParser;

import com.tools.FileUtil;

import FileTypeAnalysis.*;
import JavaFileContentFilter.*;
import PythonFileContentFilter.*;

public class mainClass {

	static String Dir = "";		//要遍历的文件路径
	static String outDir = "";		//结果存放文件路径
	static String suffixes = "";	//检索文件类型
	static String fileType = "";		//检查文件的类型
	static String InvalidName = "";	//检测结果中删去大概率软件名称匹配错误的结果，例如删除软件名称为“requests”的结果
//	static String InvalidName = "cryptography,requests";
	static String describePath = "";		//CVE漏洞描述文件路径
	static String scorePath = "";		//CVE漏洞评分文件路径
	
	public static void main(String[] args) {
		
		String vulnerabilityLibDir = mainClass.class.getResource("/").toString().substring(6).replace("/", "\\\\")+"VulnerabilityLibrary\\\\";
		
		String propertiesPath = mainClass.class.getResource("/").toString().substring(6).replace("/", "\\\\")+"loop.properties";
		
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(propertiesPath)));
			Dir = properties.getProperty("sourceFolder").replace("/", "\\\\");
			suffixes = properties.getProperty("suffixes");
			fileType = properties.getProperty("fileType").toLowerCase();
			describePath = properties.getProperty("describePath").replace("/", "\\\\");
			scorePath = properties.getProperty("scorePath").replace("/", "\\\\");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String sourceName = Dir.substring(Dir.lastIndexOf("\\")+1);
		outDir = Dir.substring(0, Dir.lastIndexOf("\\\\"))+"\\\\"+sourceName+"_Result\\\\";		//结果存放路径
		
		//获取用户输入的字符串
		String strFunc = null;

		if(args.length==0) {
			args = new String[] {"-h"};
//			args = new String[] {"-u"};
		}
		strFunc = args[0].toLowerCase();
				
		if (strFunc.equals("-h")||strFunc.equals("-help")) {
			System.out.println("    -t     Analyze the source file type.");
			System.out.println();
			System.out.println("    -u     Update the vulnerability library.");
			System.out.println();
			System.out.println("    -a     Analyze possible vulnerabilities.");
			System.out.println();
		}
		
		
		//1.分析源文件类型
		if (strFunc.equals("-t")) {
			if (StringUtils.isNotBlank(Dir) && StringUtils.isNotBlank(suffixes)) {
				List<String> searchTypeList = Arrays.asList(suffixes.split(","));
				File file=new File(outDir);		//检查结果存放文件夹是否存在，不存在则创建结果存放文件夹
				if (!file.exists()) {
					file.mkdir();
				}
				String outputPath = outDir + "FilesTypes.csv";	  //结果输出文件
				FileTypeAnalyze.main(Dir,outputPath,searchTypeList);
			}else {
				System.out.println("Incorrect configuration file！");
			}
		}
		
		//2.更新漏洞描述库
		if (strFunc.equals("-u")) {
			if (StringUtils.isNotBlank(describePath)) {
				//检查是否存在漏洞库，不存在则先更新漏洞库
				File fileVulnerabilityLib=new File(vulnerabilityLibDir);		//检查结果存放文件夹是否存在，不存在则创建结果存放文件夹
				System.out.println("Start updating the vulnerability library ! ");
				if (!fileVulnerabilityLib.exists()) {
					//将漏洞上传elasticsearch库
					Common.UploadToLu.main(describePath,vulnerabilityLibDir);
				}else {
					FileUtil.delete(vulnerabilityLibDir);
					//将漏洞上传elasticsearch库
					Common.UploadToLu.main(describePath,vulnerabilityLibDir);
				}
				System.out.println("The vulnerability library update is complete! ");
			}else {
				System.out.println("Incorrect configuration file！");
			}
		}
		
		
		//3.漏洞分析
		if (strFunc.equals("-a")) {
			if (StringUtils.isNotBlank(Dir)&&StringUtils.isNotBlank(fileType)&&StringUtils.isNotBlank(describePath)&&StringUtils.isNotBlank(scorePath)) {
				File file=new File(outDir);		//检查结果存放文件夹是否存在，不存在则创建结果存放文件夹
				if (!file.exists()) {
					file.mkdir();
				}
				//检查文件是否存在
				File describeFile=new File(describePath);
				if (!describeFile.exists()) {
					System.out.println(" describe.txt doesn't exist !");
					return;
				}
				//检查文件是否存在
				File sourceFile=new File(scorePath);
				if (!sourceFile.exists()) {
					System.out.println(" score.txt doesn't exist !");
					return;
				}
				//检查是否存在漏洞库，不存在则先更新漏洞库
				File fileVulnerabilityLib=new File(vulnerabilityLibDir);		//检查结果存放文件夹是否存在，不存在则创建结果存放文件夹
				if (!fileVulnerabilityLib.exists()) {
					//将漏洞上传elasticsearch库
					Common.UploadToLu.main(describePath,vulnerabilityLibDir);
				}
				//根据要分析的源文件类型,进行不同的处理
				if (fileType.equals("java") ) {
					//获取所有配置文件路径
					JavaFileContentFilter.GetFilePath.main(Dir , outDir);
					System.out.println();
					//根据配置文件路径，筛选软件名称及版本
					JavaFileContentFilter.GetVersion.main(outDir);
					System.out.println();
					//与describe.txt文件进行比对
					Common.GetSelected.main(outDir, fileType, vulnerabilityLibDir);
					System.out.println();
					//对比对结果进行筛选和处理
					JavaFileContentFilter.VersionMatching.main(outDir,InvalidName);
					System.out.println();
					//补充对应漏洞的危险性评分和被利用难易程度
					JavaFileContentFilter.SupplyContent.main(outDir, describePath, scorePath);
					System.out.println();
				}
				if (fileType.equals("python")) {
					//获取所有配置文件路径
					PythonFileContentFilter.GetFilePath.main(Dir , outDir);
					System.out.println();
					//根据配置文件路径，筛选软件名称及版本
					PythonFileContentFilter.GetVersion.main(outDir);
					System.out.println();
					//与describe.txt文件进行比对
					Common.GetSelected.main(outDir, fileType, vulnerabilityLibDir);
					System.out.println();
					//对比对结果进行筛选和处理
					PythonFileContentFilter.VersionMatching.main(outDir,InvalidName);
					System.out.println();
					//补充对应漏洞的危险性评分和被利用难易程度
					PythonFileContentFilter.SupplyContent.main(outDir, describePath, scorePath);
					System.out.println();
				}
				
			}else {
				System.out.println("Incorrect  configuration file! ");
			}
			
		}
		
		if (!strFunc.equals("-t") && !strFunc.equals("-u") && !strFunc.equals("-a")&& !strFunc.equals("-h")&& !strFunc.equals("-help")) {
			System.out.println("Incorrect input ! ");
		}else {
			System.out.println("End of operation ! ");
		}
	}
}
