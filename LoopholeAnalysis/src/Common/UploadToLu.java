package Common;

import com.tools.JSONUtil;
import org.apache.lucene.document.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UploadToLu {

    public static void main(String describePath , String vulnerabilityLibDir) {
        uploadLoopHole(describePath , vulnerabilityLibDir);
        System.out.println();
    }

    public static void uploadLoopHole(String path , String vulnerabilityLibDir) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
            List<HashMap<String, String>> mapList = new ArrayList<>();
            AtomicInteger counter = new AtomicInteger();
            TestLucene.IndexWriterOpt(vulnerabilityLibDir,(indexWriter, directory) -> {
                String line = null;
                try {
                    while ((line = reader.readLine()) != null) {
                        int count = counter.incrementAndGet();
                     
                        if(count%1000==0 && count>999){
    			    		System.out.print("。");
    			    		if(count%10000==0 && count>9999){
    				    		System.out.println(count);
    				    	}
    			    	}
                        
                        HashMap<String, String> map = new HashMap<>();
                        String[] ss = line.split("\t");
                        LocalDateTime localDateTime = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                        String time = localDateTime.format(formatter);
                        Document document=new Document();
                        document.add(new StringField("name",ss[0], Field.Store.YES));
                        document.add(new StringField("status",ss[1], Field.Store.YES));
                        document.add(new Field("description",ss[2].trim(), TextField.TYPE_STORED));
                        document.add(new LongPoint("time_long",System.currentTimeMillis()));
                        document.add(new StringField("time_string",time,Field.Store.YES));
                        indexWriter.addDocument(document);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}
