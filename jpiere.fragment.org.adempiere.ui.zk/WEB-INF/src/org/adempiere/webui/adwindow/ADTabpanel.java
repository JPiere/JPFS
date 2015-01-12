/******************************************************************************
 * Product: JPiere(ジェイピエール) - JPiere Base Plugin                       *
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

package org.adempiere.webui.adwindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.servlet.ServletRequest;				//JPIERE-2 Import ServletRequest to ADTabpanel

import org.adempiere.base.Core;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.webui.AdempiereIdGenerator;
import org.adempiere.webui.AdempiereWebUI;
import org.adempiere.webui.LayoutUtils;
import org.adempiere.webui.apps.AEnv;				//JPIERE-2 Import AEnv to ADTabpanel
import org.adempiere.webui.component.Borderlayout;
import org.adempiere.webui.component.Column;
import org.adempiere.webui.component.Columns;
import org.adempiere.webui.component.EditorBox;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.Group;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.NumberBox;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.SimpleTreeModel;
import org.adempiere.webui.component.Urlbox;
import org.adempiere.webui.editor.IZoomableEditor;
import org.adempiere.webui.editor.WButtonEditor;
import org.adempiere.webui.editor.WEditor;
import org.adempiere.webui.editor.WEditorPopupMenu;
import org.adempiere.webui.editor.WImageEditor;
import org.adempiere.webui.editor.WPaymentEditor;
import org.adempiere.webui.editor.WebEditorFactory;
import org.adempiere.webui.event.ContextMenuListener;
import org.adempiere.webui.panel.HelpController;
import org.adempiere.webui.util.GridTabDataBinder;
import org.adempiere.webui.util.TreeUtils;
import org.adempiere.webui.window.FDialog;
import org.compiere.model.DataStatusEvent;
import org.compiere.model.DataStatusListener;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.GridWindow;
import org.compiere.model.I_AD_Preference;
import org.compiere.model.MLookup;
import org.compiere.model.MPreference;
import org.compiere.model.MRole;
import org.compiere.model.MTab;
import org.compiere.model.MTable;
import org.compiere.model.MToolBarButton;
import org.compiere.model.MToolBarButtonRestrict;
import org.compiere.model.MTree;
import org.compiere.model.MTreeNode;
import org.compiere.model.Query;
import org.compiere.model.X_AD_FieldGroup;
import org.compiere.model.X_AD_ToolBarButton;
import org.compiere.util.CLogger;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Evaluatee;
import org.compiere.util.Msg;
import org.compiere.util.Util;
import org.zkoss.web.servlet.Servlets;				//JPIERE-2 Import Servlet to ADTabpanel
import org.zkoss.zk.au.out.AuFocus;
import org.zkoss.zk.au.out.AuScript;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zk.ui.event.SwipeEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Center;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.Div;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Separator;
import org.zkoss.zul.South;
import org.zkoss.zul.Space;
import org.zkoss.zul.Style;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.West;
import org.zkoss.zul.impl.XulElement;


/**
 *
 * This class is based on org.compiere.grid.GridController written by Jorg Janke.
 * Changes have been brought for UI compatibility.
 *
 * @author Jorg Janke
 *
 * @author <a href="mailto:agramdass@gmail.com">Ashley G Ramdass</a>
 * @date Feb 25, 2007
 * @version $Revision: 0.10 $
 *
 * @author Low Heng Sin
 */
