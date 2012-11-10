<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="no" omit-xml-declaration="yes"/>

	<xsl:template match="*[@style]|b[@style]">
		<xsl:param name="alreadyreplaced"/>
		<xsl:choose>
			<xsl:when test="not(contains($alreadyreplaced,'bold')) and local-name() = 'b'">
				<xsl:text>[b]</xsl:text>
				<xsl:apply-templates select=".">
					<xsl:with-param name="alreadyreplaced" select="'bold'"/>
				</xsl:apply-templates>
				<xsl:text>[/b]</xsl:text>
			</xsl:when>
			<xsl:when test="not(contains($alreadyreplaced,'text-align')) and contains(@style, 'text-align')">
				<xsl:apply-templates select="." mode="style">
					<xsl:with-param name="alreadyreplaced" select="$alreadyreplaced"/>
					<xsl:with-param name="style">text-align</xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:when test="not(contains($alreadyreplaced,'color')) and contains(@style, 'color')">
				<xsl:apply-templates select="." mode="style">
					<xsl:with-param name="alreadyreplaced" select="$alreadyreplaced"/>
					<xsl:with-param name="style">color</xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:when test="not(contains($alreadyreplaced,'font-size')) and contains(@style, 'font-size')">
				<xsl:apply-templates select="." mode="style">
					<xsl:with-param name="alreadyreplaced" select="$alreadyreplaced"/>
					<xsl:with-param name="style">font-size</xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="*|text()"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="*" mode="style">
		<xsl:param name="alreadyreplaced"/>
		<xsl:param name="style"/>
		<xsl:variable name="savestyle" select="concat(@style,';')"/>

		<xsl:variable name="value">
			<xsl:variable name="value2" select="normalize-space(substring-before(substring-after(substring-after($savestyle, $style),':'),';'))"/>
			<xsl:choose>
				<xsl:when test="$style = 'font-size'">
						<xsl:choose>
							<xsl:when test="$value2 = '70%'">size=1</xsl:when>
							<xsl:when test="$value2 = '120%'">size=+1</xsl:when>
							<xsl:when test="$value2 = '150%'">size=+2</xsl:when>
							<xsl:otherwise>size=1</xsl:otherwise>
						</xsl:choose>
				</xsl:when>
				<xsl:when test="$style = 'color'">
					<xsl:choose>
						<xsl:when test="starts-with($value2, 'rgb')">black</xsl:when>
						<xsl:otherwise><xsl:value-of select="$value2"/></xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$value2"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable> 
		
		<xsl:text>[</xsl:text>
		<xsl:value-of select="$value"/>
		<xsl:text>]</xsl:text>

		<xsl:apply-templates select=".">
			<xsl:with-param name="alreadyreplaced" select="concat($alreadyreplaced,$style)"/>
		</xsl:apply-templates>

		<xsl:text>[/</xsl:text>
		<xsl:choose>
			<xsl:when test="contains($value,'=')">
				<xsl:value-of select="substring-before($value,'=')"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$value"/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>]</xsl:text>
	</xsl:template>

	<xsl:template match="a[@href]">
		<xsl:choose>
			<xsl:when test="text() = 'visit link'">
				<xsl:text>[url]</xsl:text><xsl:value-of select="@href"/><xsl:text>[/url]</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>[url=</xsl:text><xsl:value-of select="@href"/><xsl:text>]</xsl:text>
				<xsl:apply-templates select="*|text()"/>
				<xsl:text>[/url]</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="big">
		<xsl:text>[size=+1]</xsl:text>
		<xsl:apply-templates select="*|text()"/>
		<xsl:text>[/size]</xsl:text>
	</xsl:template>

	<xsl:template match="small">
		<xsl:text>[size=1]</xsl:text>
		<xsl:apply-templates select="*|text()"/>
		<xsl:text>[/size]</xsl:text>
	</xsl:template>

	<xsl:template match="img">
		<xsl:text>[img]</xsl:text>
		<xsl:value-of select="@src"/>
		<xsl:text>[/img]</xsl:text>
	</xsl:template>
	
	<xsl:template match="b | i | s">
		<xsl:text>[</xsl:text><xsl:value-of select="local-name(.)"/><xsl:text>]</xsl:text>
		<xsl:apply-templates select="*|text()"/>
		<xsl:text>[/</xsl:text><xsl:value-of select="local-name(.)"/><xsl:text>]</xsl:text>
	</xsl:template>
	
	<xsl:template match="br">
		<xsl:text>
</xsl:text>
	</xsl:template>

	<xsl:template match="p">
		<xsl:apply-templates select="*|text()"/>
		<xsl:text>

</xsl:text>
	</xsl:template>

	<xsl:template match="blockquote">
		<xsl:text>[quote]</xsl:text>
		<xsl:value-of select="(.//hr[1])/following-sibling::text()"/>
		<xsl:text>[/quote]</xsl:text>
	</xsl:template>

	<xsl:template match="bbcode">
		<xsl:apply-templates select="*|text()"/>
	</xsl:template>
	
	<xsl:template match="*|@*|text()">
		<xsl:copy>
			<xsl:apply-templates select="*|@*|text()"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>