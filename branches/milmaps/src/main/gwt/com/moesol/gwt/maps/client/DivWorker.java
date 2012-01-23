package com.moesol.gwt.maps.client;

import java.util.List;

import com.moesol.gwt.maps.client.events.ProjectionChangedEvent;
import com.moesol.gwt.maps.client.events.ProjectionChangedHandler;
import com.moesol.gwt.maps.client.stats.Sample;

/**
 * DivWorker class is used to help handle all tile placements. It works with just one projection
 * scale and original dimension. It should be used in conjunction with tile builder.
 * @author <a href="www.moesol.com">Moebius Solutions, Inc.</a>
 */
public class DivWorker implements ProjectionChangedHandler {
	private GeodeticCoords m_geoCenter = new GeodeticCoords();
	private MapCoords m_divCenterMc = null; // viewport center in wc.
	private final DivDimensions m_baseDims = new DivDimensions();
	private double m_eqScale;
	private double m_offsetInMcX;
	private double m_offsetInMcY;
	private IProjection m_proj = null;
	
	private int m_mapLevel;
	
	public class BoxBounds {
		public double top;
		public double left;
		public double bot;
		public double right;
		
		@Override
		public String toString() {
			return "[" +" top: "+ top + ", left: "+ left +" bot: "+ bot + ", right: "+ right + "]";
		}
	}
	public BoxBounds m_boxBounds = new BoxBounds();
	
	public DivWorker(){
		
	}
	
	public void copyFrom( DivWorker dw ){
		setDivBaseDimensions(dw.getDivBaseDimensions());
		setGeoCenter(dw.getGeoCenter());
	}
	
	public void setDivBaseDimensions( DivDimensions dd ){ m_baseDims.copyFrom(dd); }
	public DivDimensions getDivBaseDimensions(){ return m_baseDims; }
	
	public void setDivBasePixelSize( int width, int height){
		m_baseDims.setWidth(width);
		m_baseDims.setHeight(height);
	}
	
	public void setProjection( IProjection p ){ 
		m_proj = p; 
	}
	
	public IProjection getProjection(){ return m_proj; }
	
	public void setMapLevel(int mapLevel) {
		this.m_mapLevel = mapLevel;
	}
	
	public int getMapLevel() {
		return m_mapLevel;
	}
	
	public void setEqScale(double scale){ 
		m_eqScale = scale; 
	}
	
	public double getEqScale(){ return m_eqScale; }
	
	public void setGeoCenter( GeodeticCoords gc ){
		m_geoCenter = gc;
	}
	
	public GeodeticCoords getGeoCenter(){
		return m_geoCenter;
	}
	
	public void setOffsetInWcX(int offsetInWcX) {
		this.m_offsetInMcX = offsetInWcX;
	}

	public int getOffsetInWcX() {
		return (int) Math.rint(m_offsetInMcX);
	}
	
	public void setOffsetInWcY(int offsetInWcY) {
		this.m_offsetInMcY = offsetInWcY;
	}

	public int getOffsetInWcY() {
		return (int) Math.rint(m_offsetInMcY);
	}
	
	public void setDiv( GeodeticCoords gc ){
		m_geoCenter = gc;
		m_divCenterMc = m_proj.geodeticToMapCoords(m_geoCenter);
		computeOffsets();
	}
	
	public void computeOffsets(){
		m_offsetInMcX = m_divCenterMc.getX()- m_baseDims.getWidth()/2;
		m_offsetInMcY = m_divCenterMc.getY()+ m_baseDims.getHeight()/2;
	}
	
	public int wcXtoDcX(int wcX ){
		return (int) Math.rint((wcX - m_offsetInMcX));
	}
	
	public int wcYtoDcY( int wcY ){
		// Normally we would have vY = wc.getY() - m_offsetInWcY.
		// But for the view y axis we want the y values changed to be relative to the 
		// div's top. So we will subtract dY from the div's top. This will also
		// flip the direction of the divs's y axis
		return (int) Math.rint((m_offsetInMcY - wcY));	// flip y axis	
	}
	
	/**
	 * wcToVC converts World coordinates to div coordinates based on the div's size
	 * and where the div's center sits in the world coordinate system.`
	 * @param wc
	 * @param checkWrap
	 * @return ViewCoords
	 */
	public DivCoords wcToDC( WorldCoords wc ) {
		return new DivCoords(wcXtoDcX(wc.getX()), wcYtoDcY(wc.getY()));
	}
	
