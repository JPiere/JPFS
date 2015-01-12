/******************************************************************************
 * Product: JPiere(ジェイピエール) - JPiere Fragments                         *
 * Copyright (C) Hideaki Hagiwara All Rights Reserved.                        *
 * このプログラムはGNU Gneral Public Licens Version2のもと公開しています。    *
 * このプログラムは自由に活用してもらう事を期待して公開していますが、         *
 * いかなる保証もしていません。                                               *
 * 著作権は萩原秀明(h.hagiwara@oss-erp.co.jp)が保持し、サポートサービスは     *
 * 株式会社オープンソース・イーアールピー・ソリューションズで                 *
 * 提供しています。サポートをご希望の際には、                                 *
 * 株式会社オープンソース・イーアールピー・ソリューションズまでご連絡下さい。 *
 * http://www.oss-erp.co.jp/                                                  *
 *****************************************************************************/

package org.compiere.print;

import static org.compiere.model.SystemIDs.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.print.PrinterJob;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;

import javax.print.DocFlavor;
import javax.print.StreamPrintService;
import javax.print.StreamPrintServiceFactory;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
import javax.print.event.PrintServiceAttributeEvent;
import javax.print.event.PrintServiceAttributeListener;
import javax.xml.transform.stream.StreamResult;

import org.adempiere.pdf.Document;
import org.adempiere.print.export.PrintDataExcelExporter;
import org.apache.ecs.XhtmlDocument;
import org.apache.ecs.xhtml.a;
import org.apache.ecs.xhtml.link;
import org.apache.ecs.xhtml.script;
import org.apache.ecs.xhtml.table;
import org.apache.ecs.xhtml.tbody;
import org.apache.ecs.xhtml.td;
import org.apache.ecs.xhtml.th;
import org.apache.ecs.xhtml.thead;
import org.apache.ecs.xhtml.tr;
import org.compiere.model.MClient;
import org.compiere.model.MColumn;
import org.compiere.model.MDunningRunEntry;
import org.compiere.model.MInOut;
import org.compiere.model.MInvoice;
import org.compiere.model.MLocation;				//JPIERE-3 Import MLocation to ReportEngine
import org.compiere.model.MOrder;
import org.compiere.model.MPaySelectionCheck;
import org.compiere.model.MProject;
import org.compiere.model.MQuery;
import org.compiere.model.MRfQResponse;
import org.compiere.model.PrintInfo;
import org.compiere.print.layout.LayoutEngine;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ServerProcessCtl;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.FragmentDisplayType;		//JPIERE-3 Import FragmentDisplayType to ReportEngine
import org.compiere.util.Ini;
import org.compiere.util.KeyNamePair;				//JPIERE-3 Import KeyNamePair to ReportEngine
import org.compiere.util.Language;
import org.compiere.util.NamePair;					//JPIERE-3 Import NamePair to ReportEngine
import org.compiere.util.Trx;
import org.compiere.util.Util;
import org.eevolution.model.MDDOrder;
import org.eevolution.model.X_PP_Order;

/**
 *	Report Engine.
 *  For a given PrintFormat,
 *  create a Report
 *  <p>
 *  Change log:
 *  <ul>
 *  <li>2007-02-12 - teo_sarca - [ 1658127 ] Select charset encoding on import
 *  <li>2007-02-10 - teo_sarca - [ 1652660 ] Save XML,HTML,CSV should have utf8 charset
 *  <li>2009-02-06 - globalqss - [ 2574162 ] Priority to choose invoice print format not working
 *  <li>2009-07-10 - trifonnt - [ 2819637 ] Wrong print format on non completed order
 *  </ul>
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: ReportEngine.java,v 1.4 2006/10/08 06:52:51 comdivision Exp $
 *
 * @author Teo Sarca, www.arhipac.ro
 * 			<li>BF [ 2828300 ] Error when printformat table differs from DOC_TABLES
 * 				https://sourceforge.net/tracker/?func=detail&aid=2828300&group_id=176962&atid=879332
 * 			<li>BF [ 2828886 ] Problem with reports from temporary tables
 * 				https://sourceforge.net/tracker/?func=detail&atid=879332&aid=2828886&group_id=176962
 *
 *  FR 2872010 - Dunning Run for a complete Dunning (not just level) - Developer: Carlos Ruiz - globalqss - Sponsor: Metas
 */
public class ReportEngine implements PrintServiceAttributeListener
{
	/**
	 *	Constructor
	 * 	@param ctx context
	 *  @param pf Print Format
	 *  @param query Optional Query
	 *  @param info print info
	 */
	public ReportEngine (Properties ctx, MPrintFormat pf, MQuery query, PrintInfo info)
	{
		this(ctx, pf, query, info, null);
	}	//	ReportEngine

	/**
	 *	Constructor
	 * 	@param ctx context
	 *  @param pf Print Format
	 *  @param query Optional Query
	 *  @param info print info
	 *  @param trxName
	 */
	public ReportEngine (Properties ctx, MPrintFormat pf, MQuery query, PrintInfo info, String trxName)
	{
		if (pf == null)
			throw new IllegalArgumentException("ReportEngine - no PrintFormat");
		if (log.isLoggable(Level.INFO)) log.info(pf + " -- " + query);
		m_ctx = ctx;
		//
		m_printFormat = pf;
		m_info = info;
		m_trxName = trxName;
		setQuery(query);		//	loads Data

	}	//	ReportEngine

	/**	Static Logger	*/
	private static CLogger	log	= CLogger.getCLogger (ReportEngine.class);

	/**	Context					*/
	private Properties		m_ctx;

	/**	Print Format			*/
	private MPrintFormat	m_printFormat;
	/** Print Info				*/
	private PrintInfo		m_info;
	/**	Query					*/
	private MQuery			m_query;
	/**	Query Data				*/
	private PrintData		m_printData;
	/** Layout					*/
	private LayoutEngine 	m_layout = null;
	/**	Printer					*/
	private String			m_printerName = Ini.getProperty(Ini.P_PRINTER);
	/** Transaction Name 		*/
	private String 			m_trxName = null;
	/** Where filter */
	private String 			m_whereExtended = null;
	/** Window */
	private int m_windowNo = 0;

	private boolean m_summary = false;

	private List<IReportEngineEventListener> eventListeners = new ArrayList<IReportEngineEventListener>();

	public void addEventListener(IReportEngineEventListener listener)
	{
		eventListeners.add(listener);
	}

	public boolean removeEventListener(IReportEngineEventListener listener)
	{
		return eventListeners.remove(listener);
	}

	/**
	 * 	Set PrintFormat.
	 *  If Layout was created, re-create layout
	 * 	@param pf print format
	 */
	public void setPrintFormat (MPrintFormat pf)
	{
		m_printFormat = pf;
		pf.reloadItems();
		if (m_layout != null)
		{
			setPrintData();
			m_layout.setPrintFormat(pf, false);
			m_layout.setPrintData(m_printData, m_query, true);	//	format changes data
		}

		IReportEngineEventListener[] listeners = eventListeners.toArray(new IReportEngineEventListener[0]);
		for(IReportEngineEventListener listener : listeners)
		{
			listener.onPrintFormatChanged(new ReportEngineEvent(this));
		}
	}	//	setPrintFormat

	/**
	 * 	Set Query and generate PrintData.
	 *  If Layout was created, re-create layout
	 * 	@param query query
	 */
	public void setQuery (MQuery query)
	{
		m_query = query;
		if (query == null)
			return;
		//
		setPrintData();
		if (m_layout != null)
			m_layout.setPrintData(m_printData, m_query, true);

		IReportEngineEventListener[] listeners = eventListeners.toArray(new IReportEngineEventListener[0]);
		for(IReportEngineEventListener listener : listeners)
		{
			listener.onQueryChanged(new ReportEngineEvent(this));
		}
	}	//	setQuery

	/**
	 * 	Get Query
	 * 	@return query
	 */
	public MQuery getQuery()
	{
		return m_query;
	}	//	getQuery

