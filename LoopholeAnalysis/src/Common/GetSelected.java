package Common;

import com.tools.excel.ExcelS;
import com.tools.excel.LineProcessor;
import com.tools.FileTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GetSelected {
    public static String basic_regx="(\\d+(\\.(\\d|_|x)+)*)";//+
    public static String versionNumber="("+basic_regx+"|"+basic_regx+"?"+" update ((\\d|_|x)+)?)";

    public static void main(String outDir,String fileType,String vulnerabilityLibDir) {
    	
    	String  outPath = outDir + fileType +"_VersionOk.csv";
    	
        HashSet<String> lineset=new HashSet<>();
        List<HashMap<String,String>> maplist=isInLu(outPath,vulnerabilityLibDir);
        FileTools.writeline(outDir +"selected_"+ fileType + ".txt", FileWriter->{
            for(HashMap<String,String> map:maplist) {
                try {
                    String writeline = map.get("name") + "," + map.get("keyword") + "," + map.get("versions") + "\n";
                    if (!lineset.contains(writeline)) {
                        FileWriter.write(writeline);
                        FileWriter.flush();
                        lineset.add(writeline);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        
        System.out.println("Vulnerability Library Retrieval Completed [" + (outDir +"selected_"+ fileType + ".txt").replace("\\\\", "\\")+"]");
    }


    public static String getAllRegx(){
        String pattern_3 = basic_regx+"(\\-"+basic_regx+""+")?";
        String pattern_4 = basic_regx+"(\\-(alpha|beta))?";
        String pattern = versionNumber+" and (prior|earlier)";
        String pattern1 = "(\\d+ )?before "+pattern_3;
        String pattern2 = "("+basic_regx+" (through|before|prior to|to) "+basic_regx+") ((and earlier|before|fix pack) "+basic_regx+")?";
        String pattern3 = pattern_3+" before "+pattern_3;
        String pattern4 = basic_regx+" or later";
        String pattern5 = pattern_4+" through "+pattern_4;
        String pattern6 = basic_regx;
        //6.1 before fix pack 11 (6.1.0.11)
        String all = pattern + "|"+pattern1+"|"+pattern2+"|"+pattern3+"|"+pattern4+"|"+pattern5+"|"+pattern6;
        return all;
    }



    public static List<HashMap<String,String>> isInLu(String path,String vulnerabilityLibDir){
        if(path.endsWith("csv")){
            return isInLuCSV(path,vulnerabilityLibDir);
        }
        List<HashMap<String,String>> maplist=new ArrayList<>();
        TestLucene.IndexWriterOpt(vulnerabilityLibDir,(indexWriter, directory) -> {
            BufferedReader reader= null;
            try {
                StandardQueryParser standardQueryParser = new StandardQueryParser();
                reader = new BufferedReader(new FileReader(new File(path)));
                String all = getAllRegx();
                try {
                    new ExcelS(path, new LineProcessor() {
                        @Override
                        public void proccessLine(int i, int i1, ArrayList<String> arrayList) {
                            if (arrayList.size() > 2) {
//                                System.out.println("----------------" + arrayList);
                                String keyword = arrayList.get(1);
                                String currentVersion = arrayList.get(2);
                                Query query = null;
                                try {
                                    query = standardQueryParser.parse("description : "+keyword, "time");
                                    DirectoryReader directoryReader=DirectoryReader.open(directory);
                                    IndexSearcher indexSearcher=new IndexSearcher(directoryReader);
                                    TopFieldDocs search = indexSearcher.search(query, Integer.MAX_VALUE, Sort.INDEXORDER);
                                    ScoreDoc[] scoreDocs = search.scoreDocs;
                                    if(scoreDocs.length>1&&!currentVersion.contains("SNAPSHOT")){
                                        for(ScoreDoc scoreDoc:scoreDocs) {
                                            int id = scoreDoc.doc;
                                            Document doc = indexSearcher.doc(id);
//                                            System.out.println(doc.get("name"));
//                                            System.out.println(doc.get("time_string"));
//                                            System.out.println(doc.get("description"));
//                                            System.out.println(doc.get("status"));
                                            HashMap<String, String> map = new HashMap<>();
                                            map.put("name", doc.get("name"));
                                            map.put("keyword", keyword);
                                            String versions = currentVersion;
                                            String collect = Stream.of(doc.get("description").split(",")).collect(Collectors.joining());
                                            Matcher matcher = Pattern.compile(all).matcher(collect);
                                            while (matcher.find()) {
                                                String group = matcher.group().trim();
                                                versions = versions + "," + group;
                                            }
                                            map.put("versions", versions);
                                            maplist.add(map);
                                        }
                                    }
                                } catch (QueryNodeException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return maplist;
    }

    public static List<HashMap<String,String>> isInLuCSV(String path,String vulnerabilityLibDir){
        List<HashMap<String,String>> maplist=new ArrayList<>();
        TestLucene.IndexWriterOpt(vulnerabilityLibDir,(indexWriter, directory) -> {
            BufferedReader reader= null;
            try {
                StandardQueryParser standardQueryParser = new StandardQueryParser();
                reader = new BufferedReader(new FileReader(new File(path)));
                String all = getAllRegx();
                reader = new BufferedReader(new FileReader(new File(path)));
                reader.lines().forEach(line->{
                    String[] ss=line.split(",");
                    String keyword = ss[1];
                    String currentVersion = ss[2];
                    Query query = null;
                    try {
                        query = standardQueryParser.parse("description : "+keyword, "time");
                        DirectoryReader directoryReader=DirectoryReader.open(directory);
                        IndexSearcher indexSearcher=new IndexSearcher(directoryReader);
                        TopFieldDocs search = indexSearcher.search(query, Integer.MAX_VALUE, Sort.INDEXORDER);
                        ScoreDoc[] scoreDocs = search.scoreDocs;
                        if(scoreDocs.length>1&&!currentVersion.contains("SNAPSHOT")) {
                            for (ScoreDoc scoreDoc : scoreDocs) {
                                int id = scoreDoc.doc;
                                Document doc = indexSearcher.doc(id);
//                                System.out.println(doc.get("name"));
//                                System.out.println(doc.getBinaryValue("time"));
//                                System.out.println(doc.get("description"));
//                                System.out.println(doc.get("status"));
                                HashMap<String, String> map = new HashMap<>();
                                map.put("name", doc.get("name"));
                                map.put("keyword", keyword);
                                String versions = currentVersion;
                                String collect = Stream.of(doc.get("description").split(",")).collect(Collectors.joining());
                                Matcher matcher = Pattern.compile(all).matcher(collect);
                                while (matcher.find()) {
                                    String group = matcher.group().trim();
                                    versions = versions + "," + group;
                                }
                                map.put("versions", versions);
                                maplist.add(map);
                            }
                        }
                    } catch (QueryNodeException e) {
                    	System.out.println(keyword);
                        e.printStackTrace();
                    } catch (IOException e) {
                    	System.out.println(keyword);
                        e.printStackTrace();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return maplist;
    }

    public static String matchNumber(String version) {
        String pattern="\\d+(\\.(\\d|_|x)+)+";
        Matcher matcher=Pattern.compile(pattern).matcher(version);
        while (matcher.find()){
            version=matcher.group();
            break;
        }
//        System.out.println(version);
        if(version.contains("_")){
            version=version.replaceAll("_","\\.");
        }
        if(version.contains("x")){
            version=version.replaceAll("x","1000");
        }
        return version;

    }

    public static int compareVersion(String version1,String version2){
        String[] v1=matchNumber(version1).split("\\.");
        String[] v2=matchNumber(version2).split("\\.");
//        System.out.println(Arrays.asList(v1));
//        System.out.println(Arrays.asList(v2));
        int i=0;
        while (i<v1.length&&i<v2.length){
            if(Integer.valueOf(v1[i])>Integer.valueOf(v2[i])){
                return 1;
            }
            else if(Integer.valueOf(v1[i])<Integer.valueOf(v2[i])){
                return 0;
            }
            i++;
        }
        if(v1.length>v2.length) {
            return 1;
        }
        else{
            return 0;
        }
    }

    public static void remove_dul(String file) {
        HashSet<String> lineset=new HashSet<>();
        FileTools.writeline("selected_python_before.txt", FileWriter->{
            BufferedReader reader= null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String line=null;
                while ((line=reader.readLine())!=null) {
                    try {
                        if (!lineset.contains(line)) {
                            FileWriter.write(line+"\n");
                            FileWriter.flush();
                            lineset.add(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

    }

}
