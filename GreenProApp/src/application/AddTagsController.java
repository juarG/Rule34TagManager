package application;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;


public class AddTagsController {
	
    private final String imagePath = "images/";
	
	private final String dataPath = "data/"; 

	private GreenConnection gc;

	@FXML
	private TextArea textAreaAddTags;

	@FXML
	void handleGetTags() throws IOException, SAXException, ParserConfigurationException {

		String [] postIDs = verifyFormat(textAreaAddTags.getText());
		List<String> validIDs = new ArrayList<>();
		if(postIDs != null) {

			List<List<String>> organizedTags = gc.GetWrongPostIDs(postIDs);
			List<String> wrongIDs = organizedTags.get(6);

			//creates the file with all ids of saved images
			File IDsList = new File(dataPath + "IDsList");
			for(int i = 0; i < postIDs.length ; i++) {
				if(!wrongIDs.contains(postIDs[i])) {
					validIDs.add(postIDs[i]);
				}
			}
			updateFile(IDsList, validIDs);

			System.out.println("wrong ids: " + wrongIDs.toString());

			loadMessage(ChoseMessage(wrongIDs.toString()));

			organizedTags.remove(6);
			saveTags(organizedTags);
			System.out.println("Tags saved!");
		}

	}

	@FXML
	private TextField filePathField;

	public void initialize(URL url, ResourceBundle resourceBundle) {
		filePathField.setText(""); // sets the text to empty string
	}

	@FXML 
	private void handleGetTagsFile() throws IOException {

		String TagsFilePath = filePathField.getText();

		File tagsFile = new File(TagsFilePath);
		saveTagsFile(tagsFile);
	}


