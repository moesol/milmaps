/**
 * (c) Copyright, Moebius Solutions, Inc., 2012
 *
 *                        All Rights Reserved
 *
 * LICENSE: GPLv3
 */
package com.moesol.gwt.maps.client.graphics;

import com.moesol.gwt.maps.client.GeodeticCoords;
import com.moesol.gwt.maps.client.IProjection;
import com.moesol.gwt.maps.client.ViewCoords;
import com.moesol.gwt.maps.client.ViewPort;
import com.moesol.gwt.maps.client.ViewWorker;
import com.moesol.gwt.maps.client.WorldCoords;


public class Converter implements  ICoordConverter{
	
	protected ViewPort m_viewPort;
	protected ISplit m_splitter = null;
	
	public Converter(){
	}
	
	public Converter(ViewPort vp){
		m_viewPort = vp;
		m_splitter = new Splitter(vp);
	}
	// CheckForExceptions
	
	protected void checkForExceptios(){
		if (m_viewPort == null) {
			throw new IllegalStateException("Converter: m_viewPort = null");
		}
	}
	
	@Override
	public void setViewPort(ViewPort vp) {
		m_viewPort = vp;
		m_splitter = new Splitter(vp);
	}
	
	// Coordinate conversion
	@Override
	public ViewCoords geodeticToView(GeodeticCoords gc) {
		checkForExceptios();
		ViewWorker vw = m_viewPort.getVpWorker();
		return vw.geodeticToView(gc);
	}

	@Override
	public GeodeticCoords viewToGeodetic(ViewCoords vc) {
		checkForExceptios();
		ViewWorker vw = m_viewPort.getVpWorker();
		WorldCoords wc = vw.viewToWorld(vc);
		IProjection proj = m_viewPort.getProjection();
		return proj.worldToGeodetic(wc);
	}

	@Override
	public WorldCoords geodeticToWorld(GeodeticCoords gc) {
		checkForExceptios();
		IProjection proj = m_viewPort.getProjection();
		return proj.geodeticToWorld(gc);
	}

	@Override
	public GeodeticCoords worldToGeodetic(WorldCoords wc) {
		checkForExceptios();
		IProjection proj = m_viewPort.getProjection();
		return proj.worldToGeodetic(wc);
	}
	
	@Override
	public int mapWidth(){
		checkForExceptios();
		IProjection proj = m_viewPort.getProjection();
		return proj.iMapWidth();
	}
	
	@Override
	public ViewCoords worldToView(WorldCoords wc){
		checkForExceptios();
		ViewWorker vw = m_viewPort.getVpWorker();
		return vw.wcToVC(wc);
	}
	
	@Override
	public  WorldCoords viewToWorld(ViewCoords vc){
		checkForExceptios();
		ViewWorker vw = m_viewPort.getVpWorker();
		return vw.viewToWorld(vc);
	}

	@Override
	public ISplit getISplit() {
		return m_splitter;
	}

	@Override
	public void setISplit(ISplit split) {
		m_splitter = split;
		
	}
}
