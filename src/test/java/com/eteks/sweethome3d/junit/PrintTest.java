/*
 * PrintTest.java 27 aout 2007
 * 
 * Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.junit;

import java.awt.Dialog;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.finder.AWTHierarchy;
import abbot.finder.BasicFinder;
import abbot.finder.ComponentSearchException;
import abbot.finder.matchers.ClassMatcher;
import abbot.tester.JComponentTester;
import abbot.tester.JFileChooserTester;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePrint;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.FileContentManager;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.swing.HomePrintableComponent;
import com.eteks.sweethome3d.swing.PageSetupPanel;
import com.eteks.sweethome3d.swing.PrintPreviewPanel;
import com.eteks.sweethome3d.swing.SwingViewFactory;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.View;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;

/**
 * Tests page setup and print preview panes in home.
 * @author Emmanuel Puybaret
 */
public class PrintTest extends ComponentTestFixture {
  public void testPageSetupAndPrintPreview() throws ComponentSearchException, InterruptedException, 
      NoSuchFieldException, IllegalAccessException, InvocationTargetException, IOException {
    UserPreferences preferences = new DefaultUserPreferences();
    ViewFactory viewFactory = new SwingViewFactory();
    Home home = new Home();
    ContentManager contentManager = new FileContentManager(preferences) {
        @Override
        public String showSaveDialog(View parentView, String dialogTitle, ContentType contentType, String name) {
          String os = System.getProperty("os.name");
          if (OperatingSystem.isMacOSX()) {
            // Let's pretend the OS isn't Mac OS X to get a JFileChooser instance that works better in test
            System.setProperty("os.name", "dummy");
          }
          try {
            return super.showSaveDialog(parentView, dialogTitle, contentType, name);
          } finally {
            System.setProperty("os.name", os);
          }
        }
      };
    final HomeController controller = 
        new HomeController(home, preferences, viewFactory, contentManager);

    // 1. Create a frame that displays a home view 
    JFrame frame = new JFrame("Home Print Test");    
    frame.add((JComponent)controller.getView());
    frame.pack();

    // Show home frame
    showWindow(frame);
    JComponentTester tester = new JComponentTester();
    tester.waitForIdle();
    // Add a piece of furniture to home
    List<CatalogPieceOfFurniture> selectedPieces = Arrays.asList(
        new CatalogPieceOfFurniture [] {preferences.getFurnitureCatalog().getCategories().get(0).getFurniture().get(0)}); 
    controller.getFurnitureCatalogController().setSelectedFurniture(selectedPieces);
    tester.invokeAndWait(new Runnable() { 
      public void run() {
        runAction(controller, HomePane.ActionType.ADD_HOME_FURNITURE);
      }
    });
    // Check home contains one piece
    assertEquals("Home doesn't contain any furniture", 1, home.getFurniture().size());
    
    // 2. Edit page setup dialog box
    tester.invokeLater(new Runnable() { 
        public void run() {
          // Display dialog box later in Event Dispatch Thread to avoid blocking test thread
          runAction(controller, HomePane.ActionType.PAGE_SETUP);
        }
      });
    // Wait for page setup to be shown
    tester.waitForFrameShowing(new AWTHierarchy(), preferences.getLocalizedString(
        PageSetupPanel.class, "pageSetup.title"));
    // Check dialog box is displayed
    JDialog pageSetupDialog = (JDialog)TestUtilities.findComponent(
        frame, JDialog.class);
    assertTrue("Page setup dialog not showing", pageSetupDialog.isShowing());
    // Retrieve PageSetupPanel components
    PageSetupPanel pageSetupPanel = (PageSetupPanel)TestUtilities.findComponent(
        frame, PageSetupPanel.class);
    JCheckBox furniturePrintedCheckBox = 
        (JCheckBox)TestUtilities.getField(pageSetupPanel, "furniturePrintedCheckBox");
    JCheckBox planPrintedCheckBox = 
        (JCheckBox)TestUtilities.getField(pageSetupPanel, "planPrintedCheckBox");;
    JCheckBox view3DPrintedCheckBox =
        (JCheckBox)TestUtilities.getField(pageSetupPanel, "view3DPrintedCheckBox");
    // Check default edited values
    assertTrue("Furniture printed not checked", furniturePrintedCheckBox.isSelected());
    assertTrue("Plan printed not checked", planPrintedCheckBox.isSelected());
    assertTrue("View 3D printed not checked", view3DPrintedCheckBox.isSelected());
    
    // 3. Change dialog box values
    planPrintedCheckBox.setSelected(false);
    // Click on Ok in dialog box
    final JOptionPane pageSetupOptionPane = (JOptionPane)TestUtilities.findComponent(
        pageSetupDialog, JOptionPane.class);
    tester.invokeAndWait(new Runnable() {
        public void run() {
          // Select Ok option to hide dialog box in Event Dispatch Thread
          pageSetupOptionPane.setValue(JOptionPane.OK_OPTION); 
        }
      });
    assertFalse("Page setup dialog still showing", pageSetupDialog.isShowing());
    PageFormat pageFormat = PrinterJob.getPrinterJob().defaultPage();
    // Check home print attributes are modified accordingly
    assertHomePrintEqualPrintAttributes(pageFormat, true, false, true, home);
    
    // 4. Undo changes
    runAction(controller, HomePane.ActionType.UNDO);
    // Check home attributes have previous values
    assertEquals("Home print set", null, home.getPrint());
    // Redo
    runAction(controller, HomePane.ActionType.REDO);
    // Check home attributes are modified accordingly
    assertHomePrintEqualPrintAttributes(pageFormat, true, false, true, home);
    
    // 5. Show print preview dialog box
    tester.invokeLater(new Runnable() { 
        public void run() {
          // Display dialog box later in Event Dispatch Thread to avoid blocking test thread
          runAction(controller, HomePane.ActionType.PRINT_PREVIEW);
        }
      });
    // Wait for print preview to be shown
    tester.waitForFrameShowing(new AWTHierarchy(), preferences.getLocalizedString(
        PrintPreviewPanel.class, "printPreview.title"));
    // Check dialog box is displayed
    JDialog printPreviewDialog = (JDialog)new BasicFinder().find(frame, 
        new ClassMatcher (JDialog.class, true));
    assertTrue("Print preview dialog not showing", printPreviewDialog.isShowing());
    // Retrieve PageSetupPanel components
    PrintPreviewPanel printPreviewPanel = (PrintPreviewPanel)TestUtilities.findComponent(
        frame, PrintPreviewPanel.class);
    JToolBar toolBar = 
        (JToolBar)TestUtilities.getField(printPreviewPanel, "toolBar");
    JButton previousButton = (JButton)toolBar.getComponent(0); 
    final JButton nextButton = (JButton)toolBar.getComponent(1); 
    HomePrintableComponent printableComponent = 
        (HomePrintableComponent)TestUtilities.getField(printPreviewPanel, "printableComponent");;
    // Check if buttons are enabled and if printable component displays the first page
    assertFalse("Previous button is enabled", previousButton.isEnabled());
    assertTrue("Next button is disabled", nextButton.isEnabled());
    assertEquals("Printable component doesn't display first page", 0, printableComponent.getPage());
    assertEquals("Wrong printable component page count", 2, printableComponent.getPageCount());
    
    // 6. Click on next page button
    tester.invokeAndWait(new Runnable() {
        public void run() {
          nextButton.doClick();
        }
      });
    // Check if buttons are enabled and if printable component displays the second page
    assertTrue("Previous button is enabled", previousButton.isEnabled());
    assertFalse("Next button is disabled", nextButton.isEnabled());
    assertEquals("Printable component doesn't display second page", 1, printableComponent.getPage());
    
    // Click on Ok in dialog box
    final JOptionPane printPreviewOptionPane = (JOptionPane)TestUtilities.findComponent(
        printPreviewDialog, JOptionPane.class);
    tester.invokeAndWait(new Runnable() {
        public void run() {
          // Select Ok option to hide dialog box in Event Dispatch Thread
          printPreviewOptionPane.setValue(JOptionPane.OK_OPTION); 
        }
      });
    assertFalse("Print preview dialog still showing", printPreviewDialog.isShowing());
    
    // 7. Check PDF creation
    File tmpDirectory = File.createTempFile("print", "dir");
    tmpDirectory.delete();    
    assertTrue("Couldn't create tmp directory", tmpDirectory.mkdir());
    String pdfFileBase = "test";
    // Show print to PDF dialog box
    tester.invokeLater(new Runnable() { 
        public void run() {
          // Display dialog box later in Event Dispatch Thread to avoid blocking test thread
          runAction(controller, HomePane.ActionType.PRINT_TO_PDF);
        }
      });
    // Wait for print to PDF file chooser to be shown
    tester.waitForFrameShowing(new AWTHierarchy(), preferences.getLocalizedString(
        HomePane.class, "printToPDFDialog.title"));
    // Check dialog box is displayed
    final Dialog printToPdfDialog = (Dialog)new BasicFinder().find(frame, 
        new ClassMatcher (Dialog.class, true));
    assertTrue("Print to pdf dialog not showing", printToPdfDialog.isShowing());
    // Change file in print to PDF file chooser 
    final JFileChooserTester fileChooserTester = new JFileChooserTester();
    final JFileChooser fileChooser = (JFileChooser)new BasicFinder().find(printToPdfDialog, 
        new ClassMatcher(JFileChooser.class));
    fileChooserTester.actionSetDirectory(fileChooser, tmpDirectory.getAbsolutePath());
    fileChooserTester.actionSetFilename(fileChooser, pdfFileBase);
    // Select Ok option to hide dialog box
    fileChooserTester.actionApprove(fileChooser);
    assertFalse("Print to pdf dialog still showing", printToPdfDialog.isShowing());
    // Wait PDF generation  
    File pdfFile = new File(tmpDirectory, pdfFileBase + ".pdf");
    Thread.sleep(2000);
    assertTrue("PDF file " + pdfFile + " doesn't exist", pdfFile.exists());
    assertTrue("PDF file is empty", pdfFile.length() > 0);
    pdfFile.delete();
    tmpDirectory.delete();
  }
  
