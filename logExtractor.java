import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

public class logExtractor {

    static String directory;
    static LocalDateTime fromTime;
    static LocalDateTime toTime;

    public static void main(String[] args) throws Exception {

        // taking inputs
        fromTime = LocalDateTime.parse(args[0].substring(0, args[0].length() - 1));
        toTime = LocalDateTime.parse(args[1].substring(0, args[1].length() - 1));
        directory = args[2];

        ArrayList<Integer> filesList = new ArrayList<Integer>();
        File[] files = new File(directory).listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String temp = file.getName();
                    int t = Integer.parseInt(temp.substring(8, 15));
                    filesList.add(t);
                }
            }
            Collections.sort(filesList);
            int startingId = filesList.get(0);
            int endingId = filesList.get(filesList.size() - 1);
            process(startingId, endingId);
        }
    }

    public static void process(int low, int high) throws Exception {
        // finds the first valid file and prints until there are no valid lines.

        BufferedReader reader = null;
        String line;
        String[] tempLine;
        String currentFileName;
        File tempFile = null;
        LocalDateTime currentTime = null;
        int firstValid = 0, endingId = high;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            currentFileName = getFileName(mid);
            tempFile = new File(directory + currentFileName);
            reader = new BufferedReader(new FileReader(tempFile));
            line = reader.readLine();
            if (line != null) {
                tempLine = line.split(",");
                currentTime = LocalDateTime.parse(tempLine[0].substring(0, tempLine[0].length() - 1));
                reader.close();
                if (!currentTime.isAfter(fromTime)) {
                    firstValid = mid;
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }
        }

        // fileId after printing first valid file.
        int fileId = printFirstFile(firstValid);

        while (fileId <= endingId) {

            String fileName = getFileName(fileId);
            tempFile = new File(directory + fileName);

            if (!tempFile.exists()) {
                break;
            }
            reader = new BufferedReader(new FileReader(tempFile));
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                currentTime = LocalDateTime.parse(tokens[0].substring(0, tokens[0].length() - 1));
                if (!currentTime.isAfter(toTime)) {
                    System.out.println(line); // print the log to console.
                } else if (currentTime.isAfter(toTime)) {
                    fileId = endingId + 1;
                    break;
                }
            }
            reader.close();
            fileId++; // next file
        }
        return;
    }

    public static int printFirstFile(int fileId) {

        // finds valid first line in the given file and prints from that line.
        String tempFile = getFileName(fileId);
        String[] tempLine = null;
        LocalDateTime currentTime = null;

        try {
            File file = new File(directory + tempFile);
            RandomAccessFile raf = new RandomAccessFile(file, "r"); // opening file in read mode
            long low = 0;
            long high = file.length();

            long p = -1; // pointer
            while (low < high) {
                long mid = low + (high - low) / 2;
                p = mid;
                while (p >= 0) {
                    raf.seek(p);
                    char c = (char) raf.readByte();
                    if (c == '\n')
                        break;
                    p--;
                }
                if (p < 0)
                    raf.seek(0);
                tempLine = raf.readLine().split(",");
                currentTime = LocalDateTime.parse(tempLine[0].substring(0, tempLine[0].length() - 1));
                if (currentTime.isBefore(fromTime))
                    low = mid + 1;
                else
                    high = mid;
            }
            p = low; // starting point
            while (p >= 0) {
                raf.seek(p);
                if (((char) raf.readByte()) == '\n')
                    break;
                p--;
            }
            if (p < 0)
                raf.seek(0);

            // printing valid lines of the file.
            while (true) {
                String line = raf.readLine();
                if (line == null)
                    break;
                tempLine = line.split(",");
                currentTime = LocalDateTime.parse(tempLine[0].substring(0, tempLine[0].length() - 1));
                if (currentTime.isAfter(toTime))
                    break;
                System.out.println(line);
            }
            raf.close();
        } catch (IOException e) {
            System.out.println("IOException:");
            e.printStackTrace();
        }
        return fileId + 1;
    }

    public static String getFileName(int id) {
        // returns file name in valid format from the given Id.
        String temp = String.valueOf(id);
        int length = temp.length();
        if (length == 7) {
            return "LogFile-" + temp + ".log";
        } else {
            while (temp.length() < 7) {
                temp = "0" + temp;
            }
        }
        return "LogFile-" + temp + ".log";
    }
}
