import com.opencsv.CSVWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.*;
import java.util.List;

public class CsvFile {
    private static final Logger logger = LogManager.getLogger(CsvFile.class);


    public static void importDataToCsvFile(String[] headers, List<String[]> listForCss, String file) throws IOException{
        try (
                Writer writer = new BufferedWriter(new FileWriter(file));
                CSVWriter csvWriter = new CSVWriter(writer, ';',
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END);)
        {
            logger.info("Waiting for import data to CSV file ");
            csvWriter.writeNext(headers);
            csvWriter.writeAll(listForCss);
            logger.info("Import completed successfully");

        }

    }
}
