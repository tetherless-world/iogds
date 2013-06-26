import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;


public class HTML2CSV {

  private void addAProperty(String property, String value, Hashtable<String, String> propertyTable, ArrayList<String> propertyAry) {
		property = property.replaceAll(""+ (char)(160), "");
		value = value.replaceAll(""+ (char)(160), "");
		
		value = value.replaceAll("\u0001", "");
		value = value.replaceAll("\u001c", "");
		
		if (property.endsWith(":")) {
			property = property.substring(0, property.length()-1).trim();
		}
		
		if (propertyTable.containsKey(property)) {
			int count = 2;
			String newProperty = property + "_" + count;
			while (propertyTable.containsKey(newProperty)) {
				count++;
				newProperty = property + "_" + count;
			}
			property = newProperty;
		}
		
		propertyAry.add(property);
		value = value.replaceAll("\"", "\"\"");
		propertyTable.put(property, value);
		//System.out.println(property + "\t" + value);
	}

	private int parseCatalogOnePage(String baseUrl, String pageUrl,ArrayList<Hashtable<String, String>> propertyTableList, ArrayList<ArrayList<String>> propertyAryList) throws IOException{
		URL url = new URL(pageUrl);
		JsonFactory f = new MappingJsonFactory();
		@SuppressWarnings("deprecation")
		JsonParser jp = f.createJsonParser(url);
		JsonToken current = jp.nextToken();
		int count = 0;
		if (current == JsonToken.START_ARRAY) {
			JsonNode node = jp.readValueAsTree();
			Iterator<JsonNode> datasetIter = node.elements();
		    while (datasetIter.hasNext()) {
		    	JsonNode oneDataset = datasetIter.next();
		    	count++;
		    	
				Hashtable<String, String> propertyTable = new Hashtable<String, String>();
				ArrayList<String> propertyAry = new ArrayList<String>();
				parseJsonObject("", oneDataset, propertyTable, propertyAry);
				String idValue = propertyTable.get("id");
				addAProperty("url", baseUrl + "/d/" + idValue, propertyTable, propertyAry);
				
    			propertyTableList.add(propertyTable);
    			propertyAryList.add(propertyAry);
		    }
		}
    //	System.out.println(count);
		return count;
	}	

	
	private void parseCatalogPage(String baseUrl, String pageUrl, ArrayList<Hashtable<String, String>> propertyTableList, 
			ArrayList<ArrayList<String>> propertyAryList) throws IOException{
		int count = parseCatalogOnePage(baseUrl, pageUrl, propertyTableList, propertyAryList);
		int pageNumber = 2;
		while (count > 0) {
			String nextPageUrl = pageUrl  + "?page=" + pageNumber;
			if (! pageUrl.endsWith("views")) 
				nextPageUrl = pageUrl  + "&page=" + pageNumber;
			count = parseCatalogOnePage(baseUrl, nextPageUrl, propertyTableList, propertyAryList);
			pageNumber++;
		}
	
	}
	
