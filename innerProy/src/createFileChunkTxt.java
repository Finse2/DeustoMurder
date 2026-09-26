import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public class createFileChunkTxt extends Application {

    // Helper class to hold relative line bounds before index offset calculation
    private static class FileInfo {
        Path path;
        int relativeStart;
        int relativeEnd;

        FileInfo(Path path, int relativeStart, int relativeEnd) {
            this.path = path;
            this.relativeStart = relativeStart;
            this.relativeEnd = relativeEnd;
        }
    }

    public static void createFileChunkTxt() {

        List<String> contentLines = new ArrayList<>();
        List<FileInfo> fileInfos = new ArrayList<>();

        // Map to group image files by their parent directory path
        Map<String, List<String>> imagePackages = new TreeMap<>();

        // Set to store target file paths and ensure no duplicates
        java.util.Set<Path> uniqueTextFiles = new java.util.LinkedHashSet<>();

        try (Stream<Path> paths = Files.walk(Path.of("."))) {

            paths.filter(Files::isRegularFile).forEach(path -> {
                String name = path.toString().toLowerCase();
                String parentDir = path.getParent() != null ? path.getParent().toString().replace("\\", "/") : ".";

                // Group images by package
                if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif")) {
                    imagePackages.computeIfAbsent(parentDir, k -> new ArrayList<>()).add(path.getFileName().toString());
                }
                // Queue text files into the Set to process later
                else if (name.endsWith(".java") || name.endsWith(".json") || name.endsWith("optimizationpseudocodeconcept.txt")) {
                    uniqueTextFiles.add(path);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Fetch contents of the set after the walk is finished
        for (Path path : uniqueTextFiles) {
            int relStart = contentLines.size() + 1;

            contentLines.add("==================================================");
            contentLines.add("FILE: " + path.toString().replace("\\", "/"));
            contentLines.add("==================================================");

            try {
                contentLines.addAll(Files.readAllLines(path));
                int relEnd = contentLines.size();
                fileInfos.add(new FileInfo(path, relStart, relEnd));
            } catch (IOException e) {
                System.err.println("Could not read " + path + ": " + e.getMessage());
            }

            contentLines.add("");
        }

        // Process collected image packages into the required format
        for (Map.Entry<String, List<String>> entry : imagePackages.entrySet()) {
            String dirPath = entry.getKey();
            List<String> images = entry.getValue();

            int relStart = contentLines.size() + 1;
            contentLines.add("=====");
            contentLines.add(dirPath);

            StringBuilder lineBuilder = new StringBuilder();
            for (int i = 0; i < images.size(); i++) {
                lineBuilder.append(images.get(i));

                // Append comma if not the last item on the line or in the list
                if (i < images.size() - 1 && (i + 1) % 10 != 0) {
                    lineBuilder.append(", ");
                }

                // Break line every 10 images, or on the final image
                if ((i + 1) % 10 == 0 || i == images.size() - 1) {
                    contentLines.add(lineBuilder.toString());
                    lineBuilder.setLength(0); // Reset for next chunk
                }
            }

            contentLines.add("====");
            int relEnd = contentLines.size();
            contentLines.add(""); // Spacer

            // Format for index: path/packageName/onlyImageContent
            fileInfos.add(new FileInfo(Path.of(dirPath + "/onlyImageContent"), relStart, relEnd));
        }

        // Calculate line offset introduced by placing the index at the top
        int indexOffset = 3 + fileInfos.size() + 1;

        List<String> finalOutput = new ArrayList<>();

        // 1. Build Index Header
        finalOutput.add("==================================================");
        finalOutput.add("BIBLIOGRAPHY / INDEX (considering the document, this way you can jump fetch the java or json file you want to get right away)");
        finalOutput.add("==================================================");

        // 2. Build Index Entries with Offset
        for (FileInfo info : fileInfos) {
            int actualStart = info.relativeStart + indexOffset;
            int actualEnd = info.relativeEnd + indexOffset;

            // Ensure slashes are uniform in the index
            String displayPath = info.path.toString().replace("\\", "/");
            finalOutput.add(displayPath + " lines " + actualStart + "-" + actualEnd + " || (" + (actualEnd - actualStart) + ") lines");
        }

        finalOutput.add("");

        // 3. Append All File Contents
        finalOutput.addAll(contentLines);

        try {
            Files.write(Path.of("javaProgramInTxtFileFusedTogether.txt"), finalOutput);
            System.out.println("Fusion complete!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showFusionCompletePopup(Stage ownerStage) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.initOwner(ownerStage);
        popupStage.setTitle("Notice");

        Label popupText = new Label("Fusion Complete");
        popupText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label instructionText = new Label("(Press any key to close)");
        instructionText.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");

        VBox popupLayout = new VBox(10, popupText, instructionText);
        popupLayout.setAlignment(Pos.CENTER);
        popupLayout.setPadding(new Insets(25));
        popupLayout.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1;");

        Scene popupScene = new Scene(popupLayout, 260, 120);

        popupScene.setOnKeyPressed(event -> popupStage.close());
        popupLayout.setOnKeyPressed(event -> popupStage.close());

        popupStage.setScene(popupScene);
        popupStage.setResizable(false);
        popupStage.show();

        popupLayout.requestFocus();
    }

    @Override
    public void start(Stage primaryStage) {
        createFileChunkTxt();
        showFusionCompletePopup(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}