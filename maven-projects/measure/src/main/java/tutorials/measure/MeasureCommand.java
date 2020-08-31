package tutorials.measure;

import io.scif.services.DatasetIOService;
import net.imagej.ImageJ;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.display.event.input.KyEvent;
import org.scijava.event.EventHandler;
import org.scijava.io.IOService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.service.Service;

import java.io.File;
import java.util.List;

/**
 * A command that generates a diagonal gradient image of user-given size.
 * <p>
 * For an even simpler command, see {@link HelloWorld} in this same
 * package!
 * </p>
 */
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

    /*
     * This first @Parameter is a core ImageJ service (and thus @Plugin). The
     * context will provide it automatically when this command is created.
     */

    @Parameter
    private DatasetIOService datasetIOService;

    @Parameter
    private IOService ioService;

    //@Parameter
    //private MeasureService measureService;

    /*
     * In this command, we will be using functions that can throw exceptions.
     * Best practice is to log these exceptions to let the user know what went
     * wrong. By using the LogService to do this, we let the framework decide
     * the best place to display thrown errors.
     */
    @Parameter
    private LogService logService;

    @Parameter
    private ImageJ imageJ;

    @EventHandler
    public void onEvent(final KyEvent evt) {
        System.out.println(evt.getCode());
    }

    @Override
    public void run() {
        MeasureService measureService = imageJ.get(MeasureService.class);
        measureService.setMeasureBatchRunning(true);

        try {
            // load the image
            for (File file : imageFolder.listFiles()) {
                if (datasetIOService.canOpen(file.getAbsolutePath())) {
                    // Dataset image = datasetIOService.open(file.getAbsolutePath());
                    // Img img = (Img) ioService.open(file.getAbsolutePath());
                    // imageJ.ui().show(img);
                }
            }
        }
        catch (final Exception exc) {
            logService.error(exc);
        }
    }

    /** Tests our command. */
    public static void main(final String... args) throws Exception {
        // Launch ImageJ as usual.
        final ImageJ ij = new ImageJ();
        ij.launch(args);
        List<PluginInfo<Service>> services = ij.plugin().getPluginsOfType(Service.class);
        for(PluginInfo service : services) {
            System.out.println(service.getPluginClass());
        }
        Service s = ij.context().getService(MeasureService.class);
        System.out.println(s);
    }

}
