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

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import org.compiere.model.MPaySelectionCheck;

/**
 * 	Generic Payment Export
 *  Sample implementation of Payment Export Interface - brought here from MPaySelectionCheck
 *
 * 	@author 	Jorg Janke
 *  @author		Hagiwara Hideaki
 *
 *  Contributors:
 *    Carlos Ruiz - GlobalQSS - FR 3132033 - Make payment export class configurable per bank
 */
public class JapanPaymentExport implements PaymentExport
{
	/** Logger								*/
	static private CLogger	s_log = CLogger.getCLogger (JapanPaymentExport.class);


	/** BPartner Info Index for Value       */
	private static final int     BP_VALUE = 0;
	/** BPartner Info Index for Name        */
	private static final int     BP_NAME = 1;
	/** BPartner Info Index for Contact Name    */
	private static final int     BP_CONTACT = 2;
	/** BPartner Info Index for Address 1   */
	private static final int     BP_ADDR1 = 3;
	/** BPartner Info Index for Address 2   */
	private static final int     BP_ADDR2 = 4;
	/** BPartner Info Index for City        */
	private static final int     BP_CITY = 5;
	/** BPartner Info Index for Region      */
	private static final int     BP_REGION = 6;
	/** BPartner Info Index for Postal Code */
	private static final int     BP_POSTAL = 7;
	/** BPartner Info Index for Country     */
	private static final int     BP_COUNTRY = 8;
	/** BPartner Info Index for Reference No    */
	private static final int     BP_REFNO = 9;


	/** BankAccount Info Index for Value       */
	private static final int     BA_RequesterCode = 0;
	/** BankAccount Info Index for Name        */
	private static final int     BA_RequesterName = 1;
	/** BankAccount Info Index for Contact Name    */
	private static final int     BA_DATE = 2;
	/** BankAccount Info Index for Address 1   */
	private static final int     BA_RoutingNo = 3;
	/** BankAccount Info Index for Address 2   */
	private static final int     BA_BankName_Kana = 4;
	/** BankAccount Info Index for City        */
	private static final int     BA_BranchCode = 5;
	/** BankAccount Info Index for Region      */
	private static final int     BA_BranchName_Kana = 6;
	/** BankAccount Info Index for Postal Code */
	private static final int     BA_BankAccountType = 7;
	/** BankAccount Info Index for Country     */
	private static final int     BA_AccountNo = 8;

	/** BPartner Info Index for Value       */
	private static final int     BP_RoutingNo = 0;
	/** BPartner Info Index for Name        */
	private static final int     BP_BankName_Kana = 1;
	/** BPartner Info Index for Contact Name    */
	private static final int     BP_BranchCode = 2;
	/** BPartner Info Index for Address 1   */
	private static final int     BP_BranchName_Kana = 3;
	/** BPartner Info Index for City        */
	private static final int     BP_BankAccountType = 4;
	/** BPartner Info Index for Region      */
	private static final int     BP_AccountNo = 5;
	/** BPartner Info Index for Postal Code */
	private static final int     BP_A_Name_Kana = 6;

	/** Left or Right */
	private static final boolean LEFT = true;
	private static final boolean RIGHT = false;
	/** Space or Zero */
	private static final String STR1 = " ";
	private static final String STR2 = "0";



