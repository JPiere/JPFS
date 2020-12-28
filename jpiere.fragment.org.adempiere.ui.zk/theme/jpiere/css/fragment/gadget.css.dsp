.fav-new-btn
}

.fav-new-btn img {
	height: 16px;
	width: 16px;
}

.z-panel-head {
	background-image: none;
	background-color: #003894;
	/*border: 1px solid #3a5caa;*/
	/*border-radius: 8px 8px 0px 0px;*/
	margin: 4px 4px 0px 4px;
	padding-bottom: 0px;
	border: none;
}

.desktop-left-column .z-panel-body { /*JPIERE*/
	border-radius: 0px 0px 0px 0px !important;
	margin: 0px 2px 0px 2px;
}

.z-panel {
	border-radius: 6px 6px 6px 6px;
	border: 1px solid #bbbbbb;
	background-color: #ffffff;
	/*border: 1px solid #3a5caa;*/
	border-bottom-width: 2px;

}

.z-panel-noborder {
	border: none !important;
}

.z-panelchildren {
	border: none;
}

.z-panel-head {
	padding-bottom: 1px;
	border: none;
}

.z-panel-head .z-panel-header {
	padding: 0 0 2px 0;
	color: #262626;
	background: #003894;;
	/*background: #3a5caa;JPiere*/
	font-weight: 300;
	font-size: 13px;
}

.z-caption .z-caption-content {
	padding: 0 0 2px 0;
	/*color: #262626;*/
	color: #ffffff;/*JPiere*/
	font-weight: 400;
	font-size: 13px;
	cursor: move;
}

.z-groupbox .z-caption-content {
	padding: 0 0 2px 0;
	color: #666666;
	font-weight: 400;
	font-size: 13px;
	cursor: move;
}

.dashboard-layout {
	width: 100%;
	height: 100%;
	position: relative;
}

.dashboard-widget {
	margin-top: 1px;
	margin-left: auto;
	margin-right: auto;
	position: relative;
	width: 99%;
}

.dashboard-widget-max {
	margin: auto;
	width: auto;
}

.dashboard-widget.dashboard-widget-max > .z-panel-body > .z-panelchildren {
	overflow: auto;
}

.dashboard-report-iframe {
	min-height:300px;
	border: 1px solid lightgray;
	margin:auto;
	width: 99%;
	height: 90%;
}

.favourites-box {
	width: 100%;
	margin: auto;
	/*padding: 1px 0px 1px 0px !important;
	border: 1px solid #cccccc;
	background-color: #ffffff;*/
}

.favourites-box .z-vbox {
	width: 100%;
}

.favourites-box .z-hbox {
	padding: 0px 4px;
    width: 100%;
}

.favourites-box .z-toolbar-start {
	float: right;
}
.favourites-box .trash-font-icon {
	font-family: FontAwesome;
	font-size: 20px;
}

.favourites-box .z-panel {/*JPIERE*/
    border-radius: 0px 0px 0px 0px !important;
}

.favourites-box .z-vlayout-inner{/*JPIERE*/
    border: 1px solid #cccccc !important;
	background: #E9F0FF !important;
	padding: 1px 0px 0px 2px;
	margin-bottom: 2px;
}

.recentitems-box {
	width: 100%;
	margin: auto;
	padding: 1px 0px 1px 0px;
}

.recentitems-box A {
	display: block;
	padding: 1px 0px;
}

.recentitems-box .z-toolbar-start {
	float: right;
}
.recentitems-box A.trash-toolbarbutton {
	display: inline-block;
}
.recentitems-box .z-toolbar .z-toolbar-content {
	display: inline-flex;
	align-items: center;
}
.recentitems-box .trash-toolbarbutton .z-toolbarbutton-content {
	font-size: 16px;
}
.recentitems-box .trash-font-icon {
	font-family: FontAwesome;
	font-size: 20px;
}

.views-box {
	width: 100%;
	margin: auto;
	padding: 2px 0px 2px 0px;
}

.views-box .z-vbox {
	width: 100%;
}

.views-box .z-toolbarbutton {
	width: 100%;
    padding: 0px 5px 0px 0px;
    background: #E9F0FF !important;
    display: block;
    border: 1px solid #cccccc !important;
    margin: 0px 0px;
    border-radius:0px ;
}

.views-box .z-toolbarbutton:hover {
	background: #ffffbb !important;
	text-decoration: none !important;
	display: block;
    border: 1px solid #cccccc !important;
	padding: 0px 5px 0px 0px;
}

.views-box .link img {
    height: 16px;
    width: 16px;
    padding: 2px 2px 2px 2px;
}


.views-box .z-toolbarbutton-content{
	text-decoration: none !important;
	color: #333 !important;
	text-shadow: none !important;
}

.views-box .z-toolbarbutton-cnt {
	padding: 0px 0px 0px 0px;
	font-size: 11px !important;
}

.views-box .z-vbox-separator{
    height: 2px;
}

.activities-box {
	width: 100%;
 	margin: auto;
	padding: 0px 14px;
	background-color: #E9F0FF;/*JPIERE*/
	border: 1px solid #c8c8c8;/*JPIERE*/
}

.activities-box:hover {
    background-color: #ffffbb;
}

.activities-box .z-vbox {
	width: 100%;
}

.activities-box .z-button {
	text-align: left;
}

.recentitems-box .z-toolbar, .favourites-box .z-toolbar {
	margin-top: 1px;
	margin-bottom: 1px;
}

<%-- performance indicator --%>
.performance-indicator {
	margin: 0px;
	position: relative;
}
.performance-indicator img {
	display: block;
	margin: 0px;
	padding:0px;
}
.window-view-pi .performance-indicator img {
}
.performance-indicator-box {
	background-color: #eee;
	border: 1px solid #d8d8d8;
	border-radius: 11px;
	cursor: pointer;
}
.performance-indicator-title {
	text-align: center;
	background-color: #c8c8c8;
	border: 1px solid #c8c8c8;
	padding-top: 1px;
	padding-bottom: 1px;
	line-height:12px;
}
.performance-panel .z-grid {
	border: none;
	margin:0px;
	padding:0px;
	position: relative;
	width: 100%;
}

.dashboard-widget.dashboard-widget-max .chart-gadget {
	height: 100% !important;
}

.help-content
{
	padding: 2px;
	font-size: 11px;
	font-weight: normal;
}
.mobile .help-content
{
	font-size: 14px;
}

.fav-new-btn.z-toolbarbutton [class^="z-icon-"] {
	font-size: smaller;
	color: #333;
	/*padding-left: 4px;
	padding-right: 4px;*/
}

/*JPIERE-0110 Info Gadget*/
.jpiere-infogadget-content {
	background-color: #FFFFFF !important;
}

.jpiere-infogadget-attachment {
	background-color: #FFFFFF !important;
}