	public void processCatalog(String baseUrl, String pageUrl, String destfile) 
	{
		ArrayList<Hashtable<String, String>> propertyTableList = new ArrayList<Hashtable<String, String>>();
		ArrayList<ArrayList<String>> propertyAryList = new ArrayList<ArrayList<String>>();

		try {
			parseCatalogPage(baseUrl, baseUrl+pageUrl, propertyTableList, propertyAryList);
			
			ArrayList<String> propertySet = MergePropertyName(propertyAryList);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destfile), "UTF-8"));
			writeHead(writer, propertySet);
			for (int i = 0; i < propertyTableList.size(); i++) {
				
				writeALine(writer, propertySet, propertyTableList.get(i));
				System.out.println(writer +""+ ""+propertySet+""+propertyTableList.get(i));
			}
		
			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void parseJsonArray(String oneFieldName, JsonNode oneNode, Hashtable<String, String> propertyTable, ArrayList<String> propertyAry) {
		String value = "";
		boolean isObject = false;
		JsonNode oneElem = null;
		Iterator<JsonNode> elemIter = oneNode.elements();
		if (elemIter.hasNext()) {
			oneElem = elemIter.next();
			if (oneElem.isObject()) {
				isObject = true;
			} else {
				value = oneElem.textValue();
			}
		}
		if (isObject) {
			parseJsonObject(oneFieldName , oneElem, propertyTable, propertyAry);
			while (elemIter.hasNext()) {
				oneElem = elemIter.next();
				parseJsonObject(oneFieldName , oneElem, propertyTable, propertyAry);
			}
			return;
		} 
		
		while (elemIter.hasNext()) {
			oneElem = elemIter.next();
			value += ", " + oneElem.textValue();
		}

		addAProperty(oneFieldName, value, propertyTable, propertyAry);
		
	}
	
	private void parseJsonObject(String upperFieldName, JsonNode oneNode, Hashtable<String, String> propertyTable, ArrayList<String> propertyAry) {
		Iterator<String> fieldNameIter = oneNode.fieldNames();
		while (fieldNameIter.hasNext()) {
			String oneFieldName = fieldNameIter.next();
			JsonNode oneField = oneNode.get(oneFieldName);
			
			if (oneFieldName.equals("warnings")) 
				continue;
			
			if (oneFieldName.equals("filterCondition")) 
				continue;
			
			if (oneFieldName.equals("renderTypeConfig")) 
				continue;
			
			if (oneFieldName.equals("richRendererConfigs")) 
				continue;
			
			if (oneFieldName.equals("thumbnail")) 
				continue;
			
			if (oneFieldName.equals("conditionalFormatting")) 
				continue;

			if (oneFieldName.equals("facets")) 
				continue;
			
//			if (oneFieldName.equals("custom_fields")) 
//				continue;

			String fieldName = "";
			if (upperFieldName.length() > 0) 
				fieldName = upperFieldName + "_" + oneFieldName;
			else
				fieldName = oneFieldName;
				
			
			if (oneField.isArray()) {
				parseJsonArray(fieldName, oneField, propertyTable, propertyAry);
				continue;
			}
			
			if (oneField.isObject()) {
				parseJsonObject(fieldName, oneField, propertyTable, propertyAry);
				continue;
			}

			addAProperty(fieldName, oneField.asText(), propertyTable, propertyAry);
		}
	}
	
	private ArrayList<String> MergePropertyName (ArrayList<ArrayList<String>> propertyAryList ) {
		ArrayList<String> propertyAry = propertyAryList.get(0);
		
		HashSet<String> propertySet = new HashSet<String>();
		propertySet.addAll(propertyAry);
		for (int i = 1; i < propertyAryList.size(); i++) {
			if (propertyAryList.get(i) != null)
				if (propertySet.addAll(propertyAryList.get(i))) {
					ArrayList<String> anotherPropertyAry = propertyAryList.get(i);
					for (int j = 0; j < anotherPropertyAry.size(); j++)
						if (! propertyAry.contains(anotherPropertyAry.get(j)))
							propertyAry.add(anotherPropertyAry.get(j));
				}
		}
		if (propertySet.size() != propertyAry.size()) {
//			System.out.println(propertySet);
//			System.out.println(propertyAry);
		} else {
//			System.out.println(propertyAry);
		}
		return propertyAry;
	}


	private void writeHead (BufferedWriter writer, ArrayList<String> propertySet) throws IOException{
		writer.write("\"" + propertySet.get(0) + "\"");
		for (int i = 1; i < propertySet.size(); i++) {
			writer.write("," + "\"" + propertySet.get(i) + "\"");
		}

		writer.newLine();
	}

	
	private void writeALine (BufferedWriter writer, ArrayList<String> propertySet, Hashtable<String, String> propertyTable) throws IOException{
		for (int i = 0; i < propertySet.size(); i++) {
			String property = propertySet.get(i);
			String propertyValue = "";
			if (propertyTable != null) { 
				if (propertyTable.containsKey(property)) {
					propertyValue = propertyTable.get(property);
				}
			}
			
			if (i == 0)
				writer.write("\"" + propertyValue + "\"");
			else
				writer.write("," + "\"" + propertyValue + "\"");
		}

		writer.newLine();
	}

	public static void main(String[] args) throws FileNotFoundException
	{
		
		Scanner in = new Scanner(new FileReader("C:/filename.txt"));
		String url = in.nextLine().trim();
		
		System.out.println(url);
		String destDir = "C:/sym/RPI/data_gov/csv/";
		String pageUrl = "/api/views";

		String destfile = destDir + "data.austintexas.gov.csv";

		HTML2CSV test = new HTML2CSV();
		test.processCatalog(url, pageUrl,destfile);
	}
}
