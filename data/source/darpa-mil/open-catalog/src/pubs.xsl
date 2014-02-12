<xsl:transform version="2.0" 
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xs="http://www.w3.org/2001/XMLSchema"
   xmlns:html="http://www.w3.org/1999/xhtml"
   exclude-result-prefixes="">
<xsl:output method="text"/>

<xsl:template match="/">
   <xsl:apply-templates select="//html:table[@id='pubs']/html:tbody/html:tr"/>
</xsl:template>

<xsl:template match="html:tr">
   <xsl:variable name="team"  select="html:text(html:td[1])"/>
   <xsl:variable name="title" select="html:text(html:td[2])"/>
   <xsl:variable name="link"  select="replace(html:anchor-hrefs(html:td[3]/html:a,''),'^.*url=','')"/>

   <xsl:value-of select="concat($DQ,string-join((
                                                 $team,$title,$link
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
