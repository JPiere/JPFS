<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ taglib uri="http://www.idempiere.org/dsp/web/util" prefix="u" %>

.pivotwindow .grid-layout{
    border: 1px solid #0099ff !important;
    margin: 2px !important;
    /*background-color: #E9F0FF !important;*/
}

.pivotwindow .z-group td.z-group-inner{
    border-bottom: 1px solid #0099ff !important;
    /*background-color: #E9F0FF !important;*/
}


.pivotwindow .z-grid-body .z-cell{
    padding: 5px;
    line-height: 22px;
    margin-bottom: 10px;
}


.pivotwindow .z-pivottable {
    border: 1px solid #dddddd;
}

 .pivotwindow-searchFieldGrid
,.pivotwindow-otherPivotControllerGrid{
    margin: 2px !important;
    border: 1px solid #0099ff !important;
    background-color: #FFFFFF !important;
}

 .pivotwindow-predefinedButtonGrid
,.pivotwindow-pivotFieldControllerGrid
,.pivotwindow-protrudePivotControllerGrid{
    border: none !important;
    background-color: #FFFFFF !important;
}

.z-pivot-field-control .z-vlayout .z-grid-body{
	/* Intentional overflow : min > max */
    min-height: 221px;
    max-height: 220px;
    overflow: scroll;
}

 .pivotwindow-searchFieldGrid tr.z-grid-odd
,.pivotwindow-otherPivotControllerGrid tr.z-grid-odd{
    background-color: #FFFFFF !important;
}

.pivotwindow-searchFieldGrid .editor-input.z-combobox + .editor-button {
    top: 1px; /*JPIERE*/
}


.pivotwindow-popupcellinfo{
    max-width:480px;
    margin: 2px;
    padding: 2Px;
}

.pivotwindow-rowinfo
,.pivotwindow-columninfo
,.pivotwindow-drilldowninfo{
    width:100%;
    display: inline-block;
    line-height: 18px;
    padding: 4Px;
    font-size: 12px;
    border: 1px solid #dddddd;
}


.z-pivot-field-control .z-vlayout{
    border: 1px solid #cfcfcf;
}


.z-pivot-field-control .z-vlayout-inner{
    background-color:#eeeeee;
}

.z-pivot-field-control .z-flex-item {
    min-height: 250px;
}


