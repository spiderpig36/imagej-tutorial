package tutorials.measure;

import net.imagej.ImageJ;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private long seed;

    @Parameter
    private MeasureService measureService;

    final private Pattern fileNamePattern = Pattern.compile("([a-z0-9]*)_(\\d{3})x_(\\d*)\\.tif");

    @Override
    public void run() {
        List<String> processedFiles = new ArrayList<>();
        File outputFile = new File(imageFolder.getPath() + "/measurements.csv");
        try {
            if (!outputFile.createNewFile()) {
                Scanner scanner = new Scanner(outputFile);

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] columns = line.split(",");
                    processedFiles.add(columns[0]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        measureService.setOutputFile(outputFile);

        FilenameFilter tifFilter = (dir, name) -> name.endsWith(".tif");
        List<File> files = Arrays.asList(Objects.requireNonNull(imageFolder.listFiles(tifFilter)));
        Collections.shuffle(files, new Random(seed));
        List<File> filesToProcess = files.stream()
                .filter(file -> !processedFiles.contains(file.getName()))
                .sorted(Comparator.comparing(file -> {
                    Matcher matcher = this.fileNamePattern.matcher(file.getName());
                    boolean isMatching = matcher.matches();
                    if (isMatching) {
                        return matcher.group(1);
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
        ij.launch(args);
    }

}