	/**************************************************************************
	 *  Export to File
	 *  @param checks array of checks
	 *  @param file file to export checks
	 *  @return number of lines
	 */
	public int exportToFile (MPaySelectionCheck[] checks, File file, StringBuffer err)
	{
		if (checks == null || checks.length == 0)
			return 0;
		//  Must be a file
		if (file.isDirectory())
		{
			err.append("No se puede escribir, el archivo seleccionado es un directorio - " + file.getAbsolutePath());
			s_log.log(Level.SEVERE, err.toString());
			return -1;
		}
		//  delete if exists
		try
		{
			if (file.exists())
				file.delete();
		}
		catch (Exception e)
		{
			s_log.log(Level.WARNING, "Could not delete - " + file.getAbsolutePath(), e);
		}

		char x = '"';      //  ease
		int noLines = 0;
		StringBuffer line = null;
		try
		{
			FileWriter fw = new FileWriter(file);

			//  write header
			line = new StringBuffer();
			String ba[] = getBankInfo(checks);
			line.append("1210")   // dataDiff + code + codeDiff,      1+2+1
			.append(ba[BA_RequesterCode])    // 0 RequesterCode,  10
			.append(ba[BA_RequesterName])	 // 1 RequesterName,  40
			.append(ba[BA_DATE])   			 // 2 System DATE,    4
			.append(ba[BA_RoutingNo])   	 // 3 RoutingNo,      4
			.append(ba[BA_BankName_Kana])    // 4 BankName_Kana,  15
			.append(ba[BA_BranchCode])  	 // 5 BranchCode,     3
			.append(ba[BA_BranchName_Kana])  // 6 BranchName_Kana,15
			.append(ba[BA_BankAccountType])  // 7 BankAccountType,1
			.append(ba[BA_AccountNo])   	 // 8 AccountNo,      7
			.append(strAdd(null,LEFT,STR1,17)) 	 // 9 space,      17
			.append(Env.NL);
			fw.write(line.toString());

			//  write FB lines
			BigDecimal allPayAmt= new BigDecimal(0);
			for (int i = 0; i < checks.length; i++)
			{
				MPaySelectionCheck mpp = checks[i];
				if (mpp == null)
					continue;
				//  BPartner Info
				String bp[] = getBPartnerInfo(mpp.getC_BPartner_ID());

				// Data Record
				line = new StringBuffer();
				line.append("2")    				 // static 2,			1
					.append(bp[BP_RoutingNo])   	 // 1 RoutingNo,		4
					.append(bp[BP_BankName_Kana])    // 2 BankName_Kana,	15
					.append(bp[BP_BranchCode])  	 // 3 BranchCode,		3
					.append(bp[BP_BranchName_Kana])  // 4 BranchName_Kana,	15
					.append("    ")					 // 5 Space1,			4
					.append(bp[BP_BankAccountType])  // 6 BankAccountType,	1
					.append(bp[BP_AccountNo])   	 // 7 AccountNo,		7
					.append(bp[BP_A_Name_Kana]) 			 // 8 A_Name,			30
					.append(strAdd(mpp.getPayAmt().toString(),RIGHT,STR2,10)) // PayAmount, 10
					.append(STR1) 			 		 // Space 1,			1
					.append(strAdd(null,RIGHT,STR1,10)) 			 // Space 10, 10
					.append(strAdd(null,RIGHT,STR1,10)) 			 // Space 10, 10
					.append(strAdd(null,LEFT,STR1,9)) 			 	 // Space 9
					//.append(comment.toString())      // Comment
					.append(Env.NL);
				fw.write(line.toString());
				noLines++;
				allPayAmt = allPayAmt.add(mpp.getPayAmt());

			}   //  write FB line

			// write trailer Record
			line = new StringBuffer();
			line.append("8")
				.append(strAdd(String.valueOf(noLines),RIGHT,STR2,6))
				.append(strAdd(allPayAmt.toString(),RIGHT,STR2,12))
				.append(strAdd(null,LEFT,STR1,101)) 	 // space 101
				.append(Env.NL);
			fw.write(line.toString());

			// write end Record
			line = new StringBuffer();
			line.append("9")
				.append(strAdd(null,LEFT,STR1,119))
				.append(Env.NL);
			fw.write(line.toString());
			// Jirimuto modified for Farm Bank data import --end 2010/04/02

			fw.flush();
			fw.close();
		}
		catch (Exception e)
		{
			s_log.log(Level.SEVERE, "", e);
		}

		return noLines;
	}   //  exportToFile

