package tutorials.measure;

import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.*;
import ij.io.FileSaver;
import ij.io.Opener;
import io.scif.services.DatasetIOService;
import net.imagej.ImageJService;
import net.imagej.display.DataView;
import net.imagej.display.OverlayService;
import net.imagej.display.OverlayView;
import net.imagej.overlay.LineOverlay;
import net.imglib2.img.Img;
import org.apache.commons.beanutils.PropertyUtils;
import org.scijava.command.CommandService;
import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.display.event.input.KyEvent;
import org.scijava.display.event.window.WinClosedEvent;
import org.scijava.display.event.window.WinClosingEvent;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.input.KeyCode;
import org.scijava.io.IOService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.ui.UIService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Plugin(type = Service.class)
public class MeasureService extends AbstractService implements ImageJService, ImageListener, RoiListener {
    @Parameter
    private IOService ioService;

    @Parameter
    private CommandService commandService;

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
        ImagePlus.addImageListener(this);
        Roi.addRoiListener(this);
    }

    @Override
    public void imageOpened(ImagePlus imp) {
        ImageWindow imageWindow = imp.getWindow();
    }

    @Override
    public void imageClosed(ImagePlus imp) {
        if (measureBatchRunning && imp.getTitle().equals(this.currentName())) {
            saveMeasurements();
            nextImage();
        }
    }

    public void imageUpdated(ImagePlus imp) {
    }

    @Override
    public void roiModified(ImagePlus imp, int id) {
        if (measureBatchRunning && imp != null && imp.getTitle().equals(this.currentName())) {
            measurements = new ArrayList<>();
            if (imp.getOverlay() == null) {
                imp.setOverlay(new Overlay());
            }
            Overlay overlay = imp.getOverlay();
            if (id == CREATED && imp.getRoi() instanceof Line) {
                overlay.add(imp.getRoi());
            }
            for (Roi roi : overlay) {
                if (roi instanceof Line) {
                    if (((Line) roi).getRawLength() > 0) {
                        measurements.add(((Line) roi).getRawLength() / this.scale);
                    }
                }
            }
        }
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

        Opener opener = new Opener();
        opener.open(currentFile().getAbsolutePath());
    }
}
