/*
 * TransferHandlerTest.java 11 sept 06
 * 
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights
 * Reserved.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.junit;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.finder.ComponentSearchException;
import abbot.tester.ComponentLocation;
import abbot.tester.JComponentTester;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.FurnitureCatalogTree;
import com.eteks.sweethome3d.swing.FurnitureTable;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.swing.HomeTransferableList;
import com.eteks.sweethome3d.swing.PlanComponent;
import com.eteks.sweethome3d.swing.SwingViewFactory;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.PlanController;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;

/**
 * Tests drag and drop, and cut / copy / paste.
 * @author Emmanuel Puybaret
 */
public class TransferHandlerTest extends ComponentTestFixture {
  public void testTransferHandler() throws ComponentSearchException, UnsupportedFlavorException, 
                                           IOException, InterruptedException, InvocationTargetException {
    UserPreferences preferences = new DefaultUserPreferences();
    preferences.setFurnitureCatalogViewedInTree(true);
    ViewFactory viewFactory = new SwingViewFactory();
    Home home = new Home();
    home.getCompass().setVisible(false);
    final HomeController controller = new HomeController(home, preferences, viewFactory);
    JComponent homeView = (JComponent)controller.getView();
    final FurnitureCatalogTree catalogTree = (FurnitureCatalogTree)TestUtilities.findComponent(
         homeView, FurnitureCatalogTree.class);
    FurnitureTable furnitureTable = (FurnitureTable)TestUtilities.findComponent(
        homeView, FurnitureTable.class);
    final PlanComponent planComponent = (PlanComponent)TestUtilities.findComponent(
         homeView, PlanComponent.class);

    // 1. Create a frame that displays a home view 
    JFrame frame = new JFrame("Home TransferHandler Test");    
    frame.add(homeView);
    frame.pack();
    // Ensure clipboard is empty
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(""), null);

    // Show home plan frame
    showWindow(frame);
    final JComponentTester tester = new JComponentTester();
    tester.waitForIdle();
    // Check catalog tree has default focus
    assertTrue("Tree doesn't have the focus", catalogTree.isFocusOwner());
    // Check Cut, Copy, Paste and Delete actions are disable
    assertActionsEnabled(controller, false, false, false, false);
    
    // 2. Select the first piece of furniture in catalog
    tester.invokeAndWait(new Runnable() {
      public void run() {
        catalogTree.expandRow(0); 
        catalogTree.addSelectionInterval(1, 1);
      }
    });
    // Check only Copy action is enabled
    assertActionsEnabled(controller, false, true, false, false);
    
    // 3. Drag and drop selected piece in tree to point (120, 120) in plan component
    Rectangle selectedRowBounds = catalogTree.getRowBounds(1);
    tester.actionDrag(catalogTree, new ComponentLocation( 
        new Point(selectedRowBounds.x, selectedRowBounds.y)));
    tester.actionDrop(planComponent, new ComponentLocation( 
        new Point(120, 120))); 
    tester.waitForIdle();
    // Check a piece was added to home
    assertEquals("Wrong piece count in home", 1, home.getFurniture().size());
    // Check top left corner of the piece is at (200, 200) 
    HomePieceOfFurniture piece = home.getFurniture().get(0);
    assertTrue("Incorrect X " + piece.getX(), 
        Math.abs(200 - piece.getX() + piece.getWidth() / 2) < 1E-5);
    assertTrue("Incorrect Y " + piece.getY(), 
        Math.abs(200 - piece.getY() + piece.getDepth() / 2) < 1E-5);

    // 4.  Transfer focus to plan view with TAB keys
    tester.actionKeyStroke(KeyEvent.VK_TAB);
    tester.actionKeyStroke(KeyEvent.VK_TAB);
    // Check plan component has focus
    assertTrue("Plan doesn't have the focus", planComponent.isFocusOwner());
    // Check Cut, Copy and Delete actions are enabled in plan view
    assertActionsEnabled(controller, true, true, false, true);

    // 5. Use Wall creation mode
    tester.invokeAndWait(new Runnable() {
      public void run() {
        controller.getPlanController().setMode(PlanController.Mode.WALL_CREATION);
      }
    });
    // Check Cut, Copy, Paste actions are enabled
    assertActionsEnabled(controller, true, true, false, true);    
    // Create a wall between points (25, 25) and (100, 25)
    tester.actionClick(planComponent, 25, 25);
    // Check Cut, Copy, Paste actions are disabled during wall drawing
    assertActionsEnabled(controller, false, false, false, false);    
    tester.actionClick(planComponent, 100, 25, InputEvent.BUTTON1_MASK, 2);

    // 6. Use Dimension creation mode
    tester.invokeAndWait(new Runnable() {
      public void run() {
        controller.getPlanController().setMode(PlanController.Mode.DIMENSION_LINE_CREATION);
      }
    });
    // Check Cut, Copy, Paste actions are enabled
    assertActionsEnabled(controller, true, true, false, true);
    // 7. Create a dimension line between points (25, 35) and (100, 35)
    tester.actionClick(planComponent, 25, 35);
    // Check Cut, Copy, Paste actions are disabled during dimension line drawing
    assertActionsEnabled(controller, false, false, false, false);    
    tester.actionClick(planComponent, 100, 35, InputEvent.BUTTON1_MASK, 2);
    // Use Selection mode 
    tester.invokeAndWait(new Runnable() {
        public void run() {
          controller.getPlanController().setMode(PlanController.Mode.SELECTION);
        }
      });
    // Check Cut, Copy and Delete actions are enabled
    assertActionsEnabled(controller, true, true, false, true);
    
