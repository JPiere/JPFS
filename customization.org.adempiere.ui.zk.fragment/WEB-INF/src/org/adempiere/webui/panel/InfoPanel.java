/******************************************************************************
 * Product: Posterita Ajax UI 												  *
 * Copyright (C) 2007 Posterita Ltd.  All Rights Reserved.                    *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * Posterita Ltd., 3, Draper Avenue, Quatre Bornes, Mauritius                 *
 * or via info@posterita.org or http://www.posterita.org/                     *
 *****************************************************************************/

package org.adempiere.webui.panel;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

import org.adempiere.webui.AdempiereWebUI;
import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.apps.BusyDialog;
import org.adempiere.webui.apps.WProcessCtl;
import org.adempiere.webui.component.ConfirmPanel;
import org.adempiere.webui.component.ListModelTable;
import org.adempiere.webui.component.WListItemRenderer;
import org.adempiere.webui.component.WListbox;
import org.adempiere.webui.component.Window;
import org.adempiere.webui.editor.WEditor;
import org.adempiere.webui.event.ValueChangeEvent;
import org.adempiere.webui.event.ValueChangeListener;
import org.adempiere.webui.event.WTableModelEvent;
import org.adempiere.webui.event.WTableModelListener;
import org.adempiere.webui.factory.InfoManager;
import org.adempiere.webui.part.ITabOnSelectHandler;
import org.adempiere.webui.part.WindowContainer;
import org.adempiere.webui.session.SessionManager;
import org.adempiere.webui.window.FDialog;
import org.compiere.minigrid.ColumnInfo;
import org.compiere.minigrid.IDColumn;
import org.compiere.model.MInfoWindow;
import org.compiere.model.MPInstance;
import org.compiere.model.MProcess;
import org.compiere.model.MRole;
import org.compiere.model.MTable;
import org.compiere.model.X_AD_CtxHelp;
import org.compiere.process.ProcessInfo;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.compiere.util.Trx;
import org.compiere.util.ValueNamePair;
import org.zkoss.zk.au.out.AuEcho;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Paging;
import org.zkoss.zul.event.ZulEvents;
import org.zkoss.zul.ext.Sortable;

/**
 *	Search Information and return selection - Base Class.
 *  Based on Info written by Jorg Janke
 *
 *  @author Sendy Yagambrum
 *
 * Zk Port
 * @author Elaine
 * @version	Info.java Adempiere Swing UI 3.4.1
 *
 * @contributor red1 IDEMPIERE-1711 with final review by HengSin
 */
public abstract class InfoPanel extends Window implements EventListener<Event>, WTableModelListener, Sortable<Object>, IHelpContext
{
	/**
	 *
	 */
	private static final long serialVersionUID = 2823393272482373014L;

	private final static int PAGE_SIZE = 100;
	protected boolean hasProcess = false;
	protected Map<String, WEditor> editorMap = new HashMap<String, WEditor>();

    public static InfoPanel create (int WindowNo,
            String tableName, String keyColumn, String value,
            boolean multiSelection, String whereClause)
    {
        return InfoManager.create(WindowNo, tableName, keyColumn, value, multiSelection, whereClause, true);
    }

	/**
	 * Show panel based on tablename (non modal)
	 * @param tableName
	 */
    public static void showPanel (String tableName)
	{
		InfoPanel info = InfoManager.create(0, tableName, tableName + "_ID", "", false, "", false);
		info.setAttribute(Window.MODE_KEY, Window.MODE_EMBEDDED);
		AEnv.showWindow(info);
	}	// showPanel

	/** Window Width                */
	static final int        INFO_WIDTH = 800;
	private boolean m_lookup;

	/**************************************************
     *  Detail Constructor
     * @param WindowNo  WindowNo
     * @param tableName tableName
     * @param keyColumn keyColumn
     * @param whereClause   whereClause
	 */
	protected InfoPanel (int WindowNo,
		String tableName, String keyColumn,boolean multipleSelection,
		 String whereClause)
	{
		this(WindowNo, tableName, keyColumn, multipleSelection, whereClause, true);
	}

	/**************************************************
     *  Detail Constructor
     * @param WindowNo  WindowNo
     * @param tableName tableName
     * @param keyColumn keyColumn
     * @param whereClause   whereClause
	 */
	protected InfoPanel (int WindowNo,
		String tableName, String keyColumn,boolean multipleSelection,
		 String whereClause, boolean lookup)
	{
		if (WindowNo <= 0) {
			p_WindowNo = SessionManager.getAppDesktop().registerWindow(this);
		} else {
			p_WindowNo = WindowNo;
		}
		if (log.isLoggable(Level.INFO))
			log.info("WinNo=" + WindowNo + " " + whereClause);
		p_tableName = tableName;
		p_keyColumn = keyColumn;
        p_multipleSelection = multipleSelection;
        m_lookup = lookup;

		if (whereClause == null || whereClause.indexOf('@') == -1)
			p_whereClause = whereClause == null ? "" : whereClause;
		else
		{
			p_whereClause = Env.parseContext(Env.getCtx(), p_WindowNo, whereClause, false, false);
			if (p_whereClause.length() == 0)
				log.log(Level.SEVERE, "Cannot parse context= " + whereClause);
		}
		init();

		this.setAttribute(ITabOnSelectHandler.ATTRIBUTE_KEY, new ITabOnSelectHandler() {
			public void onSelect() {
				scrollToSelectedRow();
			}
		});

		setWidgetAttribute(AdempiereWebUI.WIDGET_INSTANCE_NAME, "infopanel");

        infoWindow = MInfoWindow.get(p_keyColumn.replace("_ID", ""), null);
		addEventListener(WindowContainer.ON_WINDOW_CONTAINER_SELECTION_CHANGED_EVENT, this);
	}	//	InfoPanel