	/**
	 *  Get Customer/Vendor Info.
	 *  Based on BP_ static variables
	 *  @param C_BPartner_ID BPartner
	 *  @return info array
	 */
	private static String[] getBPartnerInfo (int C_BPartner_ID)
	{
		String[] bp = new String[10];

		String sql = "SELECT ba.RoutingNo,"
			+ "ba.JP_BankName_Kana,"
			+ "bpbc.JP_BranchCode,"
			+ "bpbc.JP_BranchName_Kana,"
			+ "case when bpbc.BankAccountType='S' then 1 else 2 end,"
			+ "bpbc.AccountNo,"
			+ "bpbc.JP_A_Name_Kana,"
			+ "bpbc.IsDefault as IsDefault "
			+ "FROM C_BP_BANKACCOUNT bpbc "
			+ "INNER JOIN C_Bank ba ON bpbc.C_Bank_ID=ba.C_Bank_ID "
			+ "WHERE bpbc.C_BPartner_ID= " + C_BPartner_ID
			+ " and bpbc.IsActive='Y' "
			+ "and bpbc.IsACH='Y' "
			+ "order by bpbc.Updated Desc ";
		try
		{
			Statement stmt = null;
			stmt = DB.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE,null);
			ResultSet rs = stmt.executeQuery (sql);

			if (rs.next())
			{
				bp[BP_RoutingNo] = rs.getString(1);
				if (bp[BP_RoutingNo] == null || bp[BP_RoutingNo].length()!=4)
					bp[BP_RoutingNo] = strAdd(bp[BP_RoutingNo],LEFT,STR1,4);
				bp[BP_BankName_Kana] = rs.getString(2);
				if (bp[BP_BankName_Kana] == null || bp[BP_BankName_Kana].length()!=15)
					bp[BP_BankName_Kana] = strAdd(bp[BP_BankName_Kana],LEFT,STR1,15);
				bp[BP_BranchCode] = rs.getString(3);
				if (bp[BP_BranchCode] == null || bp[BP_BranchCode].length()!=3)
					bp[BP_BranchCode] = strAdd(bp[BP_BranchCode],LEFT,STR1,3);
				bp[BP_BranchName_Kana] = rs.getString(4);
				if (bp[BP_BranchName_Kana] == null || bp[BP_BranchName_Kana].length()!=15)
					bp[BP_BranchName_Kana] = strAdd(bp[BP_BranchName_Kana],LEFT,STR1,15);
				bp[BP_BankAccountType] = rs.getString(5);
				if (bp[BP_BankAccountType] == null || bp[BP_BankAccountType].length()!=1)
					bp[BP_BankAccountType] = strAdd(bp[BP_BankAccountType],LEFT,STR1,1);
				bp[BP_AccountNo] = rs.getString(6);
				if (bp[BP_AccountNo] == null || bp[BP_AccountNo].length()!=7)
					bp[BP_AccountNo] = strAdd(bp[BP_AccountNo],LEFT,STR1,7);
				bp[BP_A_Name_Kana] = rs.getString(7);
				if (bp[BP_A_Name_Kana] == null || bp[BP_A_Name_Kana].length()!=30)
					bp[BP_A_Name_Kana] = strAdd(bp[BP_A_Name_Kana],LEFT,STR1,30);
				rs.beforeFirst();
			}
			else
			{
				bp[BP_RoutingNo] = strAdd(bp[BP_RoutingNo],LEFT,STR1,4);
				bp[BP_BankName_Kana] = strAdd(bp[BP_BankName_Kana],LEFT,STR1,15);
				bp[BP_BranchCode] = strAdd(bp[BP_BranchCode],LEFT,STR1,3);
				bp[BP_BranchName_Kana] = strAdd(bp[BP_BranchName_Kana],LEFT,STR1,15);
				bp[BP_BankAccountType] = strAdd(bp[BP_BankAccountType],LEFT,STR1,1);
				bp[BP_AccountNo] = strAdd(bp[BP_AccountNo],LEFT,STR1,7);
				bp[BP_A_Name_Kana] = strAdd(bp[BP_A_Name_Kana],LEFT,STR1,30);
			}

			while(rs.next())
			{
				if(rs.getString("IsDefault").compareTo("Y")==0)
				{
					bp[BP_RoutingNo] = rs.getString(1);
					if (bp[BP_RoutingNo] == null || bp[BP_RoutingNo].length()!=4)
						bp[BP_RoutingNo] = strAdd(bp[BP_RoutingNo],LEFT,STR1,4);
					bp[BP_BankName_Kana] = rs.getString(2);
					if (bp[BP_BankName_Kana] == null || bp[BP_BankName_Kana].length()!=15)
						bp[BP_BankName_Kana] = strAdd(bp[BP_BankName_Kana],LEFT,STR1,15);
					bp[BP_BranchCode] = rs.getString(3);
					if (bp[BP_BranchCode] == null || bp[BP_BranchCode].length()!=3)
						bp[BP_BranchCode] = strAdd(bp[BP_BranchCode],LEFT,STR1,3);
					bp[BP_BranchName_Kana] = rs.getString(4);
					if (bp[BP_BranchName_Kana] == null || bp[BP_BranchName_Kana].length()!=15)
						bp[BP_BranchName_Kana] = strAdd(bp[BP_BranchName_Kana],LEFT,STR1,15);
					bp[BP_BankAccountType] = rs.getString(5);
					if (bp[BP_BankAccountType] == null || bp[BP_BankAccountType].length()!=1)
						bp[BP_BankAccountType] = strAdd(bp[BP_BankAccountType],LEFT,STR1,1);
					bp[BP_AccountNo] = rs.getString(6);
					if (bp[BP_AccountNo] == null || bp[BP_AccountNo].length()!=7)
						bp[BP_AccountNo] = strAdd(bp[BP_AccountNo],LEFT,STR1,7);
					bp[BP_A_Name_Kana] = rs.getString(7);
					if (bp[BP_A_Name_Kana] == null || bp[BP_A_Name_Kana].length()!=30)
						bp[BP_A_Name_Kana] = strAdd(bp[BP_A_Name_Kana],LEFT,STR1,30);
					break;
				}
			}
			rs.close();
			stmt.close();
			stmt = null;
		}
		catch (SQLException e)
		{
			s_log.log(Level.SEVERE, sql, e);
		}
		// Jirimuto modified for Farm Bank data import --start 2010/04/02

