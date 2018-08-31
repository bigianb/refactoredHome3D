/*
 * PlanView.java 28 oct 2008
 *
 * Sweet Home 3D, Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.eteks.sweethome3d.viewcontroller;

import java.util.List;

import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.TextStyle;

/**
 * The view that displays the plan of a home.
 * @author Emmanuel Puybaret
 */
public interface PlanView extends TransferableView, ExportableView {
  /**
   * The cursor types available in plan view.
   */
  enum CursorType {SELECTION, PANNING, DRAW, ROTATION, ELEVATION, HEIGHT, POWER, RESIZE, DUPLICATION, MOVE}
  
  /**
   * Sets rectangle selection feedback coordinates. 
   */
  void setRectangleFeedback(float x0, float y0,
                            float x1, float y1);

  /**
   * Ensures selected items are visible at screen and moves
   * scroll bars if needed.
   */
  void makeSelectionVisible();

  /**
   * Ensures the point at (<code>x</code>, <code>y</code>) is visible,
   * moving scroll bars if needed.
   */
  void makePointVisible(float x, float y);

  /**
   * Returns the scale used to display the plan.
   */
  float getScale();

  /**
   * Sets the scale used to display the plan.
   */
  void setScale(float scale);

  /**
   * Moves the view from (dx, dy) unit in the scrolling zone it belongs to.
   */
  void moveView(float dx, float dy);

  /**
   * Returns <code>x</code> converted in model coordinates space.
   */
  float convertXPixelToModel(int x);

  /**
   * Returns <code>y</code> converted in model coordinates space.
   */
  float convertYPixelToModel(int y);

  /**
   * Returns <code>x</code> converted in screen coordinates space.
   */
  int convertXModelToScreen(float x);

  /**
   * Returns <code>y</code> converted in screen coordinates space.
   */
  int convertYModelToScreen(float y);

  /**
   * Returns the length in centimeters of a pixel with the current scale.
   */
  float getPixelLength();

  /**
   * Returns the coordinates of the bounding rectangle of the <code>text</code> displayed at
   * the point (<code>x</code>,<code>y</code>).  
   */
  float [][] getTextBounds(String text, TextStyle style,
                           float x, float y, float angle);

  /**
   * Sets the cursor of this component as rotation cursor. 
   */
  void setCursor(CursorType cursorType);

  /**
   * Sets tool tip text displayed as feedback. 
   * @param toolTipFeedback the text displayed in the tool tip 
   *                    or <code>null</code> to make tool tip disappear.
   */
  void setToolTipFeedback(String toolTipFeedback,
                          float x, float y);

  /**
   * Set properties edited in tool tip.
   */
  void setToolTipEditedProperties(PlanController.EditableProperty[] toolTipEditedProperties,
                                  Object[] toolTipPropertyValues,
                                  float x, float y);
  
  /**
   * Deletes tool tip text from screen. 
   */
  void deleteToolTipFeedback();

  /**
   * Sets whether the resize indicator of selected wall or piece of furniture 
   * should be visible or not. 
   */
  void setResizeIndicatorVisible(boolean resizeIndicatorVisible);

  /**
   * Sets the location point for alignment feedback.
   */
  void setAlignmentFeedback(Class<? extends Selectable> alignedObjectClass,
                            Selectable alignedObject,
                            float x,
                            float y,
                            boolean showPoint);
  

  /**
   * Sets the points used to draw an angle in plan view.
   */
  void setAngleFeedback(float xCenter, float yCenter,
                        float x1, float y1,
                        float x2, float y2);

  /**
   * Sets the feedback of dragged items drawn during a drag and drop operation, 
   * initiated from outside of plan view.
   */
  void setDraggedItemsFeedback(List<Selectable> draggedItems);

  /**
   * Sets the given dimension lines to be drawn as feedback.
   */
  void setDimensionLinesFeedback(List<DimensionLine> dimensionLines);

  /**
   * Deletes all elements shown as feedback.
   */
  void deleteFeedback();


  /**
   * Returns the component used as an horizontal ruler for this plan.
   */
  View getHorizontalRuler();

  /**
   * Returns the component used as a vertical ruler for this plan.
   */
  View getVerticalRuler();

  /**
   * Returns <code>true</code> if this plan accepts to import dragged items at the given coordinates.
   */
  boolean canImportDraggedItems(List<Selectable> items, int x, int y);

  /**
   * Returns the size of the given piece of furniture in the horizontal plan.
   */
  float [] getPieceOfFurnitureSizeInPlan(HomePieceOfFurniture piece);
  
  /**
   * Returns <code>true</code> if the view is able to compute the size of horizontally rotated furniture.
   */
  boolean isFurnitureSizeInPlanSupported();
}