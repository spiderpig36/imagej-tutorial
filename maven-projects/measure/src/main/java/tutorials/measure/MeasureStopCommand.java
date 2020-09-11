package tutorials.measure;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true,
        menuPath = "Analyze>Batch Measure>Stop Measure")
public class MeasureStopCommand implements Command {

    @Parameter
    private MeasureService measureService;

    @Override
    public void run() {
        measureService.endMeasureBatch();
    }
}
