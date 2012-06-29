/**
 * (c) Copyright, Moebius Solutions, Inc., 2012
 *
 *                        All Rights Reserved
 *
 * LICENSE: GPLv3
 */
package com.moesol.gwt.maps.client.graphics;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.user.client.Event;
import com.moesol.gwt.maps.client.GeodeticCoords;
import com.moesol.gwt.maps.client.ViewCoords;

public class NewCircleTool implements IShapeTool {
	private boolean m_mouseDown = false;
	private Canvas m_canvas = null;
	private Circle m_circle = null;
	private IShapeEditor m_editor = null;
	private ICoordConverter m_convert;

	public NewCircleTool(IShapeEditor editor) {
		m_editor = editor;
		m_canvas = editor.getCanvasTool().canvas();
		m_convert = editor.getCoordinateConverter();
	}
	
	private void drawHandles(){
		Context2d context = m_canvas.getContext2d();
		//m_circle.erase(context);
		m_circle.drawHandles(context);
	}
	
	@Override
	public void setShape(IShape shape) {
		m_circle = (Circle)shape;
	}

	@Override
	public IShape getShape() {
		return m_circle;
	}

	@Override
	public void handleMouseDown(MouseDownEvent event) {
		m_mouseDown = true;
		int x = event.getX();
		int y = event.getY();
		ViewCoords vc = new ViewCoords(x, y);
		GeodeticCoords center = m_convert.viewToGeodetic(vc);
		m_circle = new Circle().withCenter(center);
		m_editor.addShape(m_circle);
		m_circle.selected(true);
		m_circle.setCoordConverter(m_editor.getCoordinateConverter());
		IAnchorTool tool = m_circle.getRadiusAnchorTool();
		m_editor.setAnchorTool(tool);
	}

	@Override
	public void handleMouseMove(MouseMoveEvent event) {
		if (m_mouseDown) {
			if (m_circle != null && m_canvas != null) {
				m_circle.getRadiusAnchorTool().handleMouseMove(event);
				m_editor.clearCanvas().renderObjects();
				drawHandles();
			}
		}
	}

	@Override
	public void handleMouseUp(MouseUpEvent event) {
		m_mouseDown = false;
		m_circle.getRadiusAnchorTool().handleMouseUp(event);
		//drawCenterHandle();
		// we are done with initial creation so set the edit tool
		IShapeTool tool = new EditCircleTool(m_editor);
		tool.setShape((IShape)m_circle);
		m_editor.setShapeTool(tool);
		m_editor.renderObjects();
		drawHandles();
		m_circle = null;
	}

	@Override
	public void handleMouseOut(MouseOutEvent event) {
	}

	@Override
	public void done() {
		m_editor.setShapeTool(null);
	}

	@Override
	public String getType() {
		return "new_circle_tool";
	}

	@Override
	public void setAnchor(IAnchorTool anchor) {
	}

	@Override
	public void hilite() {	
	}

	@Override
	public void handleMouseDblClick(Event event) {
	}
}