	private void init()
	{
		if (isLookup())
		{
			setAttribute(Window.MODE_KEY, Window.MODE_HIGHLIGHTED);
			setBorder("normal");
			setClosable(true);
			int height = SessionManager.getAppDesktop().getClientInfo().desktopHeight * 85 / 100;
    		int width = SessionManager.getAppDesktop().getClientInfo().desktopWidth * 80 / 100;
    		setWidth(width + "px");
    		setHeight(height + "px");
    		this.setContentStyle("overflow: auto");
		}
		else
		{
			setAttribute(Window.MODE_KEY, Window.MODE_EMBEDDED);
			setBorder("none");
			setWidth("100%");
			setHeight("100%");
			setStyle("position: absolute");
			if (p_multipleSelection) hasProcess = true; //red1 IDEMPIERE-1711 !isLookUp() and is multipleSelection so add Process button first
		}

        confirmPanel = new ConfirmPanel(true, true, false, true, true, true, true, hasProcess);  // Elaine 2008/12/16 //red1 hasProcess checked by isLookup()
        confirmPanel.addActionListener(Events.ON_CLICK, this);
        confirmPanel.setHflex("1");

        // Elaine 2008/12/16
		confirmPanel.getButton(ConfirmPanel.A_CUSTOMIZE).setVisible(hasCustomize());
		confirmPanel.getButton(ConfirmPanel.A_HISTORY).setVisible(hasHistory());
		confirmPanel.getButton(ConfirmPanel.A_ZOOM).setVisible(hasZoom());
		//
		if (!isLookup())
		{
			confirmPanel.getButton(ConfirmPanel.A_OK).setVisible(false);
		}

        this.setSizable(true);
        this.setMaximizable(true);

        this.addEventListener(Events.ON_OK, this);

        contentPanel.setOddRowSclass(null);
//        contentPanel.setSizedByContent(true);
        contentPanel.setWidgetAttribute(AdempiereWebUI.WIDGET_INSTANCE_NAME, "infoListbox");

        this.setSclass("info-panel");
	}  //  init
	protected ConfirmPanel confirmPanel;
	/** Master (owning) Window  */
	protected int				p_WindowNo;
	/** Table Name              */
	protected String            p_tableName;
	/** Key Column Name         */
	protected String            p_keyColumn;
	/** Enable more than one selection  */
	protected boolean			p_multipleSelection;
	/** Initial WHERE Clause    */
	protected String			p_whereClause = "";
	protected StatusBarPanel statusBar = new StatusBarPanel();
	/**                    */
    private List<Object> line;

	private boolean			    m_ok = false;
	/** Cancel pressed - need to differentiate between OK - Cancel - Exit	*/
	private boolean			    m_cancel = false;
	/** Result IDs              */
	private ArrayList<Integer>	m_results = new ArrayList<Integer>(3);

    private ListModelTable model;
	/** Layout of Grid          */
	protected ColumnInfo[]     p_layout;
	/** Main SQL Statement      */
	protected String              m_sqlMain;
	/** Count SQL Statement		*/
	protected String              m_sqlCount;
	/** Order By Clause         */
	protected String              m_sqlOrder;
	protected String              m_sqlUserOrder;
	/**ValueChange listeners       */
    private ArrayList<ValueChangeListener> listeners = new ArrayList<ValueChangeListener>();
	/** Loading success indicator       */
	protected boolean	        p_loadedOK = false;
	/**	SO Zoom Window						*/
	private int					m_SO_Window_ID = -1;
	/**	PO Zoom Window						*/
	private int					m_PO_Window_ID = -1;

	private MInfoWindow infoWindow;

	/**	Logger			*/
	protected CLogger log = CLogger.getCLogger(getClass());

	protected WListbox contentPanel = new WListbox();
	protected Paging paging;
	protected int pageNo;
	protected int m_count;
	private int cacheStart;
	private int cacheEnd;
	private boolean m_useDatabasePaging = false;
	private BusyDialog progressWindow;
	private Listitem m_lastOnSelectItem;

	private static final String[] lISTENER_EVENTS = {};
	//red1 added for embedded Process feature
	protected int AD_InfoWindow_ID;
    private ProcessInfo m_pi = null;
	private MProcess m_process = null;
	
	private boolean isTestCount = false; //JPIERE-6 Add property isTestCount to InfoPanel

	/**
	 *  Loaded correctly
	 *  @return true if loaded OK
	 */
	public boolean loadedOK()
	{
		return p_loadedOK;
	}   //  loadedOK

	/**
	 *	Set Status Line
	 *  @param text text
	 *  @param error error
	 */
	public void setStatusLine (String text, boolean error)
	{
		statusBar.setStatusLine(text, error);
	}	//	setStatusLine

	/**
	 *	Set Status DB
	 *  @param text text
	 */
	public void setStatusDB (String text)
	{
		statusBar.setStatusDB(text);
	}	//	setStatusDB