public class ADTabpanel extends Div implements Evaluatee, EventListener<Event>,
DataStatusListener, IADTabpanel, IdSpace, IFieldEditorContainer
{
	/**
	 *
	 */
	private static final long serialVersionUID = 2592856355985389339L;

	private static final String ON_SAVE_OPEN_PREFERENCE_EVENT = "onSaveOpenPreference";

	public static final String ON_POST_INIT_EVENT = "onPostInit";

	public static final String ON_SWITCH_VIEW_EVENT = "onSwitchView";

	public static final String ON_DYNAMIC_DISPLAY_EVENT = "onDynamicDisplay";
	private static final String ON_DEFER_SET_SELECTED_NODE = "onDeferSetSelectedNode";

	private static final String ON_DEFER_SET_SELECTED_NODE_ATTR = "onDeferSetSelectedNode.Event.Posted";

	private static final CLogger logger;

    static
    {
        logger = CLogger.getCLogger(ADTabpanel.class);
    }

    private GridTab           gridTab;

    @SuppressWarnings("unused")
	private GridWindow        gridWindow;

    private AbstractADWindowContent      windowPanel;

    private int               windowNo;

    private Grid              form;

    private ArrayList<WEditor> editors = new ArrayList<WEditor>();

    private ArrayList<Component> editorComps = new ArrayList<Component>();

    private ArrayList<WButtonEditor> toolbarButtonEditors = new ArrayList<WButtonEditor>();

    private ArrayList<ToolbarProcessButton> toolbarProcessButtons = new ArrayList<ToolbarProcessButton>();

    private boolean			  uiCreated = false;

    private GridView		  listPanel;

    private Map<String, List<Row>> fieldGroupContents = new HashMap<String, List<Row>>();

    private Map<String, List<org.zkoss.zul.Row>> fieldGroupHeaders = new HashMap<String, List<org.zkoss.zul.Row>>();

	private ArrayList<Row> rowList;

	List<Group> allCollapsibleGroups = new ArrayList<Group>();

	private Borderlayout formContainer = null;

	private ADTreePanel treePanel = null;

	private GridTabDataBinder dataBinder;

	protected boolean activated = false;

	private Group currentGroup;

	private DetailPane detailPane;

	private boolean detailPaneMode;

	private int tabNo;

	/** DefaultFocusField		*/
	private WEditor	defaultFocusField = null;

	public static final String ON_TOGGLE_EVENT = "onToggle";

	private static enum SouthEvent {
    	SLIDE(),
    	OPEN(),
    	CLOSE();

		private SouthEvent() {}
    }

	public ADTabpanel()
	{
        init();
    }

    private void init()
    {
        initComponents();
        addEventListener(ON_DEFER_SET_SELECTED_NODE, this);
        addEventListener(WPaymentEditor.ON_SAVE_PAYMENT, this);

        addEventListener(ON_ACTIVATE_EVENT, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				removeAttribute(ATTR_ON_ACTIVATE_POSTED);
			}
		});
        addEventListener(ON_POST_INIT_EVENT, this);
        addEventListener(ON_SAVE_OPEN_PREFERENCE_EVENT, this);
    }

    private void initComponents()
    {
    	LayoutUtils.addSclass("adtab-content", this);

    	this.setWidth("100%");

        form = new Grid();
        form.setHflex("1");
        form.setHeight(null);
        form.setVflex(false);
        form.setSclass("grid-layout adwindow-form");
        form.setWidgetAttribute(AdempiereWebUI.WIDGET_INSTANCE_NAME, "form");

        listPanel = new GridView();
        listPanel.getListbox().addEventListener(Events.ON_DOUBLE_CLICK, this);
    }

    @Override
    public void setDetailPane(DetailPane component) {
		detailPane = component;

		Borderlayout borderLayout = (Borderlayout) formContainer;
		South south = borderLayout.getSouth();
		if (south == null) {
			south = new South();
			borderLayout.appendChild(south);
			south.setWidgetOverride("doClick_", "function (evt){this.$supers('doClick_', arguments);" +
					"var target = evt.domTarget;if (!target.id) target = target.parentNode;" +
					"if(this.$n('colled') == target) {" +
					"var se = new zk.Event(this, 'onSlide', null, {toServer: true}); zAu.send(se); } }");
			south.addEventListener(Events.ON_OPEN, this);
			south.addEventListener("onSlide", this);

			south.addEventListener(Events.ON_SWIPE, new EventListener<SwipeEvent>() {

				@Override
				public void onEvent(SwipeEvent event) throws Exception {
					if ("down".equals(event.getSwipeDirection())) {
						Borderlayout borderLayout = (Borderlayout) formContainer;
						South south = borderLayout.getSouth();
						if (south.isOpen()) {
							south.setOpen(false);
							onSouthEvent(SouthEvent.CLOSE);
						}
					}
				}
			});
		}
		south.appendChild(component);

		south.setVisible(true);
		south.setCollapsible(true);
		south.setSplittable(true);
		south.setOpen(isOpenDetailPane());
		south.setSclass("adwindow-gridview-detail");
    }

    @Override
    public DetailPane getDetailPane() {
    	return detailPane;
    }

    /**
     *
     * @param winPanel
     * @param windowNo
     * @param gridTab
     * @param gridWindow
     */
    public void init(AbstractADWindowContent winPanel, int windowNo, GridTab gridTab,
            GridWindow gridWindow)
    {
        this.windowNo = windowNo;
        this.gridWindow = gridWindow;
        this.gridTab = gridTab;
        this.windowPanel = winPanel;
        gridTab.addDataStatusListener(this);
        this.dataBinder = new GridTabDataBinder(gridTab);

        this.getChildren().clear();

        setId(AdempiereIdGenerator.escapeId(gridTab.getName()));

        int AD_Tree_ID = 0;
		if (gridTab.isTreeTab())
			AD_Tree_ID = MTree.getDefaultAD_Tree_ID (
				Env.getAD_Client_ID(Env.getCtx()), gridTab.getKeyColumnName());

		StringBuilder cssContent = new StringBuilder();
		cssContent.append(".adtab-form-borderlayout .z-south-colpsd:before { ");
		cssContent.append("content: \"");
		cssContent.append(Util.cleanAmp(Msg.getMsg(Env.getCtx(), "Detail")));
		cssContent.append("\"; ");
		cssContent.append("position: relative; font-size: 12px; font-weight: bold; ");
		cssContent.append("top: 3px; ");
		cssContent.append("left: 4px; ");
		cssContent.append("z-index: -1; ");
		cssContent.append("} ");
		Style style = new Style();
		style.setContent(cssContent.toString());
		appendChild(style);

		if (gridTab.isTreeTab() && AD_Tree_ID != 0)
		{
			Borderlayout layout = new Borderlayout();
			layout.setParent(this);
			layout.setSclass("adtab-form-borderlayout");

			treePanel = new ADTreePanel(windowNo, gridTab.getTabNo());
			West west = new West();
			west.appendChild(treePanel);
			west.setWidth("300px");
			west.setCollapsible(true);
			west.setSplittable(true);
			west.setAutoscroll(true);
			layout.appendChild(west);

			Center center = new Center();
			Vlayout div = new Vlayout();
			div.appendChild(form);
			center.appendChild(div);
			div.setVflex("1");
			div.setHflex("1");
			div.setSclass("adtab-form");
			div.setStyle("overflow-y: visible;");
			div.setSpacing("0px");
			layout.appendChild(center);

			formContainer = layout;
			treePanel.getTree().addEventListener(Events.ON_SELECT, this);

		}
		else
		{
			Vlayout div = new Vlayout();
			div.setSclass("adtab-form");
			div.appendChild(form);
												//JPIERE-2 Modify ADTabpanel#init() by Hideaki Hagiwara
			if ( AEnv.isInternetExplorer() &&
					(Servlets.getBrowser((ServletRequest) Executions.getCurrent().getNativeRequest(), "ie").intValue() < 9))
			{
				div.setStyle("overflow-y: scroll;");
			}else{
				div.setStyle("overflow-y: visible;");
			}									//JPiere-2 Finish
			div.setVflex("1");
			div.setWidth("100%");
			div.setSpacing("0px");

			Borderlayout layout = new Borderlayout();
			layout.setParent(this);
			layout.setSclass("adtab-form-borderlayout");

			Center center = new Center();
			layout.appendChild(center);
			center.appendChild(div);
			formContainer = layout;
		}

		form.getParent().appendChild(listPanel);
        listPanel.setVisible(false);
        listPanel.setWindowNo(windowNo);
        listPanel.setADWindowPanel(winPanel);

    }

	/**
     * Create UI components if not already created
     */
    @Override
    public void createUI()
    {
    	if (uiCreated) return;

    	uiCreated = true;

    	int numCols=gridTab.getNumColumns();
    	if (numCols <= 0) {
    		numCols=4;
    	}

    	// set size in percentage per column leaving a MARGIN on right
    	Columns columns = new Columns();
    	form.appendChild(columns);
    	int equalWidth = 98 / numCols;

    	for (int h=0;h<numCols;h++){
    		Column col = new Column();
    		col.setWidth(equalWidth + "%");
    		columns.appendChild(col);
    	}

    	Rows rows = form.newRows();
        GridField fields[] = gridTab.getFields();
        Row row = new Row();
        int actualxpos = 0;

        String currentFieldGroup = null;
        for (int i = 0; i < fields.length; i++)
        {
        	GridField field = fields[i];
        	if (!field.isDisplayed())
        		continue;

        	if (field.isToolbarButton()) {
        		WButtonEditor editor = (WButtonEditor) WebEditorFactory.getEditor(gridTab, field, false);

        		if (editor != null) {
        			if (windowPanel != null)
    					editor.addActionListener(windowPanel);
        			editor.setGridTab(this.getGridTab());
        			editor.setADTabpanel(this);
        			field.addPropertyChangeListener(editor);
        			editors.add(editor);
        			editor.getComponent().setId(field.getColumnName());
        			toolbarButtonEditors.add(editor);

        			continue;
        		}
        	}

        	// field group
        	String fieldGroup = field.getFieldGroup();
        	if (!Util.isEmpty(fieldGroup) && !fieldGroup.equals(currentFieldGroup)) // group changed
        	{
        		currentFieldGroup = fieldGroup;

        		if (numCols - actualxpos + 1 > 0)
        			row.appendCellChild(createSpacer(), numCols - actualxpos + 1);
        		row.setGroup(currentGroup);
        		rows.appendChild(row);
                if (rowList != null)
        			rowList.add(row);

        		List<org.zkoss.zul.Row> headerRows = new ArrayList<org.zkoss.zul.Row>();
        		fieldGroupHeaders.put(fieldGroup, headerRows);

        		rowList = new ArrayList<Row>();
        		fieldGroupContents.put(fieldGroup, rowList);

        		if (X_AD_FieldGroup.FIELDGROUPTYPE_Label.equals(field.getFieldGroupType()))
        		{
        			row = new Row();
        			Label groupLabel = new Label(fieldGroup);
        			row.appendCellChild(groupLabel, numCols);
        			rows.appendChild(row);
        			headerRows.add(row);

        			row = new Row();
        			Separator separator = new Separator();
        			separator.setBar(true);
        			row.appendCellChild(separator, numCols);
        			rows.appendChild(row);
        			headerRows.add(row);
        			currentGroup = null;
        		}
        		else
        		{
        			Group rowg = new Group(fieldGroup);
        			Cell cell = (Cell) rowg.getFirstChild();
        			cell.setSclass("z-group-inner");
        			cell.setColspan(numCols+1);
//        			rowg.appendChild(cell);

    				allCollapsibleGroups.add(rowg);
        			if (X_AD_FieldGroup.FIELDGROUPTYPE_Tab.equals(field.getFieldGroupType()) || field.getIsCollapsedByDefault())
        			{
        				rowg.setOpen(false);
        			}
        			currentGroup = rowg;
        			rows.appendChild(rowg);
        			headerRows.add(rowg);
        		}

        		row = new Row();
        		actualxpos = 0;
        	}

			//normal field
        	if (field.getXPosition() <= actualxpos) {
        		// Fill right part of the row with spacers until number of columns
        		if (numCols - actualxpos + 1 > 0)
        			row.appendCellChild(createSpacer(), numCols - actualxpos + 1);
        		row.setGroup(currentGroup);
        		rows.appendChild(row);
                if (rowList != null)
        			rowList.add(row);
        		row=new Row();
        		actualxpos = 0;
        	}
    		// Fill left part of the field
        	if (field.getXPosition()-1 - actualxpos > 0)
        		row.appendCellChild(createSpacer(), field.getXPosition()-1 - actualxpos);
        	boolean paintLabel = ! (field.getDisplayType() == DisplayType.Button || field.getDisplayType() == DisplayType.YesNo || field.isFieldOnly());
        	if (field.isHeading())
        		actualxpos = field.getXPosition();
        	else
        		actualxpos = field.getXPosition() + field.getColumnSpan()-1 + (paintLabel ? 1 : 0);

        	if (! field.isHeading()) {

        		WEditor editor = WebEditorFactory.getEditor(gridTab, field, false);

        		if (editor != null) // Not heading
        		{
        			editor.getComponent().setWidgetOverride("fieldHeader", HelpController.escapeJavascriptContent(field.getHeader()));
        			editor.getComponent().setWidgetOverride("fieldDescription", HelpController.escapeJavascriptContent(field.getDescription()));
        			editor.getComponent().setWidgetOverride("fieldHelp", HelpController.escapeJavascriptContent(field.getHelp()));
        			editor.getComponent().setWidgetListener("onFocus", "zWatch.fire('onFieldTooltip', this, null, this.fieldHeader(), this.fieldDescription(), this.fieldHelp());");

        			editor.setGridTab(this.getGridTab());
        			field.addPropertyChangeListener(editor);
        			editors.add(editor);
        			editorComps.add(editor.getComponent());
        			if (paintLabel) {
        				Div div = new Div();
        				div.setSclass("form-label");
        				Label label = editor.getLabel();
        				div.appendChild(label);
        				if (label.getDecorator() != null)
        					div.appendChild(label.getDecorator());
        				row.appendCellChild(div,1);
        			}
        			row.appendCellChild(editor.getComponent(), field.getColumnSpan());

        			if (editor instanceof WButtonEditor)
        			{
        				if (windowPanel != null)
        					((WButtonEditor)editor).addActionListener(windowPanel);
        			}
        			else
        			{
        				editor.addValueChangeListener(dataBinder);
        			}

        			//	Default Focus
        			if (defaultFocusField == null && field.isDefaultFocus())
        				defaultFocusField = editor;

        			//stretch component to fill grid cell
        			editor.fillHorizontal();

        			Component fellow = editor.getComponent().getFellowIfAny(field.getColumnName());
        			if (fellow == null) {
        				editor.getComponent().setId(field.getColumnName());
        			}

        			//setup editor context menu
        			WEditorPopupMenu popupMenu = editor.getPopupMenu();
        			if (popupMenu != null)
        			{
        				popupMenu.addMenuListener((ContextMenuListener)editor);
        				popupMenu.setId(field.getColumnName()+"-popup");
        				this.appendChild(popupMenu);
        				if (!field.isFieldOnly())
        				{
        					Label label = editor.getLabel();
        					if (popupMenu.isZoomEnabled() && editor instanceof IZoomableEditor)
        					{
        						label.addEventListener(Events.ON_CLICK, new ZoomListener((IZoomableEditor) editor));
        					}

        					popupMenu.addContextElement(label);
        					if (editor.getComponent() instanceof XulElement)
        					{
        						popupMenu.addContextElement((XulElement) editor.getComponent());
        					}
        				}
        			}
        		}
        	}
        	else // just heading
        	{
        		//display just a label if we are "heading only"
        		Label label = new Label(field.getHeader());
        		Div div = new Div();
        		div.setSclass("form-label-heading");
        		row.appendCellChild(createSpacer());
        		div.appendChild(label);
        		row.appendCellChild(div);
        	}
        }

		if (numCols - actualxpos + 1 > 0)
			row.appendCellChild(createSpacer(), numCols - actualxpos + 1);
		row.setGroup(currentGroup);
		rows.appendChild(row);
        if (rowList != null)
			rowList.add(row);

        loadToolbarButtons();

        //create tree
        if (gridTab.isTreeTab() && treePanel != null) {
        	int AD_Tree_ID = Env.getContextAsInt (Env.getCtx(), getWindowNo(), "AD_Tree_ID", true);
        	int AD_Tree_ID_Default = MTree.getDefaultAD_Tree_ID (Env.getAD_Client_ID(Env.getCtx()), gridTab.getKeyColumnName());

    		if (AD_Tree_ID != 0) {
    			treePanel.initTree(AD_Tree_ID, windowNo);
    			Events.echoEvent(ON_DEFER_SET_SELECTED_NODE, this, null);
    		} else if (AD_Tree_ID_Default != 0) {
    			treePanel.initTree(AD_Tree_ID_Default, windowNo);
    			Events.echoEvent(ON_DEFER_SET_SELECTED_NODE, this, null);
    		}
        }

        if (!gridTab.isSingleRow() && !isGridView())
        	switchRowPresentation();
    }

	private void loadToolbarButtons() {
		//get extra toolbar process buttons
        MToolBarButton[] mToolbarButtons = MToolBarButton.getProcessButtonOfTab(gridTab.getAD_Tab_ID(), null);
        for(MToolBarButton mToolbarButton : mToolbarButtons) {
           	Boolean access = MRole.getDefault().getProcessAccess(mToolbarButton.getAD_Process_ID());
        	if (access != null && access.booleanValue()) {
        		ToolbarProcessButton toolbarProcessButton = new ToolbarProcessButton(mToolbarButton, this, windowPanel, windowNo);
        		toolbarProcessButtons.add(toolbarProcessButton);
        	}
        }

        if (toolbarProcessButtons.size() > 0) {
        	int ids[] = MToolBarButtonRestrict.getProcessButtonOfTab(Env.getCtx(), Env.getAD_Role_ID(Env.getCtx()), gridTab.getAD_Tab_ID(), null);
        	if (ids != null && ids.length > 0) {
        		for(int id : ids) {
        			X_AD_ToolBarButton tbt = new X_AD_ToolBarButton(Env.getCtx(), id, null);
        			for(ToolbarProcessButton btn : toolbarProcessButtons) {
        				if (tbt.getComponentName().equals(btn.getColumnName())) {
        					toolbarProcessButtons.remove(btn);
        					break;
        				}
        			}
        		}
        	}
        }
	}

	private Component createSpacer() {
		return new Space();
	}

	/**
	 * Validate display properties of fields of current row.
	 * @param col
	 */
	@Override
	public void dynamicDisplay (int col)
	{
        if (!gridTab.isOpen())
        {
            return;
        }

    	List<Group> collapsedGroups = new ArrayList<Group>();
    	for (Group group : allCollapsibleGroups) {
    		if (! group.isOpen())
    			collapsedGroups.add(group);
    	}

        //  Selective
        if (col > 0)
        {
            GridField changedField = gridTab.getField(col);
            String columnName = changedField.getColumnName();
            ArrayList<GridField> dependants = gridTab.getDependantFields(columnName);
            if (logger.isLoggable(Level.CONFIG)) logger.config("(" + gridTab.toString() + ") "
            			+ columnName + " - Dependents=" + dependants.size());
			if ( ! (   dependants.size() > 0
					|| changedField.getCallout().length() > 0
					|| Core.findCallout(gridTab.getTableName(), columnName).size() > 0))
			{
				for (WEditor comp : editors)
				{
					comp.updateLabelStyle();
				}
                return;
            }
        }

        boolean noData = gridTab.getRowCount() == 0;
        if (logger.isLoggable(Level.CONFIG)) logger.config(gridTab.toString() + " - Rows=" + gridTab.getRowCount());

        for (WEditor comp : editors)
        {
            GridField mField = comp.getGridField();
            if (mField != null)
            {
                if (mField.isDisplayed(true))       //  check context
                {
                    if (!comp.isVisible())
                    {
                        comp.setVisible(true);      //  visibility
                    }
                    if (noData)
                    {
                        comp.setReadWrite(false);
                    }
                    else
                    {
                    	comp.dynamicDisplay();
                        boolean rw = mField.isEditable(true);   //  r/w - check Context
                        comp.setReadWrite(rw);
                        comp.setMandatory(mField.isMandatory(true));    //  check context
                    }
                }
                else if (comp.isVisible())
                {
                    comp.setVisible(false);
                }
            }
            comp.updateLabelStyle();
        }   //  all components

        //hide row if all editor within the row is invisible
        List<Component> rows = form.getRows().getChildren();
        for (Component comp : rows)
        {
        	if (comp instanceof Row) {
            	Row row = (Row) comp;
            	boolean visible = false;
            	boolean editorRow = false;
            	for (Component cellComponent : row.getChildren())
            	{
            		Component component = cellComponent.getFirstChild();
            		if (editorComps.contains(component))
            		{
            			editorRow = true;
            			// open the group if there is a mandatory unfilled field
            			WEditor editor = editors.get(editorComps.indexOf(component));
            			if (editor != null
            					&& row.getGroup() != null
            					&& ! row.getGroup().isOpen()
            					&& editor.isMandatoryStyle()) {
            				row.getGroup().setOpen(true);
            				if (collapsedGroups.contains(row.getGroup())) {
            					collapsedGroups.remove(row.getGroup());
            				}
            			}
            			if (component.isVisible())
            			{
            				visible = true;
            				break;
            			}
            		}
            	}
            	if (editorRow && (row.isVisible() != visible))
            	{
            		row.setAttribute(Group.GROUP_ROW_VISIBLE_KEY, visible ? "true" : "false");
            		row.setVisible(visible);
            	}
        	}
        }

        //hide fieldgroup if all editor row within the fieldgroup is invisible
        for(Iterator<Entry<String, List<org.zkoss.zul.Row>>> i = fieldGroupHeaders.entrySet().iterator(); i.hasNext();)
        {
        	Map.Entry<String, List<org.zkoss.zul.Row>> entry = i.next();
        	List<Row> contents = fieldGroupContents.get(entry.getKey());
        	boolean visible = false;
        	for (Row row : contents)
        	{
        		if (row.isVisible())
        		{
        			visible = true;
        			break;
        		}
        	}
        	List<org.zkoss.zul.Row> headers = entry.getValue();
        	for(org.zkoss.zul.Row row : headers)
        	{
        		if (row.isVisible() != visible)
        			row.setVisible(visible);
        	}
        }

        // collapse the groups closed
        for (Group group : collapsedGroups) {
        	group.setOpen(false);
        }

        if (listPanel.isVisible()) {
        	listPanel.dynamicDisplay(col);
        }

        for (ToolbarProcessButton btn : toolbarProcessButtons) {
        	btn.dynamicDisplay();
        }

        Events.sendEvent(this, new Event(ON_DYNAMIC_DISPLAY_EVENT, this));
        echoDeferSetSelectedNodeEvent();
        if (logger.isLoggable(Level.CONFIG)) logger.config(gridTab.toString() + " - fini - " + (col<=0 ? "complete" : "seletive"));
    }   //  dynamicDisplay

	private void echoDeferSetSelectedNodeEvent() {
		if (getAttribute(ON_DEFER_SET_SELECTED_NODE_ATTR) == null) {
        	setAttribute(ON_DEFER_SET_SELECTED_NODE_ATTR, Boolean.TRUE);
        	Events.echoEvent(ON_DEFER_SET_SELECTED_NODE, this, null);
        }
	}

    /**
     * @return String
     */
    @Override
    public String getDisplayLogic()
    {
        return gridTab.getDisplayLogic();
    }

    /**
     * @return String
     */
    @Override
    public String getTitle()
    {
        return gridTab.getName();
    } // getTitle

    /**
     * @param variableName
     */
    @Override
    public String get_ValueAsString(String variableName)
    {
        return Env.getContext(Env.getCtx(), windowNo, variableName);
    } // get_ValueAsString

    /**
     * @return The tab level of this Tabpanel
     */
    @Override
    public int getTabLevel()
    {
        return gridTab.getTabLevel();
    }

    /**
     * @return The tablename of this Tabpanel
     */
    @Override
    public String getTableName()
    {
        return gridTab.getTableName();
    }

    /**
     * @return The record ID of this Tabpanel
     */
    @Override
    public int getRecord_ID()
    {
        return gridTab.getRecord_ID();
    }

    /**
     * Is panel need refresh
     * @return boolean
     */
    @Override
    public boolean isCurrent()
    {
        return gridTab != null ? gridTab.isCurrent() : false;
    }

    /**
     *
     * @return windowNo
     */
    public int getWindowNo()
    {
        return windowNo;
    }

    /**
     * Retrieve from db
     */
    @Override
    public void query()
    {
    	boolean open = gridTab.isOpen();
        gridTab.query(false);
        if (listPanel.isVisible() && !open)
        	gridTab.getTableModel().fireTableDataChanged();
    }

    /**
     * Retrieve from db
     * @param onlyCurrentRows
     * @param onlyCurrentDays
     * @param maxRows
     */
    @Override
    public void query (boolean onlyCurrentRows, int onlyCurrentDays, int maxRows)
    {
    	boolean open = gridTab.isOpen();
        gridTab.query(onlyCurrentRows, onlyCurrentDays, maxRows);
        if (listPanel.isVisible() && !open)
        	gridTab.getTableModel().fireTableDataChanged();
    }

    /**
     * reset detail data grid for new parent record that's not saved yet
     */
    @Override
	public void resetDetailForNewParentRecord ()
    {
    	boolean open = gridTab.isOpen();
        if (open)
        {
        	gridTab.resetDetailForNewParentRecord();
        }
        else
        {
        	gridTab.setCurrentRow(-1, true);
        }
    }

    /**
     * @return GridTab
     */
    public GridTab getGridTab()
    {
        return gridTab;
    }

    /**
     * @return TreePanel
     */
    public ADTreePanel getTreePanel()
    {
    	return treePanel;
    }

    /**
     * @return TreePanel
     */
    public String getTreeDisplayedOn()
    {
    	return gridTab.getTreeDisplayedOn();
    }

    /**
     * Refresh current row
     */
    public void refresh()
    {
        gridTab.dataRefresh();
    }

    /**
     * Activate/deactivate panel
     * @param activate
     */
    public void activate(boolean activate)
    {
    	if (activate) {
	    	if (getAttribute(ATTR_ON_ACTIVATE_POSTED) != null) {
	    		return;
	    	}

	    	setAttribute(ATTR_ON_ACTIVATE_POSTED, Boolean.TRUE);
    	}

    	activated = activate;
        if (listPanel.isVisible()) {
        	if (activate)
        		listPanel.activate(gridTab);
        	else
        		listPanel.deactivate();
        } else {
        	if (activate) {
        		formContainer.setVisible(activate);
        		focusToFirstEditor();
        	}
        }

        if (gridTab.getRecord_ID() > 0 && gridTab.isTreeTab() && treePanel != null) {
        	echoDeferSetSelectedNodeEvent();
        }

        Event event = new Event(ON_ACTIVATE_EVENT, this, activate);
        Events.postEvent(event);
    }

    /**
	 * set focus to first active editor
	 */
    @Override
    public void focusToFirstEditor() {
    	focusToFirstEditor(false);
    }

    /**
     *
     * @param checkCurrent
     */
    public void focusToFirstEditor(boolean checkCurrent) {
		WEditor toFocus = null;

		if (defaultFocusField != null
				&& defaultFocusField.isVisible() && defaultFocusField.isReadWrite() && defaultFocusField.getComponent().getParent() != null
				&& !(defaultFocusField instanceof WImageEditor)) {
			toFocus = defaultFocusField;
		}
		else
		{
			for (WEditor editor : editors) {
				if (editor.isVisible() && editor.isReadWrite() && editor.getComponent().getParent() != null
					&& !(editor instanceof WImageEditor)) {
					toFocus = editor;
					break;
				}
			}
		}
		if (toFocus != null) {
			focusToEditor(toFocus, checkCurrent);
		}
	}

    /**
     * @param event
     * @see EventListener#onEvent(Event)
     */
    @SuppressWarnings("unchecked")
	public void onEvent(Event event)
    {
    	if (event.getTarget() == listPanel.getListbox())
    	{
    		Events.sendEvent(this, new Event(ON_TOGGLE_EVENT, this));
    	}
    	else if (treePanel != null && event.getTarget() == treePanel.getTree()) {
    		Treeitem item =  treePanel.getTree().getSelectedItem();
    		navigateTo((DefaultTreeNode<MTreeNode>)item.getValue());
    	}
    	else if (ON_DEFER_SET_SELECTED_NODE.equals(event.getName())) {
    		removeAttribute(ON_DEFER_SET_SELECTED_NODE_ATTR);
    		if (gridTab.getRecord_ID() >= 0 && gridTab.isTreeTab() && treePanel != null) {
            	setSelectedNode(gridTab.getRecord_ID());
            }
    	}
    	else if (WPaymentEditor.ON_SAVE_PAYMENT.equals(event.getName())) {
    		windowPanel.onSavePayment();
    	}
    	else if (ON_POST_INIT_EVENT.equals(event.getName())) {
    		if (isDetailVisible() && detailPane.getSelectedADTabpanel() != null) {
    			detailPane.getSelectedADTabpanel().activate(true);
    		}
    	}
    	else if (event.getTarget() instanceof South) {
    		if (detailPane != null) {
    			boolean openEvent = event instanceof OpenEvent;
    			if (openEvent) {
    				OpenEvent oe = (OpenEvent)event;
    				onSouthEvent(oe.isOpen() ? SouthEvent.OPEN : SouthEvent.CLOSE);
    			} else {
    				onSouthEvent(SouthEvent.SLIDE);
    			}
    		}
    	}
    	else if (event.getName().equals(ON_SAVE_OPEN_PREFERENCE_EVENT)) {
    		Boolean value = (Boolean) event.getData();
    		int windowId = getGridTab().getAD_Window_ID();
    		int adTabId = getGridTab().getAD_Tab_ID();
    		if (windowId > 0 && adTabId > 0) {
    			Query query = new Query(Env.getCtx(), MTable.get(Env.getCtx(), I_AD_Preference.Table_ID), "AD_Window_ID=? AND Attribute=?  AND AD_Process_ID IS NULL AND PreferenceFor = 'W'", null);
    			MPreference preference = query.setOnlyActiveRecords(true)
    										  .setApplyAccessFilter(true)
    										  .setParameters(windowId, adTabId+"|DetailPane.IsOpen")
    										  .first();
    			if (preference != null && preference.getAD_Preference_ID() > 0) {
    				preference.setValue(value ? "Y" : "N");
    			} else {
    				preference = new MPreference(Env.getCtx(), 0, null);
    				preference.setAD_Window_ID(windowId);
    				preference.setAttribute(adTabId+"|DetailPane.IsOpen");
    				preference.setValue(value ? "Y" : "N");
    			}
    			preference.saveEx();
    			//update current context
    			Env.getCtx().setProperty("P"+windowId+"|"+adTabId+"|DetailPane.IsOpen", value ? "Y" : "N");
    		}
    	}
    }

    private void onSouthEvent(SouthEvent event) {
    	if (event == SouthEvent.OPEN || event == SouthEvent.CLOSE) {
    		boolean open = event == SouthEvent.OPEN ? true : false;
    		Events.echoEvent(ON_SAVE_OPEN_PREFERENCE_EVENT, this, open);
    		if (!open)
    			return;
    	}

		if (detailPane.getParent() == null) {
			formContainer.appendSouth(detailPane);
		}
		IADTabpanel tabPanel = detailPane.getSelectedADTabpanel();
    	if (tabPanel != null) {
    		if (!tabPanel.isActivated()) {
    			tabPanel.activate(true);
    		}
	    	if (!tabPanel.isGridView()) {
	    		tabPanel.switchRowPresentation();
	    	}
    	}
    }

    private boolean isOpenDetailPane() {
    	boolean open = true;
    	int windowId = getGridTab().getAD_Window_ID();
		int adTabId = getGridTab().getAD_Tab_ID();
		if (windowId > 0 && adTabId > 0) {
			String preference = Env.getPreference(Env.getCtx(), windowId, adTabId+"|DetailPane.IsOpen", false);
			if (preference != null && preference.trim().length() > 0) {
				open = "Y".equals(preference);
			}
		}
    	return open;
    }

    private void navigateTo(DefaultTreeNode<MTreeNode> value) {
    	MTreeNode treeNode = value.getData();
    	//  We Have a TreeNode
		int nodeID = treeNode.getNode_ID();
		//  root of tree selected - ignore
		//if (nodeID == 0)
			//return;

		//  Search all rows for mode id
		int size = gridTab.getRowCount();
		int row = -1;
		for (int i = 0; i < size; i++)
		{
			if (gridTab.getKeyID(i) == nodeID)
			{
				row = i;
				break;
			}
		}
		if (row == -1)
		{
			if (nodeID > 0 && logger.isLoggable(Level.WARNING))
				logger.log(Level.WARNING, "Tab does not have ID with Node_ID=" + nodeID);
			if (gridTab.getCurrentRow() >= 0)
			{
				gridTab.setCurrentRow(gridTab.getCurrentRow(), true);
			}
			throw new AdempiereException(Msg.getMsg(Env.getCtx(),"RecordIsNotInCurrentSearch"));
		}

		//  Navigate to node row
		gridTab.navigate(row);
	}

    /**
     * @param e
     * @see DataStatusListener#dataStatusChanged(DataStatusEvent)
     */
	public void dataStatusChanged(DataStatusEvent e)
    {
    	//ignore background event
    	if (Executions.getCurrent() == null) return;

        int col = e.getChangedColumn();
        if (logger.isLoggable(Level.CONFIG)) logger.config("(" + gridTab + ") Col=" + col + ": " + e.toString());

        //  Process Callout
        GridField mField = gridTab.getField(col);
        if (mField != null
            && (mField.getCallout().length() > 0
            		|| (Core.findCallout(gridTab.getTableName(), mField.getColumnName())).size()>0
            		|| gridTab.hasDependants(mField.getColumnName())))
        {
            String msg = gridTab.processFieldChange(mField);     //  Dependencies & Callout
            if (msg.length() > 0)
            {
                FDialog.error(windowNo, this, msg);
            }

            // Refresh the list on dependant fields
    		for (GridField dependentField : gridTab.getDependantFields(mField.getColumnName()))
    		{
    			//  if the field has a lookup
    			if (dependentField != null && dependentField.getLookup() instanceof MLookup)
    			{
    				MLookup mLookup = (MLookup)dependentField.getLookup();
    				//  if the lookup is dynamic (i.e. contains this columnName as variable)
    				if (mLookup.getValidation().indexOf("@"+mField.getColumnName()+"@") != -1)
    				{
    					mLookup.refresh();
    				}
    			}
    		}   //  for all dependent fields

        }
        //if (col >= 0)
        if (!uiCreated)
        	createUI();
        dynamicDisplay(col);

        //sync tree
        if (treePanel != null)
        {
        	if (getTreeDisplayedOn().equals(MTab.TREEDISPLAYEDON_MasterTab))
        		treePanel.getParent().setVisible(!isDetailPaneMode());
        	else if (getTreeDisplayedOn().equals(MTab.TREEDISPLAYEDON_DetailTab))
        		treePanel.getParent().setVisible(isDetailPaneMode());

        	if ("Deleted".equalsIgnoreCase(e.getAD_Message()))
        	{
        		if (e.Record_ID != null && e.Record_ID instanceof Integer && ((Integer)e.Record_ID != gridTab.getRecord_ID()))
        			deleteNode((Integer)e.Record_ID);
        		else
        			setSelectedNode(gridTab.getRecord_ID());
        	}
        	else if (!e.isInserting())
        	{
        		boolean refresh=true;
        		Treeitem item = treePanel.getTree().getSelectedItem();
        		SimpleTreeModel model = (SimpleTreeModel)(TreeModel<?>) treePanel.getTree().getModel();
        		if (item != null)
        		{
        			@SuppressWarnings("unchecked")
					MTreeNode treeNode = ((DefaultTreeNode<MTreeNode>) item.getValue()).getData();
            		if (treeNode.getNode_ID() == gridTab.getRecord_ID()){
            			setSelectedNode(gridTab.getRecord_ID());
            			refresh = false;
            		}
				}
        		if ("Saved".equals(e.getAD_Message()) && model.find(null, gridTab.getRecord_ID())==null)
        		{
					addNewNode();
				}
        		if (refresh)
        		{
        			int AD_Tree_ID = Env.getContextAsInt (Env.getCtx(), getWindowNo(), "AD_Tree_ID", true);

    				if (AD_Tree_ID != 0)
    				{
            			if (treePanel.initTree(AD_Tree_ID, windowNo))
            				echoDeferSetSelectedNodeEvent();
            			else
            				setSelectedNode(gridTab.getRecord_ID());

            		}
    				else
    				{
    					AD_Tree_ID = MTree.getDefaultAD_Tree_ID (Env.getAD_Client_ID(Env.getCtx()), gridTab.getKeyColumnName());
    					treePanel.initTree(AD_Tree_ID, windowNo);
    				}

				}

        	}else if(e.isInserting() && gridTab.getRecord_ID() < 0 && gridTab.getTabLevel() > 0 )
        	{
    			int AD_Tree_ID = Integer.parseInt(gridTab.getParentTab().getValue("AD_Tree_ID").toString());
    			treePanel.initTree(AD_Tree_ID, windowNo);
    		}
        }
        if (listPanel.isVisible()) {
        	listPanel.updateListIndex();
        	listPanel.dynamicDisplay(col);
        }
    }

    private void deleteNode(int recordId) {
		if (recordId <= 0) return;

		SimpleTreeModel model = (SimpleTreeModel)(TreeModel<?>) treePanel.getTree().getModel();

		if (treePanel.getTree().getSelectedItem() != null) {
			DefaultTreeNode<Object> treeNode =  treePanel.getTree().getSelectedItem().getValue();
			MTreeNode data = (MTreeNode) treeNode.getData();
			if (data.getNode_ID() == recordId) {
				model.removeNode(treeNode);
				return;
			}
		}

		DefaultTreeNode<Object> treeNode = model.find(null, recordId);
		if (treeNode != null) {
			model.removeNode(treeNode);
		}
	}

	private void addNewNode() {
    	if (gridTab.getRecord_ID() > 0) {
	    	String name = (String)gridTab.getValue("Name");
			String description = (String)gridTab.getValue("Description");
			boolean summary = gridTab.getValueAsBoolean("IsSummary");
			String imageIndicator = (String)gridTab.getValue("Action");  //  Menu - Action
			//
			SimpleTreeModel model = (SimpleTreeModel)(TreeModel<?>) treePanel.getTree().getModel();
			DefaultTreeNode<Object> treeNode = model.getRoot();
			MTreeNode root = (MTreeNode) treeNode.getData();
			MTreeNode node = new MTreeNode (gridTab.getRecord_ID(), 0, name, description,
					root.getNode_ID(), summary, imageIndicator, false, null);
			DefaultTreeNode<Object> newNode = new DefaultTreeNode<Object>(node);
			model.addNode(newNode);
			int[] path = model.getPath(newNode);
			Treeitem ti = treePanel.getTree().renderItemByPath(path);
			treePanel.getTree().setSelectedItem(ti);
    	}
	}

	private void setSelectedNode(int recordId) {
		if (recordId <= 0) return;

		//force on init render
		if (TreeUtils.isOnInitRenderPosted(treePanel.getTree()) || treePanel.getTree().getTreechildren() == null
			|| treePanel.getTree().getTreechildren().getItemCount() == 0) {
			treePanel.getTree().onInitRender();
		}

		SimpleTreeModel model = (SimpleTreeModel)(TreeModel<?>) treePanel.getTree().getModel();
		if (treePanel.getTree().getSelectedItem() != null) {
			DefaultTreeNode<Object> treeNode = treePanel.getTree().getSelectedItem().getValue();
			MTreeNode data = (MTreeNode) treeNode.getData();
			if (data.getNode_ID() == recordId) {
				int[] path = model.getPath(treeNode);
				Treeitem ti = treePanel.getTree().renderItemByPath(path);
				if (ti.getPage() == null) {
					echoDeferSetSelectedNodeEvent();
				}

				boolean changed = false;
				if (Env.isBaseLanguage(Env.getCtx(), "AD_Menu")) {
					String name = (String) gridTab.getValue("Name");
					if (name != null && !name.equals(data.getName())) {
						data.setName(name);
						changed = true;
					}
				}

				Object summaryobj = gridTab.getValue("IsSummary");
				boolean summary = false;
				if (summaryobj != null) {
					if (summaryobj instanceof Boolean) {
						summary = ((Boolean)summaryobj).booleanValue();
					} else {
						summary = "Y".equals(summaryobj.toString());
					}
				}
				if (summary != data.isSummary()) {
					data.setSummary(summary);
					changed = true;
				}

				if (changed) {
					treeNode.setData(data);
				}

				return;
			}
		}

		DefaultTreeNode<Object> treeNode = model.find(null, recordId);
		if (treeNode != null) {
			int[] path = model.getPath(treeNode);
			Treeitem ti = treePanel.getTree().renderItemByPath(path);
			treePanel.getTree().selectItem(ti);
		} else {
			addNewNode();
		}
	}

	/**
	 * Toggle between form and grid view
	 */
	public void switchRowPresentation() {
		if (form.isVisible()) {
			form.setVisible(false);
			((HtmlBasedComponent)form.getParent()).setStyle("");
		} else {
			form.setVisible(true);
			((HtmlBasedComponent)form.getParent()).setStyle("overflow-y: visible;");
		}
		listPanel.setVisible(!form.isVisible());
		if (listPanel.isVisible()) {
			listPanel.refresh(gridTab);
			listPanel.scrollToCurrentRow();
			Clients.resize(listPanel);
		} else {
			listPanel.deactivate();
		}

		Events.sendEvent(this, new Event(ON_SWITCH_VIEW_EVENT, this));
	}

	static class ZoomListener implements EventListener<Event> {

		private IZoomableEditor searchEditor;

		ZoomListener(IZoomableEditor editor) {
			searchEditor = editor;
		}

		public void onEvent(Event event) throws Exception {
			if (Events.ON_CLICK.equals(event.getName())) {
				searchEditor.actionZoom();
			}

		}

	}

	/**
	 * @see IADTabpanel#afterSave(boolean)
	 */
	public void afterSave(boolean onSaveEvent) {
	}

	@Override
	public void focus() {
		if (form.isVisible())
			this.focusToFirstEditor(true);
		else
			listPanel.focus();
	}

	/**
	 *
	 * @param columnName
	 */
	public void setFocusToField(String columnName) {
		if (formContainer.isVisible()) {
			for (WEditor editor : editors) {
				if (columnName.equals(editor.getColumnName())) {
					Clients.response(new AuFocus(editor.getComponent()));
					break;
				}
			}
		} else {
			listPanel.setFocusToField(columnName);
		}
	}

	/**
	 * @see IADTabpanel#onEnterKey()
	 */
	public boolean onEnterKey() {
		if (listPanel.isVisible()) {
			return listPanel.onEnterKey();
		}
		return false;
	}

	/**
	 * @return boolean
	 */
	public boolean isGridView() {
		return listPanel.isVisible();
	}

	/**
	 *
	 * @return GridPanel
	 */
	@Override
	public GridView getGridView() {
		return listPanel;
	}

	@Override
	public boolean isActivated() {
		return activated;
	}

	@Override
	public void setDetailPaneMode(boolean detailPaneMode) {
		if (this.detailPaneMode != detailPaneMode) {
			this.detailPaneMode = detailPaneMode;
			if (detailPaneMode) {
				detachDetailPane();
			} else {
				attachDetailPane();
			}
			this.setVflex("true");
			listPanel.setDetailPaneMode(detailPaneMode);
		}
	}

	private void attachDetailPane() {
		if (formContainer.getSouth() != null) {
			formContainer.getSouth().setVisible(true);
			if (formContainer.getSouth().isOpen() && detailPane != null && detailPane.getParent() == null) {
				formContainer.appendSouth(detailPane);
			}
		}
	}

	private void detachDetailPane() {
		if (formContainer.getSouth() != null) {
			formContainer.getSouth().setVisible(false);
			if (detailPane != null && detailPane.getParent() != null) {
				detailPane.detach();
			}
		}
	}

	/**
	 * Get all visible button editors
	 * @return List<WButtonEditor>
	 */
	public List<Button> getToolbarButtons() {
		List<Button> buttonList = new ArrayList<Button>();
		for(WButtonEditor editor : toolbarButtonEditors) {
			if (editor.getComponent() != null
					&& editor.getComponent().isVisible()) {
				buttonList.add(editor.getComponent());
			}
		}

		for(ToolbarProcessButton processButton : toolbarProcessButtons) {
			if (processButton.getButton().isVisible()) {
				buttonList.add(processButton.getButton());
			}
		}
		return buttonList;
	}

	@Override
	public boolean needSave(boolean rowChange, boolean onlyRealChange) {
		return getGridTab().needSave(rowChange, onlyRealChange);
	}

	@Override
	public boolean dataSave(boolean onSaveEvent) {
		return getGridTab().dataSave(onSaveEvent);
	}

	@Override
	public boolean isDetailPaneMode() {
		return this.detailPaneMode;
	}

	@Override
	public void setTabNo(int tabNo) {
		this.tabNo = tabNo;
	}

	@Override
	public int getTabNo() {
		return tabNo;
	}

	/**
	 * activate current selected detail tab if it is visible
	 */
	public void activateDetailIfVisible() {
		if (isDetailVisible()) {
			IADTabpanel tabPanel = detailPane.getSelectedADTabpanel();
	    	if (tabPanel != null && !tabPanel.isActivated()) {
		    	tabPanel.activate(true);
		    	if (!tabPanel.isGridView()) {
		    		tabPanel.switchRowPresentation();
		    	}
	    	} else if (tabPanel != null && !tabPanel.getGridTab().isCurrent()) {
	    		tabPanel.activate(true);
	    	} else if (tabPanel != null && tabPanel.isGridView()) {
	    		//ensure row indicator is not lost
    			RowRenderer<Object[]> renderer = tabPanel.getGridView().getListbox().getRowRenderer();
    			GridTabRowRenderer gtr = (GridTabRowRenderer)renderer;
    			org.zkoss.zul.Row row = gtr.getCurrentRow();
    			if (row != null)
    				gtr.setCurrentRow(row);
	    	}
		}
	}

	/**
	 *
	 * @return true if the detailpane is visible
	 */
	public boolean isDetailVisible() {
		if (formContainer.getSouth() == null || !formContainer.getSouth().isVisible()
			|| !formContainer.getSouth().isOpen()) {
			return false;
		}

		return detailPane != null;
	}

	/**
	 *
	 * @return true if have one or more detail tabs
	 */
	public boolean hasDetailTabs() {
		if (formContainer.getSouth() == null || !formContainer.getSouth().isVisible()) {
			return false;
		}

		return detailPane != null && detailPane.getTabcount() > 0;
	}

	/**
	 * set focus to next readwrite editor from ref
	 * @param ref
	 */
	@Override
	public void focusToNextEditor(WEditor ref) {
		boolean found = false;
		for (WEditor editor : editors) {
			if (editor == ref) {
				found = true;
				continue;
			}
			if (found) {
				if (editor.isVisible() && editor.isReadWrite()) {
					focusToEditor(editor, false);
					break;
				}
			}
		}
	}

	protected void focusToEditor(WEditor toFocus, boolean checkCurrent) {
		Component c = toFocus.getComponent();
		if (c instanceof EditorBox) {
			c = ((EditorBox)c).getTextbox();
		} else if (c instanceof NumberBox) {
			c = ((NumberBox)c).getDecimalbox();
		} else if (c instanceof Urlbox) {
			c = ((Urlbox)c).getTextbox();
		}
		if (!checkCurrent) {
			((HtmlBasedComponent)c).focus();
		} else {
			StringBuilder script = new StringBuilder("var b=true;try{if (zk.currentFocus) {");
			script.append("var p=zk.Widget.$('#").append(formContainer.getCenter().getUuid()).append("');");
			script.append("if (zUtl.isAncestor(p, zk.currentFocus)) {");
			script.append("b=false;}}}catch(error){}");
			script.append("if(b){var w=zk.Widget.$('#").append(c.getUuid()).append("');w.focus(0);}");
			Clients.response(new AuScript(script.toString()));
		}
	}

	@Override
	public void setParent(Component parent) {
		super.setParent(parent);
		if (parent != null) {
			listPanel.onADTabPanelParentChanged();
		}
	}
}

