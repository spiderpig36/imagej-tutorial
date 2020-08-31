package tutorials.measure;

import net.imagej.ImageJService;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

import java.io.File;
import java.util.ArrayList;

@Plugin(type = Service.class)
public class MeasureService extends AbstractService implements ImageJService {

    private boolean measureBatchRunning;
    private ArrayList<File> files;
    // pixel per 1mm
    private int scale;

    public boolean isMeasureBatchRunning() {
        return measureBatchRunning;
    }

    public void setMeasureBatchRunning(boolean measureBatchRunning) {
        System.out.println(measureBatchRunning);
        this.measureBatchRunning = measureBatchRunning;
    }

    public ArrayList<File> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<File> files) {
        this.files = files;
    }
}