	@FXML 
	private void handleFileStructure() throws IOException {

		// Path to the file you want to open
		File structureFile = new File(dataPath + "StructureFile.txt");

		String currentWorkingDir = System.getProperty("user.dir");
		System.out.println("Current working directory: " + currentWorkingDir);

		// Define the correct relative path to the file
		Path filePath = Paths.get(dataPath + "StructureFile.txt");

		// Print the absolute path of the file to be read
		System.out.println("File path to read: " + filePath.toAbsolutePath());

		// Check if the file exists
		if (Files.exists(filePath)) {
			try {
				System.out.print("i am here");
				File structureFileFinal = filePath.toFile();
				// Use xdg-open to open the file
				ProcessBuilder pb = new ProcessBuilder("xdg-open", structureFileFinal.getAbsolutePath());
				Process process = pb.start();

				// Optionally, wait for the process to complete and check the exit value
				int exitCode = process.waitFor();
				System.out.println("Exit code: " + exitCode);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
			
			/*if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(structureFile);
                } else {
                    System.out.println("Open action not supported");
                }
            } else {
                System.out.println("Desktop not supported");
            }*/
		} else {
			System.out.println("File does not exist");
		}
	}

	@FXML
	private AnchorPane anchorPane;

	@FXML
	private void handleChoseFile () throws IOException {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Chose Tag File");
		Stage stage = (Stage) anchorPane.getScene().getWindow();

		try {
			File file = fileChooser.showOpenDialog(stage);
			// Create a filter for .txt files only
			FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("TXT Files", "*.txt");

			// Set the filter
			fileChooser.getExtensionFilters().add(extensionFilter);
			fileChooser.setSelectedExtensionFilter(extensionFilter); // TODO make it so that only txt files appear


			saveTagsFile(file);
		} catch (Exception e){
			System.out.println("No file chosen");
		}
	}


	public AddTagsController() {

		this.gc = new GreenConnection();
	}

	private String[] verifyFormat(String postIDs) throws IOException {

		String regex = "\\d+(,\\d+)*";

		boolean matches = Pattern.matches(regex, postIDs);

		if (matches && postIDs.split(",").length < 99) {
			System.out.println("The string consists of only numbers separated by commas.");
			return postIDs.split(",");
		} else {
			System.out.println(postIDs.split(",").length);
			System.out.println("The string does not match the pattern.");
			loadMessage("The IDs are not well formatted, please recheck and try again");

		}

		System.out.println("Content in TextArea: " + postIDs);
		return null; // TODO could make an  exception for this 

	}

	private void saveTags(List<List<String>> tags) {

		File generalFile = new File(dataPath + "GeneralTags");
		File artistsFile = new File(dataPath + "ArtistTags");
		File questionMarkfile = new File(dataPath + "QuestionMarkTags");
		File copyrightFile = new File(dataPath + "CopyrightTags");
		File characterFile = new File(dataPath + "CharacterTags");
		File metaFile = new File(dataPath + "MetaTags");

		updateFile(generalFile, tags.get(0));
		updateFile(artistsFile, tags.get(1));
		updateFile(questionMarkfile, tags.get(2));
		updateFile(copyrightFile, tags.get(3));
		updateFile(characterFile, tags.get(4));
		updateFile(metaFile, tags.get(5));
	}

	private void updateFile(File file,List<String> tags) { //TODO  change to not duplicate tags

		if (!file.exists()) {
			try {
				System.out.println(file.getName() + " created.");
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if(file.getName().equals("GeneralTags.txt") || file.getName().equals("GeneralTags") ) { // in general we need all even the repeated ones 
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
				bw.newLine();
				StringBuilder sb = new StringBuilder();
				for (String s : tags) {

					sb.append(s).append(" ");
				}
				bw.write(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {

			try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
					BufferedReader br = new BufferedReader(new FileReader(file))) {
				
				bw.newLine();

				StringBuilder sbTags = new StringBuilder();
				StringBuilder sbLines = new StringBuilder();

				String line;
				while ((line = br.readLine()) != null) {
					sbLines.append(line).append("\n");
				}

				String fileContents = sbLines.toString();

				for (String s : tags) {
					if(!fileContents.contains(s)) {
						sbTags.append(s).append(" ");
					}
				}
				bw.write(sbTags.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private String ChoseMessage(String s) {
		if (s.length() < 3) {
			return "All tags from the specified IDs where extracted successfully!";
		} else {
			return "The following IDs were not accepted: " + s + "\n Please recheck the IDs and try again.";
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

	private boolean choseFileUpdate(List<String> wordList, String fileName) throws IOException {

		switch(fileName) {
		case "GeneralTags.txt":

			File generalFile = new File("GeneralTags");
			updateFile(generalFile, wordList);

			return true;

		case "ArtistTags.txt":

			File artistsFile = new File("ArtistTags");
			updateFile(artistsFile, wordList);

			return true;

		case "QuestionMarkTags.txt":

			File questionMarkfile = new File("QuestionMarkTags");
			updateFile(questionMarkfile, wordList);

			return true;

		case "CopyrightTags.txt":

			File copyrightFile = new File("CopyrightTags");
			updateFile(copyrightFile, wordList);

			return true;

		case "CharacterTags.txt":

			File characterFile = new File("CharacterTags");
			updateFile(characterFile, wordList);

			return true;

		case "MetaTags.txt":

			File metaFile = new File("MetaTags");
			updateFile(metaFile, wordList);

			return true;
		default:
			loadMessage("Invalid File Name");
			return false;
		}

	}

	private void saveTagsFile (File tagsFile) throws IOException {

		if (tagsFile.exists()) {

			List<String> wordList = new ArrayList<>();
			try (Scanner scanner = new Scanner(tagsFile)) {
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					for (String word : line.split(" ")) {
						wordList.add(word);
					}
				}

				Boolean b = choseFileUpdate(wordList,tagsFile.getName());
				if(b) {
					loadMessage("New Tags added successfully !!");
				}
			} catch (FileNotFoundException e) {
				loadMessage("Invalid file path, please check or try a different one.");
			}
		} else {
			loadMessage("Invalid file path, please check or try a different one.");
		}
	}


}

