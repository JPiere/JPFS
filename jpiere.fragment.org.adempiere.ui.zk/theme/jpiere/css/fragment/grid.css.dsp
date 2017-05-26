<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ taglib uri="http://www.idempiere.org/dsp/web/util" prefix="u" %>
.z-grid tbody tr.grid-inactive-row td.z-cell {
	background-image: none !important;
	background-color: #DCDAD4 !important;
}

.z-grid tbody tr.grid-inactive-row td.row-indicator-selected {
	background-color: #DCDAD4 !important;
	background-image: url(${c:encodeURL('/theme/default/images/EditRecord16.png')}) !important;
	background-position: center;
	background-repeat: no-repeat;
	background-size: 16px 16px;
	cursor: pointer;
}

.z-grid tbody tr.highlight td.z-cell {
	background-color: #FFFFdd !important;
	background-image: none !important;
}

.z-grid tbody tr.highlight td.row-indicator-selected {
	background-color: #FFFFdd !important;
	background-image: url(${c:encodeURL('/theme/default/images/EditRecord16.png')}) !important;
	background-position: center;
	background-repeat: no-repeat;
	background-size: 16px 16px;
	cursor: pointer;
}

.z-grid tbody tr.highlight td.row-indicator {
	background-color: transparent !important;
	background-image: none !important;
}

div.z-column-cnt, div.z-grid-header div.z-auxheader-cnt {
	padding: 4px 2px 3px;
}

<%-- text overflow for grid cell --%>
.z-cell > span.z-label {
	overflow: hidden;
	text-overflow: ellipsis;
	display: inline-block;
	width: 100%;
}

.z-listcell > div.z-listcell-content {
	overflow: hidden;
	text-overflow: ellipsis;
	display: inline-block;
	width: 100%;
}

/*JPIERE-0014 .z-column-contentを削除*/
.z-listheader-content, .z-listcell-content {
	padding: 2px 3px 1px;
}

/*JPIERE-0014 追加*/
.z-column-content,.z-auxheader-content{
	padding: 0px 3px 0px;
}

.z-grid-body .z-cell {
	padding: 1px 1px;
	line-height: 22px;
}

.z-row .z-cell, .z-listitem .z-listcell, .z-listitem.z-listitem-selected>.z-listcell {
	border-left: 1px solid #dddddd ;
	border-bottom: 1px solid #dddddd !important;
}

.z-grid-emptybody td {
	text-align: left;
}

.z-grid-body {
	background-color: #FFF;
}

<%-- grid layout --%>
.grid-layout {
	border: none !important;
	margin: 0 !important;
	padding: 0 !important;
	background-color: transparent !important;
}
.grid-layout .z-row-inner, .grid-layout .z-cell {
	border: none !important;
	background-color: transparent !important;
}
.grid-layout tr.z-row-over>td.z-row-inner, .grid-layout tr.z-row-over>.z-cell {
	border: none !important;
}
.grid-layout tr.z-row-over>td.z-row-inner, .grid-layout tr.z-row-over>.z-cell {
	background-image: none !important;
}

tbody.z-grid-empty-body td {
	text-align: left;
}

tbody.z-listbox-empty-body td {
	text-align: left;
}

div.z-listbox-body .z-listcell {
	padding: 2px;
}

.z-listbox-autopaging .z-listcell-cnt {
	height: 20px;
}

tr.z-grid-odd td.z-row-inner, tr.z-grid-odd .z-cell, tr.z-grid-odd {/*JPIERE*/
	background-color: #E0EAF7;
}

/*JPIERE-Start*/
.z-row-content{
	padding: 1px 2px;
}

.z-row:hover>.z-cell{/*Display single row*/
	background: none;
}

.z-grid tbody .z-row:hover>.z-cell {/*Display multi row*/
	background-color: #ffffbb; /*Blue:E9F0FF */
}

.z-row:hover>.z-row-inner{/*Display single row at form window*/
    background: none;
}


.z-grid tbody .z-row:hover>.z-row-inner {/*Display multi row at form window*/
	background-color: #ffffbb;
}

.z-listbox-odd.z-listitem{/*info window*/
    background-color: #E9F0FF;
}

/*JPIERE-Finish*/
