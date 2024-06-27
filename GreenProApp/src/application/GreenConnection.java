package application;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class GreenConnection {

	private String apiUrl = "https://api.rule34.xxx/index.php?";
	private String limit;
	private String page;
	private String id;
	private String json;
	private String cid;
	private HttpURLConnection conn;

	public GreenConnection () {}

	public String getLimit() {
		return limit;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}
	
	public NodeList getHIS (String s) throws IOException, SAXException, ParserConfigurationException {
		
		//https://api.rule34.xxx/index.php?page=dapi&s=post&q=index&tags="s"&limit=27
		URL url = new URL(apiUrl + "page=dapi&s=post&q=index&tags=" + s + "&limit=27");
		System.out.println("Api Posts url call : " + url);
		OpenConnection(url);
		StringBuilder sb = ConnectionResponse(conn);
		
		Document doc = ProcessResponse(sb.toString());
		NodeList nodeList = doc.getElementsByTagName("post");
		
		return nodeList;
	}

	public StringBuilder getImage() throws IOException, SAXException, ParserConfigurationException {

		//https://api.rule34.xxx/index.php?page=dapi&s=post&q=index&id=<post_id>
		URL url = new URL(apiUrl + "page=dapi&s=post&q=index&id=" + this.id);
		System.out.println(url);
		OpenConnection(url);
		StringBuilder sb = ConnectionResponse(conn);

		String fileUrl = ProcessImageResponse(sb.toString());
		DownloadImage(fileUrl,"image.jpg");

		return sb;

	}

	public List<List<String>> GetWrongPostIDs (String[] postIDs) throws IOException, SAXException, ParserConfigurationException {

		List<List<String>> organizedTags = new ArrayList<>();
		
		List<String> general = new ArrayList<>(); //0
		List<String> artist = new ArrayList<>(); //1
		List<String> questionMark = new ArrayList<>(); // curious to know what is tag type 2 ...
		List<String> copyright = new ArrayList<>(); //3
		List<String> character = new ArrayList<>(); //4
		List<String> meta = new ArrayList<>(); //5
		List<String> wrongIDs = new ArrayList<>(); //6
		
		organizedTags.add(general);
		organizedTags.add(artist);
		organizedTags.add(questionMark);
		organizedTags.add(copyright);
		organizedTags.add(character);
		organizedTags.add(meta);
		organizedTags.add(wrongIDs);
		
		for (String s : postIDs) {
			if (s.length() < 10) {
				URL url = new URL(apiUrl + "page=dapi&s=post&q=index&id=" + s);
				OpenConnection(url);
				StringBuilder sb = ConnectionResponse(conn);
				Document doc = ProcessResponse(sb.toString());

				Element rootElement = doc.getDocumentElement();
				int countValue = Integer.parseInt(rootElement.getAttribute("count"));
				if(countValue != 1) {
					wrongIDs.add(s);
				} else {
					organizedTags = GetTagsType (rootElement,organizedTags);
				}

			} else {
				wrongIDs.add(s);
			}
		}
		
		return organizedTags;
	}

	private List<List<String>> GetTagsType (Element rootElement,List<List<String>> organizedTags) throws IOException, SAXException, ParserConfigurationException {
		
		NodeList postNodes = rootElement.getElementsByTagName("post");
		String [] tagsList = ((Element)postNodes.item(0)).getAttribute("tags").split(" ");
		Document doc;
		String type;
		for (String tag : tagsList) {
			if(!tag.isEmpty()) {
				doc = GetTag(tag);
				Element rootElementTag = doc.getDocumentElement();
				NodeList tagsNodes = rootElementTag.getElementsByTagName("tag");
				if (tagsNodes.getLength() > 0) {
					Element tagsElement = (Element) tagsNodes.item(0);
					type = tagsElement.getAttribute("type");
					organizedTags.get(Integer.parseInt(type)).add(tag);
				} else {
					System.out.println("No <tags> elements found.");
				}
			}

		}


		return organizedTags;

	}

	public Document GetTag (String name) throws IOException, SAXException, ParserConfigurationException {

		//https://api.rule34.xxx/index.php?page=dapi&s=tag&q=index&name=<name>
		URL url = new URL(apiUrl + "page=dapi&s=tag&q=index&name=" + name);
		//System.out.println("Api Tag url call : " + url);
		OpenConnection(url);
		StringBuilder sb = ConnectionResponse(conn);

		return ProcessResponse(sb.toString());	
	}

	private void OpenConnection(URL url) throws IOException {

		this.conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");

	}

	public void closeConnection() {

		this.conn.disconnect();
	}

	private StringBuilder ConnectionResponse (HttpURLConnection conn) throws IOException {

		Scanner scanner = new Scanner(conn.getInputStream());
		StringBuilder response = new StringBuilder();
		while (scanner.hasNextLine()) {
			response.append(scanner.nextLine());
		}
		scanner.close();

		return response;
	}

	private String ProcessImageResponse(String xmlResponse) throws SAXException, IOException, ParserConfigurationException {

		Document document = ProcessResponse(xmlResponse);
		Element postElement = (Element) document.getElementsByTagName("post").item(0);
		String fileUrl = postElement.getAttribute("file_url");

		return fileUrl;
	}


	public BufferedImage DownloadImage(String imageUrl,String name) throws IOException {
		URL url = new URL(imageUrl);
		InputStream inputStream = url.openStream();
		//OutputStream outputStream = new FileOutputStream(name); // Save the image as "image.jpg"
		BufferedImage img = ImageIO.read(inputStream);
		
		//byte[] buffer = new byte[2048];
		//int length;
		//while ((length = inputStream.read(buffer)) != -1) {
		//	outputStream.write(buffer, 0, length);
		//}
 
		inputStream.close();
		//outputStream.close();
		
		long width = img.getWidth();
        long height = img.getHeight();

		System.out.println("Image (" + name + ")downloaded successfully.Total pixels : " + Long.toString(height*width));
		if (height*width > 10000000) {
			BufferedImage downsampledImg = downsampleImage(img);
            long newWidth = downsampledImg.getWidth();
            long newHeight = downsampledImg.getHeight();
            System.out.println("Downsampled Size: " + newWidth + "x" + newHeight + "; New total pixels : " + Long.toString(newWidth*newHeight));
			return downsampledImg;
		}
		return img;
	}

	public String GetPostComments() throws IOException, SAXException, ParserConfigurationException {

		//https://api.rule34.xxx/index.php?page=dapi&s=comment&q=index&post_id=<post_id>
		URL url = new URL(apiUrl + "page=dapi&s=comment&q=index&post_id=" + this.id);
		System.out.println(url.toString());
		OpenConnection(url);
		StringBuilder sb = ConnectionResponse(conn);

		String[] comments = ProcessCommentsResponse(sb.toString());

		return gatherSringElements(comments);

	}

	private Document ProcessResponse(String xmlResponse) throws SAXException, IOException, ParserConfigurationException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document document = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes("UTF-8")));

		return document;
	}

	private String[] ProcessCommentsResponse(String xmlResponse) throws SAXException, IOException, ParserConfigurationException {

		Document document = ProcessResponse(xmlResponse);

		NodeList commentNodes = document.getElementsByTagName("comment");
		String[] nodeTextArray = new String[commentNodes.getLength()];

		for (int i = 0; i < commentNodes.getLength(); i++) {

			Element commentElement = (Element) commentNodes.item(i);
			nodeTextArray[i] = commentElement.getAttribute("creator").concat(":").concat(commentElement.getAttribute("body"));

		}

		return nodeTextArray;
	}

	private String gatherSringElements (String[] strings) {

		StringBuilder result = new StringBuilder();
		for (String element : strings) {
			result.append(element).append("\n");
		}

		if (strings.length > 0) {
			result.setLength(result.length() - 1);
		}

		return result.toString();

	}
	
	public static BufferedImage downsampleImage(BufferedImage original) {
        int newWidth = original.getWidth() / 2;
        int newHeight = original.getHeight() / 2;
        BufferedImage downsampled = new BufferedImage(newWidth, newHeight, original.getType());

        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                // Calculate the average color of the 2x2 block
                int rgb1 = original.getRGB(x * 2, y * 2);
                int rgb2 = original.getRGB(x * 2 + 1, y * 2);
                int rgb3 = original.getRGB(x * 2, y * 2 + 1);
                int rgb4 = original.getRGB(x * 2 + 1, y * 2 + 1);

                Color color1 = new Color(rgb1);
                Color color2 = new Color(rgb2);
                Color color3 = new Color(rgb3);
                Color color4 = new Color(rgb4);

                int avgRed = (color1.getRed() + color2.getRed() + color3.getRed() + color4.getRed()) / 4;
                int avgGreen = (color1.getGreen() + color2.getGreen() + color3.getGreen() + color4.getGreen()) / 4;
                int avgBlue = (color1.getBlue() + color2.getBlue() + color3.getBlue() + color4.getBlue()) / 4;

                Color avgColor = new Color(avgRed, avgGreen, avgBlue);
                downsampled.setRGB(x, y, avgColor.getRGB());
            }
        }

        return downsampled;
    }



}
