package application;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class NewImagesController {

    private final String imagePath = "images/";
	
	private final String dataPath = "data/"; 
	private NodeList nd;
	private GreenConnection gc;
	private final int IMG_MAX_X = 280;
	private final int IMG_MAX_Y = 280;
	private CheckBox[] boxes;
	private Image[] imgs;
	private ChoiceBox<String>[] ratings;
	private Stage stage;

	@FXML
	private Button selectAll;

	@FXML
	private Button apply;

	@FXML
	private AnchorPane MainPane;

	@FXML
	private ScrollPane scrollPane;

	@FXML
	private AnchorPane scrollPaneContent;

	public NewImagesController () {

		this.gc = new GreenConnection();
	}

	public void setNodeList(NodeList list) {

		this.nd = list; 
	}

	public int getIMG_MAX_X() {
		return IMG_MAX_X;
	}

	public int getIMG_MAX_Y() {
		return IMG_MAX_Y;
	}

	public AnchorPane getScrollAnchor() {
		return scrollPaneContent;
	}

	public void setBoxes(CheckBox[] b) {
		this.boxes = b;
	}

	public void setImages(Image[] i) {
		this.imgs = i;
	}

	public void setRatings (ChoiceBox<String>[] r) {
		this.ratings = r;
	}

	public GreenConnection getGC () {
		return gc;
	}

	public void setStage (Stage s) {
		this.stage = s ;
	}

	@FXML
	private void handleSelectAll() {

		for (CheckBox b : boxes) {
			if(b.isSelected())
				b.setSelected(false);
			else {
				b.setSelected(true);
			}
		}
	}

	@FXML
	private void handleApplySave() {

		File downloads = new File (imagePath + "ImagesDB.txt");
		String id = null;
		String tags = null;
		String rating = null;
		Node node = null;
		Element element;

		if (!downloads.exists()) {

			try {
				if (downloads.createNewFile()) {
					System.out.println("File ImagesDB.txt created successfully.");
				} else {
					System.out.println("Error creating ImagesDB.txt.");
				}
			} catch (IOException e) {
				System.out.println("An error occurred while creating ImagesDB.txt.");
				e.printStackTrace();
			}

			if (!downloads.exists()) {
				System.out.println("Error accessing ImagesDB.txt");
				return;
			}
		}


		try (BufferedWriter writer = new BufferedWriter(new  FileWriter(downloads, true))) {

			for (int i = 0 ; i < boxes.length; i++) {
				if(boxes[i].isSelected() && imgs[i] != null) {
					System.out.println(i);

					try {

						node = nd.item(i);
						element = (Element) node;
						id = element.getAttribute("id");
						tags = element.getAttribute("tags");
						rating = ratings[i].getValue();
						System.out.println(tags);

						File outputFile = new File(imagePath + id);
						BufferedImage bImage = SwingFXUtils.fromFXImage(imgs[i], null);
						ImageIO.write(bImage, "png", outputFile); // Adjust "jpg" according to the desired image format

						writer.write(id + " ; " + rating + " ; " + tags);
						writer.newLine();
						System.out.println(id + " ; " + rating + " ; " + tags);
						System.out.println("Image saved locally: " + Integer.toString(i));
						System.out.println("Image saved locally: " + outputFile.getAbsolutePath());
						stage.close();
					} catch (IOException e) {
						System.err.println("Error saving image: " + e.getMessage());
					}
				}

			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}


