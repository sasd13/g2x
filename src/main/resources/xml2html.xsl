<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml">
  <xsl:output method="xml"
            encoding="utf-8" 
            doctype-public="-//W3C//DTD XHTML 1.1//EN"
            doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"
            indent="yes"/>
            
  <xsl:template match="/">
    <html>
      <head>
        <title>XML to XHTML</title>
      </head>
      <body>
        <h1>Gedcom</h1>
        <hr/>
        <a>
			<xsl:attribute name="href"><xsl:value-of select="'#individus'"/></xsl:attribute>
			Individus
		</a>
		<br/>
		<a> 
			<xsl:attribute name="href"><xsl:value-of select="'#familles'"/></xsl:attribute>
			Families
		</a>
		<hr/>
        <h2>
        	<xsl:attribute name="id"><xsl:value-of select="'individus'"/></xsl:attribute>
			Individus
		</h2>
        <ul>
        	<xsl:apply-templates select="gedcom/indi">
        		<xsl:sort select="name" order="ascending"/>
        	</xsl:apply-templates>
        </ul>
        <hr/>
        <h2>
        	<xsl:attribute name="id"><xsl:value-of select="'familles'"/></xsl:attribute>
			Families
		</h2>
        <ul>
        	<xsl:apply-templates select="gedcom/fam">
        		<xsl:sort select="@id" order="ascending" data-type="number"/>
        	</xsl:apply-templates>
        </ul>
      </body>
    </html>
  </xsl:template>
  
  <xsl:template match="indi">
  	<li>
  		<xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
  		<strong><xsl:value-of select="name"/></strong>
  		<p>
  			<xsl:variable name="id" select="@id"/>
  			<xsl:variable name="textid" select="translate($id,'id-','')"/>
  			INDI <xsl:value-of select="$textid"/>
  			<xsl:if test="titl">TITL : <xsl:value-of select="titl"/></xsl:if>
  			<br/>
  			<xsl:for-each select="famx">
  				<xsl:variable name="ref" select="@ref"/>
  				<xsl:variable name="textref" select="translate($ref,'id-','')"/>
  				<xsl:value-of select="@type"/> : <a href="#{@ref}">FAM <xsl:value-of select="$textref"/></a>
  				<br/>
  			</xsl:for-each>
  			<xsl:for-each select="event">
  				<xsl:value-of select="@type"/> : <xsl:apply-templates select="."/>
  				<br/>	
  			</xsl:for-each>
  			<xsl:for-each select="obje">
  				OBJE :
  				<ul>
  					<li>
  						<xsl:if test="titl">TITL : <xsl:value-of select="titl"/></xsl:if>
  					</li>
  					<li>
  						<xsl:if test="file">FILE : <xsl:value-of select="file"/></xsl:if>
  					</li>
  					<li>
  						<xsl:if test="form">FORM : <xsl:value-of select="form"/></xsl:if>
  					</li>
  				</ul>
  				<br/>
  			</xsl:for-each>
  		</p>
  	</li>
  </xsl:template>
  
  <xsl:template match="fam">
  	<li>
  		<xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
  		<xsl:variable name="id" select="@id"/>
  		<xsl:variable name="textid" select="translate($id,'id-','')"/>
  		<strong>FAM <xsl:value-of select="$textid"/></strong>
  		<p>
  			<xsl:for-each select="indix">
  				<xsl:variable name="ref" select="@ref"/>
  				<xsl:variable name="textref" select="translate($ref,'id-','')"/>
  				<xsl:value-of select="@type"/> : <a href="#{@ref}">INDI <xsl:value-of select="$textref"/></a>
  				<br/>
  			</xsl:for-each>
  			<xsl:for-each select="event">
  				<xsl:value-of select="@type"/> : <xsl:apply-templates select="."/>
  				<br/>	
  			</xsl:for-each>
  		</p>
  	</li>
  </xsl:template>
</xsl:stylesheet> 