<xsl:transform version="2.0" 
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xs="http://www.w3.org/2001/XMLSchema"
   xmlns:html="http://www.w3.org/1999/xhtml"
   exclude-result-prefixes="">
<xsl:output method="text"/>

<xsl:template match="/">
   <xsl:apply-templates select="//html:table[@id='software']/html:tbody/html:tr"/>
</xsl:template>

<xsl:template match="html:tr">
   <xsl:variable name="team"           select="html:text(html:td[1])"/>
   <xsl:variable name="software-url"   select="replace(html:anchor-hrefs(html:td[2]/html:a,''),'^.*url=','')"/>
   <xsl:variable name="software-title" select="html:anchor-labels(html:td[2]/html:a)"/>
   <xsl:variable name="category"       select="html:text(html:td[3])"/>
   <xsl:variable name="instructional"  select="html:text(html:td[4])"/>
   <xsl:variable name="code"           select="html:text(html:td[5])"/>
   <xsl:variable name="stats"          select="html:anchor-hrefs(html:td[6]/html:a,'http://www.darpa.mil/OpenCatalog/')"/>
   <xsl:variable name="description"    select="replace(html:text(html:td[7]),$DQ,concat('\\',$DQ))"/>
   <xsl:variable name="license"        select="html:text(html:td[8])"/>

   <xsl:value-of select="concat($DQ,string-join((
                                                 $team,$software-url,$software-title,$category,
                                                 $instructional,$code,$stats,$description,$license
                                                ),
                                                concat($DQ,',',$DQ)),$DQ,$NL)"/>
</xsl:template>

<xsl:template match="@*|node()">
  <xsl:copy>
      <xsl:copy-of select="@*"/>   
      <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>

<!--xsl:template match="text()">
   <xsl:value-of select="normalize-space(.)"/>
</xsl:template-->

<xsl:variable name="NL" select="'&#xa;'"/>
<xsl:variable name="DQ" select="'&#x22;'"/>

<xsl:function name="html:text">
   <xsl:param name="node"/>
   <xsl:variable name="together">
      <xsl:for-each select="$node//text()">
         <xsl:value-of select="normalize-space(.)"/>
      </xsl:for-each>
   </xsl:variable>
   <xsl:value-of select="normalize-space($together)"/>
</xsl:function>

<xsl:function name="html:anchor-labels">
   <xsl:param name="anchors"/>

   <xsl:variable name="together">
      <xsl:for-each select="$anchors">
         <xsl:if test="position() gt 1">
            <xsl:value-of select="'||'"/>
         </xsl:if>
         <xsl:value-of select="normalize-space(.)"/>
      </xsl:for-each>
   </xsl:variable>

   <xsl:value-of select="normalize-space($together)"/>
</xsl:function>

<xsl:function name="html:anchor-hrefs">
   <xsl:param name="anchors"/>
   <xsl:param name="base"/>

   <xsl:variable name="together">
      <xsl:for-each select="$anchors">
         <xsl:if test="position() gt 1">
            <xsl:value-of select="'||'"/>
         </xsl:if>
         <xsl:value-of select="concat($base,normalize-space(@href))"/>
      </xsl:for-each>
   </xsl:variable>

   <xsl:value-of select="normalize-space($together)"/>
</xsl:function>

</xsl:transform>