	public DivCoords wcToDC( int wcX, int wcY ) {
		return new DivCoords(wcXtoDcX(wcX), wcYtoDcY(wcY));
	}
	
	public int dcXtoWcX( int dcX ){
		return (int) Math.rint((dcX + m_offsetInMcX));
	}
	
	public int dcYtoWcY( int dcY ){
		return (int) Math.rint((m_offsetInMcY - dcY));
	}
	
	/**
	 * dcXToPercent converts Div x pixel to percent position based on the div's size
	 * @param vc
	 * @return
	 */
	public double dcXToPercent( int dcX ) {
		double x = dcX;
		return x/m_baseDims.getWidth();
	}
	
	/**
	 * dcXToPercent converts Div x pixel to percent position based on the div's size
	 * @param vc
	 * @return
	 */
	public double dcYToPercent( int dcY ) {
		double y = dcY;
		return y/m_baseDims.getHeight();
	}
	
	/**
	 * computeTileBounds computes a given tiles percentage "top, left, bottom, right 
	 * position in a divPanel at its current state. 
	 * @param tileX
	 * @param tileY
	 * @param width
	 * @param height
	 * @return
	 */
	public BoxBounds computePerccentBounds ( int x, int y, int width, int height ){
		
		m_boxBounds.top   = dcYToPercent(y)*100;
		m_boxBounds.left  = dcXToPercent(x)*100;
		m_boxBounds.bot   = dcYToPercent(y+height+1)*100;
		m_boxBounds.right = dcXToPercent(x+width+1)*100;
		return m_boxBounds;
	}
	
	public double getScaleFactor(IProjection mapProj) {
		double mapScale = mapProj.getEquatorialScale();
		double divOrigScale = m_proj.getEquatorialScale();
		return (mapScale/divOrigScale);
	}
	
	public ViewCoords computeDivLayoutInView( IProjection mapProj, ViewWorker vw, DivDimensions dim ) {
		int viewOy = vw.getOffsetInWcY();
		double factor = getScaleFactor(mapProj);
		dim.setWidth((int)(m_baseDims.getWidth()*factor + 0.5));
		dim.setHeight((int)(m_baseDims.getHeight()*factor + 0.5));
		WorldCoords wc = mapProj.geodeticToWorld(m_geoCenter);
		int left = computeDivLeft(wc, dim, vw, factor);
		int top  = viewOy - (wc.getY()+ dim.getHeight()/2);
		return new ViewCoords(left, top);
	}

	private int computeIntersect(int left1, int width1, int left2, int width2) {
		int maxLeft = Math.max(left1, left2);
		int minRight = Math.min(left1 + width1, left2 + width2);
		return minRight - maxLeft;
	}
	
	private int computeDivLeft(WorldCoords wc, DivDimensions div, ViewWorker vw, double factor) {
		int worldWidth = (int)(factor * m_proj.getWorldDimension().getWidth() + 0.5);
		int viewOx = vw.getOffsetInWcX();
		ViewDimension view = vw.getDimension();
		
		int left = (wc.getX()- div.getWidth()/2) - viewOx;
		if (left > 0) {
			// Part of view empty, rotate?
			//    0 ------------- vw
			//       left----dw
			//       left--------------dw
			int oldIntersection = computeIntersect(0, view.getWidth(), left, div.getWidth());
			int newLeft = left - worldWidth;
			int newIntersection = computeIntersect(0, view.getWidth(), newLeft, div.getWidth());
			if (newIntersection > oldIntersection) {
				left = newLeft;
			}
		} else if (left + div.getWidth() < view.getWidth()) {
			// Part of view empty, rotate?
			//       0 ------------------ vw
			//  left----------dw
			//          left-------dw
			int oldIntersection = computeIntersect(0, view.getWidth(), left, div.getWidth());
			int newLeft = left + worldWidth;
			int newIntersection = computeIntersect(0, view.getWidth(), newLeft, div.getWidth());
			if (newIntersection > oldIntersection) {
				left = newLeft;
			}
		}
		return left;
	}
	
	@Override
	public String toString() {
		int oWcX = (int)Math.rint(m_offsetInMcX);
		int oWcY = (int)Math.rint(m_offsetInMcY);
		return "[" +" ox: "+ oWcX + ", oy: "+ oWcY + "]";
	}

	@Override
	public void onProjectionChanged(ProjectionChangedEvent event) {
		computeOffsets();
	}
	
}
