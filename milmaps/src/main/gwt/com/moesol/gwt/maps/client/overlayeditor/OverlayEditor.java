/**
 * (c) Copyright, Moebius Solutions, Inc., 2012
 *
 *                        All Rights Reserved
 *
 * LICENSE: GPLv3
 */
package com.moesol.gwt.maps.client.overlayeditor;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.moesol.gwt.maps.client.MapView;
import com.moesol.gwt.maps.client.controls.MapButton;
import com.moesol.gwt.maps.client.graphics.IShapeEditor;
import com.moesol.gwt.maps.client.graphics.ShapeEditor;

// we map want to add widgets to the editor.
public class OverlayEditor extends Composite {

	private final IShapeEditor m_shapeEditor;
	private ShapeSelectionDialog m_shapeSelDlg = null;
	
    public OverlayEditor(MapView mapView, boolean bHorizontal) {
        super();
        m_shapeEditor = new ShapeEditor(mapView);
        setMapView(bHorizontal);
        
    }

    public void setMapView(boolean bHorizontal) {
        // Off button
        //MapButton offBtn = new MapButton();
        //offBtn.addStyleName("map-OverlayContolOffButton");

        //offBtn.addClickHandler(new ClickHandler() {
        //    @Override
        //    public void onClick(ClickEvent event) {
        //    	m_shapeEditor.setEventFocus(false);
        //    }
        //});

        // On Button
        MapButton onBtn = new MapButton();
        onBtn.addStyleName("map-OverlayContolOnButton");

        onBtn.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
            	int x = event.getClientX();
            	int y = event.getClientY();
            	showShapeSelectionDlg(x,y);
            }
        });

        if (bHorizontal) {
            HorizontalPanel p = new HorizontalPanel();
            //p.add(offBtn);
            p.add(onBtn);
            initWidget(p);
        } else {
            VerticalPanel p = new VerticalPanel();
            //p.add(offBtn);
            p.add(onBtn);
            initWidget(p);
        }
        addStyleName("map-OverlayEditor");
        setZindex(100000);
    }
    
    
    
    public void setZindex(int zIndex) {
        this.getElement().getStyle().setZIndex(zIndex);
    }
    
    private void showShapeSelectionDlg(int x, int y){
    	m_shapeSelDlg = new ShapeSelectionDialog(m_shapeEditor);
    	m_shapeSelDlg.getElement().getStyle().setProperty("zIndex", Integer.toString(9000));
    	m_shapeSelDlg.setPopupPosition(x-150, y-150);
    	m_shapeSelDlg.show();
    }
}


/*
MouseDownHandler mdhandler = new MouseDownHandler(){
	@Override
	public void onMouseDown(MouseDownEvent event) {
		mouseDown(event.getX(), event.getY());
	}
};
m_mapView.addDomHandler(mdhandler, MouseDownEvent.getType());
*/