	/**
	 * 	Set PrintData for Format restricted by Query.
	 * 	Nothing set if there is no query
	 *  Sets m_printData
	 */
	private void setPrintData()
	{
		if (m_query == null)
			return;

		DataEngine de = new DataEngine(m_printFormat.getLanguage(),m_trxName);
		setPrintData(de.getPrintData (m_ctx, m_printFormat, m_query, m_summary));
	//	m_printData.dump();
	}	//	setPrintData


	/**
	 * 	Get PrintData
	 * 	@return print data
	 */
	public PrintData getPrintData()
	{
		return m_printData;
	}	//	getPrintData

	/**
	 * 	Set PrintData
	 * 	@param printData printData
	 */
	public void setPrintData (PrintData printData)
	{
		if (printData == null)
			return;
		m_printData = printData;
	}	//	setPrintData


	/**************************************************************************
	 * 	Layout
	 */
	private void layout()
	{
		if (m_printFormat == null)
			throw new IllegalStateException ("No print format");
		if (m_printData == null)
			throw new IllegalStateException ("No print data (Delete Print Format and restart)");
		m_layout = new LayoutEngine (m_printFormat, m_printData, m_query, m_info, m_trxName);
	}	//	layout

	/**
	 * 	Get Layout
	 *  @return Layout
	 */
	public LayoutEngine getLayout()
	{
		if (m_layout == null)
			layout();
		return m_layout;
	}	//	getLayout

	/**
	 * 	Get PrintFormat (Report) Name
	 * 	@return name
	 */
	public String getName()
	{
		return m_printFormat.getName();
	}	//	getName

	/**
	 * 	Get PrintFormat
	 * 	@return print format
	 */
	public MPrintFormat getPrintFormat()
	{
		return m_printFormat;
	}	//	getPrintFormat

	/**
	 * 	Get Print Info
	 *	@return info
	 */
	public PrintInfo getPrintInfo()
	{
		return m_info;
	}	//	getPrintInfo

	/**
	 * 	Get PrintLayout (Report) Context
	 * 	@return context
	 */
	public Properties getCtx()
	{
		return getLayout().getCtx();
	}	//	getCtx

	/**
	 * 	Get Row Count
	 * 	@return row count
	 */
	public int getRowCount()
	{
		return m_printData.getRowCount();
	}	//	getRowCount

	/**
	 * 	Get Column Count
	 * 	@return column count
	 */
	public int getColumnCount()
	{
		if (m_layout != null)
			return m_layout.getColumnCount();
		return 0;
	}	//	getColumnCount


