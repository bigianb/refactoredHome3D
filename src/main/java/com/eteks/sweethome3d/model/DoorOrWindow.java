/*
 * DoorOrWindow.java 8 mars 2009
 *
 * Sweet Home 3D, Copyright (c) 2009 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
package com.eteks.sweethome3d.model;

/**
 * A piece of furniture used as a door or a window.
 * @author Emmanuel Puybaret
 * @since  1.7
 */
public interface DoorOrWindow extends PieceOfFurniture {
  /**
   * Returns the default thickness of the wall in which this door or window should be placed.
   * @return a value in percentage of the depth of this door or window.
   */
  public abstract float getWallThickness();
  
  /**
   * Returns the default distance that should lie outside of this door or window.
   * @return a distance in percentage of the depth of this door or the window.
   */
  public abstract float getWallDistance();
  
  /**
   * Returns a copy of the sashes attached to this door or window.
   * If no sash is defined an empty array is returned. 
   */
  public abstract Sash [] getSashes();

  /**
   * Returns the shape used to cut out walls that intersect this new door or window.
   * @return <code>null</code> or a shape defined with the syntax of the d attribute of a 
   * <a href="http://www.w3.org/TR/SVG/paths.html">SVG path element</a>
   * that fits in a square spreading from (0, 0) to (1, 1) which will be 
   * scaled afterwards to the real size of this door or window. 
   * @since 4.2
   */
  public abstract String getCutOutShape();
  
  /**
   * Returns <code>true</code> if this door or window should cut out the both sides
   * of the walls it intersects, even if its front or back side are within the wall thickness. 
   * @since 5.5 
   */
  public abstract boolean isWallCutOutOnBothSides();

  /**
   * Returns <code>false</code> if the width and depth of the new door or window may 
   * not be changed independently from each other. When <code>false</code>, this door or window
   * will also make a hole in the wall when it's placed whatever its depth if its 
   * {@link #isBoundToWall() bouldToWall} flag is <code>true</code>.
   * @since 5.5
   */
  public abstract boolean isWidthDepthDeformable();
}
