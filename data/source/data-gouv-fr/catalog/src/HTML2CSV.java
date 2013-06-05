import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
	 
public class HTML2CSV {
	
	
	
	private String[] parseOneFieldWithoutLink(String line) {
		String[] contentAry = {"", ""};
		int startIndex = -1;
		int endIndex = -1;

		line = line.trim();
		while ((startIndex = line.indexOf('<')) >= 0) {
			if (startIndex > 0)
				contentAry[0] += " " + line.substring(0, startIndex);
			endIndex = line.indexOf('>');
			line = line.substring(endIndex + 1 ).trim();
		}
		contentAry[0] += " " + line;
		contentAry[0] = contentAry[0].trim();

		return contentAry;
	}

	private String parseOneFieldWithImage(String line) {
		String content = "";

		int startIndex = line.indexOf("<img");
		String tag = "title=";
		startIndex = line.indexOf(tag, startIndex) + tag.length();
		char ch = line.charAt(startIndex);
		int endIndex = line.indexOf(ch, startIndex+1);
		content = line.substring(startIndex + 1, endIndex).trim();
			
		return content;
	}

	
	private String[] parseOneFieldWithLink(String line) {
		String[] contentAry = {"", ""};
		int startIndex = -1;
		int endIndex = -1;

		String anchor = "<a ";
		startIndex = line.indexOf(anchor);
		anchor = "href";
		startIndex = line.indexOf(anchor, startIndex);
		startIndex += anchor.length() + 2;
		char ch = line.charAt(startIndex-1);
		endIndex = line.indexOf('>', startIndex);
		int urlEndIndex = line.indexOf(ch, startIndex);
//		if ((urlEndIndex < 0) || (urlEndIndex > endIndex)) 
//			urlEndIndex = line.indexOf('"', startIndex);
		contentAry[1] = line.substring(startIndex, urlEndIndex);

		int contentEndIndex = line.indexOf("</a>", endIndex);
		contentAry[0] = line.substring(endIndex + 1, contentEndIndex);
		String[] realContent = parseOneFieldWithoutLink(contentAry[0]);
		contentAry[0] = realContent[0];
		return contentAry;
	}

	
	
	private void parseOneRow (String line, ArrayList<String[]> datasetAry, String mainUrl) {
		String[] oneRow = {"", ""};
	
		String separator = "<h2 class=\"publititre\">";
		int startIndex = line.indexOf(separator);
		separator = "</a>";
		int endIndex = line.indexOf(separator, startIndex) + separator.length();
		String titleLine = line.substring(startIndex, endIndex);
		String[] titleAry = parseOneFieldWithLink(titleLine);
		oneRow[0] = titleAry[0];
		oneRow[1] = mainUrl + titleAry[1];
		datasetAry.add(oneRow);
	}
	
	private String formatContent (String content) {
		content = content.replaceAll("&amp;", "&");
		content = content.replaceAll("&nbsp;", " ");
		return content;
	}

	
	private void parseContent(String content, ArrayList<String[]> datasetAry, String mainUrl) {
		
		String separator = "<div class=\"resultatliste_item";
		String[] subStr = content.split(separator);
		for (int i = 1; i < subStr.length; i++) {
			String oneRow = separator + subStr[i];
//			System.out.println(oneRow);
			parseOneRow(oneRow, datasetAry, mainUrl);
		}
	}

	
	private String getNextLink(String line, int count) {
		String nextLink = null;
		int pageCount = count+1;
		String separator = "<li";
		String[] subStr = line.split(separator);
		for (int i = 0; i < subStr.length; i++) {
			if (subStr[i].contains("</a>") && subStr[i].contains(" href=")) {
				String[] oneFieldAry = parseOneFieldWithLink(separator + subStr[i]);
				if (oneFieldAry[0].equals(pageCount+""))
					nextLink = oneFieldAry[1];
				}
		}
		
		return nextLink;
	}

	

	private String parseOnepage(String urlStr, ArrayList<String[]> datasetAry, String mainUrl, int count) {
		try {
			URL url = new URL(urlStr);
           
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));

