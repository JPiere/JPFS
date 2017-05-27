/* .z-window.z-window-noborder.z-window-noheader.z-window-embedded
.z-window.z-window-noborder.z-window-noheader.z-window-embedded .z-window-content
twwo css make height of window always 100% its parent,
it fix some issue relate resize, but can make side effect
when detect side effect, fix to only apply for parameter window*/
.z-window.z-window-noborder.z-window-noheader.z-window-embedded,
.z-window.z-window-noborder.z-window-noheader.z-window-embedded .z-window-content,
.main-parameter-layout{
	height: 100%;
}

.main-parameter-layout,
.top-parameter-layout,
.bottom-parameter-layout,
.message-parameter-layout,
.input-paramenter-layout,
.option-paramenter-layout,
.report-option-container,
.bottom-container{
	width: 100%;
}
.top-parameter-layout{
	overflow: auto;
}

.bottom-parameter-layout{
    padding: 12px 15px 8px 15px;
    border-top: 1px solid #ddd;
    background-color: #f4f4f4;
}

.message-paramenter{
	max-height: 150pt;
	overflow: hidden;
	margin: 10px;
}

.option-input-parameter{

}

.bottom-container{
	overflow: auto;
	padding: 4px;/*JPIERE*/
}

.button-container{
	float: right;
}

.save-parameter-container{
	float: left;
	padding: 4px;/*JPIERE*/
}

.popup-dialog.z-window.z-window-overlapped.z-window-shadow,
.popup-dialog.z-window.z-window-noborder.z-window-highlighted.z-window-shadow{
	width:600px;
}

.input-paramenter-layout{
	width: 70%;
}

.popup-dialog .input-paramenter-layout{
	width: 90%;
}