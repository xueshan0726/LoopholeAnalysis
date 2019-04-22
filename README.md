# LoopholeAnalysis
### Environment
Windows  
jdk1.8  
jre1.8  

### Feature
1. Detect possible vulnerabilities in the software source code, and list the description, status, 
CVSS score and other information of the vulnerability.  
2. Detect the number and total size of files of the specified type in the software source code.  

### Usage
1. Download the source package [LoopholeAnalysis], modify the configuration file [loop.properties], 
run [LoopholeAnalysis\bin\main.java]; or download [LoopholeAnalysis_run], modify the configuration file 
[loop.properties], open the terminal to run [LoopholeAnalysis. Jar].  

2. [-h] or [-help] to get help with the tool, For example [java -jar LoopholeAnalysis.jar -h].  

3. [-t], the tool will detect the number and total size of the file type [Suffixes] in the folder 
pointed to by [sourceFolder] in the configuration file, and output the result, For example [java -jar LoopholeAnalysis.jar -t].  

4. [-a], the tool will detect the possible loopholes in the source code of the file development 
language [fileType] in the folder pointed to by [sourceFolder] in the configuration file, 
and output the result, For example [java -jar LoopholeAnalysis.jar -a].  

5. [-u], and the tool will update the vulnerability library file in [describePath] in the configuration file 
to the vulnerability database folder [VulnerabilityLibrary], For example [java -jar LoopholeAnalysis.jar -u].  

* **Configuration file loop.properties description**  

		sourceFolder: the source of the source code to be detected.  
		Suffixes: the suffix of the file to be detected, separated by ","  
		fileType: The type of source code to be detected. Currently, only the detection of 
		java and python source code is supported.  
		describePath: path to describe.txt  
		scorePath: path to score.txt  
* **Generation file description**
1. The result folder is named [source folder name_Result] and is stored in the same level directory of the detected source folder.  

2. If the development language is [python], the final result file is [FinalResult_python.xlsx], and the contents 
of each column in the file are as follows:
[NVD vulnerability number] [Dependent package name in source file] [Dependent package version in source file] 
[NVD vulnerability description in the package involved version] [matched version] [NVD vulnerability status] 
[VND vulnerability description] [CVSS Base Score] [Access Complexity].

* **Others**
1. When running [-u] or [-a], the [VulnerabilityLibrary] folder will be generated in the folder where 
[LoopholeAnalysis.jar] is located. This is the vulnerability library folder. Please do not delete it. If you delete this folder, re-run [-u].  

2. When running [-u], **the error [INVALID_SYNTAX_CANNOT_PARSE: Syntax Error, cannot parse description]** 
is a luence search engine syntax parsing error caused by special characters, which has little effect on the final result.

## What is LoopholeAnalysis
The tool analyzes the source code of the software. Then compare the version of the dependent software 
with NVD to get possible vulnerabilities. Help developers fix vulnerabilities in time to improve software 
security. Currently, only vulnerability analysis of source files for development languages for Java and 
Python is supported.  

For Python development, the requirement.txt file records all the required dependencies and their version numbers 
for the new environment deployment. This tool summarizes the dependencies and version numbers and compares 
the NVD's published vulnerability database to find possible vulnerabilities in the software.  

We will periodically re-obtain the latest vulnerability description from CVE official website,
 update the describe.txt and score.txt files, users can choose to use [-u] to update the vulnerability
 library according to their needs. In the future, we will expand more channels and enrich them.  
 