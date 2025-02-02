/*JPIERE-CSS:Start*/

 .z-combobox-input
,.z-textbox
,.z-datebox-input
,.z-timebox-input
,.z-decimalbox
 {
    border: 1px solid #0099ff;
 }

 .z-combobox-input[readonly]
,.z-textbox[readonly]
,.z-datebox-input[readonly]
,.z-timebox-input[readonly]
,.z-decimalbox[readonly]
 {
    border: 1px solid #bbbbbb;
    background-color: #f0f0f0!important; /*#D9E0EB;*/
 }

 .z-combobox-disabled .z-combobox-input {
    border: 1px solid #bbbbbb;
    background-color: #f0f0f0!important; /*#D9E0EB;*/
 }


 .z-combobox-button
,.z-timebox-button
,.z-datebox-button
,.z-combobox-button:hover
,.z-timebox-button:hover
,.z-datebox-button:hover
 {
    border-style: Solid;
    border-color: #0099ff;
    border-top-width: 1px;
    border-bottom-width: 1px;
    border-right-width: 1px;
    border-left-width: 1px;
 }

/*JPIERE-CSS:Finish*/

.mandatory-decorator-text {
	text-decoration: none; font-size: xx-small; vertical-align: top; color:red;
}

.editor-box {
	display: inline-block;
	border: none; 
	padding: 0px; 
	margin: 0px; 
	background-color: transparent;
	position: relative;
}

.editor-input {
	box-sizing: border-box;
	-moz-box-sizing: border-box; /* Firefox */
	display: inline-block;
	padding-right: 22px; 
	width: 100%;
	height: 21px;
}
.editor-input.mobile.z-decimalbox {
	padding-right: 5px;
}

.editor-input:focus {
	border: 1px solid #F39700; /*JPIERE*/
}

.editor-input-disd {
	padding-right: 2px !important;/*JPIERE*/
}
	
.editor-button {
	padding: 0px;
	margin: 0px;
	display: inline-block;
	background-color: transparent;
	background-image: none;
	width: 20px;
	height: 22px;
	min-height: 22px;
	border: none;
	position: absolute;
	right: 1px;
	top: 2px;
}

.editor-button :hover {
	-webkit-filter: contrast(1.5);
	filter: contrast(150%);
}

.editor-button img {
	vertical-align: top;
	text-align: left;
	width: 18px;
	height: 18px;
	padding: 1px 1px;
}


.editor-box .grid-editor-input.z-textbox {
}

.grid-editor-button {
}

.grid-editor-button img {
}

.number-box {
	display: inline-block; 
	white-space:nowrap;
}

.number-box .grid-editor-input.z-decimalbox {
}

.datetime-box {
	white-space:nowrap;
}
.datetime-box .z-datebox {
}
.datetime-box .z-timebox {
}

span.grid-combobox-editor {
	width: 100% !important;
	position: relative;
}

.grid-combobox-editor input {
	width: 100% !important;
	padding-right: 26px;
	border-bottom-right-radius: 6px;
	border-top-right-radius: 6px;
	border-right: 0px;
}

.grid-combobox-editor.z-combobox-disabled input {
	border-bottom-right-radius: 3px;
	border-top-right-radius: 3px;
	border-right: 1px solid #cfcfcf;
	padding-right: 5px;
}

.grid-combobox-editor .z-combobox-button {
	position: absolute;
	right: 0px;
	top: 0px; /*JPIERE*/
	border-bottom-right-radius: 3px;
	border-top-right-radius: 3px;
	border-bottom-left-radius: 0px;
	border-top-left-radius: 0px;
}

.find-window .grid-combobox-editor .z-combobox-button {
    top: 0px; /*JPIERE*/
}

.grid-combobox-editor input:focus {
	border-right: 0px;
}
	
.grid-combobox-editor input:focus + .z-combobox-button {
	border-left: 1px solid #0000ff;
}

