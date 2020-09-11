package tutorials.measure;

import ij.ImagePlus;
import ij.io.Opener;
import io.scif.services.DatasetIOService;
import net.imagej.roi.ROIService;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

@Plugin(type = Command.class, headless = true,
        menuPath = "Analyze>Batch Measure>Generate Measure Table")
public class GenerateTableCommand implements Command {

    @Parameter
    private DatasetIOService ioService;

    @Parameter
    private ROIService roiService;

    @Parameter(style = "directory", label = "Image Folder", description = "Select the folder that contains the images you want to measure")
    private File imageFolder;

    @Parameter(label = "Scale", description = "Scale of image in pixels per 1mm", min = "1")
    private int scale;

    @Override
    public void run() {
        File outputFile = new File(imageFolder.getPath() + "/measurements.csv");

        File[] files = imageFolder.listFiles(MeasureService.tifFilter);
        for (File file : files) {
            Opener opener = new Opener();
            ImagePlus imagePlus = opener.openTiff(file.getParent(), file.getName());

            System.out.println(imagePlus.getOverlay());
        }
    }

//    public void roiModified(ImagePlus imp, int id) {
//        if (imp != null && imp.getTitle().equals(this.currentName())) {
//            measurements = new ArrayList<>();
//            if (imp.getOverlay() == null) {
//                imp.setOverlay(new Overlay());
//            }
//            Overlay overlay = imp.getOverlay();
//            if (id == CREATED && imp.getRoi() instanceof Line) {
//                overlay.add(imp.getRoi());
//            }
//            for (Roi roi : overlay) {
//                if (roi instanceof Line) {
//                    if (((Line) roi).getRawLength() > 0) {
//                        measurements.add(((Line) roi).getRawLength() / this.scale);
//                    }
//                }
//            }
//        }
//    }
//
//    private void saveMeasurements() {
//        try {
//            FileWriter writer = new FileWriter(this.outputFile, true);
//
//            StringBuilder line = new StringBuilder(this.currentName());
//            for (Double measurement : this.measurements) {
//                line.append(",").append(measurement).append("mm");
//            }
//            line.append("\n");
//            writer.write(line.toString());
//
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