    // 7. Select the dimension, the wall and the piece 
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    tester.actionClick(planComponent, 30, 25); 
    tester.actionClick(planComponent, 120, 120); 
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);
    // Check home selection contains 3 items
    assertEquals("Selected items wrong count", 3, home.getSelectedItems().size());
    // Cut selected items in plan component
    runAction(tester, controller, HomePane.ActionType.CUT);
    // Check home is empty
    assertEquals("Wrong piece count in home", 0, home.getFurniture().size());
    assertEquals("Wrong wall count in home", 0, home.getWalls().size());
    assertEquals("Wrong dimension count in home", 0, home.getDimensionLines().size());
    // Check only Paste action is enabled
    assertActionsEnabled(controller, false, false, true, false);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    // Check clipboard contains two different data flavors (HomeTransferableList and Image)
    assertTrue("Missing home data flavor", clipboard.isDataFlavorAvailable(HomeTransferableList.HOME_FLAVOR));
    assertTrue("Missing String flavor", clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor));

    // 8. Paste selected items in plan component
    runAction(tester, controller, HomePane.ActionType.PASTE);
    tester.waitForIdle();
    // Check home contains one piece, one wall and one dimension
    assertEquals("Wrong piece count in home", 1, home.getFurniture().size());
    assertEquals("Wrong wall count in home", 1, home.getWalls().size());
    assertEquals("Wrong dimension count in home", 1, home.getDimensionLines().size());

    // 9. Transfer focus to furniture table
    tester.actionKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK);
    // Check furniture table has focus
    assertTrue("Table doesn't have the focus", furnitureTable.isFocusOwner());
    // Delete selection 
    runAction(tester, controller, HomePane.ActionType.DELETE);
    // Check home contains no piece, one wall and one dimension
    assertEquals("Wrong piece count in home", 0, home.getFurniture().size());
    assertEquals("Wrong wall count in home", 1, home.getWalls().size());
    assertEquals("Wrong dimension count in home", 1, home.getDimensionLines().size());
    // Check only Paste action is enabled
    assertActionsEnabled(controller, false, false, true, false);

    // 10. Paste selected items in furniture table
    runAction(tester, controller, HomePane.ActionType.PASTE);
    // Check home contains one piece, one wall and one dimension
    assertEquals("Wrong piece count in home", 1, home.getFurniture().size());
    assertEquals("Wrong wall count in home", 1, home.getWalls().size());
    assertEquals("Wrong dimension count in home", 1, home.getDimensionLines().size());
    // Check Cut, Copy and Paste actions are enabled
    assertActionsEnabled(controller, true, true, true, true);
    
    // 11. Copy selected furniture in clipboard while furniture table has focus
    runAction(tester, controller, HomePane.ActionType.COPY);    
    // Check clipboard contains two different data flavors (HomeTransferableList and String)
    assertTrue("Missing home data flavor", clipboard.isDataFlavorAvailable(HomeTransferableList.HOME_FLAVOR));
    assertTrue("Missing String flavor", clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor));
  }
  
  /**
   * Runs <code>actionPerformed</code> method matching <code>actionType</code> 
   * in <code>HomePane</code>. 
   */
  private void runAction(JComponentTester tester, final HomeController controller,
                         final HomePane.ActionType actionType) {
    tester.invokeAndWait(new Runnable() {
        public void run() {
          getAction(controller, actionType).actionPerformed(null);
        }
      });
  }

  /**
   * Returns the action matching <code>actionType</code> in <code>HomePane</code>. 
   */
  private Action getAction(HomeController controller,
                           HomePane.ActionType actionType) {
    return ((JComponent)controller.getView()).getActionMap().get(actionType);
  }
  
  /**
   * Asserts CUT, COPY, PASTE and DELETE actions in <code>HomePane</code> 
   * are enabled or disabled. 
   */
  private void assertActionsEnabled(HomeController controller,
                                    boolean cutActionEnabled, 
                                    boolean copyActionEnabled, 
                                    boolean pasteActionEnabled, 
                                    boolean deleteActionEnabled) {
    assertTrue("Cut action invalid state", 
        cutActionEnabled == getAction(controller, HomePane.ActionType.CUT).isEnabled());
    assertTrue("Copy action invalid state", 
        copyActionEnabled == getAction(controller, HomePane.ActionType.COPY).isEnabled());
    assertTrue("Paste action invalid state", 
        pasteActionEnabled == getAction(controller, HomePane.ActionType.PASTE).isEnabled());
    assertTrue("Delete action invalid state", 
        deleteActionEnabled == getAction(controller, HomePane.ActionType.DELETE).isEnabled());
  }
}