	/**************************************************************************
	 * 	Print Report
	 */
	public void print ()
	{
		if (log.isLoggable(Level.INFO)) log.info(m_info.toString());
		if (m_layout == null)
			layout();

		//	Paper Attributes: 	media-printable-area, orientation-requested, media
		PrintRequestAttributeSet prats = m_layout.getPaper().getPrintRequestAttributeSet();
		//	add:				copies, job-name, priority
		if (m_info.isDocumentCopy() || m_info.getCopies() < 1)
			prats.add (new Copies(1));
		else
			prats.add (new Copies(m_info.getCopies()));
		Locale locale = Language.getLoginLanguage().getLocale();
		prats.add(new JobName(m_printFormat.getName(), locale));
		prats.add(PrintUtil.getJobPriority(m_layout.getNumberOfPages(), m_info.getCopies(), true));

		try
		{
			//	PrinterJob
			PrinterJob job = getPrinterJob(m_info.getPrinterName());
		//	job.getPrintService().addPrintServiceAttributeListener(this);
			job.setPageable(m_layout.getPageable(false));	//	no copy
		//	Dialog
			try
			{
				if (m_info.isWithDialog() && !job.printDialog(prats))
					return;
			}
			catch (Exception e)
			{
				log.log(Level.WARNING, "Operating System Print Issue, check & try again", e);
				return;
			}

		//	submit
			boolean printCopy = m_info.isDocumentCopy() && m_info.getCopies() > 1;
			ArchiveEngine.get().archive(m_layout, m_info);
			PrintUtil.print(job, prats, false, printCopy);

			//	Document: Print Copies
			if (printCopy)
			{
				if (log.isLoggable(Level.INFO)) log.info("Copy " + (m_info.getCopies()-1));
				prats.add(new Copies(m_info.getCopies()-1));
				job = getPrinterJob(m_info.getPrinterName());
			//	job.getPrintService().addPrintServiceAttributeListener(this);
				job.setPageable (m_layout.getPageable(true));		//	Copy
				PrintUtil.print(job, prats, false, false);
			}
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "", e);
		}
	}	//	print

	/**
	 * 	Print Service Attribute Listener.
	 * 	@param psae event
	 */
	public void attributeUpdate(PrintServiceAttributeEvent psae)
	{
		/**
PrintEvent on Win32 Printer : \\MAIN\HP LaserJet 5L
PrintServiceAttributeSet - length=2
queued-job-count = 0  (class javax.print.attribute.standard.QueuedJobCount)
printer-is-accepting-jobs = accepting-jobs  (class javax.print.attribute.standard.PrinterIsAcceptingJobs)
PrintEvent on Win32 Printer : \\MAIN\HP LaserJet 5L
PrintServiceAttributeSet - length=1
queued-job-count = 1  (class javax.print.attribute.standard.QueuedJobCount)
PrintEvent on Win32 Printer : \\MAIN\HP LaserJet 5L
PrintServiceAttributeSet - length=1
queued-job-count = 0  (class javax.print.attribute.standard.QueuedJobCount)
		**/
		if (log.isLoggable(Level.FINE)) log.fine("attributeUpdate - " + psae);
	//	PrintUtil.dump (psae.getAttributes());
	}	//	attributeUpdate


	/**
	 * 	Get PrinterJob based on PrinterName
	 * 	@param printerName optional Printer Name
	 * 	@return PrinterJob
	 */
	private PrinterJob getPrinterJob (String printerName)
	{
		if (printerName != null && printerName.length() > 0)
			return PrintUtil.getPrinterJob(printerName);
		return PrintUtil.getPrinterJob(m_printerName);
	}	//	getPrinterJob

	/**
	 * 	Show Dialog and Set Paper
	 *  Optionally re-calculate layout
	 */
	public void pageSetupDialog ()
	{
		if (m_layout == null)
			layout();
		m_layout.pageSetupDialog(getPrinterJob(m_printerName));

		IReportEngineEventListener[] listeners = eventListeners.toArray(new IReportEngineEventListener[0]);
		for(IReportEngineEventListener listener : listeners)
		{
			listener.onPageSetupChanged(new ReportEngineEvent(this));
		}
	}	//	pageSetupDialog

	/**
	 * 	Set Printer (name)
	 * 	@param printerName valid printer name
	 */
	public void setPrinterName(String printerName)
	{
		if (printerName == null)
			m_printerName = Ini.getProperty(Ini.P_PRINTER);
		else
			m_printerName = printerName;
	}	//	setPrinterName

	/**
	 * 	Get Printer (name)
	 * 	@return printer name
	 */
	public String getPrinterName()
	{
		return m_printerName;
	}	//	getPrinterName

	/**************************************************************************
	 * 	Create HTML File
	 * 	@param file file
	 *  @param onlyTable if false create complete HTML document
	 *  @param language optional language - if null the default language is used to format nubers/dates
	 * 	@return true if success
	 */
	public boolean createHTML (File file, boolean onlyTable, Language language)
	{
		return createHTML(file, onlyTable, language, null);
	}

	/**************************************************************************
	 * 	Create HTML File
	 * 	@param file file
	 *  @param onlyTable if false create complete HTML document
	 *  @param language optional language - if null the default language is used to format nubers/dates
	 *  @param extension optional extension for html output
	 * 	@return true if success
	 */
	public boolean createHTML (File file, boolean onlyTable, Language language, IHTMLExtension extension)
	{
		try
		{
			Language lang = language;
			if (lang == null)
				lang = Language.getLoginLanguage();
			Writer fw = new OutputStreamWriter(new FileOutputStream(file, false), Ini.getCharset()); // teo_sarca: save using adempiere charset [ 1658127 ]
			return createHTML (new BufferedWriter(fw), onlyTable, lang, extension);
		}
		catch (FileNotFoundException fnfe)
		{
			log.log(Level.SEVERE, "(f) - " + fnfe.toString());
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "(f)", e);
		}
		return false;
	}	//	createHTML

	/**
	 * 	Write HTML to writer
	 * 	@param writer writer
	 *  @param onlyTable if false create complete HTML document
	 *  @param language optional language - if null nubers/dates are not formatted
	 * 	@return true if success
	 */
	public boolean createHTML (Writer writer, boolean onlyTable, Language language)
	{
		return createHTML(writer, onlyTable, language, null);
	}

	/**
	 * 	Write HTML to writer
	 * 	@param writer writer
	 *  @param onlyTable if false create complete HTML document
	 *  @param language optional language - if null numbers/dates are not formatted
	 *  @param extension optional extension for html output
	 * 	@return true if success
	 */
	public boolean createHTML (Writer writer, boolean onlyTable, Language language, IHTMLExtension extension)
	{
		try
		{
			String cssPrefix = extension != null ? extension.getClassPrefix() : null;
			if (cssPrefix != null && cssPrefix.trim().length() == 0)
				cssPrefix = null;

			table table = new table();
			if (cssPrefix != null)
				table.setClass(cssPrefix + "-table");
			//
			//
			table.setNeedClosingTag(false);
			PrintWriter w = new PrintWriter(writer);
			if (onlyTable)
				table.output(w);
			else
			{
				XhtmlDocument doc = new XhtmlDocument();
				doc.getHtml().setNeedClosingTag(false);
				doc.getBody().setNeedClosingTag(false);
				doc.appendBody(table);
				if (extension != null && extension.getStyleURL() != null)
				{
					link l = new link(extension.getStyleURL(), "stylesheet", "text/css");
					doc.appendHead(l);
				}
				if (extension != null && extension.getScriptURL() != null)
				{
					script jslink = new script();
					jslink.setLanguage("javascript");
					jslink.setSrc(extension.getScriptURL());
					doc.appendHead(jslink);
				}
				doc.output(w);
			}
			thead thead = new thead();
			tbody tbody = new tbody();
			//	for all rows (-1 = header row)
			for (int row = -1; row < m_printData.getRowCount(); row++)
			{
				tr tr = new tr();
				if (row != -1)
				{
					m_printData.setRowIndex(row);
					if (extension != null)
					{
						extension.extendRowElement(tr, m_printData);
					}
					if (m_printData.isFunctionRow()) {
						tr.setClass(cssPrefix + "-functionrow");
					} else if ( row < m_printData.getRowCount() && m_printData.isFunctionRow(row+1)) {
						tr.setClass(cssPrefix + "-lastgrouprow");
					}
					// add row to table body
					tbody.addElement(tr);
				} else {
					// add row to table header
					thead.addElement(tr);
				}
				//	for all columns
				for (int col = 0; col < m_printFormat.getItemCount(); col++)
				{
					MPrintFormatItem item = m_printFormat.getItem(col);
					if (item.isPrinted())
					{
						//	header row
						if (row == -1)
						{
							th th = new th();
							tr.addElement(th);
							th.addElement(Util.maskHTML(item.getPrintName(language)));
							if (cssPrefix != null)
							{
								MColumn column = MColumn.get(getCtx(), item.getAD_Column_ID());
								if (column != null && column.getAD_Column_ID() > 0)
								{
									if (DisplayType.isNumeric(column.getAD_Reference_ID()))
									{
										th.setClass(cssPrefix + "-number");
									}
								}
							}
						}
						else
						{
							td td = new td();
							tr.addElement(td);
							Object obj = m_printData.getNode(new Integer(item.getAD_Column_ID()));
							if (obj == null)
								td.addElement("&nbsp;");
							else if (obj instanceof PrintDataElement)
							{
								PrintDataElement pde = (PrintDataElement) obj;//JPIERE-3 Modify ReportEngine#createHTML by Hideaki Hagiwara
								String value = null;
								if(pde.getDisplayType()==DisplayType.Amount || pde.getDisplayType() == DisplayType.CostPrice)
								{
									value = getValueDisplay(language, getC_Currency_ID(m_printData), pde);
								}else{
									value = pde.getValueDisplay(language);
								}
								 											//JPiere-3 Finish
								if (pde.getColumnName().endsWith("_ID") && extension != null)
								{
									//link for column
									a href = new a("javascript:void(0)");
									href.setID(pde.getColumnName() + "_" + row + "_a");
									td.addElement(href);
									href.addElement(Util.maskHTML(value));
									if (cssPrefix != null)
										href.setClass(cssPrefix + "-href");

									extension.extendIDColumn(row, td, href, pde);

								}
								else
								{
									td.addElement(Util.maskHTML(value));
								}
								if (cssPrefix != null)
								{
									if (DisplayType.isNumeric(pde.getDisplayType()))
										td.setClass(cssPrefix + "-number");
									else if (DisplayType.isDate(pde.getDisplayType()))
										td.setClass(cssPrefix + "-date");
									else
										td.setClass(cssPrefix + "-text");
								}

								String style = "";
								MPrintFont mPrintFont = null;
								if (item.getAD_PrintFont_ID() > 0)
								{
									mPrintFont = MPrintFont.get(item.getAD_PrintFont_ID());
								}
								else if (m_printFormat.getAD_PrintFont_ID() > 0)
								{
									mPrintFont = MPrintFont.get(m_printFormat.getAD_PrintFont_ID());
								}
								if (mPrintFont != null && mPrintFont.getAD_PrintFont_ID() > 0)
								{
									Font font = mPrintFont.getFont();
									String fontFamily = font.getFamily();
									fontFamily = getCSSFontFamily(fontFamily);
									style = fontFamily != null ?"font-family:'"+fontFamily+"';" : "";
									if (font.isBold())
									{
										style = style + "font-weight:bold;";
									}
									if (font.isItalic())
									{
										style = style + "font-style:italic;";
									}
									int size = font.getSize();
									style=style+"font-size:"+size+"pt;";
								}

								MPrintColor mPrintColor = null;
								if (item.getAD_PrintColor_ID() > 0)
								{
									mPrintColor = MPrintColor.get(m_ctx, item.getAD_PrintColor_ID());
								}
								else if (m_printFormat.getAD_PrintColor_ID() > 0)
								{
									mPrintColor = MPrintColor.get(m_ctx, m_printFormat.getAD_PrintColor_ID());
								}
								if (mPrintColor != null && mPrintColor.getAD_PrintColor_ID() > 0)
								{
									Color color = mPrintColor.getColor();
									if (color != null)
									{
										style = style+"color:rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+");";
									}
								}

								if (style != null && style.trim().length() > 0)
									td.setStyle(style);
							}
							else if (obj instanceof PrintData)
							{
								//	ignore contained Data
							}
							else
								log.log(Level.SEVERE, "Element not PrintData(Element) " + obj.getClass());
						}
					}	//	printed
				}	//	for all columns
			}	//	for all rows

			thead.output(w);
			tbody.output(w);

			w.println();
			w.println("</table>");
			if (!onlyTable)
			{
				w.println("</body>");
				w.println("</html>");
			}
			w.flush();
			w.close();
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "(w)", e);
		}
		return false;
	}	//	createHTML


	private String getCSSFontFamily(String fontFamily) {
		if ("Dialog".equals(fontFamily) || "DialogInput".equals(fontFamily) || 	"Monospaced".equals(fontFamily))
		{
			return "monospace";
		} else if ("SansSerif".equals(fontFamily))
		{
			return "sans-serif";
		} else if ("Serif".equals(fontFamily))
		{
			return "serif";
		}
		return null;
	}

	/**************************************************************************
	 * 	Create CSV File
	 * 	@param file file
	 *  @param delimiter delimiter, e.g. comma, tab
	 *  @param language translation language
	 * 	@return true if success
	 */
	public boolean createCSV (File file, char delimiter, Language language)
	{
		try
		{
			Writer fw = new OutputStreamWriter(new FileOutputStream(file, false), Ini.getCharset()); // teo_sarca: save using adempiere charset [ 1658127 ]
			return createCSV (new BufferedWriter(fw), delimiter, language);
		}
		catch (FileNotFoundException fnfe)
		{
			log.log(Level.SEVERE, "(f) - " + fnfe.toString());
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "(f)", e);
		}
		return false;
	}	//	createCSV

	/**
	 * 	Write CSV to writer
	 * 	@param writer writer
	 *  @param delimiter delimiter, e.g. comma, tab
	 *  @param language translation language
	 * 	@return true if success
	 */
	public boolean createCSV (Writer writer, char delimiter, Language language)
	{
		if (delimiter == 0)
			delimiter = '\t';
		try
		{
			//	for all rows (-1 = header row)
			for (int row = -1; row < m_printData.getRowCount(); row++)
			{
				StringBuffer sb = new StringBuffer();
				if (row != -1)
					m_printData.setRowIndex(row);

				//	for all columns
				boolean first = true;	//	first column to print
				for (int col = 0; col < m_printFormat.getItemCount(); col++)
				{
					MPrintFormatItem item = m_printFormat.getItem(col);
					if (item.isPrinted())
					{
						//	column delimiter (comma or tab)
						if (first)
							first = false;
						else
							sb.append(delimiter);
						//	header row
						if (row == -1)
							createCSVvalue (sb, delimiter,
								m_printFormat.getItem(col).getPrintName(language));
						else
						{
							Object obj = m_printData.getNode(new Integer(item.getAD_Column_ID()));
							String data = "";
							if (obj == null)
								;
							else if (obj instanceof PrintDataElement)
							{
								PrintDataElement pde = (PrintDataElement)obj;
								if (pde.isPKey())
								{					//JPIERE-3 Modify ReportEngine#createCSV by Hideaki Hagiwara
									data = pde.getValueAsString();
								}else{
									if(pde.getDisplayType()==DisplayType.Amount || pde.getDisplayType()==DisplayType.CostPrice)
									{
										data = getValueDisplay(language, getC_Currency_ID(m_printData),pde);
									}else{
										data = pde.getValueDisplay(language);
									}
								}					//JPiere-3 Finish
							}
							else if (obj instanceof PrintData)
							{
							}
							else
								log.log(Level.SEVERE, "Element not PrintData(Element) " + obj.getClass());
							createCSVvalue (sb, delimiter, data);
						}
					}	//	printed
				}	//	for all columns
				writer.write(sb.toString());
				writer.write(Env.NL);
			}	//	for all rows
			//
			writer.flush();
			writer.close();
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "(w)", e);
		}
		return false;
	}	//	createCSV

	/**
	 * 	Add Content to CSV string.
	 *  Encapsulate/mask content in " if required
	 * 	@param sb StringBuffer to add to
	 * 	@param delimiter delimiter
	 * 	@param content column value
	 */
	private void createCSVvalue (StringBuffer sb, char delimiter, String content)
	{
		//	nothing to add
		if (content == null || content.length() == 0)
			return;
		//
		boolean needMask = false;
		StringBuffer buff = new StringBuffer();
		char chars[] = content.toCharArray();
		for (int i = 0; i < chars.length; i++)
		{
			char c = chars[i];
			if (c == '"')
			{
				needMask = true;
				buff.append(c);		//	repeat twice
			}	//	mask if any control character
			else if (!needMask && (c == delimiter || !Character.isLetterOrDigit(c)))
				needMask = true;
			buff.append(c);
		}

		//	Optionally mask value
		if (needMask)
			sb.append('"').append(buff).append('"');
		else
			sb.append(buff);
	}	//	addCSVColumnValue


	/**************************************************************************
	 * 	Create XML File
	 * 	@param file file
	 * 	@return true if success
	 */
	public boolean createXML (File file)
	{
		try
		{
			Writer fw = new OutputStreamWriter(new FileOutputStream(file, false), Ini.getCharset()); // teo_sarca: save using adempiere charset [ 1658127 ]
			return createXML (new BufferedWriter(fw));
		}
		catch (FileNotFoundException fnfe)
		{
			log.log(Level.SEVERE, "(f) - " + fnfe.toString());
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "(f)", e);
		}
		return false;
	}	//	createXML

	/**
	 * 	Write XML to writer
	 * 	@param writer writer
	 * 	@return true if success
	 */
	public boolean createXML (Writer writer)
	{
		try
		{
			m_printData.createXML(new StreamResult(writer));
			writer.flush();
			writer.close();
			return true;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "(w)", e);
		}
		return false;
	}	//	createXML


	/**************************************************************************
	 * 	Create PDF file.
	 * 	(created in temporary storage)
	 *	@return PDF file
	 */
	public File getPDF()
	{
		return getPDF (null);
	}	//	getPDF

	/**
	 * 	Create PDF file.
	 * 	@param file file
	 *	@return PDF file
	 */
	public File getPDF (File file)
	{
		try
		{
			if (file == null)
				file = File.createTempFile ("ReportEngine", ".pdf");
		}
		catch (IOException e)
		{
			log.log(Level.SEVERE, "", e);
		}
		if (createPDF (file))
			return file;
		return null;
	}	//	getPDF

	/**
	 * 	Create PDF File
	 * 	@param file file
	 * 	@return true if success
	 */
	public boolean createPDF (File file)
	{
		String fileName = null;
		URI uri = null;

		try
		{
			if (file == null)
				file = File.createTempFile ("ReportEngine", ".pdf");
			fileName = file.getAbsolutePath();
			uri = file.toURI();
			if (file.exists())
				file.delete();

		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "file", e);
			return false;
		}

		if (log.isLoggable(Level.FINE)) log.fine(uri.toString());

		try
		{
			if (m_printFormat != null && m_printFormat.getJasperProcess_ID() > 0) {
				ProcessInfo pi = new ProcessInfo ("", m_printFormat.getJasperProcess_ID(), m_printFormat.getAD_Table_ID(), m_info.getRecord_ID());
				pi.setIsBatch(true);
				pi.setPDFFileName(fileName);
				ServerProcessCtl.process(pi, (m_trxName == null ? null : Trx.get(m_trxName, false)));
			} else {
				if (m_layout == null)
					layout ();
				Document.getPDFAsFile(fileName, m_layout.getPageable(false));
				ArchiveEngine.get().archive(new File(fileName), m_info);
			}
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "PDF", e);
			return false;
		}

		File file2 = new File(fileName);
		if (log.isLoggable(Level.INFO)) log.info(file2.getAbsolutePath() + " - " + file2.length());
		return file2.exists();
	}	//	createPDF

	/**
	 * 	Create PDF as Data array
	 *	@return pdf data
	 */
	public byte[] createPDFData ()
	{
		try
		{
			if (m_layout == null)
				layout ();
			return Document.getPDFAsArray(m_layout.getPageable(false));
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "PDF", e);
		}
		return null;
	}	//	createPDFData

	/**************************************************************************
	 * 	Create PostScript File
	 * 	@param file file
	 * 	@return true if success
	 */
	public boolean createPS (File file)
	{
		try
		{
			return createPS (new FileOutputStream(file));
		}
		catch (FileNotFoundException fnfe)
		{
			log.log(Level.SEVERE, "(f) - " + fnfe.toString());
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "(f)", e);
		}
		return false;
	}	//	createPS

	/**
	 * 	Write PostScript to writer
	 * 	@param os output stream
	 * 	@return true if success
	 */
	public boolean createPS (OutputStream os)
	{
		try
		{
			String outputMimeType = DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType();
			DocFlavor docFlavor = DocFlavor.SERVICE_FORMATTED.PAGEABLE;
			StreamPrintServiceFactory[] spsfactories =
				StreamPrintServiceFactory.lookupStreamPrintServiceFactories(docFlavor, outputMimeType);
			if (spsfactories.length == 0)
			{
				log.log(Level.SEVERE, "(fos) - No StreamPrintService");
				return false;
			}
			//	just use first one - sun.print.PSStreamPrinterFactory
			//	System.out.println("- " + spsfactories[0]);
			StreamPrintService sps = spsfactories[0].getPrintService(os);
			//	get format
			if (m_layout == null)
				layout();
			//	print it
			sps.createPrintJob().print(m_layout.getPageable(false),
				new HashPrintRequestAttributeSet());
			//
			os.flush();
			//following 2 line for backward compatibility
			if (os instanceof FileOutputStream)
				((FileOutputStream)os).close();
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "(fos)", e);
		}
		return false;
	}	//	createPS

	/**
	 * Create Excel file
	 * @param outFile output file
	 * @param language
	 * @throws Exception if error
	 */
	public void createXLS(File outFile, Language language)
	throws Exception
	{
		PrintDataExcelExporter exp = new PrintDataExcelExporter(getPrintData(), getPrintFormat());
		exp.export(outFile, language);
	}

	/**************************************************************************
	 * 	Get Report Engine for process info
	 *	@param ctx context
	 *	@param pi process info with AD_PInstance_ID
	 *	@return report engine or null
	 */
	static public ReportEngine get (Properties ctx, ProcessInfo pi)
	{
		int AD_Client_ID = pi.getAD_Client_ID();
		//
		int AD_Table_ID = 0;
		int AD_ReportView_ID = 0;
		String TableName = null;
		int AD_PrintFormat_ID = 0;
		boolean IsForm = false;
		int Client_ID = -1;

		//	Get AD_Table_ID and TableName
		StringBuilder sql = new StringBuilder("SELECT rv.AD_ReportView_ID,rv.WhereClause,")
			.append(" t.AD_Table_ID,t.TableName, pf.AD_PrintFormat_ID, pf.IsForm, pf.AD_Client_ID ")
			.append("FROM AD_PInstance pi")
			.append(" INNER JOIN AD_Process p ON (pi.AD_Process_ID=p.AD_Process_ID)")
			.append(" INNER JOIN AD_ReportView rv ON (p.AD_ReportView_ID=rv.AD_ReportView_ID)")
			.append(" INNER JOIN AD_Table t ON (rv.AD_Table_ID=t.AD_Table_ID)")
			.append(" LEFT OUTER JOIN AD_PrintFormat pf ON (p.AD_ReportView_ID=pf.AD_ReportView_ID AND pf.AD_Client_ID IN (0,?) AND pf.IsActive='Y') ")
			.append("WHERE pi.AD_PInstance_ID=? ")		//	#2
			.append("ORDER BY pf.AD_Client_ID DESC, pf.IsDefault DESC");	//	own first
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setInt(2, pi.getAD_PInstance_ID());
			rs = pstmt.executeQuery();
			//	Just get first
			if (rs.next())
			{
				AD_ReportView_ID = rs.getInt(1);		//	required
				//
				AD_Table_ID = rs.getInt(3);
				TableName = rs.getString(4);			//	required for query
				AD_PrintFormat_ID = rs.getInt(5);		//	required
				IsForm = "Y".equals(rs.getString(6));	//	required
				Client_ID = rs.getInt(7);
			}
		}
		catch (SQLException e1)
		{
			log.log(Level.SEVERE, "(1) - " + sql, e1);
		}
		finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		//	Nothing found
		if (AD_ReportView_ID == 0)
		{
			//	Check Print format in Report Directly
			sql = new StringBuilder("SELECT t.AD_Table_ID,t.TableName, pf.AD_PrintFormat_ID, pf.IsForm ")
				.append("FROM AD_PInstance pi")
				.append(" INNER JOIN AD_Process p ON (pi.AD_Process_ID=p.AD_Process_ID)")
				.append(" INNER JOIN AD_PrintFormat pf ON (p.AD_PrintFormat_ID=pf.AD_PrintFormat_ID)")
				.append(" INNER JOIN AD_Table t ON (pf.AD_Table_ID=t.AD_Table_ID) ")
				.append("WHERE pi.AD_PInstance_ID=?");
			try
			{
				pstmt = DB.prepareStatement(sql.toString(), null);
				pstmt.setInt(1, pi.getAD_PInstance_ID());
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					AD_Table_ID = rs.getInt(1);
					TableName = rs.getString(2);			//	required for query
					AD_PrintFormat_ID = rs.getInt(3);		//	required
					IsForm = "Y".equals(rs.getString(4));	//	required
					Client_ID = AD_Client_ID;
				}
			}
			catch (SQLException e1)
			{
				log.log(Level.SEVERE, "(2) - " + sql, e1);
			}
			finally {
				DB.close(rs, pstmt);
				rs = null; pstmt = null;
			}
			if (AD_PrintFormat_ID == 0)
			{
				log.log(Level.SEVERE, "Report Info NOT found AD_PInstance_ID=" + pi.getAD_PInstance_ID()
					+ ",AD_Client_ID=" + AD_Client_ID);
				return null;
			}
		}

		//  Create Query from Parameters
		MQuery query = null;
		if (IsForm && pi.getRecord_ID() != 0		//	Form = one record
				&& !TableName.startsWith("T_") )	//	Not temporary table - teo_sarca, BF [ 2828886 ]
		{
			query = MQuery.getEqualQuery(TableName + "_ID", pi.getRecord_ID());
		}
		else
		{
			query = MQuery.get (ctx, pi.getAD_PInstance_ID(), TableName);
		}

		//	Get Print Format
		MPrintFormat format = null;
		Object so = pi.getSerializableObject();
		if (so instanceof MPrintFormat)
			format = (MPrintFormat)so;
		if (format == null && AD_PrintFormat_ID != 0)
		{
			//	We have a PrintFormat with the correct Client
			if (Client_ID == AD_Client_ID)
				format = MPrintFormat.get (ctx, AD_PrintFormat_ID, false);
			else
				format = MPrintFormat.copyToClient (ctx, AD_PrintFormat_ID, AD_Client_ID);
		}
		if (format != null && format.getItemCount() == 0)
		{
			if (log.isLoggable(Level.INFO)) log.info("No Items - recreating:  " + format);
			format.delete(true);
			format = null;
		}
		//	Create Format
		if (format == null && AD_ReportView_ID != 0)
			format = MPrintFormat.createFromReportView(ctx, AD_ReportView_ID, pi.getTitle());
		if (format == null)
			return null;
		//
		format.setTranslationLanguage(format.getLanguage());
		//
		PrintInfo info = new PrintInfo (pi);
		info.setAD_Table_ID(AD_Table_ID);

		return new ReportEngine(ctx, format, query, info, pi.getTransactionName());
	}	//	get

	/*************************************************************************/

	/** Order = 0				*/
	public static final int		ORDER = 0;
	/** Shipment = 1				*/
	public static final int		SHIPMENT = 1;
	/** Invoice = 2				*/
	public static final int		INVOICE = 2;
	/** Project = 3				*/
	public static final int		PROJECT = 3;
	/** RfQ = 4					*/
	public static final int		RFQ = 4;
	/** Remittance = 5			*/
	public static final int		REMITTANCE = 5;
	/** Check = 6				*/
	public static final int		CHECK = 6;
	/** Dunning = 7				*/
	public static final int		DUNNING = 7;
	/** Manufacturing Order = 8  */
	public static final int		MANUFACTURING_ORDER = 8;
	/** Distribution Order = 9  */
	public static final int		DISTRIBUTION_ORDER = 9;


