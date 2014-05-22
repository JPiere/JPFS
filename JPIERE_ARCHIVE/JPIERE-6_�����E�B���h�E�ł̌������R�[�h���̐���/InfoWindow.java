/**
 *
 */
package org.adempiere.webui.info;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;

import org.adempiere.model.MInfoRelated;
import org.adempiere.webui.AdempiereWebUI;
import org.adempiere.webui.component.Borderlayout;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Column;
import org.adempiere.webui.component.Columns;
import org.adempiere.webui.component.ConfirmPanel;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.ListModelTable;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.Tab;
import org.adempiere.webui.component.Tabbox;
import org.adempiere.webui.component.Tabpanel;
import org.adempiere.webui.component.Tabpanels;
import org.adempiere.webui.component.Tabs;
import org.adempiere.webui.component.WListbox;
import org.adempiere.webui.editor.WEditor;
import org.adempiere.webui.editor.WTableDirEditor;
import org.adempiere.webui.editor.WebEditorFactory;
import org.adempiere.webui.event.ValueChangeEvent;
import org.adempiere.webui.event.ValueChangeListener;
import org.adempiere.webui.panel.InfoPanel;
import org.adempiere.webui.session.SessionManager;
import org.adempiere.webui.window.FDialog;	//JPIERE-6 Import FDialog to InfoWindow
import org.compiere.minigrid.ColumnInfo;
import org.compiere.minigrid.EmbedWinInfo;
import org.compiere.minigrid.IDColumn;
import org.compiere.model.AccessSqlParser;
import org.compiere.model.AccessSqlParser.TableInfo;
import org.compiere.model.GridField;
import org.compiere.model.GridFieldVO;
import org.compiere.model.MInfoColumn;
import org.compiere.model.MInfoWindow;
import org.compiere.model.MLookupFactory;
import org.compiere.model.MLookupInfo;
import org.compiere.model.MRole;
import org.compiere.model.MTable;
import org.compiere.model.X_AD_InfoColumn;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.compiere.util.Util;
import org.compiere.util.ValueNamePair;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SwipeEvent;
import org.zkoss.zul.Center;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.North;
import org.zkoss.zul.Separator;
import org.zkoss.zul.South;
import org.zkoss.zul.Space;
import org.zkoss.zul.Vbox;

/**
 * AD_InfoWindow implementation
 * @author hengsin
 * @contributor red1 	IDEMPIERE-1711 Process button (reviewed by Hengsin)
 * @contributor xolali 	IDEMPIERE-1045 Sub-Info Tabs  (reviewed by red1)
 */
public class InfoWindow extends InfoPanel implements ValueChangeListener, EventListener<Event> {
	/**
	 *
	 */
	private static final long serialVersionUID = -5198550045241794995L;

	protected Grid parameterGrid;
	private Borderlayout layout;
	private Vbox southBody;
	/** List of WEditors            */
    protected List<WEditor> editors;
    protected List<WEditor> identifiers;
    protected Properties infoContext;

    /** embedded Panel **/
    Tabbox embeddedPane = new Tabbox();
    ArrayList <EmbedWinInfo> embeddedWinList = new ArrayList <EmbedWinInfo>();

	/** Max Length of Fields */
    public static final int FIELDLENGTH = 20;

    protected ColumnInfo[] columnInfos;
	protected MInfoWindow infoWindow;
	protected TableInfo[] tableInfos;
	protected MInfoColumn[] infoColumns;
	protected String queryValue;

	private List<GridField> gridFields;
	private Checkbox checkAND;

	/**
	 * @param WindowNo
	 * @param tableName
	 * @param keyColumn
	 * @param multipleSelection
	 * @param whereClause
	 */
	public InfoWindow(int WindowNo, String tableName, String keyColumn, String queryValue,
			boolean multipleSelection, String whereClause, int AD_InfoWindow_ID) {
		this(WindowNo, tableName, keyColumn, queryValue, multipleSelection, whereClause, AD_InfoWindow_ID, true);
	}

	/**
	 * @param WindowNo
	 * @param tableName
	 * @param keyColumn
	 * @param multipleSelection
	 * @param whereClause
	 * @param lookup
	 */
	public InfoWindow(int WindowNo, String tableName, String keyColumn, String queryValue,
			boolean multipleSelection, String whereClause, int AD_InfoWindow_ID, boolean lookup) {
		super(WindowNo, tableName, keyColumn, multipleSelection, whereClause,
				lookup);
		this.queryValue = queryValue;
		this.AD_InfoWindow_ID = AD_InfoWindow_ID;

		//red1 IDEMPIERE-1711 (Hengsin advised this minimal coding way)
		infoWindow = new MInfoWindow(Env.getCtx(), AD_InfoWindow_ID, null);
   		if (infoWindow.getAD_Process_ID() > 0)
   		{
   			p_multipleSelection = true;
   			hasProcess = true;
        	Button b = confirmPanel.createButton(ConfirmPanel.A_PROCESS);
            confirmPanel.addComponentsLeft(b);
            b.addEventListener(Events.ON_CLICK, this);
        }
        //red1  -- end --

   		//Xolali IDEMPIERE-1045
   		contentPanel.addActionListener(new EventListener<Event>() {
   			public void onEvent(Event event) throws Exception {
   				int row = contentPanel.getSelectedRow();
   				if (row >= 0) {
   					for (EmbedWinInfo embed : embeddedWinList) {
   						refresh(contentPanel.getValueAt(row,0),embed);
   					}// refresh for all
   				}
   			}
   		}); //xolali --end-

		infoContext = new Properties(Env.getCtx());
		p_loadedOK = loadInfoDefinition();
		loadInfoRelatedTabs();
		if (loadedOK()) {
			if (isLookup()) {
				Env.clearTabContext(Env.getCtx(), p_WindowNo, Env.TAB_INFO);
			}

			renderWindow();

			if (queryValue != null && queryValue.trim().length() > 0)
			{
				prepareTable();
				processQueryValue();
			}
		}
	}

	private void processQueryValue() {
		for(int i = 0; i < identifiers.size(); i++) {
			WEditor editor = identifiers.get(i);
			editor.setValue(queryValue);
			testCount();
			if (m_count > 0) {
				break;
			} else {
				editor.setValue(null);
			}
		}

		boolean splitValue = false;
		if (m_count <= 0) {
			String[] values = queryValue.split("[_]");
			if (values.length == 2 && identifiers.size() == 2) {
				splitValue = true;
				for(int i = 0; i < values.length && i < identifiers.size(); i++) {
					WEditor editor = identifiers.get(i);
					editor.setValue(values[i]);
				}
				testCount();
			}
		}

		if (m_count > 0) {
			executeQuery();
			renderItems();
		} else if (!splitValue) {
			editors.get(0).setValue(queryValue);
		}
	}

	private boolean loadInfoDefinition() {
		String tableName = null;
		if (AD_InfoWindow_ID > 0) {
			infoWindow = new MInfoWindow(Env.getCtx(), AD_InfoWindow_ID, null);
			if (!infoWindow.isValid()) {
				infoWindow = null;
			} else {
				tableName = MTable.getTableName(Env.getCtx(), infoWindow.getAD_Table_ID());
				if (!tableName.equalsIgnoreCase(p_tableName)) {
					throw new IllegalArgumentException("AD_InfoWindow.TableName <> TableName argument. ("+tableName + " <> " + p_tableName+")");
				}
			}
		} else {
			infoWindow = MInfoWindow.get(p_tableName, (String)null);
		}

		if (infoWindow != null) {
			if (tableName == null)
				tableName = MTable.getTableName(Env.getCtx(), infoWindow.getAD_Table_ID());

			AccessSqlParser sqlParser = new AccessSqlParser("SELECT * FROM " + infoWindow.getFromClause());
			tableInfos = sqlParser.getTableInfo(0);
			if (tableInfos[0].getSynonym() != null && tableInfos[0].getSynonym().trim().length() > 0) {
				p_tableName = tableInfos[0].getSynonym().trim();
				if (p_whereClause != null && p_whereClause.trim().length() > 0) {
					p_whereClause = p_whereClause.replace(tableName+".", p_tableName+".");
				}
			}

			infoColumns = infoWindow.getInfoColumns(tableInfos);

			gridFields = new ArrayList<GridField>();
			for(MInfoColumn infoColumn : infoColumns) {
				String columnName = infoColumn.getColumnName();
				GridFieldVO vo = GridFieldVO.createParameter(infoContext, p_WindowNo, 0,
						columnName, infoColumn.get_Translation("Name"), infoColumn.getAD_Reference_ID(),
						infoColumn.getAD_Reference_Value_ID(), false, false);
				if (infoColumn.getAD_Val_Rule_ID() > 0) {
					vo.ValidationCode = infoColumn.getAD_Val_Rule().getCode();
					if (vo.lookupInfo != null) {
						vo.lookupInfo.ValidationCode = vo.ValidationCode;
						vo.lookupInfo.IsValidated = false;
					}
				}
				vo.DisplayLogic = infoColumn.getDisplayLogic() != null ? infoColumn.getDisplayLogic() : "";
				String desc = infoColumn.get_Translation("Description");
				vo.Description = desc != null ? desc : "";
				String help = infoColumn.get_Translation("Help");
				vo.Help = help != null ? help : "";
				GridField gridField = new GridField(vo);
				gridFields.add(gridField);
			}

			StringBuilder builder = new StringBuilder(p_whereClause != null ? p_whereClause.trim() : "");
			String infoWhereClause = infoWindow.getWhereClause();
			if (infoWhereClause != null && infoWhereClause.indexOf("@") >= 0) {
				infoWhereClause = Env.parseContext(Env.getCtx(), p_WindowNo, infoWhereClause, false, false);
				if (infoWhereClause.length() == 0)
					log.log(Level.SEVERE, "Cannot parse context= " + infoWindow.getWhereClause());
			}
			if (infoWhereClause != null && infoWhereClause.trim().length() > 0) {
				if (builder.length() > 0) {
					builder.append(" AND ");
				}
				builder.append(infoWhereClause);
				p_whereClause = builder.toString();
			}

			return true;
		} else {
			return false;
		}
	}

	//private MInfoColumn[] topinfoColumns;//infoWindow.getInfoColumns(tableInfos);
	private boolean loadInfoRelatedTabs() {
		if (infoWindow == null)
			return false;

		// topinfoColumns = infoWindow.getInfoColumns();
		MInfoRelated[] relatedInfoList = infoWindow.getInfoRelated(true);
		Tabpanels tabPanels = new Tabpanels();
		Tabs tabs = new Tabs();

		if (relatedInfoList.length > 0) { // setup the panel

			//embeddedPane.setTitle(Msg.translate(Env.getCtx(), "Related Information"));
			embeddedPane.setHeight("100%");
			//tabPanels = new Tabpanels();
			embeddedPane.appendChild(tabPanels);
			//tabs = new Tabs();
			embeddedPane.appendChild(tabs);

		}

		//	for(int i=0; i <  relatedinfoList.length - 1 ; i++) {
		for (MInfoRelated relatedInfo:relatedInfoList) {

			String tableName = null;
			int infoRelatedID = relatedInfo.getRelatedInfo_ID();

			MInfoWindow embedInfo = new MInfoWindow(Env.getCtx(), infoRelatedID, null);

			AccessSqlParser sqlParser = new AccessSqlParser("SELECT * FROM " + embedInfo.getFromClause());
			TableInfo[] tableInfos = sqlParser.getTableInfo(0);
			if (tableInfos[0].getSynonym() != null && tableInfos[0].getSynonym().trim().length() > 0){
				tableName = tableInfos[0].getSynonym().trim();
			}

			WListbox embeddedTbl = new WListbox();
			String m_sqlEmbedded;

			//MInfoWindow.getInfoWindow(infoRelatedID);

			if (embedInfo != null) {
				ArrayList<ColumnInfo> list = new ArrayList<ColumnInfo>();
				list = getInfoColumnslayout(embedInfo);
				//  Convert ArrayList to Array
				ColumnInfo[] s_layoutEmbedded  = new ColumnInfo[list.size()];
				list.toArray(s_layoutEmbedded);

				/**	From Clause							*/
				String s_sqlFrom = embedInfo.getFromClause();
				/** Where Clause						*/
				String s_sqlWhere = relatedInfo.getLinkColumnName() + "=?";
				m_sqlEmbedded = embeddedTbl.prepareTable(s_layoutEmbedded, s_sqlFrom, s_sqlWhere, false, tableName);

				embeddedTbl.setMultiSelection(false);

				embeddedTbl.autoSize();

				embeddedTbl.getModel().addTableModelListener(this);

				//Xolali - add embeddedTbl to list, add m_sqlembedded to list
				EmbedWinInfo ewinInfo = new EmbedWinInfo(embedInfo,embeddedTbl,m_sqlEmbedded,relatedInfo.getLinkColumnName(), relatedInfo.getLinkInfoColumn());
				embeddedWinList.add(ewinInfo);

				MInfoWindow riw = (MInfoWindow) relatedInfo.getRelatedInfo();
				String tabTitle;
				if (riw != null)
					tabTitle = Util.cleanAmp(riw.get_Translation("Name"));
				else
					tabTitle = relatedInfo.getName();
				Tab tab = new Tab(tabTitle);
				tabs.appendChild(tab);
				Tabpanel desktopTabPanel = new Tabpanel();
				//desktopTabPanel.
				desktopTabPanel.setHeight("100%");
				desktopTabPanel.appendChild(embeddedTbl);
				tabPanels.appendChild(desktopTabPanel);
			}

		}

		return true;
	}

	protected void prepareTable() {
		List<ColumnInfo> list = new ArrayList<ColumnInfo>();
		String keyTableAlias = tableInfos[0].getSynonym() != null && tableInfos[0].getSynonym().trim().length() > 0
				? tableInfos[0].getSynonym()
				: tableInfos[0].getTableName();

		String keySelectClause = keyTableAlias+"."+p_keyColumn;
		list.add(new ColumnInfo(" ", keySelectClause, IDColumn.class));

		int i = 0;
		for(MInfoColumn infoColumn : infoColumns)
		{
			if (infoColumn.isDisplayed(infoContext, p_WindowNo))
			{
				ColumnInfo columnInfo = null;
				if (infoColumn.getAD_Reference_ID() == DisplayType.ID)
				{
					if (infoColumn.getSelectClause().equalsIgnoreCase(keySelectClause))
						continue;

					columnInfo = new ColumnInfo(infoColumn.get_Translation("Name"), infoColumn.getSelectClause(), DisplayType.getClass(infoColumn.getAD_Reference_ID(), true));
				}
				else if (DisplayType.isLookup(infoColumn.getAD_Reference_ID()))
				{
					if (infoColumn.getAD_Reference_ID() == DisplayType.List)
					{
						columnInfo = new ColumnInfo(infoColumn.get_Translation("Name"), infoColumn.getSelectClause(), ValueNamePair.class, (String)null);
					}
					else
					{
						columnInfo = createLookupColumnInfo(tableInfos, gridFields.get(i), infoColumn);
					}
				}
				else
				{
					columnInfo = new ColumnInfo(infoColumn.get_Translation("Name"), infoColumn.getSelectClause(), DisplayType.getClass(infoColumn.getAD_Reference_ID(), true));
				}
				columnInfo.setColDescription(infoColumn.get_Translation("Description"));
				columnInfo.setGridField(gridFields.get(i));
				list.add(columnInfo);
			}
			i++;
		}

		columnInfos = list.toArray(new ColumnInfo[0]);

		prepareTable(columnInfos, infoWindow.getFromClause(), p_whereClause, infoWindow.getOrderByClause());
	}

	private ColumnInfo createLookupColumnInfo(TableInfo[] tableInfos,
			GridField gridField, MInfoColumn infoColumn) {
		String columnName = gridField.getColumnName();
		String validationCode = "";
		MLookupInfo lookupInfo = MLookupFactory.getLookupInfo(Env.getCtx(), p_WindowNo, 0, infoColumn.getAD_Reference_ID(), Env.getLanguage(Env.getCtx()), columnName, infoColumn.getAD_Reference_Value_ID(), false, validationCode);
		String displayColumn = lookupInfo.DisplayColumn;

		int index = infoColumn.getSelectClause().indexOf(".");
		if (index == infoColumn.getSelectClause().lastIndexOf("."))
		{
			String synonym = infoColumn.getSelectClause().substring(0, index);
			for(TableInfo tableInfo : tableInfos)
			{
				if (tableInfo.getSynonym() != null && tableInfo.getSynonym().equals(synonym))
				{
					if (tableInfo.getTableName().equalsIgnoreCase(lookupInfo.TableName))
					{
						displayColumn = displayColumn.replace(lookupInfo.TableName+".", tableInfo.getSynonym()+".");
						ColumnInfo columnInfo = new ColumnInfo(infoColumn.get_Translation("Name"), displayColumn, KeyNamePair.class, infoColumn.getSelectClause());
						return columnInfo;
					}
					break;
				}
			}
		}

		WEditor editor = null;
        editor = WebEditorFactory.getEditor(gridField, true);
        editor.setMandatory(false);
        editor.setReadWrite(false);
        editorMap.put(infoColumn.getSelectClause(), editor);

		ColumnInfo columnInfo = new ColumnInfo(infoColumn.get_Translation("Name"), infoColumn.getSelectClause(), KeyNamePair.class, (String)null);
		return columnInfo;
	}

	/* (non-Javadoc)
	 * @see org.adempiere.webui.panel.InfoPanel#getSQLWhere()
	 */
	@Override
	protected String getSQLWhere() {
		StringBuilder builder = new StringBuilder();
		MTable table = MTable.get(Env.getCtx(), infoWindow.getAD_Table_ID());
		if (!hasIsActiveEditor() && table.get_ColumnIndex("IsActive") >=0 ) {
			if (p_whereClause != null && p_whereClause.trim().length() > 0) {
				builder.append(" AND ");
			}
			builder.append(tableInfos[0].getSynonym()).append(".IsActive='Y'");
		}
		int count = 0;
		for(WEditor editor : editors) {
			if (editor instanceof IWhereClauseEditor) {
				String whereClause = ((IWhereClauseEditor) editor).getWhereClause();
				if (whereClause != null && whereClause.trim().length() > 0) {
					count++;
					if (count == 1) {
						if (builder.length() > 0) {
							builder.append(" AND ");
							if (!checkAND.isChecked()) builder.append(" ( ");
						} else if (p_whereClause != null && p_whereClause.trim().length() > 0) {
							builder.append(" AND ");
							if (!checkAND.isChecked()) builder.append(" ( ");
						}
					} else {
						builder.append(checkAND.isChecked() ? " AND " : " OR ");
					}
					builder.append(whereClause);
				}
			} else if (editor.getGridField() != null && editor.getValue() != null && editor.getValue().toString().trim().length() > 0) {
				MInfoColumn mInfoColumn = findInfoColumn(editor.getGridField());
				if (mInfoColumn == null || mInfoColumn.getSelectClause().equals("0")) {
					continue;
				}
				String columnName = mInfoColumn.getSelectClause();
				int asIndex = columnName.toUpperCase().lastIndexOf(" AS ");
				if (asIndex > 0) {
					columnName = columnName.substring(0, asIndex);
				}

				count++;
				if (count == 1) {
					if (builder.length() > 0) {
						builder.append(" AND ");
						if (!checkAND.isChecked()) builder.append(" ( ");
					} else if (p_whereClause != null && p_whereClause.trim().length() > 0) {
						builder.append(" AND ");
						if (!checkAND.isChecked()) builder.append(" ( ");
					} else if (hasIsActiveEditor() && !checkAND.isChecked()) {
						builder.append(" ( ");
					}
				} else {
					builder.append(checkAND.isChecked() ? " AND " : " OR ");
				}

				String columnClause = null;
				if (mInfoColumn.getQueryFunction() != null && mInfoColumn.getQueryFunction().trim().length() > 0) {
					String function = mInfoColumn.getQueryFunction();
					if (function.indexOf("@") >= 0) {
						String s = Env.parseContext(infoContext, p_WindowNo, function, false, false);
						if (s.length() == 0) {
							log.log(Level.SEVERE, "Failed to parse query function. " + function);
						} else {
							function = s;
						}
					}
					if (function.indexOf("?") >= 0) {
						columnClause = function.replaceFirst("[?]", columnName);
					} else {
						columnClause = function+"("+columnName+")";
					}
				} else {
					columnClause = columnName;
				}
				builder.append(columnClause)
					   .append(" ")
					   .append(mInfoColumn.getQueryOperator())
					   .append(" ?");
			}
		}
		if (count > 0 && !checkAND.isChecked()) {
			builder.append(" ) ");
		}
		return builder.toString();
	}

	private MInfoColumn findInfoColumn(GridField gridField) {
		for(int i = 0; i < gridFields.size(); i++) {
			if (gridFields.get(i) == gridField) {
				return infoColumns[i];
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.adempiere.webui.panel.InfoPanel#setParameters(java.sql.PreparedStatement, boolean)
	 */
	@Override
	protected void setParameters(PreparedStatement pstmt, boolean forCount)
			throws SQLException {
		int parameterIndex = 0;
		for(WEditor editor : editors) {
			if (editor.getGridField() != null && editor.getValue() != null && editor.getValue().toString().trim().length() > 0) {
				MInfoColumn mInfoColumn = findInfoColumn(editor.getGridField());
				if (mInfoColumn == null || mInfoColumn.getSelectClause().equals("0")) {
					continue;
				}
				Object value = editor.getValue();
				parameterIndex++;
				if (value instanceof Boolean) {
					pstmt.setString(parameterIndex, ((Boolean) value).booleanValue() ? "Y" : "N");
				} else if (value instanceof String) {
					if (mInfoColumn.getQueryOperator().equals(X_AD_InfoColumn.QUERYOPERATOR_Like)) {
						StringBuilder valueStr = new StringBuilder(value.toString().toUpperCase());
	                    if (!valueStr.toString().endsWith("%"))
	                        valueStr.append("%");
	                    pstmt.setString(parameterIndex, valueStr.toString());
					} else {
						pstmt.setString(parameterIndex, (String)value);
					}
				} else {
					pstmt.setObject(parameterIndex, value);
				}

			}
		}

	}

	@Override
	protected void prepareTable(ColumnInfo[] layout, String from, String where,
			String orderBy) {
		super.prepareTable(layout, from, where, orderBy);
		if (m_sqlMain.indexOf("@") >= 0) {
			String sql = Env.parseContext(infoContext, p_WindowNo, m_sqlMain, true);
			if (sql == null || sql.length() == 0) {
				log.severe("Failed to parsed sql. sql=" + m_sqlMain);
			} else {
				m_sqlMain = sql;
			}
		}

		if (m_sqlMain.length() > 0 &&  infoWindow.isDistinct()) {
			m_sqlMain = m_sqlMain.substring("SELECT ".length());
			m_sqlMain = "SELECT DISTINCT " + m_sqlMain;
		}

		if (m_sqlOrder != null && m_sqlOrder.indexOf("@") >= 0) {
			String sql = Env.parseContext(infoContext, p_WindowNo, m_sqlOrder, false, false);
			if (sql == null || sql.length() == 0) {
				log.severe("Failed to parsed sql. sql=" + m_sqlOrder);
			} else {
				m_sqlOrder = sql;
			}
		}
	}

	protected void renderWindow()
	{
		setTitle(infoWindow.get_Translation("Name"));
		layout = new Borderlayout();
        layout.setWidth("100%");
        layout.setHeight("100%");
        if (!isLookup())
        {
        	layout.setStyle("position: absolute");
        }
        this.appendChild(layout);

        if (isLookup())
			contentPanel.setWidth("99%");
        else
        	contentPanel.setStyle("width: 99%; margin: 0px auto;");
        contentPanel.setVflex(true);
        contentPanel.setSizedByContent(true);

        North north = new North();
        layout.appendChild(north);
        renderParameterPane(north);


        Center center = new Center();
		layout.appendChild(center);
        renderContentPane(center);

        South south = new South();
		layout.appendChild(south);
		renderFooter(south);

		confirmPanel.getButton(ConfirmPanel.A_ZOOM).setVisible(hasZoom());
		confirmPanel.getButton(ConfirmPanel.A_ZOOM).setDisabled(true);
		if (hasProcess)
			confirmPanel.getButton(ConfirmPanel.A_PROCESS).setDisabled(true);

	}

	protected void renderFooter(South south) {
		southBody = new Vbox();
		southBody.setHflex("1");
		south.appendChild(southBody);
		southBody.appendChild(new Separator());
		southBody.appendChild(confirmPanel);
		southBody.appendChild(statusBar);
	}

	protected void insertPagingComponent() {
		southBody.insertBefore(paging, southBody.getFirstChild());
		layout.invalidate();
	}

	protected void renderContentPane(Center center) {
		Div div = new Div();
		div.setStyle("width :100%; height: 100%");
		div.setVflex("1");
		div.setHflex("1");
		div.appendChild(contentPanel);

		Borderlayout inner = new Borderlayout();
		inner.setWidth("100%");
		inner.setHeight("100%");
		int height = SessionManager.getAppDesktop().getClientInfo().desktopHeight * 90 / 100;
		if (isLookup())
			inner.setStyle("border: none; position: relative; ");
		else
			inner.setStyle("border: none; position: absolute; ");
		inner.appendCenter(div);
		//true will conflict with listbox scrolling
		inner.getCenter().setAutoscroll(false);

		if (embeddedWinList.size() > 0) {
			South south = new South();
			int detailHeight = (height * 25 / 100);
			south.setHeight(detailHeight + "px");
			south.setAutoscroll(true);
			south.setCollapsible(true);
			south.setSplittable(true);
			south.setTitle(Msg.translate(Env.getCtx(), "Related Information"));
			south.setTooltiptext(Msg.translate(Env.getCtx(), "Related Information"));

			south.addEventListener(Events.ON_SWIPE, new EventListener<SwipeEvent>() {
				@Override
				public void onEvent(SwipeEvent event) throws Exception {
					South south = (South) event.getTarget();
					if ("down".equals(event.getSwipeDirection())) {
						south.setOpen(false);
					}
				}
			});
			south.setSclass("south-collapsible-with-title");
			south.setAutoscroll(true);
			//south.sets
			inner.appendChild(south);
			embeddedPane.setSclass("info-product-tabbedpane");
			embeddedPane.setVflex("1");
			embeddedPane.setHflex("1");

			south.appendChild(embeddedPane);

		}// render embedded

		center.appendChild(inner);
	}

	protected void renderParameterPane(North north) {
		createParameterPanel();
		north.appendChild(parameterGrid);
	}

	protected void createParameterPanel() {
		parameterGrid = GridFactory.newGridLayout();
		parameterGrid.setWidgetAttribute(AdempiereWebUI.WIDGET_INSTANCE_NAME, "infoParameterPanel");
		parameterGrid.setStyle("width: 95%; margin: auto !important;");
		Columns columns = new Columns();
		parameterGrid.appendChild(columns);
		for(int i = 0; i < 6; i++)
			columns.appendChild(new Column());

		Column column = new Column();
		column.setWidth("100px");
		column.setAlign("right");
		columns.appendChild(column);

		Rows rows = new Rows();
		parameterGrid.appendChild(rows);

		editors = new ArrayList<WEditor>();
		identifiers = new ArrayList<WEditor>();
		TreeMap<Integer, List<Object[]>> tree = new TreeMap<Integer, List<Object[]>>();
		for (int i = 0; i < infoColumns.length; i++)
		{
			if (infoColumns[i].isQueryCriteria()) {
				List<Object[]> list = tree.get(infoColumns[i].getSeqNoSelection());
				if (list == null) {
					list = new ArrayList<Object[]>();
					tree.put(infoColumns[i].getSeqNoSelection(), list);
				}
				list.add(new Object[]{infoColumns[i], gridFields.get(i)});
			}
		}

		for (Integer i : tree.keySet()) {
			List<Object[]> list = tree.get(i);
			for(Object[] value : list) {
				addSelectionColumn((MInfoColumn)value[0], (GridField)value[1]);
			}
		}

		if (checkAND == null) {
			if (parameterGrid.getRows() != null && parameterGrid.getRows().getFirstChild() != null) {
				Row row = (Row) parameterGrid.getRows().getFirstChild();
				int col = row.getChildren().size();
				while (col < 6) {
					row.appendChild(new Space());
					col++;
				}
				createAndCheckbox();
				row.appendChild(checkAND);
			}
		}
		evalDisplayLogic();
	}

	private void evalDisplayLogic() {
		for(WEditor editor : editors) {
        	if (editor.getGridField() != null && !editor.getGridField().isDisplayed(true)) {
        		editor.getComponent().setVisible(false);
        		if (editor.getLabel() != null)
        			editor.getLabel().setVisible(false);
        	}
        	else if (!editor.getComponent().isVisible()) {
        		editor.getComponent().setVisible(true);
        		if (editor.getLabel() != null)
        			editor.getLabel().setVisible(true);
        	}
        }
	}

	/**
     *  Add Selection Column to first Tab
	 * @param infoColumn
     *  @param mField field
    **/
    protected void addSelectionColumn(MInfoColumn infoColumn, GridField mField)
    {
        int displayLength = mField.getDisplayLength();
        if (displayLength <= 0 || displayLength > FIELDLENGTH)
            mField.setDisplayLength(FIELDLENGTH);
        else
            displayLength = 0;

        //  Editor
        WEditor editor = null;
        if (mField.getDisplayType() == DisplayType.PAttribute)
        {
        	editor = new WInfoPAttributeEditor(infoContext, p_WindowNo, mField);
	        editor.setReadWrite(true);
        }
        else
        {
	        editor = WebEditorFactory.getEditor(mField, false);
	        editor.setMandatory(false);
	        editor.setReadWrite(true);
	        editor.dynamicDisplay();
	        editor.addValueChangeListener(this);
	        editor.fillHorizontal();
        }
        Label label = editor.getLabel();
        Component fieldEditor = editor.getComponent();

        //
        if (displayLength > 0)      //  set it back
            mField.setDisplayLength(displayLength);
        //
        if (label != null) {
        	if (infoColumn.getQueryOperator().equals(X_AD_InfoColumn.QUERYOPERATOR_Gt) ||
        		infoColumn.getQueryOperator().equals(X_AD_InfoColumn.QUERYOPERATOR_GtEq) ||
        		infoColumn.getQueryOperator().equals(X_AD_InfoColumn.QUERYOPERATOR_Le) ||
        		infoColumn.getQueryOperator().equals(X_AD_InfoColumn.QUERYOPERATOR_LeEq) ||
        		infoColumn.getQueryOperator().equals(X_AD_InfoColumn.QUERYOPERATOR_NotEq )) {
        		label.setValue(label.getValue() + " " + infoColumn.getQueryOperator());
        	}
        }

        addSearchParameter(label, fieldEditor);

        editors.add(editor);
        if (infoColumn.isIdentifier()) {
        	identifiers.add(editor);
        }

        fieldEditor.addEventListener(Events.ON_OK, this);
    }   // addSelectionColumn

	protected void addSearchParameter(Label label, Component fieldEditor) {
		Row panel = null;
        if (parameterGrid.getRows().getChildren().isEmpty())
        {
        	panel = new Row();
        	parameterGrid.getRows().appendChild(panel);
        }
        else
        {
        	panel = (Row) parameterGrid.getRows().getLastChild();
        	if (panel.getChildren().size() == 6)
        	{
        		if (parameterGrid.getRows().getChildren().size() == 1)
        		{
        			createAndCheckbox();
					panel.appendChild(checkAND);
        		}
        		else
        		{
        			panel.appendChild(new Space());
        		}
        		panel = new Row();
            	parameterGrid.getRows().appendChild(panel);
        	}
        }
        if (!(fieldEditor instanceof Checkbox))
        {
        	panel.appendChild(label.rightAlign());
        } else {
        	panel.appendChild(new Space());
        }
        panel.appendChild(fieldEditor);
	}

	private void createAndCheckbox() {
		checkAND = new Checkbox();
		checkAND.setLabel(Msg.getMsg(Env.getCtx(), "SearchAND", true));
		String tips = Msg.getMsg(Env.getCtx(), "SearchAND", false);
		if (!Util.isEmpty(tips))
		{
			checkAND.setTooltiptext(tips);
		}
		checkAND.setChecked(true);
		checkAND.addEventListener(Events.ON_CHECK, this);
	}

	protected int findColumnIndex(String columnName) {
		for(int i = 0; i < columnInfos.length; i++) {
			GridField field = columnInfos[i].getGridField();
			if (field != null && field.getColumnName().equalsIgnoreCase(columnName)) {
				return i;
			}
		}
		return -1;
	}

    /**
     *  Save Selection Details
     *  Get Location/Partner Info
     */
    @Override
    protected void saveSelectionDetail()
    {
        int row = contentPanel.getSelectedRow();
        if (row == -1)
            return;

        int column = -1;
        for(ColumnInfo columnInfo : columnInfos)
        {
        	column++;
        	GridField field = columnInfo.getGridField();
        	if (field == null) continue;

        	String columnName = field.getColumnName();
        	if (columnInfo.getColClass().equals(KeyNamePair.class))
        	{
        		KeyNamePair knp = (KeyNamePair) contentPanel.getValueAt(row, column);
        		Env.setContext(Env.getCtx(), p_WindowNo, Env.TAB_INFO, columnName, knp == null ? "0" : knp.getID());
        	}
        	else if (columnName.endsWith("_ID"))
        	{
        		Object id = contentPanel.getValueAt(row, column);
        		Env.setContext(Env.getCtx(), p_WindowNo, Env.TAB_INFO, columnName, id == null ? "0" : id.toString());
        	}
        	else
        	{
        		Object value = contentPanel.getValueAt(row, column);
        		Env.setContext(Env.getCtx(), p_WindowNo, Env.TAB_INFO, field.getColumnName(), value == null ? "" : value.toString());
        	}
        }
    }   //  saveSelectionDetail

    @Override
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

        dataSql = Msg.parseTranslation(Env.getCtx(), sql.toString());    //  Variables
        dataSql = MRole.getDefault().addAccessSQL(dataSql, getTableName(),
            MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO);

        if (infoWindow.getOtherClause() != null && infoWindow.getOtherClause().trim().length() > 0) {
        	String otherClause = infoWindow.getOtherClause();
        	if (otherClause.indexOf("@") >= 0) {
        		String s = Env.parseContext(infoContext, p_WindowNo, otherClause, false, false);
        		if (s.length() == 0) {
        			log.severe("Failed to parse other clause. " + otherClause);
        		} else {
        			otherClause = s;
        		}
        	}
        	dataSql = dataSql + " " + otherClause;
        }

        if (m_sqlUserOrder != null && m_sqlUserOrder.trim().length() > 0)
        	dataSql = dataSql + m_sqlUserOrder;
        else
        	dataSql = dataSql + m_sqlOrder;

        if (end > start && isUseDatabasePaging() && DB.getDatabase().isPagingSupported())
        {
        	dataSql = DB.getDatabase().addPagingSQL(dataSql, getCacheStart(), getCacheEnd());
        }
		return dataSql;
	}

    @Override
    protected void executeQuery() {
    	prepareTable();
    	super.executeQuery();
    }

	@Override
	protected boolean hasZoom() {
		return !isLookup() && infoWindow != null && !MTable.get(Env.getCtx(), infoWindow.getAD_Table_ID()).isView();
	}

	@Override
	protected boolean hasProcess() {  //red1 IDEMPIERE-1711 to retain InfoWindow.process_ID > 0 as true
		return hasProcess;
	}

	@Override
	public void valueChange(ValueChangeEvent evt) {
		if (evt != null && evt.getSource() instanceof WEditor)
        {
            WEditor editor = (WEditor)evt.getSource();
            boolean asiChanged = false;
            if (evt.getNewValue() == null) {
            	Env.setContext(infoContext, p_WindowNo, editor.getColumnName(), "");
            	Env.setContext(infoContext, p_WindowNo, Env.TAB_INFO, editor.getColumnName(), "");
            } else if (evt.getNewValue() instanceof Boolean) {
            	Env.setContext(infoContext, p_WindowNo, editor.getColumnName(), (Boolean)evt.getNewValue());
            	Env.setContext(infoContext, p_WindowNo, Env.TAB_INFO, editor.getColumnName(), (Boolean)evt.getNewValue());
            } else if (evt.getNewValue() instanceof Timestamp) {
            	Env.setContext(infoContext, p_WindowNo, editor.getColumnName(), (Timestamp)evt.getNewValue());
            	Env.setContext(infoContext, p_WindowNo, Env.TAB_INFO+"|"+editor.getColumnName(), (Timestamp)evt.getNewValue());
            } else {
            	Env.setContext(infoContext, p_WindowNo, editor.getColumnName(), evt.getNewValue().toString());
            	Env.setContext(infoContext, p_WindowNo, Env.TAB_INFO, editor.getColumnName(), evt.getNewValue().toString());
            }
            // if attribute set changed (from any value to any value) clear the attribute set instance m_pAttributeWhere
            if (editor instanceof WTableDirEditor && editor.getColumnName().equals("M_AttributeSet_ID"))
            	asiChanged = true;

            for(WEditor otherEditor : editors)
            {
            	if (otherEditor == editor)
            		continue;
            	if (asiChanged && otherEditor instanceof WInfoPAttributeEditor)
            		((WInfoPAttributeEditor)otherEditor).clearWhereClause();

            	otherEditor.dynamicDisplay();
            }

            evalDisplayLogic();
        }

	}

	public void onEvent(Event event)
    {
		if (event.getName().equals(Events.ON_FOCUS)) {
    		for (WEditor editor : editors)
    		{
    			if (editor.isComponentOfEditor(event.getTarget()))
    			{
        			SessionManager.getAppDesktop().updateHelpTooltip(editor.getGridField());
        			return;
    			}
    		}
    	}
    	else if (event.getName().equals(Events.ON_BLUR)) {
    		for (WEditor editor : editors)
    		{
    			if (editor.isComponentOfEditor(event.getTarget()))
    			{
        			SessionManager.getAppDesktop().updateHelpTooltip(null);
        			return;
    			}
    		}
    	}
    	else
    	{
    		super.onEvent(event);
    	}
    }

	@Override
	public void onPageAttached(Page newpage, Page oldpage) {
		super.onPageAttached(newpage, oldpage);
		if (newpage != null) {
			for (WEditor editor : editors)
			{
				editor.getComponent().addEventListener(Events.ON_FOCUS, this);
				editor.getComponent().addEventListener(Events.ON_BLUR, this);
			}
		}
	}

	/**
	 * 	Test Row Count
	 *	@return true if display
	 */
	protected boolean testCount()
	{
		long start = System.currentTimeMillis();
		String dynWhere = getSQLWhere();
		StringBuilder sql = new StringBuilder (m_sqlMain);

		if (dynWhere.length() > 0)
			sql.append(dynWhere);   //  includes first AND

		String countSql = Msg.parseTranslation(Env.getCtx(), sql.toString());	//	Variables
		if (countSql.trim().endsWith("WHERE")) {
			countSql = countSql.trim();
			countSql = countSql.substring(0, countSql.length() - 5);
		}
		countSql = MRole.getDefault().addAccessSQL	(countSql, getTableName(),
													MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO);

		countSql = "SELECT COUNT(*) FROM ( " + countSql + " ) a";

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
		}

		if (log.isLoggable(Level.FINE))
			log.fine("#" + m_count + " - " + (System.currentTimeMillis()-start) + "ms");

		int maxQueryRecords = MRole.getDefault().getMaxQueryRecords();//JPIERE-6 Modify InfoWindow#testCount()
		if(maxQueryRecords > 1 )
		{
			if(m_count > maxQueryRecords)
			{
				FDialog.error(p_WindowNo, this, "FindOverMax", m_count + " > " + maxQueryRecords);
				return false;
			}
		}//JPiere-6 Finish

		return true;
	}	//	testCount

	/** Return true if there is an 'IsActive' criteria */
	boolean hasIsActiveEditor() {
		for (WEditor editor : editors) {
			if (editor.getGridField() != null && "IsActive".equals(editor.getGridField().getColumnName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @author xolali IDEMPIERE-1045
	 * getInfoColumnslayout(MInfoWindow info)
	 */
	public ArrayList<ColumnInfo> getInfoColumnslayout(MInfoWindow info){

		AccessSqlParser sqlParser = new AccessSqlParser("SELECT * FROM " + info.getFromClause());
		TableInfo[] tableInfos = sqlParser.getTableInfo(0);

		MInfoColumn[] infoColumns = info.getInfoColumns(tableInfos);
		ArrayList<ColumnInfo> list = new ArrayList<ColumnInfo>();
		String keyTableAlias = tableInfos[0].getSynonym() != null && tableInfos[0].getSynonym().trim().length() > 0
				? tableInfos[0].getSynonym()
						: tableInfos[0].getTableName();

				String keySelectClause = keyTableAlias + "." + p_keyColumn;

				for (MInfoColumn infoColumn : infoColumns)
				{
					if (infoColumn.isDisplayed(infoContext, p_WindowNo))
					{
						ColumnInfo columnInfo = null;
						if (infoColumn.getAD_Reference_ID() == DisplayType.ID)
						{
							if (infoColumn.getSelectClause().equalsIgnoreCase(keySelectClause))
								continue;

							columnInfo = new ColumnInfo(infoColumn.get_Translation("Name"), infoColumn.getSelectClause(), DisplayType.getClass(infoColumn.getAD_Reference_ID(), true));
						}
						else if (DisplayType.isLookup(infoColumn.getAD_Reference_ID()))
						{
							if (infoColumn.getAD_Reference_ID() == DisplayType.List)
							{
								columnInfo = new ColumnInfo(infoColumn.get_Translation("Name"), infoColumn.getSelectClause(), ValueNamePair.class, (String)null);
							}
							else
							{
								GridField field = getGridField(infoColumn);
								columnInfo = createLookupColumnInfo(tableInfos, field, infoColumn);
							}
						}
						else
						{
							columnInfo = new ColumnInfo(infoColumn.get_Translation("Name"), infoColumn.getSelectClause(), DisplayType.getClass(infoColumn.getAD_Reference_ID(), true));
						}
						columnInfo.setColDescription(infoColumn.get_Translation("Description"));
						columnInfo.setGridField(getGridField(infoColumn));
						list.add(columnInfo);
					}

				}

				return   list;
	}

	/**
	 * @author xolali IDEMPIERE-1045
	 * refresh(Object obj, EmbedWinInfo relatedInfo)
	 */
	private void refresh(Object obj, EmbedWinInfo relatedInfo)
	{
		StringBuilder sql = new StringBuilder();
		sql.append(relatedInfo.getInfoSql()); // delete get sql method from MInfoWindow
		if (log.isLoggable(Level.FINEST))
			log.finest(sql.toString());
		IDColumn ID = (IDColumn) obj;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(relatedInfo.getInfoSql(), null);
			pstmt.setObject(1, ID.getRecord_ID());
			rs = pstmt.executeQuery();
			loadEmbedded(rs, relatedInfo);
		}
		catch (Exception e)
		{
			log.log(Level.WARNING, sql.toString(), e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
	}	//	refresh

	/**
	 * @author xolali IDEMPIERE-1045
	 * loadEmbedded(ResultSet rs, EmbedWinInfo info)
	 */
	public void loadEmbedded(ResultSet rs, EmbedWinInfo info) throws SQLException{

		ListModelTable model;
		ArrayList<ColumnInfo> list = new ArrayList<ColumnInfo>();
		list = getInfoColumnslayout(info.getInfowin());

		//  Convert ArrayList to Array
		ColumnInfo[] s_layoutEmbedded  = new ColumnInfo[list.size()];
		list.toArray(s_layoutEmbedded);
		List<Object> data = new ArrayList<Object>();
		ArrayList<Object> lines =  new ArrayList<Object>();

		while (rs.next())
		{
			try {
				data = readData(rs, s_layoutEmbedded);
			} catch (SQLException e) {
				//Xolali - Auto-generated catch block
				e.printStackTrace();
			}
			lines.add(data);
		}
		model = new ListModelTable(lines);

		WListbox content = (WListbox) info.getInfoTbl();
		content.setData(model, null);
	}

	/**
	 * @author xolali IDEMPIERE-1045
	 * GridField getGridField(MInfoColumn infoColumn)
	 */
	private GridField getGridField(MInfoColumn infoColumn){
		String columnName = infoColumn.getColumnName();
		GridFieldVO vo = GridFieldVO.createParameter(infoContext, p_WindowNo, 0,
				columnName, infoColumn.get_Translation("Name"), infoColumn.getAD_Reference_ID(),
				infoColumn.getAD_Reference_Value_ID(), false, false);
		if (infoColumn.getAD_Val_Rule_ID() > 0) {
			vo.ValidationCode = infoColumn.getAD_Val_Rule().getCode();
			if (vo.lookupInfo != null) {
				vo.lookupInfo.ValidationCode = vo.ValidationCode;
				vo.lookupInfo.IsValidated = false;
			}
		}
		vo.DisplayLogic = infoColumn.getDisplayLogic() != null ? infoColumn.getDisplayLogic() : "";
		String desc = infoColumn.get_Translation("Description");
		vo.Description = desc != null ? desc : "";
		String help = infoColumn.get_Translation("Help");
		vo.Help = help != null ? help : "";
		GridField gridField = new GridField(vo);

		return gridField;
	}

	private  ArrayList<Object> readData(ResultSet rs, ColumnInfo[] p_layout) throws SQLException {

		int colOffset = 1;  //  columns start with 1
		ArrayList<Object> data = new ArrayList<Object>();
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
						WEditor editor = editorMap.get(p_layout[col].getColSQL()); // rework this, it will fail
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

		return data;
	}

}