.find-window .editor-input.z-combobox + .editor-button {
    top: 1px; /*JPIERE*/
}

.info-panel .editor-input.z-combobox + .editor-button {
    top: 1px; /*JPIERE*/
}

.input-paramenter-layout .editor-input.z-combobox + .editor-button {
    top: 1px; /*JPIERE*/
}

.editor-input.z-combobox + .editor-button {
	background-color: #FFFFFF;/*JPIERE*/
	width: 22px;
    height: 24px;
    min-height: 24px;
    right: 0px;
    /*JPIERE-Start*/
    top: 0px;
    border: 1px solid #0099ff;
    /*border-radius: 0;*/
	border-bottom-right-radius: 3px;
	border-top-right-radius: 3px;
	border-bottom-left-radius: 0px;
	border-top-left-radius: 0px;
	/*JPIERE-End*/
    border-left: 1px solid transparent;
}
.editor-input.z-combobox > .z-combobox-input {
	border-bottom-right-radius: 0;
	border-top-right-radius: 0;
}

<%-- payment rule --%>
.payment-rule-editor {
	display: inline-block;
	border: none; 
	padding: 0px; 
	margin: 0px; 
	background-color: transparent;
	position: relative;
}
.payment-rule-editor .z-combobox {
	width: 100%;
}
.payment-rule-editor .z-combobox-input {
	display: inline-block;
	padding-right: 44px; 
	width: 100%;
	height: 24px;
	border-bottom-right-radius: 6px;
	border-top-right-radius: 6px;
	border-right: 0px;
}
.payment-rule-editor .z-combobox-input:focus {
	/*border: 1px solid #0000ff;*/
	border: 1px solid #F39700;/*JPIERE*/
}
.payment-rule-editor .z-combobox-input.editor-input-disd {
	padding-right: 22px !important;
}
.payment-rule-editor .z-combobox-button {
	position: absolute;
	right: 0px;

	/*JPIERE-Start*/
	top: 0px;
	border-left-color: #0099ff;
	border-left-style: Solid;
	border-left-width: 1px;
	/*JPIERE-End*/
}
.payment-rule-editor .z-combobox .z-combobox-button-hover {
	background-color: #ddd;
	background-position: 0px 0px;
}
.payment-rule-editor .editor-button {
	border-radius: 0px;
	right: 24px;
}

<%-- chart --%>
.chart-field {
	padding: 10px; 
	border: 1px solid lightgray !important;
}

.field-label {
	position: relative; 
	float: right;
}

.z-image  /*JPIERE*/
{
    object-fit: contain;
}
 .image-field {
	cursor: pointer;
	border: 1px solid #C5C5C5;
	height: 24px;
	width: 24px;
	object-fit: contain; /*JPIERE*/
}
.image-field.image-field-readonly {
	cursor: default;
	border: none;
}
.image-fit-contain {
	object-fit: contain;
}
.z-cell.image-field-cell {
	z-index: 1;
}

.html-field {
	cursor: pointer;
	border: 1px solid #C5C5C5;
	overflow: auto;
}

.dashboard-field-panel.z-panel, .dashboard-field-panel.z-panel > .z-panel-body,  .dashboard-field-panel.z-panel > .z-panel-body > .z-panelchildren  {
	overflow: visible;
}

/*JPIERE
.idempiere-mandatory, .idempiere-mandatory input, .idempiere-mandatory a {
    border-color:red;
}
*/

.idempiere-label {
    color: #333;
}

.idempiere-mandatory-label{
   color: red!important;
}

.idempiere-zoomable-label {
    cursor: pointer; 
    text-decoration: underline;
}

<%-- full size image hover --%>
.fullsize-image {
	padding: 5px;
  	border: 1px solid #ccc;
  	background: #e3f4f9;
}

 .z-select{
	height: 24px;
    border: 1px solid #0099ff;
    border-radius: 3px;
 }

 .z-select:focus {
	border: 1px solid #F39700;/*JPIERE*/
}
