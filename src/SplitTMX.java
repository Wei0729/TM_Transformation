import java.util.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.commons.io.FileUtils;

public class SplitTMX {
	public static Charset charset = StandardCharsets.UTF_8;
	public static String outputFolder = "C:\\work\\TM_Transform\\done";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String mainFolder = "C:\\work\\TM_Transform\\Split_TMX";
		SearchFiles(mainFolder);
		System.out.println("done");
	}
	
	public static void readXMLFile(String filePath, String oldTMName, boolean GLTMS, String targetPath){
		try{
			File xmlFile = new File(filePath);
			comment(filePath);
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(xmlFile);
			if(GLTMS){
				changeAttribute(doc,oldTMName);
			}
			else{
				moveTags(doc,oldTMName);
			}
			WriteXML(doc,targetPath);
			uncomment(filePath);
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	public static void moveTags(Document doc, String oldTMName){
		try{
			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression poseXPath = xpath.compile("//tuv/prop");
			NodeList oldPropNodes = (NodeList)poseXPath.evaluate(doc,XPathConstants.NODESET);
			if(oldPropNodes.getLength() == 0)
				return;
				
			for(int i = oldPropNodes.getLength() - 1; i >= 0; i--){
				Node oldPropNode = oldPropNodes.item(i);
				NamedNodeMap attrs = oldPropNode.getAttributes();
				Node typeNode = attrs.getNamedItem("type");
				if(typeNode.getTextContent().contains("Context")){
					typeNode.setTextContent("old_tm_name");
					oldPropNode.setTextContent(oldTMName);
				}
				Node oldPropCopy = oldPropNode.cloneNode(true);
				Node parNode = oldPropNode.getParentNode();
				Node grandParNode = parNode.getParentNode();
				Node firstChild = grandParNode.getFirstChild();
				grandParNode.insertBefore(oldPropCopy, firstChild);
				parNode.removeChild(oldPropNode);
			}
			
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	public static void changeAttribute(Document doc, String oldTMName){
		try{
			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression poseXPath = xpath.compile("//prop");
			NodeList oldPropNodes = (NodeList)poseXPath.evaluate(doc,XPathConstants.NODESET);
				
			for(int i = oldPropNodes.getLength() - 1; i >= 0; i--){
				Node oldPropNode = oldPropNodes.item(i);
				NamedNodeMap attrs = oldPropNode.getAttributes();
				Node typeNode = attrs.getNamedItem("type");
				if(typeNode.getTextContent().contains("Context")){
					typeNode.setTextContent("old_tm_name");
					oldPropNode.setTextContent(oldTMName);
				}
			}
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
    /** Print the document on stdout */
    public static void WriteXML(Document doc, String filePath) {
    	try{
    		doc.setXmlStandalone(true);
        	XPath xp = XPathFactory.newInstance().newXPath();
        	NodeList nl = (NodeList) xp.evaluate("//text()[normalize-space(.)='']", doc, XPathConstants.NODESET);
        	for (int i=0; i < nl.getLength(); ++i) {
        	    Node node = nl.item(i);
        	    node.getParentNode().removeChild(node);
        	}
        	
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StringWriter sw = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(sw));       
            String output = sw.getBuffer().toString().replaceAll("><", ">\n<");
            output = output.replaceAll("(?m)^[ \t]*\r?\n", "");
            Path path = Paths.get(filePath);
            Files.write(path, output.getBytes(charset));
            
    	}catch(Exception e){
    		System.out.println(e.getMessage());
    	}
    }
	
	public static void SearchFiles(String mainFolder){
		try{
			File folder = new File(mainFolder);
			File[] listOfFiles = folder.listFiles();
			for(File file : listOfFiles){
				if(file.isFile() && file.getName().endsWith(".tmx")){
					if(file.getName().contains("GLTMS-emptyTus")){
						String fileName = file.getName();
						String oldTMName = fileName.substring(0, fileName.indexOf("-GLTMS")) + ".tmx";
						String filePath = file.getAbsolutePath();
						String part = filePath.substring(0, filePath.lastIndexOf("\\"));
						String projectName = part.substring(part.lastIndexOf("\\")+1);
						String targetFolder = outputFolder + "\\" + projectName;
						File dir = new File(targetFolder);
						if (!dir.exists()) {
							dir.mkdir();
						}
						String outputFilePath = targetFolder + "\\" + fileName;
						readXMLFile(file.getAbsolutePath(),oldTMName,true,outputFilePath);
						checkTags(outputFilePath);
					}
					else if(file.getName().contains("ambiguousTus")){
						String fileName = file.getName();
						String oldTMName = fileName.substring(0, fileName.indexOf("-ambiguousTus")) + ".tmx";
						String filePath = file.getAbsolutePath();
						String part = filePath.substring(0, filePath.lastIndexOf("\\"));
						String projectName = part.substring(part.lastIndexOf("\\")+1);
						String targetFolder = outputFolder + "\\" + projectName;
						File dir = new File(targetFolder);
						if (!dir.exists()) {
							dir.mkdir();
						}
						String outputFilePath = targetFolder + "\\" + fileName;
						readXMLFile(file.getAbsolutePath(),oldTMName,false,outputFilePath);
						checkTags(outputFilePath);
					}
					else{
						String sourcePath = file.getAbsolutePath();
						String targetPath = file.getAbsolutePath().replaceAll("Split_TMX", "done");
						String part = sourcePath.substring(0, sourcePath.lastIndexOf("\\"));
						String projectName = part.substring(part.lastIndexOf("\\")+1);
						String targetFolder = outputFolder + "\\" + projectName;
						File dir = new File(targetFolder);
						if (!dir.exists()) {
							dir.mkdir();
						}
						File source = new File(sourcePath);
						File dest = new File(targetPath);
						FileUtils.copyFile(source, dest);
					}
					
				}
				else if(file.isDirectory()){
					SearchFiles(file.getAbsolutePath());
				}
			}
		}catch(Exception e){
			System.out.println(e.getMessage());
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
}
