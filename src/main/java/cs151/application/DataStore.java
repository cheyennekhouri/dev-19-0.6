package cs151.application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public final class DataStore {
    private static final Path DATA_DIR = Paths.get(System.getProperty("user.home"), ".knowledgetrack");
    private static final Path DATA_FILE = DATA_DIR.resolve("languages.csv");
    private static final Path PROFILE_FILE = DATA_DIR.resolve("profiles.csv");

    private static final ObservableList<ProgrammingLanguages> LIST =
            FXCollections.observableArrayList();

    private static final ObservableList<StudentProfile> NAME =
            FXCollections.observableArrayList();

    private static boolean loadedOnce = false;

    private DataStore() {
    }

    public static ObservableList<ProgrammingLanguages> getList() {
        return LIST;
    }

    public static ObservableList<StudentProfile> getFullName() {
        return NAME;
    }

    private static void seedDefaultLanguagesIfAbsent() {
        if (Files.exists(DATA_FILE)) return;
        LIST.setAll(
                new ProgrammingLanguages("Java"),
                new ProgrammingLanguages("Python"),
                new ProgrammingLanguages("C++")
        );
        save();
    }

    public static void load() {
        if (loadedOnce) return;
        loadedOnce = true;

        try {
            if (!Files.exists(DATA_DIR)) Files.createDirectories(DATA_DIR);
        } catch (IOException ignored) {
        }
        seedDefaultLanguagesIfAbsent();

        LIST.clear();
        try (BufferedReader br = Files.newBufferedReader(DATA_FILE, StandardCharsets.UTF_8)) {
            String header = br.readLine();
            if (header == null) return;

            String row;
            while ((row = br.readLine()) != null) {
                parseLineIntoList(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            if (!Files.exists(DATA_DIR)) Files.createDirectories(DATA_DIR);
            try (BufferedWriter bw = Files.newBufferedWriter(DATA_FILE, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                bw.write("programmingLanguage");
                bw.newLine();

                for (ProgrammingLanguages pl : LIST) {
                    //  bw.write(csv(pl.getFullName()));
                    //bw.write(',');
                    bw.write(csv(pl.getProgrammingLanguage()));
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static String csv(String s) {
        if (s == null) s = "";
        String q = s.replace("\"", "\"\"");
        return "\"" + q + "\"";
    }

    private static void parseLineIntoList(String line) {
        String[] cols = parseCsvLine(line, 1);
        if (cols == null) return;
        String lang = cols[0];
        if (!lang.isEmpty()) {
            LIST.add(new ProgrammingLanguages(lang));
        }
    }

    private static String[] parseCsvLine(String line, int expectedCols) {
        if (line == null) return null;
        String[] out = new String[expectedCols];
        int idx = 0;
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    if (idx < expectedCols) out[idx++] = sb.toString();
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
        }
        if (idx < expectedCols) out[idx++] = sb.toString();
        if (idx != expectedCols) return null;
        return out;
    }

    public static void saveProfiles() {
        try {
            if (!Files.exists(DATA_DIR)) Files.createDirectories(DATA_DIR);
            try (BufferedWriter bw = Files.newBufferedWriter(
                    PROFILE_FILE, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

                // header (10 columns)
                bw.write(String.join(",", "name", "major", "academicStatus", "employment",
                        "jobDetails", "languages", "preferredRole", "comments", "whiteList", "blackList"));
                bw.newLine();

                for (StudentProfile sp : NAME) {
                    String langsJoined = (sp.getLanguages() == null) ? "" : String.join("|", sp.getLanguages());
                    String line = String.join(",",
                            csv(sp.getName()),
                            csv(sp.getMajor()),
                            csv(sp.getAcademicStatus()),
                            csv(sp.isEmployed() ? "Employed" : "Not Employed"),
                            csv(sp.getJobDetails()),
                            csv(String.join("|", sp.getLanguages())),
                            csv(sp.getPreferredRole()),
                            csv(sp.getComments()),
                            csv(Boolean.toString(sp.isWhiteList())),
                            csv(Boolean.toString(sp.isBlackList())));
                    bw.write(line);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadProfiles() {
        NAME.clear();
        if (!Files.exists(PROFILE_FILE)) return;

        try (BufferedReader br = Files.newBufferedReader(PROFILE_FILE, StandardCharsets.UTF_8)) {
            br.readLine(); // skip header
            for (String row; (row = br.readLine()) != null; ) {
                String[] c = parseCsvLine(row, 1);
                if (c == null) continue;

                StudentProfile sp = new StudentProfile(
                        c[0]//,
                     /*   c[1],
                        c[5].isEmpty() ? List.of() : List.of(c[5].split("\\|")) // languages*/
                );
                /*sp.setAcademicStatus(c[2]);
                sp.setEmployeed("Employed".equalsIgnoreCase(c[3]));
                sp.setJobDetails(c[4]);
                sp.setPreferredRole(c[6]);
                sp.setComments(c[7]);
                sp.setWhiteList(Boolean.parseBoolean(c[8]));
                sp.setBlackList(Boolean.parseBoolean(c[9]));
*/
                NAME.add(sp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}