	protected void prepareTable (ColumnInfo[] layout,
            String from,
            String where,
            String orderBy)
	{
        String sql =contentPanel.prepareTable(layout, from,
                where,p_multipleSelection && m_lookup,
                getTableName(),false);
        p_layout = contentPanel.getLayout();
		m_sqlMain = sql;
		m_sqlCount = "SELECT COUNT(*) FROM " + from + " WHERE " + where;
		//
		m_sqlOrder = "";
//		m_sqlUserOrder = "";
		if (orderBy != null && orderBy.length() > 0)
			m_sqlOrder = " ORDER BY " + orderBy;
	}   //  prepareTable


	/**************************************************************************
	 *  Execute Query
	 */
	protected void executeQuery()
	{
		line = new ArrayList<Object>();
		setCacheStart(-1);
		cacheEnd = -1;

		isTestCount = testCount();	//JPIERE-6 Modify InfoPanel#executeQuery()
		if(!isTestCount) return ;	//JPiere-6 Finish

		m_useDatabasePaging = (m_count > 1000);
		if (m_useDatabasePaging)
		{
			return ;
		}
		else
		{
			readLine(0, -1);
		}
	}

	private void readData(ResultSet rs) throws SQLException {
		int colOffset = 1;  //  columns start with 1
		List<Object> data = new ArrayList<Object>();
		for (int col = 0; col < p_layout.length; col++)
		{
			Object value = null;
			Class<?> c = p_layout[col].getColClass();
			int colIndex = col + colOffset;
			if (c == IDColumn.class)
			{
		        value = new IDColumn(rs.getInt(colIndex));

			}
			else if (c == Boolean.class)
		        value = new Boolean("Y".equals(rs.getString(colIndex)));
			else if (c == Timestamp.class)
		        value = rs.getTimestamp(colIndex);
			else if (c == BigDecimal.class)
		        value = rs.getBigDecimal(colIndex);
			else if (c == Double.class)
		        value = new Double(rs.getDouble(colIndex));
			else if (c == Integer.class)
		        value = new Integer(rs.getInt(colIndex));
			else if (c == KeyNamePair.class)
			{
				if (p_layout[col].isKeyPairCol())
				{
					String display = rs.getString(colIndex);
					int key = rs.getInt(colIndex+1);
					if (! rs.wasNull()) {
		                value = new KeyNamePair(key, display);
					}

					colOffset++;
				}
				else
				{
					int key = rs.getInt(colIndex);
					if (! rs.wasNull()) {
						WEditor editor = editorMap.get(p_layout[col].getColSQL());
						if (editor != null)
						{
							editor.setValue(key);
							value = new KeyNamePair(key, editor.getDisplayTextForGridView(key));
						}
						else
						{
							value = new KeyNamePair(key, Integer.toString(key));
						}
					}
				}
			}
			else if (c == ValueNamePair.class)
			{
				String key = rs.getString(colIndex);
				WEditor editor = editorMap.get(p_layout[col].getColSQL());
				if (editor != null)
				{
					value = new ValueNamePair(key, editor.getDisplayTextForGridView(key));
				}
				else
				{
					value = new ValueNamePair(key, key);
				}
			}
			else
			{
		        value = rs.getString(colIndex);
			}
			data.add(value);
		}
        line.add(data);
	}

    protected void renderItems()
    {
        if (m_count > 0)
        {
        	if (m_count > PAGE_SIZE)
        	{
        		if (paging == null)
        		{
	        		paging = new Paging();
	    			paging.setPageSize(PAGE_SIZE);
	    			paging.setTotalSize(m_count);
	    			paging.setDetailed(true);
	    			paging.addEventListener(ZulEvents.ON_PAGING, this);
	    			insertPagingComponent();
        		}
        		else
        		{
        			paging.setTotalSize(m_count);
        			paging.setActivePage(0);
        		}
    			List<Object> subList = readLine(0, PAGE_SIZE);
    			model = new ListModelTable(subList);
    			model.setSorter(this);
	            model.addTableModelListener(this);
	            model.setMultiple(p_multipleSelection);
	            contentPanel.setData(model, null);

	            pageNo = 0;
        	}
        	else
        	{
        		if (paging != null)
        		{
        			paging.setTotalSize(m_count);
        			paging.setActivePage(0);
        			pageNo = 0;
        		}
	            model = new ListModelTable(readLine(0, -1));
	            model.setSorter(this);
	            model.addTableModelListener(this);
	            model.setMultiple(p_multipleSelection);
	            contentPanel.setData(model, null);
        	}
        }
        else
        {
        	if (paging != null)
    		{
    			paging.setTotalSize(m_count);
    			paging.setActivePage(0);
    			pageNo = 0;
    		}
        	model = new ListModelTable(new ArrayList<Object>());
        	model.setSorter(this);
            model.addTableModelListener(this);
            model.setMultiple(p_multipleSelection);
            contentPanel.setData(model, null);
        }
        int no = m_count;
        setStatusLine(Integer.toString(no) + " " + Msg.getMsg(Env.getCtx(), "SearchRows_EnterQuery"), false);
        setStatusDB(Integer.toString(no));

        addDoubleClickListener();
    }