  /**
   * Runs <code>actionPerformed</code> method matching <code>actionType</code> 
   * in <code>HomePane</code>. 
   */
  private void runAction(HomeController controller,
                         HomePane.ActionType actionType) {
    ((JComponent)controller.getView()).getActionMap().get(actionType).actionPerformed(null);
  }

  /**
   * Asserts the print attributes given in parameter match <code>home</code> print.
   */
  private void assertHomePrintEqualPrintAttributes(PageFormat pageFormat,
                                                   boolean furniturePrinted,
                                                   boolean planPrinted,
                                                   boolean view3DPrinted, 
                                                   Home home) {
    HomePrint homePrint = home.getPrint();
    assertEquals("Wrong paper width", (float)pageFormat.getWidth(), homePrint.getPaperWidth());
    assertEquals("Wrong paper height", (float)pageFormat.getHeight(), homePrint.getPaperHeight());
    assertEquals("Wrong paper left margin", (float)pageFormat.getImageableX(), homePrint.getPaperLeftMargin());
    assertEquals("Wrong paper top margin", (float)pageFormat.getImageableY(), homePrint.getPaperTopMargin());
    assertEquals("Wrong paper right margin", 
        (float)(pageFormat.getWidth() - pageFormat.getImageableX() - pageFormat.getImageableWidth()), 
        homePrint.getPaperRightMargin());
    assertEquals("Wrong paper bottom margin", 
        (float)(pageFormat.getHeight() - pageFormat.getImageableY() - pageFormat.getImageableHeight()), 
        homePrint.getPaperBottomMargin());
    switch (pageFormat.getOrientation()) {
      case PageFormat.PORTRAIT :
        assertEquals("Wrong paper orientation", 
            HomePrint.PaperOrientation.PORTRAIT, homePrint.getPaperOrientation());
        break;
      case PageFormat.LANDSCAPE :
        assertEquals("Wrong paper orientation", 
            HomePrint.PaperOrientation.LANDSCAPE, homePrint.getPaperOrientation());
        break;
      case PageFormat.REVERSE_LANDSCAPE :
        assertEquals("Wrong paper orientation", 
            HomePrint.PaperOrientation.REVERSE_LANDSCAPE, homePrint.getPaperOrientation());
        break;
    }
    assertEquals("Wrong furniture printed", furniturePrinted, homePrint.isFurniturePrinted());
    assertEquals("Wrong plan printed", planPrinted, homePrint.isPlanPrinted());
    assertEquals("Wrong view 3D printed", view3DPrinted, homePrint.isView3DPrinted());
  }
}
