package de.kiwiwings.gccom.ListingParser.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import de.kiwiwings.gccom.ListingParser.SpiderConfig;
import de.kiwiwings.gccom.ListingParser.SpiderContext;

public class SaveDatabasePlugin implements SpiderPlugin {
	private Map<String,Cell> templateMap = new HashMap<String,Cell>();
	private Workbook wb;
	
	public void execute(SpiderContext ctx) throws Exception {
		SpiderConfig config = ctx.getConfig();
		File excelF = new File(config.getExcelFile());
		wb = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(excelF)));
		Sheet sheet = wb.getSheet(ctx.getDatatable());
		
		List<Map<String,String>> database = ctx.getDatabase();
		Map<String,Integer> pkIdx = ctx.getPkIdx();
		Map<String,Integer> schema = ctx.getSchema();
		for (String pk : ctx.getChangedIdx()) {
			int rowi = pkIdx.get(pk);
			Map<String,String> entry = database.get(rowi);
			Row row = sheet.getRow(rowi+1);
			if (row == null) {
				row = sheet.createRow(rowi+1);
			}

			for (Map.Entry<String,Integer> column : schema.entrySet()) {
				int celli = column.getValue();
				Cell cell = row.getCell(celli);
				String value = entry.get(column.getKey());

				if (value == null || "".equals(value)) {
					if (cell != null) {
						row.removeCell(cell);
					}
					continue;
				}

				Cell template = getTemplateCell(sheet, column.getKey(), celli, false);
				if (cell == null) {
					if (template == null) {
						cell = row.createCell(celli, Cell.CELL_TYPE_STRING);
						CellStyle cs = sheet.getColumnStyle(celli);
						if (cs != null) cell.setCellStyle(cs);
					} else {
						cell = row.createCell(celli, template.getCellType());
						cell.setCellStyle(template.getCellStyle());
					}
				}

				switch (cell.getCellType()) {
					case Cell.CELL_TYPE_BLANK:
					case Cell.CELL_TYPE_STRING:
						cell.setCellValue(value);
						break;
					case Cell.CELL_TYPE_NUMERIC:
						if (DateUtil.isCellDateFormatted(cell)) {
							Date date = SpiderContext.fullDateFormat.parse(value);
							cell.setCellValue(date);
						} else {
							double d = Double.parseDouble(value);
							cell.setCellValue(d);
						}
						break;
					case Cell.CELL_TYPE_BOOLEAN:
						cell.setCellValue("true".equalsIgnoreCase(value));
						break;
					case Cell.CELL_TYPE_FORMULA:
						if ("1".equals(value) || "0".equals(value)
							|| "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
							CellStyle cs = null;
							if (template != null) {
								cs = template.getCellStyle();
								boolean refresh = (template.getRow().getRowNum() == rowi);
								row.removeCell(cell);
								cell = row.createCell(celli, Cell.CELL_TYPE_BOOLEAN);
								cell.setCellStyle(cs);
								if (refresh) getTemplateCell(sheet, column.getKey(), celli, true);
							}
							cell.setCellValue("true".equalsIgnoreCase(value) || "1".equals(value));
						} else {
							cell.setCellFormula(value);
						}
						break;
					default:
						throw new Exception("unhandled cell type - row: "+rowi+", col: "+celli+", name: "+value);
				}
			}
		}
		
	    FileOutputStream fileOut = new FileOutputStream(excelF);
	    wb.write(fileOut);
	    fileOut.close();
	}

	private Cell getTemplateCell(Sheet sheet, String column, int celli, boolean refresh) {
		Cell cs = templateMap.get(column);
		if (refresh || (cs == null && !templateMap.containsKey(column))) {
			int maxRows = sheet.getLastRowNum();
			for (int rowi=1; cs == null && rowi<maxRows; rowi++) {
				Row row = sheet.getRow(rowi);
				if (row == null) continue;
				cs = row.getCell(celli);
			}
			templateMap.put(column, cs);
		}
		return cs;
	}
		
}