    private List<Object> readLine(int start, int end) {
    	//cacheStart & cacheEnd - 1 based index, start & end - 0 based index
    	if (getCacheStart() >= 1 && cacheEnd > getCacheStart())
    	{
    		if (m_useDatabasePaging)
    		{
    			if (start+1 >= getCacheStart() && end+1 <= cacheEnd)
    			{
    				return end == -1 ? line : line.subList(start-getCacheStart()+1, end-getCacheStart()+2);
    			}
    		}
    		else
    		{
    			if (end >= cacheEnd || end <= 0)
    			{
    				end = cacheEnd-1;
    			}
    			return line.subList(start, end+1);
    		}
    	}

    	setCacheStart(start + 1 - (PAGE_SIZE * 4));
    	if (getCacheStart() <= 0)
    		setCacheStart(1);

    	if (end == -1)
    	{
    		cacheEnd = m_count;
    	}
    	else
    	{
	    	cacheEnd = end + 1 + (PAGE_SIZE * 4);
	    	if (cacheEnd > m_count)
	    		cacheEnd = m_count;
    	}

    	line = new ArrayList<Object>();

    	PreparedStatement m_pstmt = null;
		ResultSet m_rs = null;
		String dataSql = null;

		long startTime = System.currentTimeMillis();
			//

        dataSql = buildDataSQL(start, end);
        if (log.isLoggable(Level.FINER))
        	log.finer(dataSql);
		try
		{
			m_pstmt = DB.prepareStatement(dataSql, null);
			setParameters (m_pstmt, false);	//	no count
			if (log.isLoggable(Level.FINE))
				log.fine("Start query - " + (System.currentTimeMillis()-startTime) + "ms");
			m_pstmt.setFetchSize(100);
			m_rs = m_pstmt.executeQuery();
			if (log.isLoggable(Level.FINE))
				log.fine("End query - " + (System.currentTimeMillis()-startTime) + "ms");
			//skips the row that we dont need if we can't use native db paging
			if (end > start && m_useDatabasePaging && !DB.getDatabase().isPagingSupported())
			{
				for (int i = 0; i < getCacheStart() - 1; i++)
				{
					if (!m_rs.next())
						break;
				}
			}

			int rowPointer = getCacheStart()-1;
			while (m_rs.next())
			{
				rowPointer++;
				readData(m_rs);
				//check now of rows loaded, break if we hit the suppose end
				if (m_useDatabasePaging && rowPointer >= cacheEnd)
				{
					break;
				}
			}
		}

		catch (SQLException e)
		{
			log.log(Level.SEVERE, dataSql, e);
		}

		finally
		{
			DB.close(m_rs, m_pstmt);
		}

		if (end >= cacheEnd || end <= 0)
		{
			end = cacheEnd-1;
		}

		if (end == -1)
		{
			return line;
		}
		else
		{
			int fromIndex = start-getCacheStart()+1;
			int toIndex = end-getCacheStart()+2;
			if (toIndex > line.size())
				toIndex = line.size();
			return line.subList(fromIndex, toIndex);
		}
	}

	protected String buildDataSQL(int start, int end) {
		String dataSql;
		String dynWhere = getSQLWhere();
        StringBuilder sql = new StringBuilder (m_sqlMain);
        if (dynWhere.length() > 0)
            sql.append(dynWhere);   //  includes first AND

        if (sql.toString().trim().endsWith("WHERE")) {
        	int index = sql.lastIndexOf(" WHERE");
        	sql.delete(index, sql.length());
        }
        if (m_sqlUserOrder != null && m_sqlUserOrder.trim().length() > 0)
        	sql.append(m_sqlUserOrder);
        else
        	sql.append(m_sqlOrder);
        dataSql = Msg.parseTranslation(Env.getCtx(), sql.toString());    //  Variables
        dataSql = MRole.getDefault().addAccessSQL(dataSql, getTableName(),
            MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO);
        if (end > start && m_useDatabasePaging && DB.getDatabase().isPagingSupported())
        {
        	dataSql = DB.getDatabase().addPagingSQL(dataSql, getCacheStart(), cacheEnd);
        }
		return dataSql;
	}

    private void addDoubleClickListener() {
    	Iterator<EventListener<? extends Event>> i = contentPanel.getEventListeners(Events.ON_DOUBLE_CLICK).iterator();
		while (i.hasNext()) {
			if (i.next() == this)
				return;
		}
		contentPanel.addEventListener(Events.ON_DOUBLE_CLICK, this);
		contentPanel.addEventListener(Events.ON_SELECT, this);
	}

    protected void insertPagingComponent() {
		contentPanel.getParent().insertBefore(paging, contentPanel.getNextSibling());
	}

