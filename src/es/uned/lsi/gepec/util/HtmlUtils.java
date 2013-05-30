/* OpenMark Authoring Tool (GEPEQ)
 * Copyright (C) 2013 UNED
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package es.uned.lsi.gepec.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

public class HtmlUtils
{
	/**
	 * Get an instance of org.w3c.Document class with the document appropiated for a text containing HTML tags
	 * @param text Text to read (expected in HTML syntax)
	 * @return Instance of org.w3c.Document class with the document appropiated for a text containing
	 * HTML tags
	 */
	public static Document readAsHTML(String text) throws Exception
	{
		// Use JTidy to parse HTML
		Tidy tidy=new Tidy();
		tidy.setXmlOut(true);
		return tidy.parseDOM(new StringReader(text),new StringWriter());
	}
	
	/**
	 * @param htmlDoc Document (expected to be an HTML document read with JTidy)
	 * @param indent true to indent output, false otherwise
	 * @return HTML content as string
	 */
	public static String getHTMLContent(Document htmlDoc,boolean indent)
	{
		String htmlContent=null;
		
		// We need to find <body> tag
		Node bodyNode=findBodyNode(htmlDoc);
		if (bodyNode!=null)
		{
			// Transform HTML document to string
			Transformer t=null;
			try
			{
				TransformerFactory tf=TransformerFactory.newInstance();
				t=tf.newTransformer();
				t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
				t.setOutputProperty(OutputKeys.INDENT,indent?"yes":"no");
			}
			catch (TransformerConfigurationException tce)
			{
				t=null;
			}
			catch (IllegalArgumentException iae)
			{
				t=null;
			}
			StringWriter htmlContentSW=null;
			if (t!=null)
			{
				htmlContentSW=new StringWriter();
				Result r=new StreamResult(htmlContentSW);
				Source s=new DOMSource(bodyNode);
				try
				{
					t.transform(s,r);
				}
				catch (TransformerException te)
				{
					htmlContentSW=null;
				}
			}
			if (htmlContentSW!=null)
			{
				htmlContent=htmlContentSW.toString();
				if (htmlContent.startsWith("<body>") && htmlContent.endsWith("</body>"))
				{
					htmlContent=htmlContentSW.toString().substring("<body>".length());
					htmlContent=htmlContent.substring(0,htmlContent.length()-"</body>".length());
				}
				else
				{
					htmlContent=null;
				}
			}
		}
		return htmlContent==null?"":htmlContent;
	}
	
	/**
	 * Get content of HTML document as plain text.<br/><br/>
	 * Alignment, color, fonts and that kind of things are ignored so text is displayed unformatted.<br/><br/>
	 * Moreover images, scripts, forms, input fields and other things are ignored.<br/><br/>
	 * Unordered and ordered lists are supported... however type attribute is ignored so items from unordered lists are
	 * always written with a preceding * and items from ordered lists with their corresponding number followed by .<br/><br/>
	 * Tables are supported but only if they are not nested and in any case rowspan/colspan attributes are ignored and 
	 * borders are not displayed.<br/><br/>
	 * &lt;pre&gt; and &lt;xmp&gt; tag are also supported...<br/><br/>
	 * &lt;pre&gt; will ignore tags and unescape escaped characters.<br/><br/>
	 * &lt;xmp&gt; write all (even html tags with their attributes although output can be slightly different from
	 * input due to html parsing) and won't unescape anything.<br/><br/>
	 * <b>IMPORTANT</b>: If you want to display a plain text with a navigator you must include it whithin 
	 * &lt;pre&gt;...&lt;/pre&gt; (if there are no tags nor escaped characters) or &lt;xmp&gt;...&lt;/xmp&gt; for correct 
	 * display.
	 * @param htmlDoc Document (expected to be an HTML document read with JTidy)
	 * @return Content of HTML document as plain text
	 */
	public static String getPlainText(Document htmlDoc)
	{
		// We need to find <body> tag
		Node bodyNode=findBodyNode(htmlDoc);
		return getPlainTextFromNode(bodyNode,0,true);
	}
	
	/**
	 * Get content from a node of an HTML document as plain text.
	 * @param parent Node from wich get content as plain text
	 * @param tabLevel Level of indentation
	 * @param allowTable Flag to indicate if it is allowed to display tables (used to ignore nested tables)
	 * @return Content from a node of an HTML document as plain text
	 */
	private static String getPlainTextFromNode(Node parent,int tabLevel,boolean allowTable)
	{
		StringBuffer plainTextFromNode=new StringBuffer();
		NodeList childs=parent.getChildNodes();
		for (int i=0;i<childs.getLength();i++)
		{
			Node child=childs.item(i);
			if (child.getNodeType()==Node.ELEMENT_NODE)
			{
				if (child.getNodeName().equalsIgnoreCase("a") || child.getNodeName().equalsIgnoreCase("b") ||
					child.getNodeName().equalsIgnoreCase("bdo") || child.getNodeName().equalsIgnoreCase("big") || 
					child.getNodeName().equalsIgnoreCase("body") || child.getNodeName().equalsIgnoreCase("center") || 
					child.getNodeName().equalsIgnoreCase("cite") ||child.getNodeName().equalsIgnoreCase("code") || 
					child.getNodeName().equalsIgnoreCase("del") ||child.getNodeName().equalsIgnoreCase("dfn") || 
					child.getNodeName().equalsIgnoreCase("em") || child.getNodeName().equalsIgnoreCase("font") ||
					child.getNodeName().equalsIgnoreCase("head") || child.getNodeName().equalsIgnoreCase("html") ||
					child.getNodeName().equalsIgnoreCase("i") || child.getNodeName().equalsIgnoreCase("ins") ||
					child.getNodeName().equalsIgnoreCase("kbd") || child.getNodeName().equalsIgnoreCase("label") || 
					child.getNodeName().equalsIgnoreCase("s") || child.getNodeName().equalsIgnoreCase("samp") ||
					child.getNodeName().equalsIgnoreCase("small") || child.getNodeName().equalsIgnoreCase("span") || 
					child.getNodeName().equalsIgnoreCase("strike") || child.getNodeName().equalsIgnoreCase("strong") ||
					child.getNodeName().equalsIgnoreCase("sub") || child.getNodeName().equalsIgnoreCase("sup") ||
					child.getNodeName().equalsIgnoreCase("tt") || child.getNodeName().equalsIgnoreCase("u") ||
					child.getNodeName().equalsIgnoreCase("var")
					)
				{
					plainTextFromNode.append(getPlainTextFromNode(child,tabLevel,allowTable));
				}
				else if (child.getNodeName().equalsIgnoreCase("address") || child.getNodeName().equalsIgnoreCase("div") || 
					child.getNodeName().equalsIgnoreCase("fieldset") || child.getNodeName().equalsIgnoreCase("form") || 
					child.getNodeName().equalsIgnoreCase("h1") || child.getNodeName().equalsIgnoreCase("h2") || 
					child.getNodeName().equalsIgnoreCase("h3") || child.getNodeName().equalsIgnoreCase("h4") || 
					child.getNodeName().equalsIgnoreCase("h5") || child.getNodeName().equalsIgnoreCase("h6") || 
					child.getNodeName().equalsIgnoreCase("iframe") || child.getNodeName().equalsIgnoreCase("p") || 
					child.getNodeName().equalsIgnoreCase("pre")
					)
				{
					if (plainTextFromNode.length()>0 && !plainTextFromNode.toString().endsWith("\n"))
					{
						plainTextFromNode.append('\n');
					}
					String elementText=getPlainTextFromNode(child,tabLevel,allowTable);
					if (!elementText.equals(""))
					{
						if (plainTextFromNode.toString().endsWith("\n"))
						{
							for (int iTab=0;iTab<tabLevel;iTab++)
							{
								plainTextFromNode.append("\t");
							}
						}
						plainTextFromNode.append(elementText);
					}
				}
				else if (child.getNodeName().equalsIgnoreCase("blockquote") || child.getNodeName().equalsIgnoreCase("dd") ||
					child.getNodeName().equalsIgnoreCase("dir") || child.getNodeName().equalsIgnoreCase("menu") ||
					child.getNodeName().equalsIgnoreCase("ol") || child.getNodeName().equalsIgnoreCase("ul")
					)
				{
					if (plainTextFromNode.length()>0 && !plainTextFromNode.toString().endsWith("\n"))
					{
						plainTextFromNode.append('\n');
						for (int iTab=0;iTab<tabLevel;iTab++)
						{
							plainTextFromNode.append("\t");
						}
					}
					plainTextFromNode.append('\t');
					plainTextFromNode.append(getPlainTextFromNode(child,tabLevel+1,allowTable));
					if (child.getNextSibling()!=null)
					{
						plainTextFromNode.append('\n');
						for (int iTab=0;iTab<tabLevel;iTab++)
						{
							plainTextFromNode.append("\t");
						}
					}
				}
				else if (child.getNodeName().equalsIgnoreCase("br") || child.getNodeName().equalsIgnoreCase("hr")
					)
				{
					plainTextFromNode.append('\n');
				}
				else if (child.getNodeName().equals("legend"))
				{
					if (parent.getNodeType()==Node.ELEMENT_NODE && parent.getNodeName().equalsIgnoreCase("fieldset") && 
						parent.getFirstChild().equals(child))
					{
						plainTextFromNode.append(getPlainTextFromNode(child,tabLevel,allowTable));
						plainTextFromNode.append('\n');
					}
				}
				else if (child.getNodeName().equals("dt"))
				{
					if (plainTextFromNode.length()>0 && !plainTextFromNode.toString().endsWith("\n"))
					{
						plainTextFromNode.append('\n');
					}
					String dtText=getPlainTextFromNode(child,tabLevel,allowTable);
					if (!dtText.equals(""))
					{
						for (int iTab=0;iTab<tabLevel;iTab++)
						{
							plainTextFromNode.append("\t");
						}
						plainTextFromNode.append(dtText);
					}
				}
				else if (child.getNodeName().equals("li"))
				{
					if (plainTextFromNode.length()>0 && !plainTextFromNode.toString().endsWith("\n"))
					{
						plainTextFromNode.append('\n');
						for (int iTab=0;iTab<tabLevel;iTab++)
						{
							plainTextFromNode.append("\t");
						}
					}
					if ((parent.getNodeType()==Node.ELEMENT_NODE && parent.getNodeName().equalsIgnoreCase("ol")))
					{
						int liCount=1;
						Node olChild=parent.getFirstChild();
						while (!olChild.equals(child))
						{
							if (olChild.getNodeType()==Node.ELEMENT_NODE && olChild.getNodeName().equalsIgnoreCase("li"))
							{
								liCount++;
							}
							olChild=olChild.getNextSibling();
						}
						plainTextFromNode.append(liCount);
						plainTextFromNode.append(". ");
						plainTextFromNode.append(getPlainTextFromNode(child,tabLevel,allowTable));
					}
					else
					{
						plainTextFromNode.append("* ");
						plainTextFromNode.append(getPlainTextFromNode(child,tabLevel,allowTable));
					}
				}
				else if (child.getNodeName().equalsIgnoreCase("q"))
				{
					plainTextFromNode.append('\"');
					plainTextFromNode.append(getPlainTextFromNode(child,tabLevel,allowTable));
					plainTextFromNode.append('\"');
				}
				else if (allowTable && child.getNodeName().equalsIgnoreCase("table"))
				{
					if (plainTextFromNode.length()>0 && !plainTextFromNode.toString().endsWith("\n"))
					{
						plainTextFromNode.append('\n');
						for (int iTab=0;iTab<tabLevel;iTab++)
						{
							plainTextFromNode.append("\t");
						}
					}
					NodeList tableChilds=child.getChildNodes();
					Node caption=null;
					List<List<List<String>>> tableRows=new ArrayList<List<List<String>>>();
					List<List<List<String>>> tableHeadRows=new ArrayList<List<List<String>>>();
					List<List<List<String>>> tableFootRows=new ArrayList<List<List<String>>>();
					for (int iTableChild=0;iTableChild<tableChilds.getLength();iTableChild++)
					{
						Node tableChild=tableChilds.item(iTableChild);
						if (tableChild.getNodeType()==Node.ELEMENT_NODE)
						{
							if (caption==null && tableChild.getNodeName().equalsIgnoreCase("caption"))
							{
								caption=tableChild;
							}
							else if (tableChild.getNodeName().equalsIgnoreCase("tr"))
							{
								List<List<String>> tableCells=new ArrayList<List<String>>();
								tableRows.add(tableCells);
								NodeList trChilds=tableChild.getChildNodes();
								for (int iTrChild=0;iTrChild<trChilds.getLength();iTrChild++)
								{
									Node trChild=trChilds.item(iTrChild);
									if (trChild.getNodeType()==Node.ELEMENT_NODE && 
										(trChild.getNodeName().equalsIgnoreCase("th") || 
										trChild.getNodeName().equalsIgnoreCase("td")))
									{
										List<String> cell=new ArrayList<String>();
										String cellText=getPlainTextFromNode(trChild,0,false);
										String[] cellLines=cellText.split("\n");
										for (String cellLine:cellLines)
										{
											cell.add(cellLine);
										}
									}
								}
							}
							else if (tableChild.getNodeName().equalsIgnoreCase("tbody"))
							{
								NodeList tbodyChilds=tableChild.getChildNodes();
								for (int iTbodyChild=0;iTbodyChild<tbodyChilds.getLength();iTbodyChild++)
								{
									Node tbodyChild=tbodyChilds.item(iTbodyChild);
									if (tbodyChild.getNodeName().equalsIgnoreCase("tr"))
									{
										List<List<String>> tableCells=new ArrayList<List<String>>();
										tableRows.add(tableCells);
										NodeList trChilds=tbodyChild.getChildNodes();
										for (int iTrChild=0;iTrChild<trChilds.getLength();iTrChild++)
										{
											Node trChild=trChilds.item(iTrChild);
											if (trChild.getNodeType()==Node.ELEMENT_NODE && 
												(trChild.getNodeName().equalsIgnoreCase("th") || 
												trChild.getNodeName().equalsIgnoreCase("td")))
											{
												List<String> cell=new ArrayList<String>();
												String cellText=getPlainTextFromNode(trChild,0,false);
												String[] cellLines=cellText.split("\n");
												for (String cellLine:cellLines)
												{
													cell.add(cellLine);
												}
											}
										}
									}
								}
							}
							else if (tableChild.getNodeName().equalsIgnoreCase("thead"))
							{
								NodeList theadChilds=tableChild.getChildNodes();
								for (int iTheadChild=0;iTheadChild<theadChilds.getLength();iTheadChild++)
								{
									Node theadChild=theadChilds.item(iTheadChild);
									if (theadChild.getNodeName().equalsIgnoreCase("tr"))
									{
										List<List<String>> tableCells=new ArrayList<List<String>>();
										tableHeadRows.add(tableCells);
										NodeList trChilds=theadChild.getChildNodes();
										for (int iTrChild=0;iTrChild<trChilds.getLength();iTrChild++)
										{
											Node trChild=trChilds.item(iTrChild);
											if (trChild.getNodeType()==Node.ELEMENT_NODE && 
												(trChild.getNodeName().equalsIgnoreCase("th") || 
												trChild.getNodeName().equalsIgnoreCase("td")))
											{
												List<String> cell=new ArrayList<String>();
												String cellText=getPlainTextFromNode(trChild,0,false);
												String[] cellLines=cellText.split("\n");
												for (String cellLine:cellLines)
												{
													cell.add(cellLine);
												}
											}
										}
									}
								}
							}
							else if (tableChild.getNodeName().equalsIgnoreCase("tfoot"))
							{
								NodeList tfootChilds=tableChild.getChildNodes();
								for (int iTfootChild=0;iTfootChild<tfootChilds.getLength();iTfootChild++)
								{
									Node tfootChild=tfootChilds.item(iTfootChild);
									if (tfootChild.getNodeName().equalsIgnoreCase("tr"))
									{
										List<List<String>> tableCells=new ArrayList<List<String>>();
										tableFootRows.add(tableCells);
										NodeList trChilds=tfootChild.getChildNodes();
										for (int iTrChild=0;iTrChild<trChilds.getLength();iTrChild++)
										{
											Node trChild=trChilds.item(iTrChild);
											if (trChild.getNodeType()==Node.ELEMENT_NODE && 
												(trChild.getNodeName().equalsIgnoreCase("th") || 
												trChild.getNodeName().equalsIgnoreCase("td")))
											{
												List<String> cell=new ArrayList<String>();
												String cellText=getPlainTextFromNode(trChild,0,false);
												String[] cellLines=cellText.split("\n");
												for (String cellLine:cellLines)
												{
													cell.add(cellLine);
												}
											}
										}
									}
								}
							}
						}
					}
					for (int iHeadRow=0;iHeadRow<tableHeadRows.size();iHeadRow++)
					{
						tableRows.add(iHeadRow,tableHeadRows.get(iHeadRow));
					}
					for (int iFootRow=0;iFootRow<tableFootRows.size();iFootRow++)
					{
						tableRows.add(tableFootRows.get(iFootRow));
					}
					if (caption!=null)
					{
						plainTextFromNode.append(getPlainTextFromNode(caption,tabLevel,false));
						plainTextFromNode.append('\n');
						for (int iTab=0;iTab<tabLevel;iTab++)
						{
							plainTextFromNode.append("\t");
						}
					}
					for (int iTableRow=0;iTableRow<tableRows.size();i++)
					{
						List<List<String>> tableRow=tableRows.get(iTableRow);
						for (int iRowLine=0;iRowLine<getTableRowHeight(tableRow);iRowLine++)
						{
							for (int iCol=0;iCol<tableRow.size();iCol++)
							{
								List<String> tableCol=tableRow.get(iCol);
								String colLine=tableCol.size()>iRowLine?tableCol.get(iRowLine):"";
								plainTextFromNode.append(colLine);
								int colWidth=getTableColWidth(tableRows,iCol);
								for (int iRemaining=0;iRemaining<colWidth;iRemaining++)
								{
									plainTextFromNode.append(' ');
								}
								if (iCol<tableRow.size()-1)
								{
									plainTextFromNode.append(' ');
								}
							}
							plainTextFromNode.append('\n');
							for (int iTab=0;iTab<tabLevel;iTab++)
							{
								plainTextFromNode.append("\t");
							}
						}
					}
				}
				else if (child.getNodeName().equalsIgnoreCase("xmp"))
				{
					if (plainTextFromNode.length()>0 && !plainTextFromNode.toString().endsWith("\n"))
					{
						plainTextFromNode.append('\n');
						for (int iTab=0;iTab<tabLevel;iTab++)
						{
							plainTextFromNode.append("\t");
						}
					}
					String xmpText=getPlainTextFromXmpNode(child,tabLevel);
					if (!xmpText.equals(""))
					{
						plainTextFromNode.append(xmpText);
						plainTextFromNode.append('\n');
						for (int iTab=0;iTab<tabLevel;iTab++)
						{
							plainTextFromNode.append("\t");
						}
					}
				}
			}
			else if (child.getNodeType()==Node.TEXT_NODE)
			{
				if (plainTextFromNode.length()>0 && plainTextFromNode.charAt(plainTextFromNode.length()-1)=='\n')
				{
					for (int iTab=0;iTab<tabLevel;iTab++)
					{
						plainTextFromNode.append("\t");
					}
				}
				if (parent.getNodeType()==Node.ELEMENT_NODE && parent.getNodeName().equalsIgnoreCase("pre"))
				{
					plainTextFromNode.append(StringEscapeUtils.unescapeHtml(child.getNodeValue()));
				}
				else
				{
					plainTextFromNode.append(singledWhitespace(StringEscapeUtils.unescapeHtml(child.getNodeValue())).trim());
				}
			}
			
		}
		return plainTextFromNode.toString();
	}
	
	private static int getTableRowHeight(List<List<String>> tableRow)
	{
		int tableRowHeight=0;
		for (List<String> tableCell:tableRow)
		{
			if (tableCell.size()>tableRowHeight)
			{
				tableRowHeight=tableCell.size();
			}
		}
		return tableRowHeight;
	}
	
	private static int getTableColWidth(List<List<List<String>>> tableRows,int iTableCol)
	{
		int tableColWidth=0;
		for (List<List<String>> tableRow:tableRows)
		{
			if (tableRow.size()>iTableCol)
			{
				List<String> tableCol=tableRow.get(iTableCol);
				for (String line:tableCol)
				{
					if (line.length()>tableColWidth)
					{
						tableColWidth=line.length();
					}
				}
				
			}
		}
		return tableColWidth;
	}
	
	private static String getPlainTextFromXmpNode(Node parent,int tabLevel)
	{
		StringBuffer plainTextFromXmpNode=new StringBuffer();
		NodeList childs=parent.getChildNodes();
		for (int i=0;i<childs.getLength();i++)
		{
			Node child=childs.item(i);
			if (child.getNodeType()==Node.ELEMENT_NODE)
			{
				NodeList elementChilds=child.getChildNodes();
				plainTextFromXmpNode.append('<');
				plainTextFromXmpNode.append(child.getNodeName());
				NamedNodeMap attributes=child.getAttributes();
				for (int iAttribute=0;iAttribute<attributes.getLength();iAttribute++)
				{
					Node attribute=attributes.item(iAttribute);
					plainTextFromXmpNode.append(' ');
					plainTextFromXmpNode.append(attribute.getNodeName());
					plainTextFromXmpNode.append("=\"");
					plainTextFromXmpNode.append(attribute.getNodeValue().replace("\"","&quot;"));
					plainTextFromXmpNode.append('\"');
				}
				if (elementChilds.getLength()>0)
				{
					plainTextFromXmpNode.append('>');
					plainTextFromXmpNode.append(getPlainTextFromXmpNode(child,tabLevel));
					plainTextFromXmpNode.append("</");
					plainTextFromXmpNode.append(child.getNodeName());
					plainTextFromXmpNode.append('>');
				}
				else
				{
					plainTextFromXmpNode.append("/>");
				}
			}
			else if (child.getNodeType()==Node.TEXT_NODE)
			{
				StringBuffer newLineWithTabs=new StringBuffer();
				newLineWithTabs.append('\n');
				for (int iTab=0;iTab<tabLevel;iTab++)
				{
					newLineWithTabs.append('\t');
				}
				plainTextFromXmpNode.append(child.getNodeValue().replace("\n",newLineWithTabs.toString()));
			}
		}
		return plainTextFromXmpNode.toString();
	}
	
	/**
	 * Returns the same string but replacing every occurrence of several together whitespaces with a single whitespace.
	 * @param s String
	 * @return Same string but replacing every occurrence of several together whitespaces with a single whitespace
	 */
	private static String singledWhitespace(String s)
	{
		StringBuffer result=new StringBuffer();
		for(int i=0;i<s.length();i++)
		{
			char c=s.charAt(i);
			if (Character.isWhitespace(c))
			{
				boolean searchNotWhitespace=true;
				int iNotWhiteSpace=i;
				while (searchNotWhitespace && iNotWhiteSpace<s.length())
				{
					char c2=s.charAt(iNotWhiteSpace);
					if (Character.isWhitespace(c2))
					{
						iNotWhiteSpace++;
					}
					else
					{
						searchNotWhitespace=false;
					}
				}
				if (!searchNotWhitespace)
				{
					i=iNotWhiteSpace-1;
				}
				result.append(' ');
			}
			else
			{
				result.append(c);
			}
		}
		return result.toString();
	}
	
	/**
	 * Import all nodes under &lt;body&gt; tag of another document to the received root node. 
	 * @param rootNode Root node
	 * @param htmlDoc Document (expected to be an HTML document read with JTidy)
	 */
	public static void importHtmlSubtree(Node rootNode,Document htmlDoc)
	{
		// We need to find <body> tag
		Node bodyNode=findBodyNode(htmlDoc);
		
		// Import <body> subtree to the received root node
		if (bodyNode!=null)
		{
			// Get an imported subtree from HTML document node under <html> tag
			Node importedHtmlSubtree=importNode(rootNode.getOwnerDocument(),bodyNode);
			
			// Move all child nodes from imported subtree to the received root node
			for (Node nextChild=importedHtmlSubtree.getFirstChild();
				nextChild!=null;
				nextChild=importedHtmlSubtree.getFirstChild())
			{
				rootNode.appendChild(nextChild);
			}
		}
	}
	
	/**
	 * @param htmlDoc Document (expected to be an HTML document read with JTidy)
	 * @return &lt;body&gt; tag node
	 */
	private static Node findBodyNode(Document htmlDoc)
	{
		Node bodyNode=null;
		if (htmlDoc!=null)
		{
			// First find <html> tag inside the HTML document
			Node htmlNode=htmlDoc.getFirstChild();
			while (htmlNode!=null && (htmlNode.getNodeName()==null || !htmlNode.getNodeName().equals("html")))
			{
				htmlNode=htmlNode.getFirstChild();
			}
			
			// Now we need to find <body> tag as one of <html> child nodes
			if (htmlNode!=null)
			{
				NodeList htmlChilds=htmlNode.getChildNodes();
				for (int i=0;bodyNode==null || i<htmlChilds.getLength();i++)
				{
					Node htmlChid=htmlChilds.item(i);
					if (htmlChid.getNodeName()!=null && htmlChid.getNodeName().equals("body"))
					{
						bodyNode=htmlChid;
					}
				}
			}
		}
		return bodyNode;
	}
	
	// This method is needed because JTidy doesn't support: 
	// Node importNode(Node importedNode,boolean deep) 
	// from org.w3c.Document
	/**
	 * Get an imported node from a node of another document.<br/><br/>
	 * Note that this method imports recursively all child nodes under the node to import.<br/><br/>
	 * However be careful that this method only import element nodes (with their attributes) and text nodes.
	 * @param doc Document to import to
	 * @param importNode Node to import from
	 * @return Imported node from a node of another document
	 */
	private static Node importNode(Document doc,Node importNode)
	{
		Node importedNode=null;
		if (importNode!=null)
		{
			// We only import element and text nodes
			if (importNode.getNodeType()==Node.ELEMENT_NODE)
			{
				importedNode=doc.createElement(importNode.getNodeName());
				
				// As this is an element node we need to import their attributes
				NamedNodeMap importAttributes=importNode.getAttributes();
				for (int i=0;i<importAttributes.getLength();i++)
				{
					Node importAttribute=importAttributes.item(i);
					((Element)importedNode).setAttribute(importAttribute.getNodeName(),importAttribute.getNodeValue());
				}
				
				// Finally we need to import recursively all its children
				NodeList importChilds=importNode.getChildNodes();
				for (int i=0;i<importChilds.getLength();i++)
				{
					importedNode.appendChild(importNode(doc,importChilds.item(i)));
				}
			}
			else if (importNode.getNodeType()==Node.TEXT_NODE)
			{
				importedNode=doc.createTextNode(importNode.getNodeValue());
			}
		}
		return importedNode;
	}
	
	/**
	 * @param text Text
	 * @return Same text but replacing '\n' characters with &lt;br/&gt; tags after escaping it 
	 * using HTML entities 
	 */
	public static String breakText(String text)
	{
		return breakText(text,0,true);
	}
	
	/**
	 * @param text Text
	 * @param maxBreaks Maximum number of breaks allowed or 0 unlimited
	 * @return Same text but replacing '\n' characters with &lt;br/&gt; tags after escaping it 
	 * using HTML entities
	 */
	public static String breakText(String text,int maxBreaks)
	{
		return breakText(text,maxBreaks,true);
	}
	
	/**
	 * @param text Text
	 * @param maxBreaks Maximum number of breaks allowed or 0 unlimited
	 * @param escape Flag to indicate if characters within text must be escaped using HTML entities 
	 * (true) or not (false).
	 * @return Same text but replacing '\n' characters with &lt;br/&gt; tags after escaping it 
	 * using HTML entities (optional)
	 */
	public static String breakText(String text,int maxBreaks,boolean escape)
	{
		StringBuffer newText=null;
		if (text==null)
		{
			text="";
		}
		else
		{
			text=text.replace("\r\n","\n").replace('\r','\n');
			if (escape)
			{
				text=StringEscapeUtils.escapeHtml(text);
			}
			if (maxBreaks>0)
			{
				newText=new StringBuffer();
				int iCurrentPos=0;
				int iBreak=0;
				int currentBreaks=0;
				while (iBreak!=-1 && currentBreaks<maxBreaks)
				{
					iBreak=text.indexOf('\n',iCurrentPos);
					if (iBreak!=-1)
					{
						currentBreaks++;
						newText.append(text.substring(iCurrentPos,iBreak));
						newText.append("<br/>");
						iCurrentPos=iBreak+1;
					}
				}
				if (currentBreaks<maxBreaks || text.indexOf('\n',iCurrentPos)==-1)
				{
					newText.append(text.substring(iCurrentPos));
				}
				else
				{
					newText.append("...");
				}
			}
			else
			{
				text=text.replace("\n","<br/>");
			}
		}
		return newText==null?text:newText.toString();
	}
}
