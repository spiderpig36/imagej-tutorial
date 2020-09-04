package tutorials.measure;

import net.imagej.ImageJ;

//import net.imagej.legacy.LegacyService;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UserInterface;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Plugin(type = Command.class, headless = true,
        menuPath = "Analyze>Batch Measure")
public class MeasureCommand implements Command {

    @Parameter(style = "directory", label = "Image Folder", description = "Select the folder that contains the images you want to measure")
    private File imageFolder;

    @Parameter(label = "Seed", description = "Seed to initialize the random number generator")
    private long seed;

    @Parameter(label = "Scale", description = "Scale of image in pixels per 1mm", min = "1")
    private int scale;

    @Parameter
    private MeasureService measureService;

    final public static Pattern fileNamePattern = Pattern.compile("([a-z0-9]*)_(\\d{3})x_(\\d*)\\.tif");

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
                    Matcher matcher = MeasureCommand.fileNamePattern.matcher(file.getName());
                    boolean isMatching = matcher.matches();
                    if (isMatching) {
                        return matcher.group(3);
                    }
                    return "";
                }))
                .collect(Collectors.toList());
        measureService.setFiles(filesToProcess);

        measureService.setScale(this.scale);

        measureService.startMeasureBatch();
        measureService.nextImage();
    }

    /** Tests the command. */
    public static void main(final String... args) throws Exception {
        // Launch ImageJ for test run.
        final ImageJ ij = new ImageJ();

//        ij.context().inject(LegacyService.class);

        List<UserInterface> userInterfaces = ij.ui().getAvailableUIs();
        System.out.println(userInterfaces);
        System.out.println(ij.ui().getDefaultUI());
        //ij.ui().setDefaultUI(userInterfaces.stream().filter(userInterface -> userInterface.toString().equals("swing")).findAny().orElse(null));

        ij.launch(args);

        ij.command().run(MeasureCommand.class, true);
    }

}
