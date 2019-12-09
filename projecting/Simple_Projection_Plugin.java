package projecting;

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

public class Simple_Projection_Plugin implements PlugInFilter {

	public Simple_Projection_Plugin() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		// TODO Auto-generated method stub
		ImageConverter ic = new ImageConverter(imp);
		ic.convertToGray8();
		Projective_Transform pt = new Projective_Transform();
		ImagePlus imp2 = pt.projecting(imp);
		imp2.show();
		return DOES_8G;
	}

	@Override
	public void run(ImageProcessor ip) {
		// TODO Auto-generated method stub

	}

}
