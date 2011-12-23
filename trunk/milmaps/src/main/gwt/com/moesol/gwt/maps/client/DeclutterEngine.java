package com.moesol.gwt.maps.client;

import java.util.List;

import com.google.gwt.user.client.ui.Label;
import com.moesol.gwt.maps.client.util.BitSet;

/**
 * Declutter using O(n) grid layout.
 * 
 * @author hastings
 */
public class DeclutterEngine {
	/*
	 * The declutter search uses offsets from SEARCH_ROW_OFFSETS and SEARCH_COL_OFFSETS.
	 * The size of SEARCH_ROW_OFFSETS and SEARCH_COL_OFFSETS should be the same.
	 * This search pattern tries to put all the labels on the right vertically, then tries the same on the left.
	 */
	static int[] SEARCH_ROW_OFFSETS = {
		0, -1, -2, -3, -4, 1, 2, 3, 4, -5, -6, -7, -8, 5, 6, 7, 8,
//		0, -1, -2, -3, -4, 1, 2, 3, 4, -5, -6, -7, -8, 5, 6, 7, 8,
	};
	static int[] SEARCH_COL_OFFSETS = {
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
//		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	};
	static int CELL_WIDTH = 4; // px
	static int CELL_HEIGHT = 8; // px
	
	private final IMapView m_mapView;
	private BitSet m_bitSet;
	int m_nRowsInView;
	int m_nColsInView;
	private int m_iconCenterRow;
	private int m_iconStartRow;
	private int m_iconEndRow;
	private int m_iconStartCol;
	private int m_iconEndCol;
	private ViewCoords m_iconCenter;
	
	static class GridCoords {
		final int row;
		final int col;
		public GridCoords(int r, int c) {
			row = r;
			col = c;
		}
		@Override
		public String toString() {
			return "GridCoords [row=" + row + ", col=" + col + "]";
		}
	}

	public DeclutterEngine(IMapView mapView) {
		m_mapView = mapView;
	}

	public void declutter(List<Icon> icons) {
		makeBitSet();
		
		for (Icon i : icons) {
			markOneIcon(i);
		}
		for (Icon i : icons) {
			positionOne(i);
		}
	}

	/**
	 * Marks the bits that the icon (symbol) will occupy. We assume
	 * the icon offset centers the icon. If not we might be able to
	 * use the image width/height.
	 * 
	 * @param icon
	 */
	private void markOneIcon(Icon icon) {
		computeIconBounds(icon);

		int startRow = Math.max(0, m_iconStartRow);
		int endRow = Math.min(m_nRowsInView, m_iconEndRow);

		int startCol = Math.max(0, m_iconStartCol);
		int endCol = Math.min(m_nColsInView, m_iconEndCol);
		
		for (int r = startRow; r <= endRow; r++) {
			for (int c = startCol; c <= endCol; c++) {
				m_bitSet.set(r * m_nColsInView + c);
			}
		}
	}

	private void computeIconBounds(Icon icon) {
		m_iconCenter = computeIconCenterViewCoords(icon);
		ViewCoords topLeft = m_iconCenter.translate(icon.getIconOffset());
		ViewCoords bottomRight = m_iconCenter.translate(icon.getIconOffset().scale(-1));

		GridCoords centerGC = computeIconCenterGridCoords(m_iconCenter);
		GridCoords topLeftGC = computeIconCenterGridCoords(topLeft);
		GridCoords bottomRightGC = computeIconCenterGridCoords(bottomRight);
		
		m_iconCenterRow = centerGC.row;
		m_iconStartRow = Math.min(topLeftGC.row, bottomRightGC.row);
		m_iconEndRow = Math.max(topLeftGC.row, bottomRightGC.row);
		m_iconStartCol = Math.min(topLeftGC.col, bottomRightGC.col);
		m_iconEndCol = Math.max(topLeftGC.col, bottomRightGC.col);
	}

	int makeBitSet() {
		ViewPort viewport = m_mapView.getViewport();
		m_nRowsInView = roundUp(viewport.getHeight(), CELL_HEIGHT);
		m_nColsInView = roundUp(viewport.getWidth(), CELL_WIDTH);
		int numBits = m_nRowsInView * m_nColsInView;
		m_bitSet = new BitSet(numBits);
		return numBits;
	}
	
	private void positionOne(Icon icon) {
		Label label = icon.getLabel();
		if (label == null) {
			return;
		}
		searchSlots(icon);
	}

	private void searchSlots(Icon icon) {
		computeIconBounds(icon);
		
		int nLabelCols = computeLabelColumnSpan(icon);
		int nLabelRows = computeLabelRowSpan(icon);
		for (int i = 0; i < SEARCH_ROW_OFFSETS.length; i++) {
			int rowOffset = SEARCH_ROW_OFFSETS[i];
			int colOffset = SEARCH_COL_OFFSETS[i];

			int startRow = m_iconCenterRow + rowOffset;
			int startCol;
			if (colOffset < 0) {
				startCol = m_iconStartCol + colOffset - nLabelCols;
				colOffset -= nLabelCols;
			} else {
				startCol = m_iconEndCol + colOffset;
			}
			
			if (searchOneSlot(startRow, startCol, nLabelRows, nLabelCols)) {
				moveDeclutterOffset(icon, rowOffset, colOffset);
				return;
			}
		}
		// No cell found, move offscreen.
		moveDeclutterOffset(icon, m_nRowsInView + 1, m_nColsInView + 1);
	}

	ViewCoords computeIconCenterViewCoords(Icon icon) {
		WorldCoords wc = m_mapView.getProjection().geodeticToWorld(icon.getLocation());
		ViewCoords vc = m_mapView.getViewport().worldToView(wc, true);
		return new ViewCoords(vc.getX(), vc.getY());
	}
	
	private void moveDeclutterOffset(Icon icon, int rowOffset, int colOffset) {
		int cellY = m_iconCenter.getY() % CELL_HEIGHT;
		int cellX = m_iconCenter.getX() % CELL_WIDTH;
		
		int offsetY = rowOffset * CELL_HEIGHT - cellY;
		int offsetX = colOffset * CELL_WIDTH;
		if (colOffset < 0) { // left
			offsetX += icon.getIconOffset().getX();
			offsetX -= cellX;
		} else {
			offsetX -= icon.getIconOffset().getX();
			offsetX += CELL_WIDTH - cellX;
		}
		icon.getDeclutterOffset().setY(offsetY);
		icon.getDeclutterOffset().setX(offsetX);
	}

	private boolean searchOneSlot(int startRow, int startCol, int nLabelRows, int nLabelCols) {
		if (startRow < 0) {
			return false;
		}
		if (startCol < 0) {
			return false;
		}
		int endRows = startRow + nLabelRows;
		int endCols = startCol + nLabelCols;
		if (endRows >= m_nRowsInView) {
			return false;
		}
		if (endCols >= m_nColsInView) {
			return false;
		}
		
		for (int r = startRow; r < endRows; r++) {
			for (int c = startCol; c < endCols; c++) {
				if (m_bitSet.get(r * m_nColsInView + c)) {
					// Collision
					return false;
				}
			}
		}
		
		// OK we found a slot mark it used.
		for (int r = startRow; r < endRows; r++) {
			for (int c = startCol; c < endCols; c++) {
				m_bitSet.set(r * m_nColsInView + c);
			}
		}
		return true;
	}

	private GridCoords computeIconCenterGridCoords(ViewCoords vc) {
		GridCoords rc = new GridCoords(vc.getY() / CELL_HEIGHT, vc.getX() / CELL_WIDTH);
		return rc;
	}

	private int computeLabelColumnSpan(Icon icon) {
		return roundUp(icon.getLabel().getOffsetWidth(), CELL_WIDTH);
	}

	private int computeLabelRowSpan(Icon icon) {
		return roundUp(icon.getLabel().getOffsetHeight(), CELL_HEIGHT);
	}

	static int roundUp(int total, int dividend) {
		return (total + (dividend - 1)) / dividend;
	}
}
