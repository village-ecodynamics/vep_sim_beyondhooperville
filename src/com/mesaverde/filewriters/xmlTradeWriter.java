package com.mesaverde.filewriters;

import java.io.*;
import org.w3c.dom.*;

import javax.xml.parsers.*; 
import javax.xml.transform.*; 
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult; 

/**
 * Records Village exchanges in a simple but readable/parseable XML format.
 *
 * @author		Ben Ford <bdford@wsu.edu>
 * @version		1.0
 */
public class xmlTradeWriter 
{
	private DocumentBuilderFactory factory;
	private DocumentBuilder docBuilder;
	private Document doc;
	private Element root;

	/**
	 * Default constructor.
	 *
	 * Initializes the XML subsystem and prepares the document & root node.
	 */
	public xmlTradeWriter()
	{
		try 
		{
			this.factory = DocumentBuilderFactory.newInstance();
			this.docBuilder = this.factory.newDocumentBuilder();
			this.doc = this.docBuilder.newDocument();
			this.root = this.doc.createElement("exchanges");
			this.doc.appendChild(this.root);
			
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * Records a trade.
	 *
	 * @id			The agent's id.
	 * @x			The agent's X coordinate
	 * @y			The agent's Y coordinate
	 * @network		The network type this exchange took place in, e.g. "brn"
	 * @type		The type of exchange this is, e.g. "payback"
	 * @currency	The currency the trade took place in, e.g "maize"
	 * @partner		The trade partner
	 * @amount		The amount of the trade
	 *
	 * Records a trade.  Finds the agent by id and adds to that record.
	 * If not found create one and add to it.
	 */
	public void recordTrade(int id, int x, int y, String network, String type, String currency, int partner, int amount) 
	{
		try
		{
			Element agent = findAgent(id);
			
			if(agent == null)
			{
				agent = this.doc.createElement("agent");
				agent.setAttribute("id", String.format("%s", id));
				agent.setAttribute("x", String.format("%s", x));
				agent.setAttribute("y", String.format("%s", y));
				root.appendChild(agent);
			}

			Element trade = this.doc.createElement("trade"); //amount???
			trade.appendChild(this.doc.createTextNode(String.format("%s", amount)));
			trade.setAttribute("network", String.format("%s", network));
			trade.setAttribute("type", String.format("%s", type));
			trade.setAttribute("partner", String.format("%s", partner));
			trade.setAttribute("currency", String.format("%s", currency));
			agent.appendChild(trade);
			
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * Writes record to disk.
	 *
	 * @name the base name of the file
	 * @year the year of this file
	 *
	 * Writes record to disk.  Constructs file name as ${name}${year}.xml.
	 * Frees the doc, then creates a new one; just in case of memory leaks.
	 */
	public void flushToDisk(String name, int year)
	{
		String filename = String.format("output/%s%d.xml", name, year);
		boolean newfile = false;
		
		try{
			File file = new File(filename);
			
			if(!file.exists())
			{
				file.createNewFile();
				newfile=true;
			}
			
			if (file.exists())
			{
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
				
				Source src = new DOMSource(this.doc); 
				Result dest = new StreamResult(file); 
				transformer.transform(src, dest);
			}			
			
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		
		if(newfile){
			this.startNewDoc();
		}
		
	}
	
	public void startNewDoc()
	{		
		this.doc = this.docBuilder.newDocument();
		this.root = this.doc.createElement("exchanges");
		this.doc.appendChild(this.root);
	}
	
	/**
	 * Find and return an agent by id attribute
	 *
	 * @id the id to match
	 * @returns the element if found, null if not
	 */
	private Element findAgent(int id)
	{
		String idStr = String.format("%s", id); 
		NodeList list = this.doc.getElementsByTagName("agent");
		for (int i=0; i<list.getLength(); i++)
		{
			// Get element
			Element element = (Element)list.item(i);
						
			if(idStr.equals(element.getAttribute("id")))
			{
				return element;
			}
		}
		
		return null;
	}
}

