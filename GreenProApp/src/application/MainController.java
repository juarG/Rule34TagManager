package application;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;

public class MainController {
	
	private final String imagePath = "images/";
	
	private final String dataPath = "data/"; 
	
	private String searchTag;
	
	private List<Image> tops;
	
	private TextField[] textFields;
	
	private TextArea[] textAreas;
	

	private Boolean showTagsInitialized = false;

	@FXML
	private Button showTags;

	@FXML
	private AnchorPane anchorPaneTags;

	@FXML
	private ScrollPane scrollPaneTags;

	@FXML
	private void openAddTags() throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddTags.fxml"));
		loader.setController(new AddTagsController());
		Parent root = loader.load();
		Stage stage = new Stage();
		stage.setScene(new Scene(root));
		stage.show();
	}


	@FXML
	private TextField searchText;

	@FXML
	private void handleSaveNewImages () throws IOException, SAXException, ParserConfigurationException {

		String tag = searchText.getText();
		GreenConnection gc = new GreenConnection();

		Document tagValue = gc.GetTag(tag);
		Element rootElementTag = tagValue.getDocumentElement();
		NodeList tagsNodes = rootElementTag.getElementsByTagName("tag");

		if (tagsNodes.getLength() == 0) {
			loadMessage("Invalid Tag.");
			return;
		}

		NodeList posts = gc.getHIS(tag);


		NewImagesController nIC = new NewImagesController();
		nIC.setNodeList(posts);

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/NewImages.fxml"));
		loader.setController(nIC);
		Parent root = loader.load();
		Stage stage = new Stage();
		nIC.setStage(stage);
		placeImages(posts,nIC); 


		stage.setScene(new Scene(root));
		stage.show();

	}

	private void placeImages (NodeList nd , NewImagesController nIC) throws IOException {

		int columns = 3;
		int n = nd.getLength();
		Image[] images = getImages(nd,nIC);
		ImageView[] views = new ImageView[n];
		CheckBox[] checkBoxes = new CheckBox[n];
		ChoiceBox<String>[] rating = new ChoiceBox[n];
		nIC.setBoxes(checkBoxes);
		nIC.setImages(images);
		nIC.setRatings(rating);

		for (int i = 0; i < n; i++) {
			views[i] = new ImageView(images[i]);
			views[i].setPreserveRatio(true);
			views[i].setFitWidth(nIC.getIMG_MAX_X());
			views[i].setFitHeight(nIC.getIMG_MAX_Y());

			if (i == 0 || (i % columns) == 0) {
				views[i].setLayoutX(20);
				if (i < columns) {
					views[i].setLayoutY(30);
				} else {
					views[i].setLayoutY(views[i - columns].getLayoutY() + views[i - columns].getFitHeight() + 60);
				}
			} else {
				views[i].setLayoutX(views[i - 1].getLayoutX() + views[i - 1].getFitWidth() + 30);
				if (i < 4) {
					views[i].setLayoutY(30);
				} else {
					views[i].setLayoutY(views[i - columns].getLayoutY() + views[i - columns].getFitHeight() + 60);
				}
			}

			checkBoxes[i] = new CheckBox();
			checkBoxes[i].setLayoutX(views[i].getLayoutX());
			checkBoxes[i].setLayoutY(views[i].getLayoutY() + views[i].getFitHeight() + 5);

			rating[i] = new ChoiceBox<>(FXCollections.observableArrayList());

			for (int j = 1; j <= 20; j++) {
				rating[i].getItems().add(String.valueOf(j));
			}

			rating[i].setLayoutX(views[i].getLayoutX() + 30);
			rating[i].setLayoutY(views[i].getLayoutY() + views[i].getFitHeight() + 5);


			nIC.getScrollAnchor().getChildren().add(views[i]);
			nIC.getScrollAnchor().getChildren().add(checkBoxes[i]);
			nIC.getScrollAnchor().getChildren().add(rating[i]);

		}

	}

	protected void loadMessage (String s) throws IOException {

		Parent root = FXMLLoader.load(getClass().getResource("/Message.fxml"));
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		AnchorPane anchor = (AnchorPane) root;


		VBox myVBox = (VBox) anchor.getChildren().get(0);
		TextArea messageText = (TextArea) myVBox.getChildren().get(0);
		messageText.setText(s);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.show();

	}

	private Image[] getImages (NodeList nd, NewImagesController nIC) throws IOException {

		List<String> fileUrls = new ArrayList<>();
		BufferedImage preImg;
		Image[] imgs = new Image[27];

		for (int i = 0; i < nd.getLength(); i++) {
			Node postNode = nd.item(i);

			if (postNode.getNodeType() == Node.ELEMENT_NODE) {
				Element postElement = (Element) postNode;

				// Get the file_url attribute value
				String fileUrl = postElement.getAttribute("file_url");
				if (!fileUrl.isEmpty()) {

					if (fileUrl.contains(".mp4") || fileUrl.contains(".gif")) {
						String resourcePath = imagePath + "InvalidFormat.png";
						File file = new File(resourcePath);
						if (!file.exists()) {
						    System.out.println("Resource not found: " + resourcePath);
						} else {
						    System.out.println("Resource found: " + resourcePath);
						}
						
						BufferedImage bufferedImage = null;
						try {
							bufferedImage = ImageIO.read(file);
						} catch (IOException e) {
							e.printStackTrace();
						}
						imgs[i] =  SwingFXUtils.toFXImage(bufferedImage, null);
						System.out.println("invalid format");

					} else {
						fileUrls.add(fileUrl);
						System.out.println(fileUrl);
						preImg = nIC.getGC().DownloadImage(fileUrl, Integer.toString(i).concat(fileUrl.substring(fileUrl.length()-4)));
						imgs[i] = SwingFXUtils.toFXImage(preImg, null);
					}

				}
			}

		}
		return imgs;


	}

	@FXML
	public void handleShowTags () throws IOException {

		int n = 5;

		if (scrollPaneTags.isVisible()) {
			scrollPaneTags.setVisible(false);
			scrollPaneTop.setVisible(false);
			scrollPaneSaved.setVisible(false);
			System.out.println("Tags set to invisible");
		} else {
			scrollPaneTags.setVisible(true);
			scrollPaneTop.setVisible(false);
			scrollPaneSaved.setVisible(false);
			System.out.println("Tags set to visible");
		}

		checkInitialized();
		List<List<String>> tags = getTags();
		
		for(int i = 0 ; i < 5 ; i++) {
			if(tags == null || tags.get(i).isEmpty()) {
				textAreas[i].setText("No Tags yet");
			} else {
				textAreas[i].setText(tags.get(i).toString());
			}
		}
        //TODO as some files have empty lines there are some extra ","

	}
	
	private List<List<String>> getTags() throws IOException {
		
		List<List<String>> tags = new ArrayList<>();
		List<String> general = new ArrayList<>();
		List<String> artist = new ArrayList<>();
		List<String> copyright = new ArrayList<>();
		List<String> character = new ArrayList<>();
		List<String> meta = new ArrayList<>();
		
		tags.add(general);
		tags.add(artist);
		tags.add(copyright);
		tags.add(character);
		tags.add(meta);
		
		File[] files = new File[5];
		String path;
		for(int i = 0; i < 5; i++) {
			
			path = dataPath + nameIt(i)+ "Tags";
			files[i] = new File(path);
			
			if(!files[i].exists()) {
				System.out.println(path + " file does not exist");
				return null;
			}
			
			BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            
            if(path.contains("General")) {
            	
            	while ((line = reader.readLine()) != null) {
                    String[] lineWords = line.split("\\s+");

                    lineWords = findRepeatedWords(lineWords,5);
                    
                    for (String word : lineWords) {
                        tags.get(i).add(word);
                    }
                }
            } else {
            	while ((line = reader.readLine()) != null) {
                    String[] lineWords = line.split("\\s+");

                    for (String word : lineWords) {
                        tags.get(i).add(word);
                    }
                }
            }

            // Close the reader
            reader.close();
			
		}
		
		
		return tags;
	}

	private void checkInitialized () {
		
		int n = 5;

		if (!showTagsInitialized) {

			TextField[] titles = new TextField[5]; // 1 general 2 artist 3 copy 4 char 5 meta
			TextArea[] areas = new TextArea[5];
			
			textFields= titles;
			textAreas = areas;

			for (int i = 0; i < titles.length; i++) {
				titles[i] = new TextField(nameIt(i));
				areas[i] = new TextArea("one");
				titles[i].setStyle("-fx-background-color: transparent; -fx-font-size: 24px; -fx-text-fill: #DBE7C9;");
				areas[i].setStyle("-fx-control-inner-background: #50623A; -fx-font-size: 18px;");
				titles[i].setEditable(false);
				areas[i].setEditable(false);

				areas[i].setWrapText(true);

				titles[i].setPrefWidth(200); // Optional: Set preferred width
				areas[i].setPrefWidth(500);  // Optional: Set preferred width

				// Add to the AnchorPane first
				anchorPaneTags.getChildren().add(titles[i]);
				anchorPaneTags.getChildren().add(areas[i]);

				// Use final variable to capture current index i
				final int index = i;

				// Use boundsInParentProperty listener to wait for the layout pass
				titles[i].boundsInParentProperty().addListener((obs, oldBounds, newBounds) -> {
					if (newBounds.getHeight() != 0) { // Ensure height is valid
						double newY;
						if (index == 0) {
							// Set initial position for the first element
							newY = n;
						} else {
							// Set position based on the previous elements
							newY = areas[index - 1].getLayoutY() + areas[index - 1].getHeight() + n;
						}
						// Set positions
						titles[index].setLayoutY(newY);
						areas[index].setLayoutY(newY + titles[index].getHeight() + n);
					}
				});
			}

			showTagsInitialized = true; // Mark as initialized
		}
	}

	private String nameIt (int i) {
		switch (i) {
		case 0: 
			return "General";
		case 1:
			return "Artist";
		case 2: 
			return "Copyright";
		case 3:
			return "Character";
		case 4:
			return "Meta";
		default:
			return "null";
		}
	}
	
	public static String[] findRepeatedWords(String[] words , int n) {
        // HashMap to store word frequencies
        Map<String, Integer> wordFrequency = new HashMap<>();

        // Count occurrences of each word
        for (String word : words) {
            wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
        }

        // List to collect words repeated 5 or more times
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : wordFrequency.entrySet()) {
            if (entry.getValue() >= n) {
                result.add(entry.getKey());
            }
        }

        // Convert List to String array
        String[] repeatedWordsArray = result.toArray(new String[0]);

        return repeatedWordsArray;
    }
	
	@FXML
	private Button topImages;
	
	@FXML 
	private ScrollPane scrollPaneTop;
	
	@FXML
	private AnchorPane anchorPaneTop;
	
	@FXML
	public void handleGetTop () throws IOException {
		
		if (scrollPaneTop.isVisible()) {
			scrollPaneTop.setVisible(false);
			scrollPaneSaved.setVisible(false);
			scrollPaneTags.setVisible(false);
		} else {
			scrollPaneTop.setVisible(true);
			scrollPaneSaved.setVisible(false);
			scrollPaneTags.setVisible(false);
		}
		
		if (tops != null) {
			placeTopImages(tops, anchorPaneTop);
			System.out.println("Top images from last scan.");
			return;
		}
		
		
		String path = imagePath +"ImagesDB.txt";
		String line;
		
		HashMap<String,String> map = new HashMap<String,String>();
		
		File file = new File(path);
		
		
		if (!file.exists()) {
			
			loadMessage("There are no saved images");
			
			return;
		}
		
		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			
			while ((line = reader.readLine()) != null) {
			    
				if(!line.isEmpty() && !(line.split(";")[1].trim().equals("null"))) {
					map.put(line.split(";")[0], line.split(";")[1].trim());
				}
			
			}
			
			reader.close();
			
		}
		
		List<String> top10Keys = getTop10HighestKeys(map);
		List<Image> images = getImageFiles(top10Keys);
		tops = images;
		placeTopImages(images, anchorPaneTop);
		
	}
	
	private static List<String> getTop10HighestKeys(HashMap<String, String> map) {
        // Convert map entries to a list
        List<Entry<String, String>> entries = new ArrayList<>(map.entrySet());

        // Sort the list based on the integer value of the map's values
        entries.sort((e1, e2) -> Integer.compare(Integer.parseInt(e2.getValue()), Integer.parseInt(e1.getValue())));

        // Extract the top 10 keys
        List<String> top10Keys = new ArrayList<>();
        for (int i = 0; i < Math.min(10, entries.size()); i++) {
            top10Keys.add(entries.get(i).getKey());
        }

        return top10Keys;
    }
	
	private List<Image> getImageFiles(List<String> names){
		
		List<Image> files = new ArrayList<>();
		
		for(String s : names) {
			File f = new File(imagePath + s.trim());
			if(f.exists()) {
				files.add(new Image (f.toURI().toString()));
			} else {
				System.out.println("IEEEEEEEEEEEEEEEEEEW");
			}
		}
		
		return files;
	}
	
	private void placeTopImages(List<Image> images, AnchorPane anchor) {
		
		int MAX = 280;
		int columns = 2;
		ImageView[] views = new ImageView[images.size()];
		
		int i = 0; //TODO this is so wrong
		for(Image img : images) {
			views[i] = new ImageView(img);
			i++;
		}
		
		anchor.getChildren().clear();
		for(i = 0; i < images.size();i++) {
			
			
			views[i].setPreserveRatio(true);
			views[i].setFitWidth(MAX);
			views[i].setFitHeight(MAX);
			
			if (i == 0 || (i % columns) == 0) {
				views[i].setLayoutX(20);
				if (i < columns) {
					views[i].setLayoutY(30);
				} else {
					views[i].setLayoutY(views[i - columns].getLayoutY() + views[i - columns].getFitHeight() + 60);
				}
			} else {
				views[i].setLayoutX(views[i - 1].getLayoutX() + views[i - 1].getFitWidth() + 30);
				if (i < columns + 1) {
					views[i].setLayoutY(30);
				} else {
					views[i].setLayoutY(views[i - columns].getLayoutY() + views[i - columns].getFitHeight() + 60);
				}
			}
			
			anchor.getChildren().add(views[i]);
			
		}
		
		
		
	}
	
	@FXML 
	private ScrollPane scrollPaneSaved;
	
	@FXML
	private AnchorPane anchorPaneSaved;
	
	@FXML
	public void handleSeachImages () throws IOException {
		System.out.println("hey");
		
		String tag = searchText.getText();
		
		if (scrollPaneSaved.isVisible() && tag.equals(searchTag)) {
			System.out.println("Images already loaded for this tag");
			return;
		}
		
		searchTag = tag; 
		String path = imagePath + "ImagesDB.txt";
		File file = new File(path);
		
		if(!file.exists()) {
			
			loadMessage("ImageDB file not found");
			return;
		} 
		
		List<String> imagesIDs = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			
            String line;
            String imageTags;
            
            while ((line = br.readLine()) != null) {
                imageTags = line.split(";")[2];
                
                if(imageTags.contains(tag)) {
                	imagesIDs.add(line.split(";")[0]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            loadMessage("Error reading the file");
        }
		
		List<Image> images = getImageFiles(imagesIDs);
		
		
		placeTopImages(images , anchorPaneSaved);
		
		System.out.println("before vis");
		scrollPaneSaved.setVisible(true);
		scrollPaneTop.setVisible(false);
		scrollPaneTags.setVisible(false);
		
		
	}
}
