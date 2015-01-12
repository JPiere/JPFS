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

package org.adempiere.webui.editor;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.adempiere.webui.ValuePreference;
import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.NumberBox;
import org.adempiere.webui.event.ContextMenuEvent;
import org.adempiere.webui.event.ContextMenuListener;
import org.adempiere.webui.event.ValueChangeEvent;
import org.adempiere.webui.window.WFieldRecordInfo;
import org.compiere.model.GridField;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.FragmentDisplayType;	//JPIERE-3 Import FragmentDisplayType to WNumberEditor
import org.compiere.util.Language;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;

/**
 *
 * @author  <a href="mailto:agramdass@gmail.com">Ashley G Ramdass</a>
 * @date    Mar 11, 2007
 * @version $Revision: 0.10 $
 *
 * @author Low Heng Sin
 * @author Cristina Ghita, www.arhipac.ro
 *  	   <li> BF [3058780] WNumberEditor allow only BigDecimal
 *  	   @see https://sourceforge.net/tracker/?func=detail&aid=3058780&group_id=176962&atid=955896
 */
public class WNumberEditor extends WEditor implements ContextMenuListener
{
    public static final String[] LISTENER_EVENTS = {Events.ON_CHANGE, Events.ON_OK};

    public static final int MAX_DISPLAY_LENGTH = 35;
    public static final int MIN_DISPLAY_LENGTH = 11;

    private Object oldValue;

	private int displayType;

	private boolean tableEditor;

    public WNumberEditor()
    {
    	this("Number", false, false, true, DisplayType.Number, "");
    }

    /**
    *
    * @param gridField
    */
    public WNumberEditor(GridField gridField)
    {
    	this(false, gridField);
    }

    /**
     *
     * @param gridField
     */
    public WNumberEditor(boolean tableEditor, GridField gridField)
    {
        super(new NumberBox(gridField.getDisplayType() == DisplayType.Integer, tableEditor),
                gridField);
        this.displayType = gridField.getDisplayType();
        this.tableEditor = tableEditor;
        init();
    }

    /**
     *
     * @param gridField
     * @param integral
     */
    public WNumberEditor(GridField gridField, boolean integral)
    {
        super(new NumberBox(integral), gridField);
        this.displayType = integral ? DisplayType.Integer : DisplayType.Number;
        init();
    }

    /**
     *
     * @param columnName
     * @param mandatory
     * @param readonly
     * @param updateable
     * @param displayType
     * @param title
     */
    public WNumberEditor(String columnName, boolean mandatory, boolean readonly, boolean updateable,
			int displayType, String title)
    {
		super(new NumberBox(displayType == DisplayType.Integer), columnName, title, null, mandatory,
				readonly, updateable);
		this.displayType = displayType;
		init();
	}

	private void init()
    {
		if (gridField != null)
		{
			getComponent().setTooltiptext(gridField.getDescription());
	        int displayLength = gridField.getDisplayLength();
	        if (displayLength > MAX_DISPLAY_LENGTH)
	            displayLength = MAX_DISPLAY_LENGTH;
	        else if (displayLength <= 0 || displayLength < MIN_DISPLAY_LENGTH)
	        	displayLength = MIN_DISPLAY_LENGTH;
	        if (!tableEditor)
	        	getComponent().getDecimalbox().setCols(displayLength);
//	        else
//	        	getComponent().getDecimalbox().setCols(0);
		}

		if (DisplayType.isID(displayType))
			displayType = DisplayType.Integer;
		else if (!DisplayType.isNumeric(displayType))
			displayType = DisplayType.Number;
		// IDEMPIERE-989
		Language lang = AEnv.getLanguage(Env.getCtx());
		DecimalFormat format = DisplayType.getNumberFormat(displayType, lang);
		if (gridField != null && gridField.getFormatPattern() != null)
			getComponent().getDecimalbox().setFormat(gridField.getFormatPattern());
		else
			getComponent().getDecimalbox().setFormat(format.toPattern());
		getComponent().getDecimalbox().setLocale(lang.getLocale());
		getComponent().setFormat(format);

		popupMenu = new WEditorPopupMenu(false, false, isShowPreference());
    	addChangeLogMenu(popupMenu);
    }

	/**
	 * Event handler
	 * @param event
	 */
    public void onEvent(Event event)
    {
    	if (Events.ON_CHANGE.equalsIgnoreCase(event.getName()) || Events.ON_OK.equalsIgnoreCase(event.getName()))
    	{
	        Object newValue = getComponent().getValue();
	        if (oldValue == null && newValue == null) {
	        	return;
	        }

	        if (displayType == DisplayType.Integer) {
		        if (newValue != null && newValue instanceof BigDecimal) {
		        	newValue = new Integer(((BigDecimal)newValue).intValue());
		        }
		        if (oldValue != null && oldValue instanceof BigDecimal) {
		        	oldValue = new Integer(((BigDecimal)oldValue).intValue());
		        }
	        }

	        if (oldValue != null && newValue != null && oldValue.equals(newValue))
	        {
	    	    return;
	    	}

	        ValueChangeEvent changeEvent = new ValueChangeEvent(this, this.getColumnName(), oldValue, newValue);
	        super.fireValueChange(changeEvent);
	        oldValue = getComponent().getValue(); // IDEMPIERE-963 - check again the value could be changed by callout
    	}
    }

    @Override
	public NumberBox getComponent() {
		return (NumberBox) component;
	}

	@Override
	public boolean isReadWrite() {
		return getComponent().isEnabled();
	}

	@Override
	public void setReadWrite(boolean readWrite) {
		getComponent().setEnabled(readWrite);
	}

	@Override
    public String getDisplay()
    {
        return getComponent().getText();
    }

    @Override
    public Object getValue()
    {
        return getComponent().getValue();
    }

    @Override
    public void setValue(Object value)
    {
    	if (value == null)
    		oldValue = null;
    	else if (value instanceof BigDecimal)
    	{										//JPIERE-3 Modify WNumberEditor#setValue() by Hideaki Hagiwara
    		oldValue = (BigDecimal) value;
    		if(gridField != null  && (displayType==DisplayType.Amount || displayType==DisplayType.CostPrice) )
    		{
    			DecimalFormat format = FragmentDisplayType.getNumberFormat(displayType, null, gridField.getFormatPattern()
    					,Env.getContextAsInt (gridField.getVO().ctx, gridField.getVO().WindowNo, gridField.getVO().TabNo, "C_Currency_ID"));
    			getComponent().getDecimalbox().setFormat(format.toPattern());
    			getComponent().setFormat(format);
    		}
    	} 												//JPiere-3 Finish
    	else if (value instanceof Number)
    		oldValue = BigDecimal.valueOf(((Number)value).doubleValue());
    	else
    		oldValue = new BigDecimal(value.toString());
    	getComponent().setValue(oldValue);
    }

    @Override
    public String[] getEvents()
    {
        return LISTENER_EVENTS;
    }

    /**
     * Handle context menu events
     * @param evt
     */
    public void onMenu(ContextMenuEvent evt)
	{
	 	if (WEditorPopupMenu.PREFERENCE_EVENT.equals(evt.getContextEvent()))
		{
			if (isShowPreference())
				ValuePreference.start (getComponent(), this.getGridField(), getValue());
			return;
		}
	 	else if (WEditorPopupMenu.CHANGE_LOG_EVENT.equals(evt.getContextEvent()))
		{
			WFieldRecordInfo.start(gridField);
		}
	}

	@Override
	public void setTableEditor(boolean b) {
		super.setTableEditor(b);
		getComponent().setTableEditorMode(b);
	}

}
