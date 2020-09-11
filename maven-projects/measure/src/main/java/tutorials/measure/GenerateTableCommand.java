package tutorials.measure;

import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.io.Opener;
import io.scif.services.DatasetIOService;
import net.imagej.roi.ROIService;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;

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
        Map<String, Map<String, Double>> table = new TreeMap<>();
        List<String> columnNames = new ArrayList<>();

        File[] files = imageFolder.listFiles(MeasureService.tifFilter);
        for (File file : files) {
            String featureName = "";
            String individual = "";

            Matcher matcher = MeasureService.fileNamePattern.matcher(file.getName());
            boolean isMatching = matcher.matches();
            if (isMatching) {
                individual = matcher.group(1);
                featureName = matcher.group(3);
            }

            if (!table.containsKey(individual)) {
                table.put(individual, new HashMap<>());
            }
            Map<String, Double> row = table.get(individual);

            Opener opener = new Opener();
            ImagePlus imagePlus = opener.openTiff(file.getParent(), file.getName());

            Overlay overlay = imagePlus.getOverlay();
            for (Roi roi : overlay) {
                if (roi instanceof Line) {
                    Line line = (Line) roi;
                    if (line.getRawLength() > 0) {
                        if (!columnNames.contains(featureName)) {
                            columnNames.add(featureName);
                        }
                        row.put(featureName, line.getRawLength() / this.scale);
                    }
                }
            }
        }

        columnNames.sort(String::compareTo);

        String dateString = new SimpleDateFormat("dd-MM-yyyy_HHmm").format(new Date());
        File outputFile = new File(imageFolder.getPath() + "/measurements_" + dateString + ".csv");

        try {
            FileWriter writer = new FileWriter(outputFile, false);

            StringBuilder header = new StringBuilder("Individual");
            for (String title : columnNames) {
                header.append(",").append(title);
            }
            header.append("\n");
            writer.write(header.toString());

            for (Map.Entry<String, Map<String, Double>> entry : table.entrySet()) {
                StringBuilder line = new StringBuilder(entry.getKey());
                Map<String, Double> column = entry.getValue();
                for (String title : columnNames) {
                    line.append(",");
                    if (!column.containsKey(title)) {
                        continue;
                    }
                    line.append(column.get(title)).append("mm");
                }
                line.append("\n");
                writer.write(line.toString());
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
