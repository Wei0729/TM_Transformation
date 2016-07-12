import java.util.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.commons.io.FileUtils;

public class Transform {
	public static int count = 0;
	public static ArrayList<String> tmxPaths = new ArrayList<String>();
	public static HashMap<String,String> doubleName = new HashMap<String,String>();
	public static ArrayList<String> GLtmxPaths = new ArrayList<String>();
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		createDoubleName();
		String mainFolder = "C:\\work\\TM_Transform\\done\\Software";
//		printTMX(mainFolder);
//		System.out.println("Total tmx files " + count);
//		String apPath = "C:\\work\\AP\\bin";
//		createBatchFile(apPath,mainFolder);
//		String filePath = "C:\\work\\filesamples\\done\\ACG Ad-hoc request TM_AR-SA_EN-US-GLTMS.tmx";
//		check(mainFolder);
//		System.out.println("Done with checking and changing tags");
//		changeFolders();
//		//changeFolder(filePath);
		
		GetMovableFiles(mainFolder);
		System.out.println(count);
		System.out.println("Done with all");
	}
	
	public static void GetMovableFiles(String mainFolder){
		File folder = new File(mainFolder);
		File[] listOfFiles = folder.listFiles();
		for(int i = 0; i < listOfFiles.length; i++){
			if(listOfFiles[i].isFile() && (!listOfFiles[i].getName().contains("TmxBilingualRemainder")) && (!listOfFiles[i].getName().endsWith("-GLTMS.tmx")) && (listOfFiles[i].getName().endsWith("log") || 
					listOfFiles[i].getName().contains("ambiguous") || 
							listOfFiles[i].getName().contains("emptyTus"))){
				//System.out.println(listOfFiles[i].getAbsolutePath());
				MoveFile(listOfFiles[i].getAbsolutePath());
				count++;
			}
		}
	}	
	
	public static void MoveFile(String filePath){
		try{
			String sourcePath = filePath;
			String folder = filePath.substring(0, filePath.lastIndexOf("\\"));		
			folder = folder.replaceAll("done", "move");	
			File dir = new File(folder);
			if (!dir.exists()) {
				dir.mkdir();
			}
			String fileName = filePath.substring(filePath.lastIndexOf("\\")+1);
			String targetPath = folder + "\\" + fileName;
			File source = new File(sourcePath);
			File dest = new File(targetPath);
			FileUtils.copyFile(source, dest);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}	
	public static void changeIN2ID(String filePath){
		try{
			Path path = Paths.get(filePath);
			Charset charset = StandardCharsets.UTF_8;
			String content = new String(Files.readAllBytes(path), charset);
			content = content.replaceAll("<tuv xml:lang=\"IN\"", "<tuv xml:lang=\"ID\"");
			Files.write(path, content.getBytes(charset));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public static void changeFolders(){
		for(String path : GLtmxPaths){
			changeFolder(path);
		}
	}
	
	public static void changeFolder(String filePath){
		try{
			String sourcePath = filePath;
			String folder = filePath.substring(0, filePath.lastIndexOf("\\"));		
			folder = folder.replaceAll("done", "completed");	
			String tmxName = filePath.substring(filePath.lastIndexOf("\\")+1);
			String tempStr = tmxName.substring(0,tmxName.lastIndexOf("_"));
			String sourceLang = tempStr.substring(tempStr.lastIndexOf("_")+1);
			folder = folder + "\\" + sourceLang;
			File dir = new File(folder);
			if (!dir.exists()) {
				dir.mkdir();
			}
			String targetPath = folder + "\\" + tmxName;
			File source = new File(sourcePath);
			File dest = new File(targetPath);
			FileUtils.copyFile(source, dest);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void check(String mainFolder){
		File folder = new File(mainFolder);
		File[] listOfFiles = folder.listFiles();
		for(int i = 0; i < listOfFiles.length; i++){
			if(listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith("GLTMS.tmx") && !listOfFiles[i].getName().contains("GLTMS-GLTMS")){
				GLtmxPaths.add(listOfFiles[i].getAbsolutePath());
				changeTag(listOfFiles[i].getAbsolutePath());
				checkTags(listOfFiles[i].getAbsolutePath());
				count++;
			}
			else if(listOfFiles[i].isDirectory()){
				check(listOfFiles[i].getAbsolutePath());
			}
		}
	}
	
	public static void comment(String filePath){
		try{
			Path path = Paths.get(filePath);
			Charset charset = StandardCharsets.UTF_8;
			String content = new String(Files.readAllBytes(path), charset);
			content = content.replaceAll("<!DOCTYPE tmx SYSTEM \"http://www.lisa.org/tmx/tmx14.dtd\" >", "<!--!DOCTYPE tmx SYSTEM \"http://www.lisa.org/tmx/tmx14.dtd\" -->");
			Files.write(path, content.getBytes(charset));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void uncomment(String filePath){
		try{
			Path path = Paths.get(filePath);
			Charset charset = StandardCharsets.UTF_8;
			String content = new String(Files.readAllBytes(path), charset);
			content = content.replaceAll("<!--!DOCTYPE tmx SYSTEM \"http://www.lisa.org/tmx/tmx14.dtd\" -->", "<!DOCTYPE tmx SYSTEM \"http://www.lisa.org/tmx/tmx14.dtd\" >");
			Files.write(path, content.getBytes(charset));
		}catch(Exception e){
			e.printStackTrace();
		}		
	}
	
	public static void checkTags(String filePath){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc = null;
        try{
        	comment(filePath);
            builder = factory.newDocumentBuilder();
            doc = builder.parse(filePath);

            // Create XPathFactory object
            XPathFactory xpathFactory = XPathFactory.newInstance();

            // Create XPath object
            XPath xpath = xpathFactory.newXPath();
    		
            XPathExpression expr =
	                xpath.compile("count(//tuv/prop)");
	        Double count = (Double) expr.evaluate(doc, XPathConstants.NUMBER);
	        if(count > 0.0)
	        	System.out.println(filePath + " contains incorrect tags");
	        uncomment(filePath);
        }catch(Exception e){
        	e.printStackTrace();
        }
	}
	
	public static void changeTag(String filePath){
		String filename = filePath.substring(filePath.lastIndexOf("\\")+1);
		filename = filename.replaceAll("-GLTMS", "");
		try{
			Path path = Paths.get(filePath);
			Charset charset = StandardCharsets.UTF_8;

			String content = new String(Files.readAllBytes(path), charset);
			content = content.replaceAll("<prop type=\"Context\">TEXT</prop>", "<prop type=\"old_tm_name\">"+filename+"</prop>");
			Files.write(path, content.getBytes(charset));
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	
	public static void createDoubleName(){
		doubleName.put("FR","FR-FR");
		doubleName.put("BG","BG-BG");
		doubleName.put("CS","CS-CZ");
		doubleName.put("DA", "DA-DK");
		doubleName.put("DE", "DE-DE");
		doubleName.put("IT","IT-IT");
		doubleName.put("JA", "JA-JP");
		doubleName.put("RU","RU-RU");
	}
	
	public static void createBatchFile(String apPath,String mainFolder){
		String BatchFilePath = mainFolder + "\\" + "runMe.bat";
		System.out.println(BatchFilePath);
		File file = new File(BatchFilePath);
		ArrayList<String> createLuceneTmList = new ArrayList<String>();
		ArrayList<String> importTmxList = new ArrayList<String>();
		ArrayList<String> exportTmxList = new ArrayList<String>();
		ArrayList<String> TmxanalyzerList = new ArrayList<String>();
		ArrayList<String> TmxBilingualRemainderList = new ArrayList<String>();
		try{
			if(!file.exists()){
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for(String filePath : tmxPaths){
				
				String tempStr = filePath.substring(0, filePath.lastIndexOf("_"));
				String sourceLang = tempStr.substring(tempStr.lastIndexOf("_")+1);
				if(doubleName.containsKey(sourceLang))
					sourceLang = doubleName.get(sourceLang);
				String targetLang = filePath.substring(filePath.lastIndexOf("_")+1,filePath.lastIndexOf(".tmx"));
				if(doubleName.containsKey(targetLang))
					targetLang = doubleName.get(targetLang);
				String TMname = filePath.substring(filePath.lastIndexOf("\\")+1, filePath.lastIndexOf("."));
				createLuceneTmList.add("call " + apPath + "\\createLuceneTm -l " + sourceLang + " -p " + targetLang + " -t \"file://"+ TMname +"\"\n");
				importTmxList.add("call " + apPath + "\\importTmx -t " + "\"file://"+ TMname +"\" -u alwaysadd " + "\""+ TMname +".tmx\"\n");
				exportTmxList.add("call " + apPath + "\\exportTmx -t " + "\"file://"+ TMname +"\" -o " + "\""+ TMname +"-GLTMS.tmx\"\n");
				TmxanalyzerList.add("call " + apPath + "\\Tmxanalyzer \"" + TMname +".tmx\" > \"" + TMname + ".tmx.log\"\n");
				TmxBilingualRemainderList.add("call " + apPath + "\\TmxBilingualRemainder -t " + targetLang + " \"" + TMname +".tmx\" > "+ mainFolder +"\\TmxBilingualRemainder"+TMname+".log\n");
			}
			bw.write("ECHO OFF\n");
			for(String str : createLuceneTmList)
				bw.write(str);
			bw.write("\n");
			for(String str : importTmxList)
				bw.write(str);
			bw.write("\n");
			for(String str : exportTmxList)
				bw.write(str);
			bw.write("\n");
			for(String str : TmxanalyzerList)
				bw.write(str);
			bw.write("\n");
			for(String str : TmxBilingualRemainderList)
				bw.write(str);
			bw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void printTMX(String filePath){
		File folder = new File(filePath);
		File[] listOfFiles = folder.listFiles();
		for(int i = 0; i < listOfFiles.length; i++){
			if(listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".tmx")){
				System.out.println(listOfFiles[i].getAbsolutePath());
				if(listOfFiles[i].getName().endsWith("IN.tmx"))
					changeIN2ID(listOfFiles[i].getAbsolutePath());
				tmxPaths.add(listOfFiles[i].getAbsolutePath());
				count++;
			}
			else if(listOfFiles[i].isDirectory()){
				printTMX(listOfFiles[i].getAbsolutePath());
			}
		}
	}
	
}
