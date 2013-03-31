REM  *****  BASIC  *****

Option Explicit

Sub Main
Dim doc as Object
Set doc = thisComponent

Dim docPath as String
docPath = ConvertFromURL(doc.getLocation())
docPath = left(docPath, len(docPath)-4) & "-mobile.xls"
docPath = ConvertToURL(docPath)

Dim oProp As New com.sun.star.beans.PropertyValue
oProp.Name = "FilterName"
oProp.Value = "MS Excel 97"

doc.storeAsUrl(docPath, Array(oProp))

doc.Sheets.removeByName("My Hides")

Dim sheet as Object
Set sheet = doc.Sheets(0)

Dim ColIdx as Integer, lastIdx as Integer, insideRemove as Boolean
lastIdx = -1
insideRemove = false
for ColIdx = 200 to -1 Step -1
	Dim isMini as Boolean
	
	if (ColIdx = -1) then
		isMini = true
	else
		Dim cell As Object
		Set cell = sheet.getCellByPosition(ColIdx,0)

		if (cell.String = "") then
			goto Continue
		end if
		
		isMini = (instr(cell.Annotation.String,"minixls") > 0)
		cell.clearContents( 8 )
	end if
		
	if (isMini) then
		if (insideRemove) then
			insideRemove = false
			Sheet.Columns.removeByIndex(ColIdx+1,lastIdx-ColIdx)
		end if
	else
		if (not (insideRemove)) then
			insideRemove = true
			lastIdx = ColIdx
		end if
	end if

Continue:
next ColIdx

doc.store()
End Sub