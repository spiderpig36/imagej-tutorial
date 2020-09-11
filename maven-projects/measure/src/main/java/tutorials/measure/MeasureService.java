package tutorials.measure;

import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.RoiListener;
import ij.io.FileSaver;
import ij.io.Opener;
import net.imagej.ImageJService;
import org.scijava.command.CommandService;
import org.scijava.event.EventService;
import org.scijava.io.IOService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.ui.UIService;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

@Plugin(type = Service.class)
public class MeasureService extends AbstractService implements ImageJService, ImageListener, RoiListener {

    public static final FilenameFilter tifFilter = (dir, name) -> name.endsWith(".tif");
    public static final Pattern fileNamePattern = Pattern.compile("([a-z0-9]*)_(\\d{3})x_(\\d*)\\.tif");

    @Parameter
    private IOService ioService;

    @Parameter
    private CommandService commandService;

    @Parameter
    private UIService uiService;

    @Parameter
    private EventService eventService;

    private boolean measureBatchRunning;
    private File stateFile;
    private List<File> files;
    private int currentFileIndex;
    private ImagePlus imageToSave;

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public void setStateFile(File stateFile) {
        this.stateFile = stateFile;
    }

    @Override
    public void initialize() {
        ImagePlus.addImageListener(this);
        Roi.addRoiListener(this);
    }

    @Override
    public void imageOpened(ImagePlus imp) {
    }

    @Override
    public void imageClosed(ImagePlus imp) {
        if (measureBatchRunning && imp.getTitle().equals(this.currentName())) {
            updateState();

            if (imageToSave != null) {
                FileSaver fileSaver = new FileSaver(imageToSave);
                fileSaver.saveAsTiff(this.currentPath());
            }

            nextImage();
        }
    }

    @Override
    public void imageUpdated(ImagePlus imp) {
    }

    @Override
    public void roiModified(ImagePlus imp, int id) {
        if (measureBatchRunning && imp != null && imp.getTitle().equals(this.currentName())) {
            if (imp.getOverlay() == null) {
                imp.setOverlay(new Overlay());
            }
            Overlay overlay = imp.getOverlay();
            if (id == CREATED && imp.getRoi() instanceof Line) {
                overlay.add(imp.getRoi());
            }
            this.imageToSave = (ImagePlus) imp.clone();
        }
    }

    private void updateState() {
        try {
            FileWriter writer = new FileWriter(this.stateFile, true);
            writer.write(this.currentName() + "\n");
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

    public String currentPath() {
        return currentFile().getAbsolutePath();
    }

    public void nextImage() {
        this.currentFileIndex++;
        if (currentFileIndex > this.files.size() - 1) {
            this.endMeasureBatch();
            this.commandService.run(GenerateTableCommand.class, true);
            return;
        }

        this.imageToSave = null;

        Opener opener = new Opener();
        opener.open(this.currentPath());
    }
}
