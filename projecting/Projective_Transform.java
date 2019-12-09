package projecting;

import java.awt.Point;
import Jama.Matrix;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.gui.WaitForUserDialog;
import ij.process.ImageProcessor;

/**
 * This class implements a routine that applies geometric transformations in
 * given images to project distorted documents in the screen plane.
 * 
 * @authors: Luana Letícia de Souza Silva
 * 
 *           Reference: Digital Images Processing - Wilhelm Burger.
 * 
 */

public class Projective_Transform {

	private ImageProcessor ip;
	private int w;
	private int h;
	private int new_h;
	private int new_w;
	private Point[] endVertices;

	public Projective_Transform() {
		// TODO Auto-generated constructor stub
	}

	// Method edit given image
	ImagePlus projecting(ImagePlus imp) {
		// Turn attributes of input image global to class to be used in others methods
		this.ip = imp.getProcessor();
		this.w = imp.getWidth();
		this.h = imp.getHeight();

		// Call method that ask to the user orientation of the new image 
		int orientIndex = aksImageOrientationDialog();
		// Call method that define image orientation to the class setting parameters
		defineImageOrientation(orientIndex);

		// Create new image to receive transformed image
		ImagePlus transformedImp = NewImage.createByteImage("Projected" + imp.getTitle(), new_w, new_h, 1,
				NewImage.FILL_WHITE);
		ImageProcessor newIp = transformedImp.getProcessor();

		// Projective Transformation
		
		// Calculate matrix transform of unit square to final rectangle (final image)
		Matrix T2 = calculateMatrixTransform(endVertices);

		// Call method that ask to the user the 4 vertices of document in initial image
		Point[] startVertices = selectVertices(imp);
		
		// Calculate matrix transform of unit square to initial quadrangle (initial image) and get inverse
		Matrix T1 = calculateMatrixTransform(startVertices);
		Matrix inverseT1 = T1.inverse();

		// Get final transformation matrix and inverse
		Matrix T = T2.times(inverseT1);
		Matrix inverseT = T.inverse();

		// Get coefficients of inverse matrix transformation
		double t11 = inverseT.get(0, 0);
		double t12 = inverseT.get(0, 1);
		double t13 = inverseT.get(0, 2);
		double t21 = inverseT.get(1, 0);
		double t22 = inverseT.get(1, 1);
		double t23 = inverseT.get(1, 2);
		double t31 = inverseT.get(2, 0);
		double t32 = inverseT.get(2, 1);
		double t33 = inverseT.get(2, 2);

		// Iterate in new coordinates to get coordinates in original image by inverse transformation 
		for (int y = 0; y < new_h; y++) {
			for (int x = 0; x < new_w; x++) {
				double h = t31 * x + t32 * y + t33;
				double u = (t11 * x + t12 * y + t13) / h;
				double v = (t21 * x + t22 * y + t23) / h;

				int temp = 0;
				// Verify if coordinate is not out of initial image
				if (!isOutside(u, v)) {
					// Interpolate in pixels region to obtain new pixel to paint new image
					temp = this.interpolateValue(u, v);
				}

				// Set new pixel in new image
				newIp.putPixel(x, y, temp);
			}
		}

		return transformedImp;
	}

	// Method ask image orientation to user and return index's choice (0: portrait, 1: landscape)
	private int aksImageOrientationDialog() {
		GenericDialog gd = new GenericDialog("Orientation of image");
		String[] options = { "Portrait", "Landscape" };
		gd.addMessage("Please select orientation of document");
		gd.addChoice("Image Orientation", options, "Portrait");
		gd.pack();
		gd.showDialog();

		int choiceIndex = gd.getNextChoiceIndex();
		return choiceIndex;
	}

	// Method define document orientation according to choice of user
	private void defineImageOrientation(int orientIndex) {
		if (orientIndex == 0) {
			// If image orientation is portrait
			this.new_h = 2376; // Height has greatest value
			this.new_w = 1680; // Width has smallest value
			this.endVertices = new Point[] { new Point(0, 0), new Point(1680, 0), new Point(1680, 2376),
					new Point(0, 2376) };
		}
		if (orientIndex == 1) {
			// If image orientation is landscape
			this.new_h = 1680; // Height has smallest value
			this.new_w = 2376; // Width has greatest value
			this.endVertices = new Point[] { new Point(0, 0), new Point(2376, 0), new Point(2376, 1680),
					new Point(0, 1680) };
		}
	}

	// Method calculates matrix transformation given 4 points
	private Matrix calculateMatrixTransform(Point[] points) {
		// Get coordinates separated
		double x1 = points[0].x;
		double x2 = points[1].x;
		double x3 = points[2].x;
		double x4 = points[3].x;
		double y1 = points[0].y;
		double y2 = points[1].y;
		double y3 = points[2].y;
		double y4 = points[3].y;

		// Defining matrix coefficients
		double a31 = ((x1 - x2 + x3 - x4) * (y4 - y3) - (y1 - y2 + y3 - y4) * (x4 - x3))
				/ ((x2 - x3) * (y4 - y3) - (x4 - x3) * (y2 - y3));

		double a32 = ((y1 - y2 + y3 - y4) * (x2 - x3) - (x1 - x2 + x3 - x4) * (y2 - y3))
				/ ((x2 - x3) * (y4 - y3) - (x4 - x3) * (y2 - y3));

		double a11 = x2 - x1 + a31 * x2;
		double a12 = x4 - x1 + a32 * x4;
		double a13 = x1;
		double a21 = y2 - y1 + a31 * y2;
		double a22 = y4 - y1 + a32 * y4;
		double a23 = y1;

		// Get matrix
		double[][] A = { { a11, a12, a13 }, { a21, a22, a23 }, { a31, a32, 1 } };
		Matrix T = new Matrix(A);
		return T;
	}

	// Method ask to the user get four points in initial image that correspond to vertices of the document
	private Point[] selectVertices(ImagePlus imp) {
		imp.show();
		// While user do not give four points, repeat
		boolean waitFlag = true;		
		while(waitFlag) {
			WaitForUserDialog wait = new WaitForUserDialog(
					"Select 4 non-collinear points that correspond to the vertices of document."
							+ "\nPlease select first the top left point of document and others in clockwise direction order.");
			wait.show(); 
			int n = imp.getRoi().getContainedPoints().length;
			if(n == 4) {
				waitFlag = false;
			}	
		}
		
		Point[] vertices = imp.getRoi().getContainedPoints();		
		return vertices;
	}

	// Method interpolate pixels to paint the new image
	private int interpolateValue(double u, double v) {

		int a = (int) Math.floor(u);
		int b = a + 1;
		int c = (int) Math.floor(v);
		int d = c + 1;

		int Ia = ip.getPixel(a, c);
		int Ib = ip.getPixel(b, c);
		int Ic = ip.getPixel(a, d);
		int Id = ip.getPixel(b, d);

		double dx = u - a;
		double dy = v - c;

		int temp = (int) ((1 - dx) * (1 - dy) * Ia + (1 - dx) * (dy) * Ib + (dx) * (1 - dy) * Ic + (dx) * (dy) * Id);

		return temp;
	}

	// Method returns if the coordinate is out of the original image (input) or not
	private boolean isOutside(double x, double y) {
		return ((x < 0) || (x >= w) || (y < 0) || (y >= h));
	}
}
