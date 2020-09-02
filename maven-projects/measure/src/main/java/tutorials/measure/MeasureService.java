package tutorials.measure;

import io.scif.services.DatasetIOService;
import net.imagej.DatasetService;
import net.imagej.ImageJService;
import net.imagej.display.OverlayView;
import net.imagej.overlay.LineOverlay;
import net.imglib2.img.Img;
import org.scijava.command.CommandService;
import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.display.event.input.KyEvent;
import org.scijava.display.event.window.WinClosedEvent;
import org.scijava.display.event.window.WinClosingEvent;
import org.scijava.event.EventHandler;
import org.scijava.input.KeyCode;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.ui.UIService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Plugin(type = Service.class)
public class MeasureService extends AbstractService implements ImageJService {
    @Parameter
    private DatasetIOService ioService;

    @Parameter
    private DisplayService displayService;

    @Parameter
    private UIService uiService;

    private boolean measureBatchRunning;
    private List<File> files;
    private File outputFile;
    private int currentFileIndex;
    private Img currentImg;

    private Map<String, List<Double>> results;

    // pixel per 1mm
    private int scale;

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public void initialize() {
        this.files = new ArrayList<>();
        this.results = new HashMap<>();
    }

    @EventHandler
    public void onKeyEvent(final KyEvent evt) {
        if (evt.getModifiers().isMetaDown() && evt.getCode() == KeyCode.W) {
            this.measure();
        }
    }

    @EventHandler
    public void onWindowClosingEvent(final WinClosingEvent evt) {
        this.measure();
    }

    private void measure() {
        Display<?> display = displayService.getActiveDisplay();
        ArrayList<Double> measurements = new ArrayList<>();
        results.put(currentFile().getName(), measurements);
        if (display.toString().equals(currentFile().getName())) {
            for (Object o : display) {
                if (o instanceof OverlayView) {
                    OverlayView view = (OverlayView) o;
                    if (view.getData() instanceof LineOverlay) {
                        LineOverlay line = (LineOverlay) view.getData();
                        measurements.add(calculateLineLength(line));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onWindowClosedEvent(final WinClosedEvent evt) {
        Display<?> display = evt.getDisplay();
        if (display.toString().equals(currentFile().getName())) {
            saveResults();
            nextImage();
        }
    }

    private void saveResults() {
        try {
            FileWriter writer = new FileWriter(this.outputFile);
            for (Map.Entry<String, List<Double>> entry : this.results.entrySet()) {
                StringBuilder line = new StringBuilder(entry.getKey());
                for (Double measurement : entry.getValue()) {
                    line.append(",").append(measurement);
                }
                writer.append(line.toString());
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double calculateLineLength(LineOverlay line) {
        double[] lineStart = new double[2];
        line.getLineStart(lineStart);
        double[] lineEnd = new double[2];
        line.getLineEnd(lineEnd);

        double a = Math.abs(lineStart[0] - lineEnd[0]);
        double b = Math.abs(lineStart[1] - lineEnd[1]);

        return Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }

    public boolean isMeasureBatchRunning() {
        return measureBatchRunning;
    }

    public void startMeasureBatch() {
        this.currentFileIndex = -1;
        this.measureBatchRunning = true;
        System.out.println("run Forest, run!");
    }

    public void endMeasureBatch() {
        this.measureBatchRunning = false;
        this.files = new ArrayList<>();
        this.results = new HashMap<>();
    }

    public void addFile(File file) {
        this.files.add(file);
    }

    public File currentFile() {
        if (this.currentFileIndex == -1) {
            return null;
        }
        return this.files.get(this.currentFileIndex);
    }

    public void nextImage() {
        this.currentFileIndex++;
        if (currentFileIndex > this.files.size() - 1) {
            this.endMeasureBatch();
            return;
        }
        try {
            this.currentImg = ioService.open(currentFile().getAbsolutePath());
            uiService.show(this.currentImg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
