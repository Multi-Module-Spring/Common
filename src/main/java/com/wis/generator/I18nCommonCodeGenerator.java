package com.wis.generator;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class I18nCommonCodeGenerator {

    private static final String[] I18N_PATHS = {"src/main/resources/i18n"};
    private static final String OUTPUT_PATH_TRANSLATE_COMMON = "src/main/java/com/wis/i18n/TranslateCommon.java";
    private static final String OUTPUT_PATH_KEYTRANSLATE_COMMON = "src/main/java/com/wis/i18n/KeyTranslateCommon.java";
    private static final String OUTPUT_PATH_COMMON_EXCEPTION = "src/main/java/com/wis/i18n/exception/TranslateCommonException.java";
    private static final String PACKAGE_NAME = "com.wis.i18n";
    private static final String EXCEPTION_PACKAGE = "com.wis.i18n.exception";

    public static void main(String[] args) {
        try {
            new I18nCommonCodeGenerator().generate();
        } catch (Exception e) {
            System.err.println("Error generating i18n common code: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void generate() throws IOException {
        Map<String, List<Map.Entry<String, String>>> entriesTranslateCommonBySheet = new LinkedHashMap<>();
        Map<String, List<Map.Entry<String, String>>> entriesKeyTranslateCommonBySheet = new LinkedHashMap<>();
        int totalFiles = 0;

        for (String i18nDir : I18N_PATHS) {
            Path dirPath = Paths.get(i18nDir);
            if (!Files.isDirectory(dirPath)) {
                System.out.println("Directory not found: " + i18nDir);
                continue;
            }

            System.out.println("Scanning directory: " + i18nDir);

            try (Stream<Path> paths = Files.list(dirPath)) {
                List<Path> xlsxFiles = paths
                        .filter(p -> p.getFileName().toString().endsWith(".xlsx"))
                        .filter(p -> !p.getFileName().toString().startsWith("~$"))
                        .toList();

                for (Path file : xlsxFiles) {
                    totalFiles++;
                    System.out.println("Reading file: " + file);
                    processExcelFile(file, entriesTranslateCommonBySheet, entriesKeyTranslateCommonBySheet);
                }
            }
        }

        generateEnumFile(OUTPUT_PATH_TRANSLATE_COMMON, entriesTranslateCommonBySheet, "TranslateCommon");
        generateEnumFile(OUTPUT_PATH_KEYTRANSLATE_COMMON, entriesKeyTranslateCommonBySheet, "KeyTranslateCommon");
        generateCommonExceptionFile();

        int totalTranslate = entriesTranslateCommonBySheet.values().stream().mapToInt(List::size).sum();
        int totalKeyTranslate = entriesKeyTranslateCommonBySheet.values().stream().mapToInt(List::size).sum();

        System.out.printf("\nSummary: %d in TranslateCommon, %d in KeyTranslateCommon from %d Excel file(s).%n",
                totalTranslate, totalKeyTranslate, totalFiles);
    }

    private void processExcelFile(Path file,
                                  Map<String, List<Map.Entry<String, String>>> entriesTranslate,
                                  Map<String, List<Map.Entry<String, String>>> entriesKeyTranslate) {
        try (FileInputStream fis = new FileInputStream(file.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);
                if (sheet == null) continue;
                String sheetName = sheet.getSheetName();

                Row headerRow = sheet.getRow(0);
                if (headerRow == null) continue;

                Map<String, Integer> columnMap = new HashMap<>();
                for (Cell cell : headerRow) {
                    String header = getCellValueAsString(cell).trim().toLowerCase();
                    columnMap.put(header, cell.getColumnIndex());
                }

                Integer keyIdx = columnMap.get("key");
                Integer viIdx = columnMap.get("vi_vn");
                Integer errIdx = columnMap.get("is_error");

                if (keyIdx == null || viIdx == null) {
                    System.out.println("Missing column 'key' or 'vi_vn' in " + file.getFileName() + " (Sheet: " + sheetName + ")");
                    continue;
                }

                List<Map.Entry<String, String>> transList =
                        entriesTranslate.computeIfAbsent(sheetName, k -> new ArrayList<>());
                List<Map.Entry<String, String>> keyList =
                        entriesKeyTranslate.computeIfAbsent(sheetName, k -> new ArrayList<>());

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String key = getCellValue(row, keyIdx);
                    String viVal = getCellValue(row, viIdx);
                    String isError = errIdx != null ? getCellValue(row, errIdx) : null;

                    if (key == null || key.trim().isEmpty()) continue;

                    key = key.trim();
                    viVal = viVal != null && !viVal.trim().isEmpty() ? viVal.trim() : key;

                    if ("true".equalsIgnoreCase(isError)) {
                        transList.add(Map.entry(key, viVal));
                    } else {
                        keyList.add(Map.entry(key, viVal));
                    }
                }

                System.out.println("  -> Loaded sheet: " + sheetName +
                        " | " + transList.size() + " error, " + keyList.size() + " normal keys.");
            }

        } catch (Exception e) {
            System.err.println("Error reading " + file.getFileName() + ": " + e.getMessage());
        }
    }

    private String getCellValue(Row row, int idx) {
        if (idx >= row.getLastCellNum()) return null;
        Cell cell = row.getCell(idx);
        return getCellValueAsString(cell);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private String toEnumName(String key) {
        String s = key.replaceAll("[^A-Za-z0-9]", "_");
        s = s.replaceAll("_+", "_").toUpperCase();
        s = s.replaceAll("^_+|_+$", "");
        if (s.isEmpty()) s = "KEY";
        if (Character.isDigit(s.charAt(0))) s = "K_" + s;
        return s;
    }

    private void generateEnumFile(String outputPath,
                                  Map<String, List<Map.Entry<String, String>>> entriesBySheet,
                                  String enumName) throws IOException {
        if (entriesBySheet.isEmpty()) {
            System.out.println("No entries for " + enumName + ", skipped.");
            return;
        }

        Path path = Paths.get(outputPath);
        Files.createDirectories(path.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("package " + PACKAGE_NAME + ";\n\n");
            writer.write("import lombok.Getter;\n");
            writer.write("import lombok.AllArgsConstructor;\n");
            writer.write("import lombok.ToString;\n\n");
            writer.write("@Getter\n@AllArgsConstructor\n@ToString\n");
            writer.write("public enum " + enumName + " {\n\n");

            boolean firstSheet = true;
            for (Map.Entry<String, List<Map.Entry<String, String>>> sheetEntry : entriesBySheet.entrySet()) {
                String sheetName = sheetEntry.getKey();
                List<Map.Entry<String, String>> items = sheetEntry.getValue();
                if (items.isEmpty()) continue;

                if (!firstSheet) writer.write("\n");
                firstSheet = false;

                writer.write("    // ===== Sheet: " + sheetName + " =====\n");
                for (int i = 0; i < items.size(); i++) {
                    Map.Entry<String, String> entry = items.get(i);
                    String enumKey = toEnumName(entry.getKey());
                    String value = entry.getValue().replace("\\", "\\\\").replace("\"", "\\\"");
                    writer.write(String.format("    %s(\"%s\"),\n", enumKey, value));
                }
            }

            writer.write("\n    ;\n\n    private final String description;\n}\n");
        }

        System.out.println("Created file: " + outputPath + " (" +
                entriesBySheet.values().stream().mapToInt(List::size).sum() + " keys)");
    }

    private void generateCommonExceptionFile() throws IOException {
        Path translateCommonFile = Paths.get(OUTPUT_PATH_TRANSLATE_COMMON);
        if (!Files.exists(translateCommonFile)) {
            System.out.println("Not found file enum: " + OUTPUT_PATH_TRANSLATE_COMMON);
            return;
        }

        Path path = Paths.get(OUTPUT_PATH_COMMON_EXCEPTION);
        Files.createDirectories(path.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("package " + EXCEPTION_PACKAGE + ";\n\n");
            writer.write("import com.wis.i18n.TranslateCommon;\n");
            writer.write("import com.wis.main.exception.ServiceException;\n");
            writer.write("import org.springframework.http.HttpStatus;\n");
            writer.write("import java.util.List;\n");
            writer.write("import java.util.Arrays;\n\n");
            writer.write("public class TranslateCommonException extends ServiceException {\n\n");

            writer.write("    public TranslateCommonException(HttpStatus status, String translateKey, Object... args) {\n");
            writer.write("        super(status, translateKey, false, null, Arrays.stream(args).toList());\n");
            writer.write("    }\n\n");

            writer.write("    public TranslateCommonException(HttpStatus status, TranslateCommon translate) {\n");
            writer.write("        super(status, translate.name(), false, null, List.of());\n");
            writer.write("    }\n\n");

            writer.write("    public TranslateCommonException(HttpStatus status, TranslateCommon translate, Object... args) {\n");
            writer.write("        super(status, translate.name(), true, null, Arrays.stream(args).toList());\n");
            writer.write("    }\n\n");

            writer.write("    public TranslateCommonException(TranslateCommon translate) {\n");
            writer.write("        super(HttpStatus.BAD_REQUEST, translate.name(), false, null, List.of());\n");
            writer.write("    }\n\n");

            writer.write("    public TranslateCommonException(TranslateCommon translate, Object... args) {\n");
            writer.write("        super(HttpStatus.BAD_REQUEST, translate.name(), true, null, Arrays.stream(args).toList());\n");
            writer.write("    }\n");

            writer.write("}\n");
        }

        System.out.println("-> Created file: " + OUTPUT_PATH_COMMON_EXCEPTION);
        System.out.println("-> TranslateCommonException is ready to use.");
    }
}