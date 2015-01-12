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

package org.compiere.util;

import static org.compiere.model.SystemIDs.*;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Level;

import org.compiere.model.MCurrency;

public class FragmentDisplayType {		//JPIERE-3 Create FragmentDisplayType Class

	/** Maximum number of digits    */
	private static final int    MAX_DIGITS = 28;        //  Oracle Standard Limitation 38 digits
	/** Digits of an Integer        */
	private static final int    INTEGER_DIGITS = 10;
	/** Maximum number of fractions */
	private static final int    MAX_FRACTION = 12;
	/** Default Amount Precision    */
	private static final int    AMOUNT_FRACTION = 2;

	/** Display Type 11	Integer	*/
	public static final int Integer    = REFERENCE_DATATYPE_INTEGER;
	/** Display Type 12	Amount	*/
	public static final int Amount     = REFERENCE_DATATYPE_AMOUNT;
	/** Display Type 22	Number	*/
	public static final int Number     = REFERENCE_DATATYPE_NUMBER;
	/** Display Type 29	Quantity	*/
	public static final int Quantity   = REFERENCE_DATATYPE_QUANTITY;
	/** Display Type 37	CostPrice	*/
	public static final int CostPrice  = REFERENCE_DATATYPE_COSTPRICE;


	/**	Logger	*/
	private static CLogger s_log = CLogger.getCLogger (FragmentDisplayType.class);

	/**************************************************************************
	 *Return Format for numeric DisplayType
	 *  @param displayType Display Type (default Number)
	 *  @param language Language
	 *  @param pattern Java Number Format pattern e.g. "#,##0.00"
	 *  @param C_Currency_ID int
	 *  @return number format
	 */
	public static DecimalFormat getNumberFormat(int displayType, Language language, String pattern, int C_Currency_ID)
	{
		Language myLanguage = language;
		if (myLanguage == null)
			myLanguage = Language.getLoginLanguage();
		Locale locale = myLanguage.getLocale();
		DecimalFormat format = null;
		if (locale != null)
			format = (DecimalFormat)NumberFormat.getNumberInstance(locale);
		else
			format = (DecimalFormat)NumberFormat.getNumberInstance(Locale.US);
		//
		if (pattern != null && pattern.length() > 0)
		{
			try {
				format.applyPattern(pattern);
				return format;
			}
			catch (IllegalArgumentException e) {
				s_log.log(Level.WARNING, "Invalid number format: " + pattern);
			}
		}
		else if (displayType == Integer)
		{
			format.setParseIntegerOnly(true);
			format.setMaximumIntegerDigits(INTEGER_DIGITS);
			format.setMaximumFractionDigits(0);
		}
		else if (displayType == Quantity)
		{
			format.setMaximumIntegerDigits(MAX_DIGITS);
			format.setMaximumFractionDigits(MAX_FRACTION);
		}
		else if (displayType == Amount)
		{
			format.setMaximumIntegerDigits(MAX_DIGITS);
			format.setMaximumFractionDigits(MAX_FRACTION);
			if(C_Currency_ID==0)
			{
				format.setMinimumFractionDigits(AMOUNT_FRACTION);
			}else{
				format.setMinimumFractionDigits(MCurrency.get(Env.getCtx(), C_Currency_ID).getStdPrecision());
			}
		}
		else if (displayType == CostPrice)
		{
			format.setMaximumIntegerDigits(MAX_DIGITS);
			format.setMaximumFractionDigits(MAX_FRACTION);
			if(C_Currency_ID==0)
			{
				format.setMinimumFractionDigits(AMOUNT_FRACTION);
			}else{
				format.setMinimumFractionDigits(MCurrency.get(Env.getCtx(), C_Currency_ID).getCostingPrecision());
			}
		}
		else //if (displayType == Number)
		{
			format.setMaximumIntegerDigits(MAX_DIGITS);
			format.setMaximumFractionDigits(MAX_FRACTION);
			format.setMinimumFractionDigits(1);
		}
		return format;
	}//getDecimalFormat
}
