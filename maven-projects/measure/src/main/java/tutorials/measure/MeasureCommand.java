package tutorials.measure;

import net.imagej.ImageJ;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

@Plugin(type = Command.class, headless = true,
        menuPath = "Analyze>Batch Measure")
public class MeasureCommand implements Command {

    /*
     * We need to know what folder to open. So, the framework will ask the user
     * via the active user interface to select a file to open. This command is
     * "UI agnostic": it does not need to know the specific user interface
     * currently active.
     */
    @Parameter(style = "directory")
    private File imageFolder;

    @Parameter
    private MeasureService measureService;

    @Override
    public void run() {
        measureService.startMeasureBatch();

        for (File file : imageFolder.listFiles()) {
            measureService.addFile(file);
            // Sort and randomise logic
        }

        measureService.setOutputFile(new File(imageFolder.getPath() + "/measurements.csv"));
        measureService.nextImage();
    }

    /** Tests the command. */
    public static void main(final String... args) throws Exception {
        // Launch ImageJ for test run.
        final ImageJ ij = new ImageJ();
        ij.launch(args);
    }

}
