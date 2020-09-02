package tutorials.measure;

import io.scif.services.DatasetIOService;
import net.imagej.ImageJService;
import net.imagej.display.DataView;
import net.imagej.display.OverlayView;
import net.imagej.overlay.LineOverlay;
import net.imglib2.img.Img;
import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.display.event.input.KyEvent;
import org.scijava.display.event.window.WinClosedEvent;
import org.scijava.display.event.window.WinClosingEvent;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
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
import java.util.List;
import java.util.stream.Collectors;

@Plugin(type = Service.class)
public class MeasureService extends AbstractService implements ImageJService {
    @Parameter
    private DatasetIOService ioService;

    @Parameter
    private DisplayService displayService;

    @Parameter
    private UIService uiService;

    @Parameter
    private EventService eventService;

    private boolean measureBatchRunning;
    private File outputFile;
    private List<File> files;
    private int currentFileIndex;
    private List<Double> measurements;
    private int scale = 0;

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    /**
     * Set the scale for the line length
     * @param scale pixels per 1mm
     */
    public void setScale(int scale) {
        if (scale <= 0) {
            throw new IllegalArgumentException();
        }
        this.scale = scale;
    }

    @Override
    public void initialize() {
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

    @EventHandler
    public void onWindowClosedEvent(final WinClosedEvent evt) {
        Display<?> display = evt.getDisplay();
        if (display.toString().equals(this.currentName())) {
            saveMeasurements();
            nextImage();
        }
    }

    private void measure() {
        Display<DataView> activeDisplay = (Display<DataView>) displayService.getActiveDisplay();
        if (activeDisplay.toString().equals(this.currentName())) {
            measurements = new ArrayList<>();
            List<DataView> distinctViews = activeDisplay.stream().distinct().collect(Collectors.toList());
            for (DataView v : distinctViews) {
                if (v instanceof OverlayView && v.getData() instanceof LineOverlay) {
                    LineOverlay line = (LineOverlay) v.getData();
                    measurements.add(calculateLineLength(line));
                }
            }
        }
    }

    private double calculateLineLength(LineOverlay line) {
        double[] lineStart = new double[2];
        line.getLineStart(lineStart);
        double[] lineEnd = new double[2];
        line.getLineEnd(lineEnd);

        double a = Math.abs(lineStart[0] - lineEnd[0]);
        double b = Math.abs(lineStart[1] - lineEnd[1]);

        return Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2)) / this.scale;
    }

    private void saveMeasurements() {
        try {
            FileWriter writer = new FileWriter(this.outputFile, true);

            StringBuilder line = new StringBuilder(this.currentName());
            for (Double measurement : this.measurements) {
                line.append(",").append(measurement).append("mm");
            }
            line.append("\n");
            writer.write(line.toString());

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isMeasureBatchRunning() {
        return measureBatchRunning;
    }

    public void startMeasureBatch() {
        this.currentFileIndex = -1;
        this.measureBatchRunning = true;
    }

    public void endMeasureBatch() {
        this.measureBatchRunning = false;
    }

    public File currentFile() {
        if (this.currentFileIndex == -1) {
            return null;
        }
        return this.files.get(this.currentFileIndex);
    }

    public String currentName() {
        return this.currentFile().getName();
    }

    public void nextImage() {
        this.currentFileIndex++;
        if (currentFileIndex > this.files.size() - 1) {
            this.endMeasureBatch();
            return;
        }
        try {
            Img currentImg = ioService.open(currentFile().getAbsolutePath());
            uiService.show(currentImg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
