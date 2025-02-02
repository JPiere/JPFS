<%-- Attachment Item --%>
.z-attachment-item {
	border: 1px solid #dcdcdc;
	border-radius: 4px;
	background-color: #f5f5f5;
	width: auto !important;
	display: inline-block;
	margin-right: 5px; 
	margin-bottom: 5px;
	padding-left: 5px;
	padding-right: 5px;
}

.z-attachment-item-del-button {
	float: right;
	background-color: #f5f5f5;
}

<%-- Combobox --%>
.z-combobox-disabled, .z-combobox[disabled], .z-datebox-disabled {
	color: black !important; cursor: default !important; opacity: 1; -moz-opacity: 1; -khtml-opacity: 1; filter: alpha(opacity=100);
}

.z-combobox-disabled * {
	color: black !important; cursor: default !important;
}

.z-combobox-text-disabled {
	background-color: #ECEAE4 !important;
}
.z-comboitem {
	min-height:14px;
}
<%-- highlight focus form element --%>
input:focus, textarea:focus, .z-combobox-input:focus, z-datebox-input:focus, select:focus {
	border: 1px solid #F39700;/*JPIERE*/
	background: #FFFFCC;
}

.z-textbox-readonly, .z-intbox-readonly, .z-longbox-readonly, .z-doublebox-readonly, 
.z-decimalbox-readonly, .z-datebox-readonly, .z-timebox-readonly, .editor-input-disd, 
.z-textbox[readonly], .z-intbox[readonly], .z-longbox[readonly], .z-doublebox[readonly], 
.z-decimalbox[readonly], .z-datebox[readonly], .z-timebox[readonly] {
	background-color: #F0F0F0;
}

.z-textbox[disabled], .z-intbox[disabled], .z-longbox[disabled], .z-doublebox[disabled], 
.z-decimalbox[disabled], .z-datebox[disabled], .z-timebox[disabled], .z-datebox-input[readonly] {
	color: black !important;
	background-color: #F0F0F0 !important;
	cursor: default !important;
	opacity: 1 !important;
	border: 1px solid #cfcfcf !important;
}

<%-- workaround for http://jira.idempiere.com/browse/IDEMPIERE-692 --%>
.z-combobox-popup {
	max-height: 200px;
}