	 		int flag = 0;
	 		String line;
	 		String content = "";
	 		String nextContent = "";
	 		while ((line = reader.readLine()) != null) {
	           	
	          	switch (flag) {
	           	case 0: int index = line.indexOf("<ul id=\"resultatliste\""); 
	           			if (index < 0)  
	           				break; 
	           			else {
	           				flag = 1;
	           				line = line.substring(index)+ " ";
	           			}
	           	case 1: index = line.indexOf("<section id=\"pagibas\"");
           	   			if ( index < 0) {
           	   				content += line + " ";
           	   				break;
           	   			}
           	   			flag = 2;
           	   			line = line.substring(index);
           	   	case 2: index = line.indexOf("</section>");
   	   					if ( index >= 0) {
   	   						nextContent += line.substring(0, index) + " ";
   	   						flag = 3;
   	   					} else {
   	   						nextContent += line + " ";
   	   					}
	          	}
			}
	 
			reader.close();
			
//			System.out.println(content);
//			System.out.println(nextContent);
			content = formatContent(content);
			nextContent = formatContent(nextContent);
//			content = eliminateComments(content);
			parseContent(content,datasetAry, mainUrl);
			return getNextLink(nextContent, count);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}  catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void processCatalog(String mainUrl, String pageUrl, String destfile) {
		ArrayList<String[]> datasetAry = new ArrayList<String[]>();
		String url = mainUrl + pageUrl;
		int count = 1;
		
/*		url = "http://www.data.gouv.fr/content/search/%28offset%29/352000?SearchText=%23all&Type=data&Contexte=q%3D%2523all%2BAND%2B%2528type%253Adata%2529%26add_hit_meta%3Dhtml_simple_view%2540html_simple_view&Facet=";
		count = 35201;
		
		String nextPageLink = parseOnepage(url, datasetAry, mainUrl, count);
		while (nextPageLink != null) {
			url = mainUrl + nextPageLink;
			System.out.println(url);
			count++;
			nextPageLink = parseOnepage(url, datasetAry, mainUrl, count);
			if (count % 100 == 0) {
				writeIndexFile(datasetAry, destfile);
				datasetAry.clear();
			}
		}

		if (datasetAry.size()>0) {
			writeIndexFile(datasetAry, destfile);
			datasetAry.clear();
		}
*/

/*		
		for (int i = 0; i < datasetAry.size(); i++) {
			String[] oneDataset = datasetAry.get(i);
			for (int j = 0; j < oneDataset.length; j++)
				System.out.print(oneDataset[j] + "\t");
			System.out.println();
		}
*/		

//		readIndexFile(datasetAry, destfile + "_index_3");

		ArrayList<Hashtable<String, ArrayList<String>>> propertyList = new ArrayList<Hashtable<String, ArrayList<String>>>();
		ArrayList<ArrayList<String>> propertyAryList = new ArrayList<ArrayList<String>>();

		try {
			File file = new File(destfile + "_index_2");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String line = "";
			count = 0;
			while ((line = reader.readLine()) != null) {
				count++;
				if (count < 225001) 
					continue;
				StringTokenizer st = new StringTokenizer(line, "\t");
				String[] oneDataset = {"", ""};
				oneDataset[0] = st.nextToken();
				oneDataset[1] = st.nextToken();
				datasetAry.add(oneDataset);
				Hashtable<String, ArrayList<String>> propertyTable = new Hashtable<String, ArrayList<String>>();
				ArrayList<String> propertyAry = parseOneDatasetPage(oneDataset[1], propertyTable);
				propertyList.add(propertyTable);
				propertyAryList.add(propertyAry);
			
								
				if (count % 5000 == 0) {
					ArrayList<String> propertySet = MergePropertyName(propertyAryList);
					String outfilename = destfile + "_2_" + (count/5000);
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfilename), "UTF-8"));
					writeHead(writer, propertySet);
					for (int i = 0; i < datasetAry.size(); i++) {
						writeALine(writer, datasetAry.get(i), propertySet, propertyList.get(i));
					}
				
					writer.close();
					datasetAry.clear();
					propertyList.clear();
					propertyAryList.clear();
				}

			}
			reader.close();
			
			if (datasetAry.size()>0) {
				ArrayList<String> propertySet = MergePropertyName(propertyAryList);
				String outfilename = destfile + "_2_" + (count/5000+1);
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfilename), "UTF-8"));
				writeHead(writer, propertySet);
				for (int i = 0; i < datasetAry.size(); i++) {
					writeALine(writer, datasetAry.get(i), propertySet, propertyList.get(i));
				}
				writer.close();
			}
			
		
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}  catch (IOException e) {
			e.printStackTrace();
		}	

	}

	
	private void writeIndexFile(ArrayList<String[]> datasetAry, String destfile) {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destfile, true), "UTF-8"));
			for (int i = 0; i < datasetAry.size(); i++) {
				String[] oneDataset = datasetAry.get(i);
				writer.write(oneDataset[0] + "\t"  + oneDataset[1]);
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readIndexFile(ArrayList<String[]> datasetAry, String indexFile) {
		try {
			File file = new File(indexFile);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String line = "";
			while ((line = reader.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, "\t");
				String[] oneDataset = {"", ""};
				oneDataset[0] = st.nextToken();
				oneDataset[1] = st.nextToken();
				datasetAry.add(oneDataset);
			}
			reader.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}  catch (IOException e) {
			e.printStackTrace();
		}	
	}

	
	private ArrayList<String> parseOneDatasetPage(String urlStr, Hashtable<String, ArrayList<String>> propertyTable) {
		try {
			System.out.println(urlStr);
			URL url = new URL(urlStr);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
	 		int flag = 0;
			String line;
			String content = "";
			while ((line = reader.readLine()) != null) {
	           	
	          	switch (flag) {
	          	case 0: int index = line.indexOf("<article id=\"main\"");
      					if (index < 0)  
      						break; 
      					else {
      						flag = 1;
      						line = line.substring(index)+ " ";
      					}
	          	case 1: index = line.indexOf("<aside class=\"facettes\">");
      					if ( index >= 0) 
      						flag = 2;
      					else {
      							content += line + " ";
     					}
	         	}
			}
			reader.close();

//			System.out.println(content);
			content = formatContent(content);
			content = eliminateComments(content);
//			content = eliminateScripts(content);
			return parseDatasetContent(content, propertyTable, urlStr);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}  catch (IOException e) {
			e.printStackTrace();
		}	
		return null;
	}


	private ArrayList<String> parseDatasetContent(String content, Hashtable<String, ArrayList<String>> propertyTable, String pageUrl) {
		ArrayList<String> propertyAry = new ArrayList<String>();
	
		String separator = "<details open>";
		int index = content.indexOf(separator);
		content = content.substring(index);

		separator = "<section>";
		index = content.indexOf(separator);
		String firstStr = content.substring(0, index);
		String secondStr = content.substring(index);
		
//		System.out.println(firstStr);
		parseFirstPart(firstStr, propertyAry, propertyTable);
//		System.out.println(secondStr);
		parseSecondPart(secondStr, propertyAry, propertyTable, pageUrl);
		return propertyAry;
	}
	
	private void parseFirstPart(String content, ArrayList<String> propertyAry,  Hashtable<String, ArrayList<String>> propertyTable) {
		String separator  = "</summary>";
		int index = content.indexOf(separator);
		String summaryContent = content.substring(0, index);
		content = content.substring(index);

		separator  = "</time>";
		index = summaryContent.indexOf(separator) + separator.length();
		String timeStr = summaryContent.substring(0, index).trim();
		summaryContent = summaryContent.substring(index).trim();
		String[] timeProperty = parseOneFieldWithoutLink(timeStr);
//		System.out.println("pubdate" + "\t" + timeProperty[0]);
		addAProperty("pubdate", timeProperty[0], propertyAry, propertyTable);
		
		separator  = "<br/>";
		index = summaryContent.indexOf(separator) ;
		String publisherStr = summaryContent.substring(1, index).trim();
		summaryContent = summaryContent.substring(index).trim();
		String[] publisherProperty = parseOneFieldWithoutLink(publisherStr);
//		System.out.println("publisher" + "\t" + publisherProperty[0]);
		addAProperty("publisher", publisherProperty[0], propertyAry, propertyTable);
		
		String[] descProperty = parseOneFieldWithoutLink(summaryContent);
		descProperty[0] = descProperty[0].replaceAll("\"", "\"\"");
//		System.out.println("description" + "\t" + descProperty[0]);
		addAProperty("description", descProperty[0], propertyAry, propertyTable);
		
		separator  = "<dl>";
		String[] subStr = content.split(separator);
		for (int i = 1; i < subStr.length; i++) {
//			System.out.println(separator + subStr[i]);
			String[] oneProperty =  parseDatasetOneRow(separator + subStr[i], "</dt>");
			if (oneProperty[0].length() > 0) {
//				System.out.println(oneProperty[0] + "\t" + oneProperty[1]);
				addAProperty(oneProperty[0], oneProperty[1], propertyAry, propertyTable);
			}
		}
	}
	
	
	private String formatURL (String url, String mainUrl) {
		String newUrl = "";
		if (url.length() > 0)  
			if (! url.startsWith("http") && !url.startsWith("ftp")) {
				int index = mainUrl.lastIndexOf('/');
				String rootUrl = mainUrl.substring(0, index);
				newUrl = rootUrl + "/" + url;
			} else 
				newUrl = url;
		return newUrl;
	}
	
	private void parseSecondPart(String content, ArrayList<String> propertyAry,  Hashtable<String, ArrayList<String>> propertyTable, String pageUrl) {
		String separator  = "<h6>";
		int index = content.indexOf(separator);
		String downloadLine = content;
		if (index >= 0) {
			downloadLine = content.substring(0, index);
			content = content.substring(index);
		} 
		
		separator  = "<p class=";
		String[] subStr = downloadLine.split(separator);
		String[] typeValue =  parseDatasetOneRow(separator + subStr[1], "</span>");
//		System.out.println(typeValue[0] + "\t" + typeValue[1]);
		addAProperty(typeValue[0], typeValue[1], propertyAry, propertyTable);
		
		if (subStr.length >= 3) {
			String[] formatValue =  parseDatasetOneRow(separator + subStr[2], "<span>");
	//		System.out.println(formatValue[0] + "\t" + formatValue[1]);
			addAProperty(formatValue[0], formatValue[1], propertyAry, propertyTable);
		
			String[] downloadProperty = parseOneFieldWithLink(separator + subStr[3]);
			downloadProperty[1] = formatURL(downloadProperty[1], pageUrl);
//			System.out.println(downloadProperty[0] + "\t" + downloadProperty[1]);
			addAProperty(downloadProperty[0], downloadProperty[1], propertyAry, propertyTable);
		}
		
		if (index < 0)
			return;

		index = content.indexOf("</h6>");
		String tagLine = content.substring(0, index);
		content = content.substring(index);
		
		String[] tagStr = parseOneFieldWithoutLink(tagLine);
		separator  = "<li>";
		String tagValue = "";
		subStr = content.split(separator);
		for (int i = 1; i < subStr.length; i++) {
//			System.out.println(separator + subStr[i]);
			String[] oneValue =  parseOneFieldWithoutLink(separator + subStr[i]);
			if (tagValue.length() == 0)
				tagValue += oneValue[0];
			else
				tagValue += " , " + oneValue[0];
		}
		if (tagValue.length() == 0) {
			String[] oneValue =  parseOneFieldWithoutLink(content);
			tagValue = oneValue[0];
		}
//		System.out.println(tagStr[0] + "\t" + tagValue);
		addAProperty(tagStr[0], tagValue, propertyAry, propertyTable);
	}
	
	
	
	
	private void addAProperty(String property, String value, ArrayList<String> propertyAry, Hashtable<String, ArrayList<String>> propertyTable) {		propertyAry.add(property);
		ArrayList<String> valueAry = new ArrayList<String>();
		valueAry.add(value);
		propertyTable.put(property, valueAry);

	}
	
		
	private String[] parseDatasetOneRow (String line, String separator) {
		String[] oneRow = {"", ""};
		
		int index = line.indexOf(separator) ; 
		if (index < 0)
			return oneRow;
		index += separator.length();
		String labelLine = line.substring(0, index);
//		System.out.println(labelLine);
		String[] labelAry = parseOneFieldWithoutLink(labelLine);
		oneRow[0] = labelAry[0];
		if (oneRow[0].endsWith(":")) {
			int length = oneRow[0].length();
			oneRow[0] = oneRow[0].substring(0, length-1).trim();
		}
		
		String valueLine = line.substring(index);
//		System.out.println(valueLine);
		String[] valueAry = parseOneFieldWithoutLink(valueLine);
		oneRow[1] = valueAry[0];
		
//		System.out.println(oneRow[0] + "\t" + oneRow[1]);
		return oneRow;
	}
	
	private ArrayList<String> MergePropertyName (ArrayList<ArrayList<String>> propertyAryList ) {
		HashSet<String> propertySet = new HashSet<String>();
		ArrayList<String> propertyAry = propertyAryList.get(0);
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
			System.out.println(propertySet);
			System.out.println(propertyAry);
		} else {
			System.out.println(propertyAry);
		}
		return propertyAry;
	}
	
	private void writeHead (BufferedWriter writer, ArrayList<String> propertySet) throws IOException{
		writer.write("\"" + "Title" + "\"");
		writer.write(","+ "\"" + "url" + "\"");
				
		for (int i = 0; i < propertySet.size(); i++) {
			writer.write("," + "\"" + propertySet.get(i) + "\"");
		}

		writer.newLine();
	}

	private void writeALine (BufferedWriter writer, String[] datasetAry, ArrayList<String> propertySet, Hashtable<String, ArrayList<String>> propertyTable) throws IOException{
		writer.write("\"" + datasetAry[0] + "\"");
		writer.write("," + "\"" + datasetAry[1] + "\"");
			
		for (int i = 0; i < propertySet.size(); i++) {
			String property = propertySet.get(i);
			String value  = "";
			if (propertyTable != null) { 
				if (propertyTable.containsKey(property)) {
					ArrayList<String> propertyAry = propertyTable.get(property);
					for (int j = 0; j < propertyAry.size(); j++) {
						String oneValue = propertyAry.get(j).trim();
						if (oneValue.length() > 0) {
							if (value.length() > 0) 
								value += ";";
							value +=  oneValue ;
						}
					}
				}
			}
			
			writer.write("," + "\"" + value + "\"");
		}

		writer.newLine();
	}

	private String eliminateComments(String content) {
		String newContent = "";
		String startTag = "<!--";
		String endTag = "-->";
		int startIndex = content.indexOf(startTag);
		while ( startIndex >= 0) {
			int endIndex = content.indexOf(endTag);
			newContent += content.substring(0, startIndex) + " ";
			content = content.substring(endIndex + endTag.length()).trim();
			startIndex = content.indexOf(startTag);
		}
		newContent += content;
		return newContent.trim();
	}

	private String eliminateScripts(String content) {
		String newContent = "";
		String startTag = "<script ";
		String endTag = "</script>";
		int startIndex = content.indexOf(startTag);
		while ( startIndex >= 0) {
			int endIndex = content.indexOf(endTag);
			newContent += content.substring(0, startIndex) + " ";
			content = content.substring(endIndex + endTag.length()).trim();
			startIndex = content.indexOf(startTag);
		}
		newContent += content;
		return newContent.trim();
	}
	
	
	private Hashtable<String, ArrayList<String>> findFileWithSameHead(File dir) {
		Hashtable<String, ArrayList<String>> resultTable = new Hashtable<String, ArrayList<String>>();
		try {
			File[] fileAry = dir.listFiles();
			for (int i = 0; i < fileAry.length; i++) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileAry[i]), "UTF-8"));
				String line = reader.readLine();
				reader.close();
				ArrayList<String> indexAry = new ArrayList<String>();
				if (resultTable.containsKey(line)) 
					indexAry = resultTable.get(line);
				indexAry.add(fileAry[i].getName());
				resultTable.put(line, indexAry);
			}
			Iterator<String> iter = resultTable.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				System.out.println(key);
				ArrayList<String> fileList = resultTable.get(key);
				for (int i = 0; i < fileList.size(); i++) {
					System.out.println("\t" + fileList.get(i));
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}  catch (IOException e) {
			e.printStackTrace();
		}	
		
		return resultTable;
	}
	
	public static void main(String[] args) {
		String mainUrl = "http://www.data.gouv.fr";
		String pageUrl ="/content/search?SearchText=%23all&Type=data";
		String destDir = "C:\\sym\\RPI\\data_gov\\csv";
		String destfile = destDir + "\\" + "data.gouv.fr.csv";
		data_gouv_fr test = new data_gouv_fr();
//		test.processCatalog(mainUrl, pageUrl, destfile);
		
		File combineDir = new File(destDir, "data.gouv.fr_combine");
		test.findFileWithSameHead(combineDir);
	}
}