		return bp;
	}   //  getBPartnerInfo



	private static String[] getBankInfo (MPaySelectionCheck[]  checks)
	{
		String[] ba = new String[10];
		int C_PaymentSelection_ID = checks[0].getParent().get_ID();

		String sql = "SELECT bc.JP_RequesterCode, "
			+ "bc.JP_RequesterName,"
			+ "to_char(ps.PayDate,'MMDD'),"
			+ "ba.RoutingNo,"
			+ "ba.JP_BankName_Kana,"
			+ "bc.JP_BranchCode,"
			+ "bc.JP_BranchName_Kana,"
			+ "case when bc.BankAccountType='S' then 1 else 2 end,"
			+ "bc.AccountNo "
			+ "FROM C_PaySelection ps "
			+ " INNER JOIN C_BankAccount bc ON (ps.C_BankAccount_ID=bc.C_BankAccount_ID) "
			+ " INNER JOIN C_Bank ba ON (bc.C_Bank_ID=ba.C_Bank_ID) "
			+ "WHERE ps.C_PaySelection_ID=? ";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, C_PaymentSelection_ID);
			ResultSet rs = pstmt.executeQuery();
			//
			if (rs.next())
			{
				ba[BA_RequesterCode] = rs.getString(1);
				if (ba[BA_RequesterCode] == null || ba[BA_RequesterCode].length()!=10)
					ba[BA_RequesterCode] = strAdd(ba[BA_RequesterCode],LEFT,STR1,10);
				ba[BA_RequesterName] = rs.getString(2);
				if (ba[BA_RequesterName] == null || ba[BA_RequesterName].length()!=40 )
					ba[BA_RequesterName] = strAdd(ba[BA_RequesterName],LEFT,STR1,40);
				ba[BA_DATE] = rs.getString(3);
				if (ba[BA_DATE] == null)
					ba[BA_DATE] = strAdd(ba[BA_DATE],LEFT,STR1,4);
				ba[BA_RoutingNo] = rs.getString(4);
				if (ba[BA_RoutingNo] == null || ba[BA_RoutingNo].length()!=4)
					ba[BA_RoutingNo] = strAdd(ba[BA_RoutingNo],LEFT,STR1,4);
				ba[BA_BankName_Kana] = rs.getString(5);
				if (ba[BA_BankName_Kana] == null || ba[BA_BankName_Kana].length()!= 15)
					ba[BA_BankName_Kana] = strAdd(ba[BA_BankName_Kana],LEFT,STR1,15);
				ba[BA_BranchCode] = rs.getString(6);
				if (ba[BA_BranchCode] == null || ba[BA_BranchCode].length()!=3)
					ba[BA_BranchCode] = strAdd(ba[BA_BranchCode],LEFT,STR1,3);
				ba[BA_BranchName_Kana] = rs.getString(7);
				if (ba[BA_BranchName_Kana] == null || ba[BA_BranchName_Kana].length()!=15)
					ba[BA_BranchName_Kana] = strAdd(ba[BA_BranchName_Kana],LEFT,STR1,15);
				ba[BA_BankAccountType] = rs.getString(8);
				if (ba[BA_BankAccountType] == null || ba[BA_BankAccountType].length()!=1)
					ba[BA_BankAccountType] = strAdd(ba[BA_BankAccountType],LEFT,STR1,1);
				ba[BA_AccountNo] = rs.getString(9);
				if (ba[BA_AccountNo] == null || ba[BA_AccountNo].length()!=7 )
					ba[BA_AccountNo] = strAdd(ba[BA_AccountNo],LEFT,STR1,7);
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.log(Level.SEVERE, sql, e);
		}
		return ba;
	}   //  getBankInfo



	public static String strAdd(String strIn, boolean left, String addStr, int strLength)
	{
		StringBuffer returnStr= new StringBuffer("");
		int length1=0;

		if(strIn!=null)
		{
			returnStr.append(strIn);
			length1=strIn.length();
		}

		if(left)
		{
			if(length1<strLength)
			{
				for(int i=length1;i<strLength;i++)
				{
					returnStr.append(addStr);
				}
			}
			else
			{
				returnStr.delete(strLength, length1);
			}
		}
		else
		{
			for(int i=length1;i<strLength;i++)
			{
				returnStr.insert(0,addStr);
			}
		}
		return returnStr.toString();
	}


}	//	PaymentExport
