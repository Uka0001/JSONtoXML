package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * JSON to XML converter task by KnubiSoft
 */
public class JsonToXmlJackson {
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Method to convert json file to Excel file
     *
     * @param srcFile
     * @param targetFileExtension
     * @return file
     */

    public File jsonFileToExcelFile(File srcFile, String targetFileExtension) {

        try {
            if (!srcFile.getName().endsWith(".json")) {
                throw new IllegalArgumentException("The source file should be .json file only");
            } else {
                Workbook workbook = null;

                //Creating workbook object based on target file format
                if (targetFileExtension.equals(".xls")) {
                    workbook = new HSSFWorkbook();
                } else if (targetFileExtension.equals(".xlsx")) {
                    workbook = new XSSFWorkbook();
                } else {
                    throw new IllegalArgumentException("The target file extension should be .xls or .xlsx only");
                }

                //Reading the json file
                JsonNode jsonData = mapper.readTree(srcFile);

                //Iterating over each sheets
                Iterator<String> sheetItr = jsonData.fieldNames();
                while (sheetItr.hasNext()) {
                    // create the workbook sheet
                    String sheetName = sheetItr.next();
                    Sheet sheet = workbook.createSheet(sheetName);

                    ArrayNode sheetData = (ArrayNode) jsonData.get(sheetName);
                    ArrayList<String> headers = new ArrayList<String>();

                    //Creating cell style for header to make it bold
                    CellStyle headerStyle = workbook.createCellStyle();
                    Font font = workbook.createFont();
                    font.setBold(true);
                    headerStyle.setFont(font);

                    //creating the header into the sheet
                    Row header = sheet.createRow(0);
                    Iterator<String> it = sheetData.get(0).fieldNames();
                    int headerIdx = 0;
                    while (it.hasNext()) {
                        String headerName = it.next();
                        headers.add(headerName);
                        Cell cell = header.createCell(headerIdx++);
                        cell.setCellValue(headerName);
                        //apply the bold style to headers
                        cell.setCellStyle(headerStyle);
                    }

                    //Iterating over the row data and writing into the sheet
                    for (int i = 0; i < sheetData.size(); i++) {
                        ObjectNode rowData = (ObjectNode) sheetData.get(i);
                        Row row = sheet.createRow(i + 1);
                        for (int j = 0; j < headers.size(); j++) {
                            String value = rowData.get(headers.get(j)).asText();
                            row.createCell(j).setCellValue(value);
                        }
                    }

                    /*
                     * automatic adjust data in column using autoSizeColumn, autoSizeColumn should
                     * be made after populating the data into the Excel. Calling before populating
                     * data will not have any effect.
                     */
                    for (int i = 0; i < headers.size(); i++) {
                        sheet.autoSizeColumn(i);
                    }

                }

                //creating a target file
                String filename = srcFile.getName();
                filename = filename.substring(0, filename.lastIndexOf(".json")) + targetFileExtension;
                File targetFile = new File(srcFile.getParent(), filename);

                // write the workbook into target file
                FileOutputStream fos = new FileOutputStream(targetFile);
                workbook.write(fos);

                //close the workbook and fos
                workbook.close();
                fos.close();
                return targetFile;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Main method to test this converter
     *
     * @param args
     */
    public static void main(String[] args) throws FileNotFoundException {

        JsonToXmlJackson converter = new JsonToXmlJackson();
        File srcFile = new File("/Users/oleksii/Documents/GitHub/JSONtoXLS/src/main/resources/startupExhibitors.json");
        File xlsFile = converter.jsonFileToExcelFile(srcFile, ".xls");
        System.out.println("Successfully converted JSON to Excel file at =" + xlsFile.getAbsolutePath());

    }
}