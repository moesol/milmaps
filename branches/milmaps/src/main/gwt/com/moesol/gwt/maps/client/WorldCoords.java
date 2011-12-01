package com.moesol.gwt.maps.client;

public class WorldCoords {
	private int m_x;
	private int m_y;

	public WorldCoords() {
		m_x = m_y = 0;
	}
	
	public WorldCoords(int x, int y) {
		m_x = x;
		m_y = y;
	}

	public WorldCoords(WorldCoords v) {
		copyFrom(v);
	}

	public int getX() {
		return m_x;
	}

	public void setX(int x) {
		m_x = x;
	}

	public int getY() {
		return m_y;
	}

	public void setY(int y) {
		m_y = y;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + m_x;
		result = PRIME * result + m_y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof WorldCoords)) 
			return false;
		final WorldCoords other = (WorldCoords) obj;
		if (m_x != other.m_x)
			return false;
		if (m_y != other.m_y)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + m_x + "," + m_y + "]";
	}

	public void copyFrom(WorldCoords wc) {
		m_x = wc.getX();
		m_y = wc.getY();
	}
	
	private int round( double val ){
		if ( val < 0 )
			return (int)(val-0.5);
		return (int)(val + 0.5);
	}
	
	public void copyFrom(MapCoords mc) {
		
		m_x = round(mc.getX());
		m_y = round(mc.getY());
	}
}
