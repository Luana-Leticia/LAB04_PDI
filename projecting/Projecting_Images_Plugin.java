package projecting;

import java.io.File;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.io.Opener;
import ij.plugin.PlugIn;
import ij.process.ImageConverter;

/**
 * This plugIn open a set of images and call a class that applies geometric
 * transformations in these images. Then, save and show the results.
 * 
 * @authors: Luana Letícia de Souza Silva
 * 
 * Reference: Digital Images Processing - Wilhelm Burger.
 * 
 */

public class Projecting_Images_Plugin implements PlugIn {

	public Projecting_Images_Plugin() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		// Call dialog to get paths
		GenericDialog gd = createInfoDialog();

		// Get and store the passed paths
		String imagesFolderPath = gd.getNextString() + "/";
		String editedImagesFolderPath = gd.getNextString() + "/";

		// Get list of filenames in images folder
		File file = new File(imagesFolderPath);
		String[] fileList = file.list();

		// Initialize ImageJ class to open images and class to edit images
		Opener op = new Opener();
		Projective_Transform pt = new Projective_Transform();

		// Iterate to edit each image passed folder and save and show edited
		for (String filename : fileList) {
			ImagePlus imp = op.openImage(imagesFolderPath + filename);			
			// Convert input image to 8-bit gray scale
			ImageConverter ic = new ImageConverter(imp);
			ic.convertToGray8();
			// Get transformed image, save and show
			ImagePlus edited = pt.projecting(imp);	
			IJ.save(edited, editedImagesFolderPath + "Edited_" + filename);
			edited.setTitle("Edited_" + filename);
			edited.show();
			// Close input image
			imp.close();			
		}
	}

	// Method create generic dialog to get folders of images paths
	private GenericDialog createInfoDialog() {
		GenericDialog gd = new GenericDialog("Images folders selection");
		gd.addMessage("Tell the folder's path of image(s) to edit:");
		gd.addStringField("Path:", "C:/Users/luana/Downloads/LABS/LAB04/imagens-originais", 70);
		gd.addMessage("Tell the folder's path to save the transformed image(s):");
		gd.addStringField("Path:", "C:/Users/luana/Downloads/LABS/LAB04/imagens-transformadas", 70);
		gd.addMessage("\n(please check if slash orientation is as default:  '/' ) \n\n");
		gd.pack();
		gd.showDialog();
		return gd;
	}
	
}