//	private static final String[]	DOC_TABLES = new String[] {
//		"C_Order_Header_v", "M_InOut_Header_v", "C_Invoice_Header_v", "C_Project_Header_v",
//		"C_RfQResponse_v",
//		"C_PaySelection_Check_v", "C_PaySelection_Check_v",
//		"C_DunningRunEntry_v","PP_Order_Header_v","DD_Order_Header_v" };
	private static final String[]	DOC_BASETABLES = new String[] {
		"C_Order", "M_InOut", "C_Invoice", "C_Project",
		"C_RfQResponse",
		"C_PaySelectionCheck", "C_PaySelectionCheck",
		"C_DunningRunEntry","PP_Order", "DD_Order"};
	private static final String[]	DOC_IDS = new String[] {
		"C_Order_ID", "M_InOut_ID", "C_Invoice_ID", "C_Project_ID",
		"C_RfQResponse_ID",
		"C_PaySelectionCheck_ID", "C_PaySelectionCheck_ID",
		"C_DunningRunEntry_ID" , "PP_Order_ID" , "DD_Order_ID" };
	private static final int[]	DOC_TABLE_ID = new int[] {
		MOrder.Table_ID, MInOut.Table_ID, MInvoice.Table_ID, MProject.Table_ID,
		MRfQResponse.Table_ID,
		MPaySelectionCheck.Table_ID, MPaySelectionCheck.Table_ID,
		MDunningRunEntry.Table_ID, X_PP_Order.Table_ID, MDDOrder.Table_ID };

	/**************************************************************************
	 * 	Get Document Print Engine for Document Type.
	 * 	@param ctx context
	 * 	@param type document type
	 * 	@param Record_ID id
	 * 	@return Report Engine or null
	 */
	public static ReportEngine get (Properties ctx, int type, int Record_ID)
	{
		return get(ctx, type, Record_ID, null);
	}

	/**************************************************************************
	 * 	Get Document Print Engine for Document Type.
	 * 	@param ctx context
	 * 	@param type document type
	 * 	@param Record_ID id
	 *  @param trxName
	 * 	@return Report Engine or null
	 */
	public static ReportEngine get (Properties ctx, int type, int Record_ID, String trxName)
	{
		if (Record_ID < 1)
		{
			log.log(Level.WARNING, "No PrintFormat for Record_ID=" + Record_ID
					+ ", Type=" + type);
			return null;
		}
		//	Order - Print Shipment or Invoice
		if (type == ORDER)
		{
			int[] what = getDocumentWhat (Record_ID);
			if (what != null)
			{
				type = what[0];
				Record_ID = what[1];
			}
		}	//	Order

		int AD_PrintFormat_ID = 0;
		int C_BPartner_ID = 0;
		String DocumentNo = null;
		int copies = 1;

		//	Language
		MClient client = MClient.get(ctx);
		Language language = client.getLanguage();
		//	Get Document Info
		StringBuilder sql = null;
		if (type == CHECK)
			sql = new StringBuilder("SELECT bad.Check_PrintFormat_ID,")								//	1
				.append("	c.IsMultiLingualDocument,bp.AD_Language,bp.C_BPartner_ID,d.DocumentNo ")		//	2..5
				.append("FROM C_PaySelectionCheck d")
				.append(" INNER JOIN C_PaySelection ps ON (d.C_PaySelection_ID=ps.C_PaySelection_ID)")
				.append(" INNER JOIN C_BankAccountDoc bad ON (ps.C_BankAccount_ID=bad.C_BankAccount_ID AND d.PaymentRule=bad.PaymentRule)")
				.append(" INNER JOIN AD_Client c ON (d.AD_Client_ID=c.AD_Client_ID)")
				.append(" INNER JOIN C_BPartner bp ON (d.C_BPartner_ID=bp.C_BPartner_ID) ")
				.append("WHERE d.C_PaySelectionCheck_ID=?");		//	info from BankAccount
		else if (type == DUNNING)
			sql = new StringBuilder("SELECT dl.Dunning_PrintFormat_ID,")
				.append(" c.IsMultiLingualDocument,bp.AD_Language,bp.C_BPartner_ID,dr.DunningDate ")
				.append("FROM C_DunningRunEntry d")
				.append(" INNER JOIN AD_Client c ON (d.AD_Client_ID=c.AD_Client_ID)")
				.append(" INNER JOIN C_BPartner bp ON (d.C_BPartner_ID=bp.C_BPartner_ID)")
				.append(" INNER JOIN C_DunningRun dr ON (d.C_DunningRun_ID=dr.C_DunningRun_ID)")
				.append(" INNER JOIN C_DunningLevel dl ON (dl.C_DunningLevel_ID=d.C_DunningLevel_ID) ")
				.append("WHERE d.C_DunningRunEntry_ID=?");			//	info from Dunning
		else if (type == REMITTANCE)
			sql = new StringBuilder("SELECT pf.Remittance_PrintFormat_ID,")
				.append(" c.IsMultiLingualDocument,bp.AD_Language,bp.C_BPartner_ID,d.DocumentNo ")
				.append("FROM C_PaySelectionCheck d")
				.append(" INNER JOIN AD_Client c ON (d.AD_Client_ID=c.AD_Client_ID)")
				.append(" INNER JOIN AD_PrintForm pf ON (c.AD_Client_ID=pf.AD_Client_ID)")
				.append(" INNER JOIN C_BPartner bp ON (d.C_BPartner_ID=bp.C_BPartner_ID) ")
				.append("WHERE d.C_PaySelectionCheck_ID=?")		//	info from PrintForm
				.append(" AND pf.AD_Org_ID IN (0,d.AD_Org_ID) ORDER BY pf.AD_Org_ID DESC");
		else if (type == PROJECT)
			sql = new StringBuilder("SELECT pf.Project_PrintFormat_ID,")
				.append(" c.IsMultiLingualDocument,bp.AD_Language,bp.C_BPartner_ID,d.Value ")
				.append("FROM C_Project d")
				.append(" INNER JOIN AD_Client c ON (d.AD_Client_ID=c.AD_Client_ID)")
				.append(" INNER JOIN AD_PrintForm pf ON (c.AD_Client_ID=pf.AD_Client_ID)")
				.append(" LEFT OUTER JOIN C_BPartner bp ON (d.C_BPartner_ID=bp.C_BPartner_ID) ")
				.append("WHERE d.C_Project_ID=?")					//	info from PrintForm
				.append(" AND pf.AD_Org_ID IN (0,d.AD_Org_ID) ORDER BY pf.AD_Org_ID DESC");
		else if (type == MANUFACTURING_ORDER)
			sql = new StringBuilder("SELECT pf.Manuf_Order_PrintFormat_ID,")
				.append(" c.IsMultiLingualDocument,bp.AD_Language, 0 , d.DocumentNo ")
				.append("FROM PP_Order d")
				.append(" INNER JOIN AD_Client c ON (d.AD_Client_ID=c.AD_Client_ID)")
				.append(" LEFT OUTER JOIN AD_User u ON (u.AD_User_ID=d.Planner_ID)")
				.append(" LEFT OUTER JOIN C_BPartner bp ON (u.C_BPartner_ID=bp.C_BPartner_ID) ")
				.append(" INNER JOIN AD_PrintForm pf ON (c.AD_Client_ID=pf.AD_Client_ID)")
				.append("WHERE d.PP_Order_ID=?")					//	info from PrintForm
				.append(" AND pf.AD_Org_ID IN (0,d.AD_Org_ID) ORDER BY pf.AD_Org_ID DESC");
		else if (type == DISTRIBUTION_ORDER)
			sql = new StringBuilder("SELECT pf.Distrib_Order_PrintFormat_ID,")
				.append(" c.IsMultiLingualDocument,bp.AD_Language, bp.C_BPartner_ID , d.DocumentNo ")
				.append("FROM DD_Order d")
				.append(" INNER JOIN AD_Client c ON (d.AD_Client_ID=c.AD_Client_ID)")
				.append(" INNER JOIN AD_PrintForm pf ON (c.AD_Client_ID=pf.AD_Client_ID)")
				.append(" LEFT OUTER JOIN C_BPartner bp ON (d.C_BPartner_ID=bp.C_BPartner_ID) ")
				.append("WHERE d.DD_Order_ID=?")					//	info from PrintForm
				.append(" AND pf.AD_Org_ID IN (0,d.AD_Org_ID) ORDER BY pf.AD_Org_ID DESC");
		else if (type == RFQ)
			sql = new StringBuilder("SELECT COALESCE(t.AD_PrintFormat_ID, pf.AD_PrintFormat_ID),")
				.append(" c.IsMultiLingualDocument,bp.AD_Language,bp.C_BPartner_ID,rr.Name ")
				.append("FROM C_RfQResponse rr")
				.append(" INNER JOIN C_RfQ r ON (rr.C_RfQ_ID=r.C_RfQ_ID)")
				.append(" INNER JOIN C_RfQ_Topic t ON (r.C_RfQ_Topic_ID=t.C_RfQ_Topic_ID)")
				.append(" INNER JOIN AD_Client c ON (rr.AD_Client_ID=c.AD_Client_ID)")
				.append(" INNER JOIN C_BPartner bp ON (rr.C_BPartner_ID=bp.C_BPartner_ID),")
				.append(" AD_PrintFormat pf ")
				.append("WHERE pf.AD_Client_ID IN (0,rr.AD_Client_ID)")
				.append(" AND pf.AD_Table_ID=725 AND pf.IsTableBased='N'")	//	from RfQ PrintFormat
				.append(" AND rr.C_RfQResponse_ID=? ")				//	Info from RfQTopic
				.append("ORDER BY t.AD_PrintFormat_ID, pf.AD_Client_ID DESC, pf.AD_Org_ID DESC");
		// Fix [2574162] Priority to choose invoice print format not working
		else if (type == ORDER || type == INVOICE)
			sql = new StringBuilder("SELECT pf.Order_PrintFormat_ID,pf.Shipment_PrintFormat_ID,")		//	1..2
				//	Prio: 1. BPartner 2. DocType, 3. PrintFormat (Org)	//	see InvoicePrint
				.append(" COALESCE (bp.Invoice_PrintFormat_ID,dt.AD_PrintFormat_ID,pf.Invoice_PrintFormat_ID),") // 3
				.append(" pf.Project_PrintFormat_ID, pf.Remittance_PrintFormat_ID,")		//	4..5
				.append(" c.IsMultiLingualDocument, bp.AD_Language,")						//	6..7
				.append(" COALESCE(dt.DocumentCopies,0)+COALESCE(bp.DocumentCopies,1), ") 	// 	8
				.append(" dt.AD_PrintFormat_ID,bp.C_BPartner_ID,d.DocumentNo ")			//	9..11
				.append("FROM " + DOC_BASETABLES[type] + " d")
				.append(" INNER JOIN AD_Client c ON (d.AD_Client_ID=c.AD_Client_ID)")
				.append(" INNER JOIN AD_PrintForm pf ON (c.AD_Client_ID=pf.AD_Client_ID)")
				.append(" INNER JOIN C_BPartner bp ON (d.C_BPartner_ID=bp.C_BPartner_ID)")
				.append(" LEFT OUTER JOIN C_DocType dt ON ((d.C_DocType_ID>0 AND d.C_DocType_ID=dt.C_DocType_ID) OR (d.C_DocType_ID=0 AND d.C_DocTypeTarget_ID=dt.C_DocType_ID)) ")
				.append("WHERE d." + DOC_IDS[type] + "=?")			//	info from PrintForm
				.append(" AND pf.AD_Org_ID IN (0,d.AD_Org_ID) ")
				.append("ORDER BY pf.AD_Org_ID DESC");
		else	//	Get PrintFormat from Org or 0 of document client
			sql = new StringBuilder("SELECT pf.Order_PrintFormat_ID,pf.Shipment_PrintFormat_ID,")		//	1..2
				//	Prio: 1. BPartner 2. DocType, 3. PrintFormat (Org)	//	see InvoicePrint
				.append(" COALESCE (bp.Invoice_PrintFormat_ID,dt.AD_PrintFormat_ID,pf.Invoice_PrintFormat_ID),") // 3
				.append(" pf.Project_PrintFormat_ID, pf.Remittance_PrintFormat_ID,")		//	4..5
				.append(" c.IsMultiLingualDocument, bp.AD_Language,")						//	6..7
				.append(" COALESCE(dt.DocumentCopies,0)+COALESCE(bp.DocumentCopies,1), ") 	// 	8
				.append(" dt.AD_PrintFormat_ID,bp.C_BPartner_ID,d.DocumentNo, ")			//  9..11
				.append(" pf.Manuf_Order_PrintFormat_ID, pf.Distrib_Order_PrintFormat_ID ")	//	12..13
				.append("FROM " + DOC_BASETABLES[type] + " d")
				.append(" INNER JOIN AD_Client c ON (d.AD_Client_ID=c.AD_Client_ID)")
				.append(" INNER JOIN AD_PrintForm pf ON (c.AD_Client_ID=pf.AD_Client_ID)")
				.append(" INNER JOIN C_BPartner bp ON (d.C_BPartner_ID=bp.C_BPartner_ID)")
				.append(" LEFT OUTER JOIN C_DocType dt ON (d.C_DocType_ID=dt.C_DocType_ID) ")
				.append("WHERE d." + DOC_IDS[type] + "=?")			//	info from PrintForm
				.append(" AND pf.AD_Org_ID IN (0,d.AD_Org_ID) ")
				.append("ORDER BY pf.AD_Org_ID DESC");
		//
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), trxName);
			pstmt.setInt(1, Record_ID);
			rs = pstmt.executeQuery();
			if (rs.next())	//	first record only
			{
				if (type == CHECK || type == DUNNING || type == REMITTANCE
					|| type == PROJECT || type == RFQ || type == MANUFACTURING_ORDER || type == DISTRIBUTION_ORDER)
				{
					AD_PrintFormat_ID = rs.getInt(1);
					copies = 1;
					//	Set Language when enabled
					String AD_Language = rs.getString(3);
					if (AD_Language != null)// && "Y".equals(rs.getString(2)))	//	IsMultiLingualDocument
						language = Language.getLanguage(AD_Language);
					C_BPartner_ID = rs.getInt(4);
					if (type == DUNNING)
					{
						Timestamp ts = rs.getTimestamp(5);
						DocumentNo = ts.toString();
					}
					else
						DocumentNo = rs.getString(5);
				}
				else
				{
					//	Set PrintFormat
					AD_PrintFormat_ID = rs.getInt(type+1);
					if (type != INVOICE && rs.getInt(9) != 0)		//	C_DocType.AD_PrintFormat_ID
						AD_PrintFormat_ID = rs.getInt(9);
					copies = rs.getInt(8);
					//	Set Language when enabled
					String AD_Language = rs.getString(7);
					if (AD_Language != null) // && "Y".equals(rs.getString(6)))	//	IsMultiLingualDocument
						language = Language.getLanguage(AD_Language);
					C_BPartner_ID = rs.getInt(10);
					DocumentNo = rs.getString(11);
				}
			}
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "Record_ID=" + Record_ID + ", SQL=" + sql, e);
		}
		finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		if (AD_PrintFormat_ID == 0)
		{
			log.log(Level.SEVERE, "No PrintFormat found for Type=" + type + ", Record_ID=" + Record_ID);
			return null;
		}

		//	Get Format & Data
		MPrintFormat format = MPrintFormat.get (ctx, AD_PrintFormat_ID, false);
		format.setLanguage(language);		//	BP Language if Multi-Lingual
		format.setTranslationLanguage(language);
		//	query
		MQuery query = new MQuery(format.getAD_Table_ID());
		query.addRestriction(DOC_IDS[type], MQuery.EQUAL, Record_ID);
	//	log.config( "ReportCtrl.startDocumentPrint - " + format, query + " - " + language.getAD_Language());
		//
		if (DocumentNo == null || DocumentNo.length() == 0)
			DocumentNo = "DocPrint";
		PrintInfo info = new PrintInfo(
			DocumentNo,
			DOC_TABLE_ID[type],
			Record_ID,
			C_BPartner_ID);
		info.setCopies(copies);
		info.setDocumentCopy(false);		//	true prints "Copy" on second
		info.setPrinterName(format.getPrinterName());

		//	Engine
		ReportEngine re = new ReportEngine(ctx, format, query, info, trxName);
		return re;
	}	//	get

	/**
	 *	Determine what Order document to print.
	 *  @param C_Order_ID id
	 *	@return int Array with [printWhat, ID]
	 */
	private static int[] getDocumentWhat (int C_Order_ID)
	{
		int[] what = new int[2];
		what[0] = ORDER;
		what[1] = C_Order_ID;
		//
		StringBuilder sql = new StringBuilder("SELECT dt.DocSubTypeSO ")
			.append("FROM C_DocType dt, C_Order o ")
			.append("WHERE o.C_DocType_ID=dt.C_DocType_ID")
			.append(" AND o.C_Order_ID=?");
		String DocSubTypeSO = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, C_Order_ID);
			rs = pstmt.executeQuery();
			if (rs.next())
				DocSubTypeSO = rs.getString(1);

			// @Trifon - Order is not completed(C_DoctType_ID=0) then try with C_DocTypeTarget_ID
			// [ 2819637 ] Wrong print format on non completed order - https://sourceforge.net/tracker/?func=detail&aid=2819637&group_id=176962&atid=879332
			if (DocSubTypeSO == null || "".equals(DocSubTypeSO)) {
				sql = new StringBuilder("SELECT dt.DocSubTypeSO ")
					.append("FROM C_DocType dt, C_Order o ")
					.append("WHERE o.C_DocTypeTarget_ID=dt.C_DocType_ID")
					.append(" AND o.C_Order_ID=?");
				pstmt = DB.prepareStatement(sql.toString(), null);
				pstmt.setInt(1, C_Order_ID);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					DocSubTypeSO = rs.getString(1);
				}
			}
		}
		catch (SQLException e1)
		{
			log.log(Level.SEVERE, "(1) - " + sql, e1);
			return null;		//	error
		}
		finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		if (DocSubTypeSO == null)
			DocSubTypeSO = "";
		//	WalkIn Receipt, WalkIn Invoice,
		if (DocSubTypeSO.equals("WR") || DocSubTypeSO.equals("WI"))
			what[0] = INVOICE;
		//	WalkIn Pickup,
		else if (DocSubTypeSO.equals("WP"))
			what[0] = SHIPMENT;
		//	Offer Binding, Offer Nonbinding, Standard Order
		else
			return what;

		//	Get Record_ID of Invoice/Receipt
		if (what[0] == INVOICE)
			sql = new StringBuilder("SELECT C_Invoice_ID REC FROM C_Invoice WHERE C_Order_ID=?")	//	1
				.append(" ORDER BY C_Invoice_ID DESC");
		else
			sql = new StringBuilder("SELECT M_InOut_ID REC FROM M_InOut WHERE C_Order_ID=?") 	//	1
				.append(" ORDER BY M_InOut_ID DESC");
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, C_Order_ID);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
			//	if (i == 1 && ADialog.ask(0, null, what[0] == INVOICE ? "PrintOnlyRecentInvoice?" : "PrintOnlyRecentShipment?")) break;
				what[1] = rs.getInt(1);
			}
			else	//	No Document Found
				what[0] = ORDER;
		}
		catch (SQLException e2)
		{
			log.log(Level.SEVERE, "(2) - " + sql, e2);
			return null;
		}
		finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		if (log.isLoggable(Level.FINE)) log.fine("Order => " + what[0] + " ID=" + what[1]);
		return what;
	}	//	getDocumentWhat

	/**
	 * 	Print Confirm.
	 *  Update Date Printed
	 * 	@param type document type
	 * 	@param Record_ID record id
	 */
	public static void printConfirm (int type, int Record_ID)
	{
		StringBuilder sql = new StringBuilder();
		if (type == ORDER || type == SHIPMENT || type == INVOICE)
			sql.append("UPDATE ").append(DOC_BASETABLES[type])
				.append(" SET DatePrinted=SysDate, IsPrinted='Y' WHERE ")
				.append(DOC_IDS[type]).append("=").append(Record_ID);
		//
		if (sql.length() > 0)
		{
			int no = DB.executeUpdate(sql.toString(), null);
			if (no != 1)
				log.log(Level.SEVERE, "Updated records=" + no + " - should be just one");
		}
	}	//	printConfirm


	/*************************************************************************
	 * 	Test
	 * 	@param args args
	 */
	public static void main(String[] args)
	{
		org.compiere.Adempiere.startupEnvironment(true);
		//
		int AD_Table_ID = TABLE_AD_TABLE;
		MQuery q = new MQuery("AD_Table");
		q.addRestriction("AD_Table_ID", "<", 108);
		//
		MPrintFormat f = MPrintFormat.createFromTable(Env.getCtx(), AD_Table_ID);
		PrintInfo i = new PrintInfo("test", AD_Table_ID, 108, 0);
		i.setAD_Table_ID(AD_Table_ID);
		ReportEngine re = new ReportEngine(Env.getCtx(), f, q, i);
		re.layout();
		/**
		re.createCSV(new File("C:\\Temp\\test.csv"), ',', Language.getLanguage());
		re.createHTML(new File("C:\\Temp\\test.html"), false, Language.getLanguage());
		re.createXML(new File("C:\\Temp\\test.xml"));
		re.createPS(new File ("C:\\Temp\\test.ps"));
		re.createPDF(new File("C:\\Temp\\test.pdf"));
		/****/
		re.print();
	//	re.print(true, 1, false, "Epson Stylus COLOR 900 ESC/P 2");		//	Dialog
	//	re.print(true, 1, false, "HP LaserJet 3300 Series PCL 6");		//	Dialog
	//	re.print(false, 1, false, "Epson Stylus COLOR 900 ESC/P 2");	//	Dialog
		System.exit(0);
	}	//	main

	public void setWhereExtended(String whereExtended) {
		m_whereExtended = whereExtended;
	}

	public String getWhereExtended() {
		return m_whereExtended;
	}

	/* Save windowNo of the report to parse the context */
	public void setWindowNo(int windowNo) {
		m_windowNo = windowNo;
	}

	public int getWindowNo() {
		return m_windowNo;
	}

	public void setSummary(boolean summary)
	{
		m_summary = summary;
	}


	/**
	 * Get C_Currency_ID
	 * JPIERE-3 Add ReportEngine#getC_Currency_ID()
	 * @author Hideaki Hagiwara
	 */
	private int getC_Currency_ID(PrintData printData)
	{
		int indexOfCurrency = printData.getIndex("C_Currency_ID");
		int C_Currency_ID = 0;

		if( indexOfCurrency >= 0 ) // when  currencyIndex < 0 , PrintData instance doesn't have PrintDataElement of C_Currency_ID
		{
			Object obj = printData.getNode(indexOfCurrency);
			if( obj instanceof PrintDataElement)
			{
				PrintDataElement data = (PrintDataElement)obj;
				String value = data.getValueKey();
				try
				{
					C_Currency_ID = Integer.parseInt(value);
				}catch (Exception e ){
					log.info("Value="+value.toString());
				}
			}
		}
		return C_Currency_ID;
	}//getC_Currency_ID


	/**
	 * Get Value Display
	 * JPIERE-3 Add ReportEngine#getValueDisplay()
	 * @author Hideaki Hagiwara
	 */
	private String getValueDisplay (Language language, int C_Currency_ID, PrintDataElement pde)
	{
		Object m_value = pde.getValue();
		int m_displayType = pde.getDisplayType();

		if (m_value == null)
			return "";
		String retValue = m_value.toString();
		if (m_displayType == DisplayType.Location)
			return pde.getValueDisplay(language);
		// ID columns should be printed as ID numbers - teo_sarca [ 1673363 ]
		else if (DisplayType.ID == m_displayType && m_value instanceof KeyNamePair)
			return ((KeyNamePair)m_value).getID();
		else if (pde.getColumnName().equals("C_BPartner_Location_ID") || pde.getColumnName().equals("Bill_Location_ID"))
			return getValueDisplay_BPLocation(pde);
		else if (m_displayType == 0 || m_value instanceof String || m_value instanceof NamePair)
			;
		else if (language != null)//Optional formatting of Numbers and Dates
		{
			if (DisplayType.isNumeric(m_displayType))
				retValue = FragmentDisplayType.getNumberFormat(m_displayType, language, pde.getM_formatPattern() , C_Currency_ID).format(m_value);
			else if (DisplayType.isDate(m_displayType))
				retValue = DisplayType.getDateFormat(m_displayType, language, pde.getM_formatPattern() ).format(m_value);
		}
		return retValue;
	}//getValueDisplay

	/**
	 * Get Value Display BPLocation
	 * JPIERE-3 Add ReportEngine#getValueDisplay_BPLocation()
	 *
	 * @author Hideaki Hagiwara
	 */
	private String getValueDisplay_BPLocation ( PrintDataElement pde)
	{
		try
		{
			int C_BPartner_Location_ID = Integer.parseInt (pde.getValueKey ());
			if (C_BPartner_Location_ID != 0)
			{
				MLocation loc = MLocation.getBPLocation(Env.getCtx(), C_BPartner_Location_ID, null);
				if (loc != null)
					return loc.toStringCR();
			}
		}
		catch (Exception ex)
		{
		}
		return pde.getValue().toString();
	}	//	getValueDisplay_BPLocation
}	//	ReportEngine
