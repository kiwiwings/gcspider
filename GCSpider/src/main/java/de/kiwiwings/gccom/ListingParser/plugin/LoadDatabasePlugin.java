package de.kiwiwings.gccom.ListingParser.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;

import de.kiwiwings.gccom.ListingParser.CacheAttribute;
import de.kiwiwings.gccom.ListingParser.SpiderConfig;
import de.kiwiwings.gccom.ListingParser.SpiderContext;

public class LoadDatabasePlugin implements SpiderPlugin {
	public void execute(SpiderContext ctx) throws Exception {
		SpiderConfig config = ctx.getConfig();
		File excelF = new File(config.getExcelFile());
		Workbook wb = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(excelF)));
		Sheet sheet = wb.getSheet(ctx.getDatatable());
		
		Map<String,Integer> schema = ctx.getSchema();
		schema.clear();
		
		Map<Integer,String> schemaRev = new HashMap<Integer,String>();
		
		List<CacheAttribute> attrList = new ArrayList<CacheAttribute>();
		ctx.setCacheAttr(attrList);
		
		int maxNames = wb.getNumberOfNames();
		for (int i=0; i<maxNames; i++) {
			Name name = wb.getNameAt(i);
			// only parse names for the current table
			if (!name.getSheetName().equals(ctx.getDatatable())) continue;
		    AreaReference aref = new AreaReference(name.getRefersToFormula());

		    // only parse names referencing one cell
		    if (!aref.getFirstCell().equals(aref.getLastCell())) continue;
		    CellReference allCrefs[] = aref.getAllReferencedCells();
		    if (allCrefs.length != 1) continue;
		    
		    int rowi = allCrefs[0].getRow();
		    int celli = allCrefs[0].getCol();

		    schema.put(name.getNameName(), celli);
		    schemaRev.put(celli, name.getNameName());
		    
		    if (name.getNameName().startsWith("attribute_")) {
		    	Comment comm = sheet.getRow(rowi).getCell(celli).getCellComment();
		    	if (comm != null) {
		    		String text = comm.getString().getString();
		    		if (text != null && text.contains(";")) {
		    			String parts[] = text.split(";");
		    			CacheAttribute ca = new CacheAttribute();
		    			ca.setColumn(name.getNameName());
		    			ca.setIndex(Integer.parseInt(parts[0]));
		    			ca.setLabel(parts[1]);
		    			attrList.add(ca);
		    		}
		    	}
		    }
		}

		Collections.sort(attrList, new Comparator<CacheAttribute>() {
			@Override
			public int compare(CacheAttribute o1, CacheAttribute o2) {
				return o1.getIndex()-o2.getIndex();
			}
		});
		
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setGroupingUsed(false);
		
		int maxRows = sheet.getLastRowNum();
		for (int rowi=1; rowi<=maxRows; rowi++) {
			Row row = sheet.getRow(rowi);
			
			Map<String,String> entry = new HashMap<String,String>();
			
			int maxCols = row.getLastCellNum();
			for (int celli=0; celli<maxCols; celli++) {
				String name = schemaRev.get(celli);
				Cell cell = row.getCell(celli);
				String value = "";
				if (cell != null) { 
					switch (cell.getCellType()) {
						case Cell.CELL_TYPE_BOOLEAN:
							value = Boolean.toString(cell.getBooleanCellValue());
							break;
						case Cell.CELL_TYPE_NUMERIC:
							if (DateUtil.isCellDateFormatted(cell)) {
								Date date = cell.getDateCellValue();
								value = SpiderContext.fullDateFormat.format(date);
							} else {
								value = nf.format(cell.getNumericCellValue());
							}
							break;
						case Cell.CELL_TYPE_BLANK:
						case Cell.CELL_TYPE_STRING:
							value = cell.getStringCellValue();
							break;
						case Cell.CELL_TYPE_FORMULA:
							value = cell.getCellFormula();
							break;
						default:
							throw new Exception("unhandled cell type - row: "+rowi+", col: "+celli+", name: "+name);
					}
				}
				entry.put(name, value);
			}

			ctx.putDatabaseEntry(entry);
		}
		ctx.getChangedIdx().clear();
	}
}
