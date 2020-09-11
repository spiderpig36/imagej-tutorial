package tutorials.measure;

import net.imagej.ImageJ;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UserInterface;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Plugin(type = Command.class, headless = true,
        menuPath = "Analyze>Batch Measure>Start Measure")
public class MeasureCommand implements Command {

    @Parameter(style = "directory", label = "Image Folder", description = "Select the folder that contains the images you want to measure")
    private File imageFolder;

    @Parameter(label = "Seed", description = "Seed to initialize the random number generator")
    private long seed;

    @Parameter(label = "Restart", description = "Discard list of processed files")
    private boolean restart;

    @Parameter
    private MeasureService measureService;

    @Override
    public void run() {
        List<String> processedFiles = new ArrayList<>();
        File stateFile = new File(imageFolder.getPath() + "/state.txt");

        if (restart) {
            stateFile.delete();
        }
        try {
            if (!stateFile.createNewFile()) {
                Scanner scanner = new Scanner(stateFile);

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    processedFiles.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        measureService.setStateFile(stateFile);

        List<File> files = Arrays.asList(Objects.requireNonNull(imageFolder.listFiles(MeasureService.tifFilter)));
        Collections.shuffle(files, new Random(seed));
        List<File> filesToProcess = files.stream()
                .filter(file -> !processedFiles.contains(file.getName()))
                .sorted(Comparator.comparing(file -> {
                    Matcher matcher = MeasureService.fileNamePattern.matcher(file.getName());
                    boolean isMatching = matcher.matches();
                    if (isMatching) {
                        return matcher.group(3);
                    }
                    return "";
                }))
                .collect(Collectors.toList());
        measureService.setFiles(filesToProcess);

        measureService.startMeasureBatch();
        measureService.nextImage();
    }

    /** Tests the command. */
    public static void main(final String... args) throws Exception {
        // Launch ImageJ for test run.
        final ImageJ ij = new ImageJ();

        List<UserInterface> userInterfaces = ij.ui().getAvailableUIs();
        System.out.println(userInterfaces);
        System.out.println(ij.ui().getDefaultUI());

        ij.launch(args);

        ij.command().run(MeasureCommand.class, true);
    }

}