    public Vector<String> getColumnHeader(ColumnInfo[] p_layout)
    {
        Vector<String> columnHeader = new Vector<String>();

        for (ColumnInfo info: p_layout)
        {
             columnHeader.add(info.getColHeader());
        }
        return columnHeader;
    }
	/**
	 * 	Test Row Count
	 *	@return true if display
	 */
	protected boolean testCount()
	{
		long start = System.currentTimeMillis();
		String dynWhere = getSQLWhere();
		StringBuilder sql = new StringBuilder (m_sqlCount);

		if (dynWhere.length() > 0)
			sql.append(dynWhere);   //  includes first AND

		String countSql = Msg.parseTranslation(Env.getCtx(), sql.toString());	//	Variables
		if (countSql.trim().endsWith("WHERE")) {
			countSql = countSql.trim();
			countSql = countSql.substring(0, countSql.length() - 5);
		}
		countSql = MRole.getDefault().addAccessSQL	(countSql, getTableName(),
													MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO);
		if (log.isLoggable(Level.FINER))
			log.finer(countSql);
		m_count = -1;

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(countSql, null);
			setParameters (pstmt, true);
			rs = pstmt.executeQuery();

			if (rs.next())
				m_count = rs.getInt(1);
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, countSql, e);
			m_count = -2;
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}

		if (log.isLoggable(Level.FINE))
			log.fine("#" + m_count + " - " + (System.currentTimeMillis()-start) + "ms");

		return true;
	}	//	testCount


	/**
	 *	Save Selection	- Called by dispose
	 */
	protected void saveSelection ()
	{
		//	Already disposed
		if (contentPanel == null)
			return;

		if (log.isLoggable(Level.CONFIG)) log.config( "OK=" + m_ok);

		if (!m_ok)      //  did not press OK
		{
			m_results.clear();
			contentPanel = null;
			this.detach();
            return;
		}

		//	Multi Selection
		if (p_multipleSelection)
		{
			m_results.addAll(getSelectedRowKeys());
		}
		else    //  singleSelection
		{
			Integer data = getSelectedRowKey();
			if (data != null)
				m_results.add(data);
		}

		if (log.isLoggable(Level.CONFIG)) log.config(getSelectedSQL());

		//	Save Settings of detail info screens
		saveSelectionDetail();

	}	//	saveSelection

	/**
	 *  Get the key of currently selected row
	 *  @return selected key
	 */
	protected Integer getSelectedRowKey()
	{
		Integer key = contentPanel.getSelectedRowKey();

		return key;
	}   //  getSelectedRowKey

	/**
     *  Get the keys of selected row/s based on layout defined in prepareTable
     *  @return IDs if selection present
     *  @author ashley
     */
    protected ArrayList<Integer> getSelectedRowKeys()
    {
        ArrayList<Integer> selectedDataList = new ArrayList<Integer>();

        if (contentPanel.getKeyColumnIndex() == -1)
        {
            return selectedDataList;
        }

        if (p_multipleSelection)
        {
        	int[] rows = contentPanel.getSelectedIndices();
            for (int row = 0; row < rows.length; row++)
            {
                Object data = contentPanel.getModel().getValueAt(rows[row], contentPanel.getKeyColumnIndex());
                if (data instanceof IDColumn)
                {
                    IDColumn dataColumn = (IDColumn)data;
                    selectedDataList.add(dataColumn.getRecord_ID());
                }
                else
                {
                    log.severe("For multiple selection, IDColumn should be key column for selection");
                }
            }
        }

        if (selectedDataList.size() == 0)
        {
        	int row = contentPanel.getSelectedRow();
    		if (row != -1 && contentPanel.getKeyColumnIndex() != -1)
    		{
    			Object data = contentPanel.getModel().getValueAt(row, contentPanel.getKeyColumnIndex());
    			if (data instanceof IDColumn)
    				selectedDataList.add(((IDColumn)data).getRecord_ID());
    			if (data instanceof Integer)
    				selectedDataList.add((Integer)data);
    		}
        }

        return selectedDataList;
    }   //  getSelectedRowKeys

    /**
	 *	Get selected Keys as Collection
	 *  @return selected keys (Integers)
	 */
	public Collection<Integer> getSelectedKeysCollection()
	{
		m_ok = true;
		saveSelection();
		if (!m_ok || m_results.size() == 0)
			return null;
		return m_results;
	}

	/**
	 *	Get selected Keys
	 *  @return selected keys (Integers)
	 */
	public Object[] getSelectedKeys()
	{
		if (!m_ok || m_results.size() == 0)
			return null;
		return m_results.toArray(new Integer[0]);
	}	//	getSelectedKeys;

	/**
	 *	Get (first) selected Key
	 *  @return selected key
	 */
	public Object getSelectedKey()
	{
		if (!m_ok || m_results.size() == 0)
			return null;
		return m_results.get(0);
	}	//	getSelectedKey

	/**
	 *	Is cancelled?
	 *	- if pressed Cancel = true
	 *	- if pressed OK or window closed = false
	 *  @return true if cancelled
	 */
	public boolean isCancelled()
	{
		return m_cancel;
	}	//	isCancelled

	/**
	 *	Get where clause for (first) selected key
	 *  @return WHERE Clause
	 */
	public String getSelectedSQL()
	{
		//	No results
		Object[] keys = getSelectedKeys();
		if (keys == null || keys.length == 0)
		{
			if (log.isLoggable(Level.CONFIG)) log.config("No Results - OK="
						+ m_ok + ", Cancel=" + m_cancel);
			return "";
		}
		//
		StringBuilder sb = new StringBuilder(getKeyColumn());
		if (keys.length > 1)
			sb.append(" IN (");
		else
			sb.append("=");

		//	Add elements
		for (int i = 0; i < keys.length; i++)
		{
			if (getKeyColumn().endsWith("_ID"))
				sb.append(keys[i].toString()).append(",");
			else
				sb.append("'").append(keys[i].toString()).append("',");
		}

		sb.replace(sb.length()-1, sb.length(), "");
		if (keys.length > 1)
			sb.append(")");
		return sb.toString();
	}	//	getSelectedSQL;



	/**
	 *  Get Table name Synonym
	 *  @return table name
	 */
	protected String getTableName()
	{
		return p_tableName;
	}   //  getTableName

	/**
	 *  Get Key Column Name
	 *  @return column name
	 */
	protected String getKeyColumn()
	{
		return p_keyColumn;
	}   //  getKeyColumn


	public String[] getEvents()
    {
        return InfoPanel.lISTENER_EVENTS;
    }

	// Elaine 2008/11/28
	/**
	 *  Enable OK, History, Zoom if row/s selected
     *  ---
     *  Changes: Changed the logic for accommodating multiple selection
     *  @author ashley
	 */
	protected void enableButtons ()
	{
		boolean enable = (contentPanel.getSelectedCount() > 0);
		confirmPanel.getOKButton().setEnabled(contentPanel.getSelectedCount() > 0); //red1 allow Process for 1 or more records

		if (hasHistory())
			confirmPanel.getButton(ConfirmPanel.A_HISTORY).setEnabled(enable);
		if (hasZoom())
			confirmPanel.getButton(ConfirmPanel.A_ZOOM).setEnabled( (contentPanel.getSelectedCount() == 1) ); //red1 only zoom for single record
		if (hasProcess())
			confirmPanel.getButton(ConfirmPanel.A_PROCESS).setEnabled(enable);
	}   //  enableButtons
	//

	/**************************************************************************
	 *  Get dynamic WHERE part of SQL
	 *	To be overwritten by concrete classes
	 *  @return WHERE clause
	 */
	protected abstract String getSQLWhere();

	/**
	 *  Set Parameters for Query
	 *	To be overwritten by concrete classes
	 *  @param pstmt statement
	 *  @param forCount for counting records
	 *  @throws SQLException
	 */
	protected abstract void setParameters (PreparedStatement pstmt, boolean forCount)
		throws SQLException;
    /**
     * notify to search editor of a value change in the selection info
     * @param event event
    *
     */

	protected void showHistory()					{}
	/**
	 *  Has History (false)
	 *	To be overwritten by concrete classes
	 *  @return true if it has history (default false)
	 */
	protected boolean hasHistory()				{return false;}
	/**
	 *  Customize dialog
	 *	To be overwritten by concrete classes
	 */
	protected boolean hasProcess()				{return false;}
	/**
	 *  Customize dialog
	 *	To be overwritten by concrete classes
	 */
	protected void customize()					{}
	/**
	 *  Has Customize (false)
	 *	To be overwritten by concrete classes
	 *  @return true if it has customize (default false)
	 */
	protected boolean hasCustomize()				{return false;}
	/**
	 *  Has Zoom (false)
	 *	To be overwritten by concrete classes
	 *  @return true if it has zoom (default false)
	 */
	protected boolean hasZoom()					{return false;}
	/**
	 *  Save Selection Details
	 *	To be overwritten by concrete classes
	 */
	protected void saveSelectionDetail()          {}

	/**
	 * 	Get Zoom Window
	 *	@param tableName table name
	 *	@param isSOTrx sales trx
	 *	@return AD_Window_ID
	 */
	protected int getAD_Window_ID (String tableName, boolean isSOTrx)
	{
		if (!isSOTrx && m_PO_Window_ID > 0)
			return m_PO_Window_ID;
		if (m_SO_Window_ID > 0)
			return m_SO_Window_ID;
		//
		String sql = "SELECT AD_Window_ID, PO_Window_ID FROM AD_Table WHERE TableName=?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, null);
			pstmt.setString(1, tableName);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				m_SO_Window_ID = rs.getInt(1);
				m_PO_Window_ID = rs.getInt(2);
			}
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}
		//
		if (!isSOTrx && m_PO_Window_ID > 0)
			return m_PO_Window_ID;
		return m_SO_Window_ID;
	}	//	getAD_Window_ID

    public void onEvent(Event event)
    {
        if  (event!=null)
        {
            if (event.getTarget().equals(confirmPanel.getButton(ConfirmPanel.A_OK)))
            {
                onOk();
            }
            else if (event.getTarget() == contentPanel && event.getName().equals(Events.ON_SELECT))
            {
            	m_lastOnSelectItem = null;
            	SelectEvent<?, ?> selectEvent = (SelectEvent<?, ?>) event;
            	if (selectEvent.getReference() != null && selectEvent.getReference() instanceof Listitem)
            		m_lastOnSelectItem = (Listitem) selectEvent.getReference();
            }
            else if (event.getTarget() == contentPanel && event.getName().equals(Events.ON_DOUBLE_CLICK))
            {
            	if (contentPanel.isMultiple()) {
            		if (m_lastOnSelectItem != null)
            			contentPanel.setSelectedItem(m_lastOnSelectItem);
            	}
            	onDoubleClick();
            }
            else if (event.getTarget().equals(confirmPanel.getButton(ConfirmPanel.A_REFRESH)))
            {
            	showBusyDialog();
            	Clients.response(new AuEcho(this, "onQueryCallback", null));
            }
            else if (event.getTarget().equals(confirmPanel.getButton(ConfirmPanel.A_CANCEL)))
            {
            	m_cancel = true;
                dispose(false);
            }
            // Elaine 2008/12/16
            else if (event.getTarget().equals(confirmPanel.getButton(ConfirmPanel.A_HISTORY)))
            {
            	if (!contentPanel.getChildren().isEmpty() && contentPanel.getSelectedRowKey()!=null)
                {
            		showHistory();
                }
            }
    		else if (event.getTarget().equals(confirmPanel.getButton(ConfirmPanel.A_CUSTOMIZE)))
    		{
            	if (!contentPanel.getChildren().isEmpty() && contentPanel.getSelectedRowKey()!=null)
                {
            		customize();
                }
    		}
            //
            else if (event.getTarget().equals(confirmPanel.getButton(ConfirmPanel.A_ZOOM)))
            {
                if (!contentPanel.getChildren().isEmpty() && contentPanel.getSelectedRowKey()!=null)
                {
                    zoom();
                    if (isLookup())
                    	this.detach();
                }
            }
            //red1 handle process_ID
            else if (event.getTarget().equals(confirmPanel.getButton(ConfirmPanel.A_PROCESS)))
            {
            	if (hasProcess) {
            		infoWindow = new MInfoWindow(Env.getCtx(), AD_InfoWindow_ID, null);
            		m_process = MProcess.get(Env.getCtx(), infoWindow.getAD_Process_ID());
            		m_pi = new ProcessInfo(m_process.getName(),
            				infoWindow.getAD_Process_ID());
            		m_pi.setAD_User_ID(Env.getAD_User_ID(Env.getCtx()));
            		m_pi.setAD_Client_ID(Env.getAD_Client_ID(Env.getCtx()));

            		MPInstance instance = new MPInstance(Env.getCtx(), infoWindow.getAD_Process_ID(), 0);
            		instance.saveEx();
            		// store in T_Selection table selected rows for Execute Process that retrieves from T_Selection in code.
            		DB.createT_Selection(instance.getAD_PInstance_ID(), getSelectedKeysCollection(),
        				null);
            		//clear back
            		m_results.clear();
            		// Execute Process
            		m_pi.setAD_PInstance_ID(instance.getAD_PInstance_ID());

            		//HengSin - to let process end with message and requery
            		WProcessCtl.process(p_WindowNo, m_pi, (Trx)null, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							if (!m_pi.isError()) {
								Clients.response(new AuEcho(InfoPanel.this, "onQueryCallback", null));
							}
							if (m_pi.isError())
								FDialog.error(p_WindowNo, m_pi.getSummary());
							else
								FDialog.info(p_WindowNo, null, m_pi.getSummary());
					//HengSin -- end --
						}
					});
               		confirmPanel.getButton(ConfirmPanel.A_PROCESS).setEnabled(false);
             	}
            } //red1 IDEMPIERE-1711 --end--
            else if (event.getTarget() == paging)
            {
            	int pgNo = paging.getActivePage();
            	if (pageNo != pgNo)
            	{

            		contentPanel.clearSelection();

            		pageNo = pgNo;
            		int start = pageNo * PAGE_SIZE;
            		int end = start + PAGE_SIZE;
            		if (end >= m_count)
            			end = m_count - 1;
            		List<Object> subList = readLine(start, end);
        			model = new ListModelTable(subList);
        			model.setSorter(this);
    	            model.addTableModelListener(this);
    	            model.setMultiple(p_multipleSelection);
    	            contentPanel.setData(model, null);

    				contentPanel.setSelectedIndex(0);
    			}
            }
            else if (event.getName().equals(Events.ON_CHANGE))
            {
            }
            else if (event.getName().equals(WindowContainer.ON_WINDOW_CONTAINER_SELECTION_CHANGED_EVENT))
        	{
        		if (infoWindow != null)
    				SessionManager.getAppDesktop().updateHelpContext(X_AD_CtxHelp.CTXTYPE_Info, infoWindow.getAD_InfoWindow_ID());
    			else
    				SessionManager.getAppDesktop().updateHelpContext(X_AD_CtxHelp.CTXTYPE_Home, 0);
        	}
            //default
            else
            {
            	showBusyDialog();
            	Clients.response(new AuEcho(this, "onQueryCallback", null));
            }
        }
    }  //  onEvent

	private void showBusyDialog() {
		progressWindow = new BusyDialog();
		progressWindow.setPage(this.getPage());
		progressWindow.doHighlighted();
	}

	private void hideBusyDialog() {
		if (progressWindow != null) {
			progressWindow.dispose();
			progressWindow = null;
		}
	}

    public void onQueryCallback()
    {
    	try
    	{
    		Listhead listHead = contentPanel.getListHead();
    		if (listHead != null) {
    			List<?> headers = listHead.getChildren();
    			for(Object obj : headers)
    			{
    				Listheader header = (Listheader) obj;
    				header.setSortDirection("natural");
    			}
    		}
    		m_sqlUserOrder="";
        	executeQuery();
        	if(isTestCount)		//JPIERE-6 Modify InfoPanel#onQueryCallBack()
        		renderItems();	//JPiere-6 Finish
        }
    	finally
    	{
    		hideBusyDialog();
    	}
    }

    private void onOk()
    {
		if (!contentPanel.getChildren().isEmpty() && contentPanel.getSelectedRowKey()!=null)
		{
		    dispose(true);
		}
	}

    private void onDoubleClick()
	{
		if (isLookup())
		{
			dispose(true);
		}
		else
		{
			zoom();
		}

	}

    public void tableChanged(WTableModelEvent event)
    {
    	enableButtons();
    }

    public void zoom()
    {
    	if (listeners != null && listeners.size() > 0)
    	{
	        ValueChangeEvent event = new ValueChangeEvent(this,"zoom",
	                   contentPanel.getSelectedRowKey(),contentPanel.getSelectedRowKey());
	        fireValueChange(event);
    	}
    	else
    	{
    		Integer recordId = contentPanel.getSelectedRowKey();
    		int AD_Table_ID = MTable.getTable_ID(p_tableName);
    		if (AD_Table_ID <= 0)
    		{
    			if (p_keyColumn.endsWith("_ID"))
    			{
    				AD_Table_ID = MTable.getTable_ID(p_keyColumn.substring(0, p_keyColumn.length() - 3));
    			}
    		}
    		if (AD_Table_ID > 0)
    			AEnv.zoom(AD_Table_ID, recordId);
    	}
    }

    public void addValueChangeListener(ValueChangeListener listener)
    {
        if (listener == null)
        {
            return;
        }

        listeners.add(listener);
    }

    public void fireValueChange(ValueChangeEvent event)
    {
        for (ValueChangeListener listener : listeners)
        {
           listener.valueChange(event);
        }
    }
    /**
     *  Dispose and save Selection
     *  @param ok OK pressed
     */
    public void dispose(boolean ok)
    {
    	if (log.isLoggable(Level.CONFIG)) log.config("OK=" + ok);
        m_ok = ok;

        //  End Worker
        if (isLookup())
        {
        	saveSelection();
        }
        if (Window.MODE_EMBEDDED.equals(getAttribute(Window.MODE_KEY)))
        	SessionManager.getAppDesktop().closeActiveWindow();
        else
	        this.detach();
    }   //  dispose

	public void sort(Comparator<Object> cmpr, boolean ascending) {
		WListItemRenderer.ColumnComparator lsc = (WListItemRenderer.ColumnComparator) cmpr;
		if (m_useDatabasePaging)
		{
			int col = lsc.getColumnIndex();
			String colsql = p_layout[col].getColSQL().trim();
			int lastSpaceIdx = colsql.lastIndexOf(" ");
			if (lastSpaceIdx > 0)
			{
				String tmp = colsql.substring(0, lastSpaceIdx).trim();
				char last = tmp.charAt(tmp.length() - 1);
				if (tmp.toLowerCase().endsWith("as"))
				{
					colsql = colsql.substring(lastSpaceIdx).trim();
				}
				else if (!(last == '*' || last == '-' || last == '+' || last == '/' || last == '>' || last == '<' || last == '='))
				{
					tmp = colsql.substring(lastSpaceIdx).trim();
					if (tmp.startsWith("\"") && tmp.endsWith("\""))
					{
						colsql = colsql.substring(lastSpaceIdx).trim();
					}
					else
					{
						boolean hasAlias = true;
						for(int i = 0; i < tmp.length(); i++)
						{
							char c = tmp.charAt(i);
							if (Character.isLetterOrDigit(c))
							{
								continue;
							}
							else
							{
								hasAlias = false;
								break;
							}
						}
						if (hasAlias)
						{
							colsql = colsql.substring(lastSpaceIdx).trim();
						}
					}
				}
			}
			m_sqlUserOrder = " ORDER BY " + colsql;
			if (!ascending)
				m_sqlUserOrder += " DESC ";
			executeQuery();
			renderItems();
		}
		else
		{
			Collections.sort(line, lsc);
			renderItems();
		}
	}

    public boolean isLookup()
    {
    	return m_lookup;
    }

    public void scrollToSelectedRow()
    {
    	if (contentPanel != null && contentPanel.getSelectedIndex() >= 0) {
    		Listitem selected = contentPanel.getItemAtIndex(contentPanel.getSelectedIndex());
    		if (selected != null) {
    			selected.focus();
    		}
    	}
    }

	@Override
	public String getSortDirection(Comparator<Object> cmpr) {
		return "natural";
	}

	public int getWindowNo() {
		return p_WindowNo;
	}

	public int getRowCount() {
		return contentPanel.getRowCount();
	}

	public Integer getFirstRowKey() {
		return contentPanel.getFirstRowKey();
	}

	/**
	 * @return the cacheStart
	 */
	protected int getCacheStart() {
		return cacheStart;
	}

	/**
	 * @param cacheStart the cacheStart to set
	 */
	private void setCacheStart(int cacheStart) {
		this.cacheStart = cacheStart;
	}

	/**
	 * @return the cacheEnd
	 */
	protected int getCacheEnd() {
		return cacheEnd;
	}

	protected boolean isUseDatabasePaging() {
		return m_useDatabasePaging;
	}

	@Override
	public void onPageAttached(Page newpage, Page oldpage) {
		super.onPageAttached(newpage, oldpage);
		if (newpage != null) {
			if (infoWindow != null)
				SessionManager.getAppDesktop().updateHelpContext(X_AD_CtxHelp.CTXTYPE_Info, infoWindow.getAD_InfoWindow_ID());
			else
				SessionManager.getAppDesktop().updateHelpContext(X_AD_CtxHelp.CTXTYPE_Home, 0);
		}
	}
}	//	Info

