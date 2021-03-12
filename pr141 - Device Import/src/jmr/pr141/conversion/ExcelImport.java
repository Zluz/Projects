package jmr.pr141.conversion;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * following guidance from:
 * https://www.javatpoint.com/how-to-read-excel-file-in-java
 * 
 *  .. no good, runs out of memory when trying to load.
 */
public class ExcelImport {
	
	public final static String FILE = 
			"D:\\Tasks\\20210309 - COSMIC-417 - Devices\\"
			+ "TAC Lookup 20210122\\testing.xlsx";
	

	public static void main(String[] args) {
		try {
//			File file = new File("C:\\demo\\employee.xlsx"); // creating a new
																// file instance
			
			final File file = new File( FILE );
			
			FileInputStream fis = new FileInputStream(file); // obtaining bytes
																// from the file
			// creating Workbook instance that refers to .xlsx file
			System.out.println( "About to instantiate the XSSFWorkbook.." );
			XSSFWorkbook wb = new XSSFWorkbook(fis);
			System.out.println( "XSSFWorkbook ready." );
			XSSFSheet sheet = wb.getSheetAt(0); // creating a Sheet object to
												// retrieve object
			Iterator<Row> itr = sheet.iterator(); // iterating over excel file
			while (itr.hasNext()) {
				Row row = itr.next();
				Iterator<Cell> cellIterator = row.cellIterator(); // iterating
																	// over each
																	// column
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					switch (cell.getCellType()) {
					case Cell.CELL_TYPE_STRING: // field that represents string
												// cell type
						System.out.print(cell.getStringCellValue() + "\t\t\t");
						break;
					case Cell.CELL_TYPE_NUMERIC: // field that represents number
													// cell type
						System.out.print(cell.getNumericCellValue() + "\t\t\t");
						break;
					default:
					}
				}
				System.out.println("");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
