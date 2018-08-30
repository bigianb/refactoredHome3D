/*
 * HomeFileRecorderTest.java 28 aout 2006
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
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.junit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

import com.eteks.sweethome3d.io.DefaultFurnitureCatalog;
import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.io.FileUserPreferences;
import com.eteks.sweethome3d.io.HomeFileRecorder;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.DamagedHomeRecorderException;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.TextStyle;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.tools.URLContent;

import junit.framework.TestCase;

/**
 * Tests {@link HomeFileRecorder} class.
 * @author Emmanuel Puybaret
 */
public class HomeFileRecorderTest extends TestCase {
  public void testWriteReadHome() throws RecorderException {
    // Create an empty home with a wall and a piece of furniture
    Home home1 = new Home();
    Wall wall = new Wall(0, 10, 100, 80, 10, home1.getWallHeight());
    wall.setProperty("id", "wall1");
    home1.addWall(wall);
    FurnitureCatalog catalog = new DefaultFurnitureCatalog();
    HomePieceOfFurniture piece = new HomePieceOfFurniture(
        catalog.getCategories().get(0).getFurniture().get(0));
    piece.setProperty("id", "piece1");
    piece.setProperty("name", "value");
    home1.addPieceOfFurniture(piece);

    // Test if home is correctly saved
    checkSavedHome(home1, new HomeFileRecorder());
    // Test if home with XML entry is correctly saved
    checkSavedHome(home1, new HomeFileRecorder(9, false, null, false, true));
  }

  private void checkSavedHome(Home home, HomeRecorder recorder) throws RecorderException {
    // 1. Record home in a file named test.sh3d in current directory
    String testFile = new File("test.sh3d").getAbsolutePath();
    recorder.writeHome(home, testFile);
    // Check test.sh3d file exists
    assertTrue("File test.sh3d doesn't exist", recorder.exists(testFile));

    // 2. Read test.sh3d file in a new home
    Home readHome = recorder.readHome(testFile);
    // Compare home content
    assertNotSame("Home not loaded", home, readHome);
    assertEquals("Home wall height",
        home.getWallHeight(), readHome.getWallHeight());
    assertEquals("Home walls wrong count",
        home.getWalls().size(), readHome.getWalls().size());
    assertEquals(home.getWalls().iterator().next(), readHome.getWalls().iterator().next());
    assertEquals("Home furniture wrong count",
        home.getFurniture().size(), readHome.getFurniture().size());
    assertEquals(home.getFurniture().iterator().next(), readHome.getFurniture().get(0));

    // Delete file
    if (!new File(testFile).delete()) {
      fail("Couldn't delete file " + testFile);
    }
  }

  public void testXMLEntryConsistency() throws URISyntaxException, RecorderException, IOException {
    checkXMLEntryConsistency(new File(HomeControllerTest.class.getResource("resources/home1.sh3d").toURI()));

    // Create a home with properties and text style set on group items
    Home home = new Home();
    Level level0 = new Level("Level0", 0, 0.1f, 250f);
    home.addLevel(level0);
    Level level1 = new Level("Level1", 252f, 2f, 250f);
    level1.setViewable(false);
    home.addLevel(level1);
    home.setSelectedLevel(level0);
    home.addWall(new Wall(0, 10, 100, 80, 10, Float.NaN)); // Use NaN to check how it will be written then read
    FurnitureCatalog catalog = new DefaultFurnitureCatalog();
    HomePieceOfFurniture piece1 = new HomePieceOfFurniture(catalog.getCategories().get(0).getFurniture().get(0));
    piece1.setProperty("id", "piece1");
    HomePieceOfFurniture piece2 = new HomePieceOfFurniture(catalog.getCategories().get(0).getFurniture().get(1));
    piece2.setProperty("id", "piece2");
    piece2.setNameStyle(new TextStyle(12, false, false));
    HomeFurnitureGroup group = new HomeFurnitureGroup(Arrays.asList(piece1, piece2), "Group");
    group.setProperty("test", "value");
    home.addPieceOfFurniture(group);
    // Save home with Home serialized entry
    File savedFileWithHomeEntry = File.createTempFile("test", ".sh3d");
    new HomeFileRecorder(0, false, null, false, false).writeHome(home, savedFileWithHomeEntry.getAbsolutePath());
    checkXMLEntryConsistency(savedFileWithHomeEntry);
    savedFileWithHomeEntry.delete();
  }

  public void checkXMLEntryConsistency(File homeFile) throws RecorderException, IOException {
    HomeRecorder homeEntryRecorder = new HomeFileRecorder(0, false, null, false, false);
    Home home = homeEntryRecorder.readHome(homeFile.getAbsolutePath());
    assertTrue("Tested home has no walls", home.getWalls().size() > 0);
    assertTrue("Tested home has no furniture", home.getFurniture().size() > 0);
    // Save home with an XML entry
    HomeRecorder homeXmlEntryRecorder = new HomeFileRecorder(0, false, null, false, true);
    File savedFileWithXmlEntry = File.createTempFile("homeXML", ".sh3d");
    homeXmlEntryRecorder.writeHome(home, savedFileWithXmlEntry.getAbsolutePath());
    // Read home again using XML entry and save it in an other file
    home = homeXmlEntryRecorder.readHome(savedFileWithXmlEntry.getAbsolutePath());
    File savedFileWithXmlEntry2 = File.createTempFile("homeXML", ".sh3d");
    homeXmlEntryRecorder.writeHome(home, savedFileWithXmlEntry2.getAbsolutePath());
    // Compare the XML entries of the two files
    assertContentEquals("Home.xml entries different",
        new URLContent(new URL("jar:" + savedFileWithXmlEntry.toURI().toURL() + "!/Home.xml")),
        new URLContent(new URL("jar:" + savedFileWithXmlEntry2.toURI().toURL() + "!/Home.xml")));
    savedFileWithXmlEntry.delete();
    savedFileWithXmlEntry2.delete();
  }

  /**
   * Test repaired home file management.
   */
  public void testRepairedFile() throws URISyntaxException, RecorderException, IOException {
    // Test repair on corrupted file
    checkDamagedFileIsRepaired(new File(
        HomeControllerTest.class.getResource("resources/damagedHomeWithContentDigests.sh3d").toURI()).getAbsolutePath(), 5);
    // Test repair on valid zip file but with missing entries
    checkDamagedFileIsRepaired(new File(
        HomeControllerTest.class.getResource("resources/damagedHomeInValidZipWithContentDigestsAndNoContent.sh3d").toURI()).getAbsolutePath(), 9);
  }

  private void checkDamagedFileIsRepaired(String testFile, int damagedContentCount) throws RecorderException, IOException {
    try {
      // Check if opened home isn't repaired if preferences content isn't provided
      HomeRecorder recorder = new HomeFileRecorder(0, false, null, false);
      recorder.readHome(testFile);
      fail("Home shouldn't be readable");
    } catch (DamagedHomeRecorderException ex) {
      assertEquals("Missing damaged content", damagedContentCount, ex.getInvalidContent().size());
    }
    try {
      // Check if opened home will be fully repaired with preferences content
      HomeRecorder recorder = new HomeFileRecorder(0, false, new DefaultUserPreferences(), false);
      Home home = recorder.readHome(testFile);
      assertTrue("Home is not flagged as repaired", home.isRepaired());
      // Check repaired home can be saved
      File savedFile = File.createTempFile("repaired", ".sh3d");
      recorder.writeHome(home, savedFile.getAbsolutePath());
      savedFile.delete();
    } catch (DamagedHomeRecorderException ex) {
      fail("Home should be repaired with default catalogs");
    }
  }

  /**
   * Test direct reading of a XML file.
   */
  public void testReadXMLFile() throws RecorderException, URISyntaxException {
    HomeRecorder recorder = new HomeFileRecorder(0, false, new FileUserPreferences(), false, true, false);
    Home home = recorder.readHome(new File(
        HomeControllerTest.class.getResource("resources/homeTest.xml").toURI()).getAbsolutePath());
    assertEquals("Incorrect furniture count", 2, home.getFurniture().size());
    assertEquals("Incorrect walls count", 4, home.getWalls().size());
  }

  /**
   * Asserts <code>wall1</code> and <code>wall2</code> are different walls
   * containing the same data.
   */
  private void assertEquals(Wall wall1, Wall wall2) {
    assertNotSame("Wall not loaded", wall1, wall2);
    assertEquals("Different X start", wall1.getXStart(), wall2.getXStart());
    assertEquals("Different Y start", wall1.getYStart(), wall2.getYStart());
    assertEquals("Different X end", wall1.getXEnd(), wall2.getXEnd());
    assertEquals("Different Y end", wall1.getYEnd(), wall2.getYEnd());
    assertEquals("Different thickness", wall1.getThickness(), wall2.getThickness());
    if (wall1.getWallAtStart() == null) {
      assertEquals("Different wall at start", wall2.getWallAtStart(), null);
    } else {
      assertFalse("Different wall at start", wall2.getWallAtStart() == null);
      assertNotSame("Wall at start not loaded", wall1.getWallAtStart(), wall2.getWallAtEnd());
    }
    if (wall1.getWallAtEnd() == null) {
      assertEquals("Different wall at end", wall2.getWallAtEnd(), null);
    } else {
      assertFalse("Different wall at end", wall2.getWallAtEnd() == null);
      assertNotSame("Wall at end not loaded", wall1.getWallAtStart(), wall2.getWallAtEnd());
    }
    assertEquals("Different property id", wall1.getProperty("id"), wall2.getProperty("id"));
  }

  /**
   * Asserts <code>piece1</code> and <code>piece2</code> are different pieces
   * containing the same data.
   */
  private void assertEquals(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
    assertNotSame("Piece not loaded", piece1, piece2);
    assertEquals("Different X", piece1.getX(), piece2.getX());
    assertEquals("Different Y", piece1.getY(), piece2.getY());
    assertEquals("Different color", piece1.getColor(), piece2.getColor());
    assertEquals("Different width", piece1.getWidth(), piece2.getWidth());
    assertEquals("Different height", piece1.getHeight(), piece2.getHeight());
    assertEquals("Different depth", piece1.getDepth(), piece2.getDepth());
    assertEquals("Different name", piece1.getName(), piece2.getName());
    assertNotSame("Piece icon not loaded", piece1.getIcon(), piece2.getIcon());
    assertContentEquals("Different icon content", piece1.getIcon(), piece2.getIcon());
    assertNotSame("Piece model not loaded", piece1.getModel(), piece2.getModel());
    assertContentEquals("Different model content", piece1.getModel(), piece2.getModel());
    assertEquals("Different property id", piece1.getProperty("id"), piece2.getProperty("id"));
    assertEquals("Different property name", piece1.getProperty("name"), piece2.getProperty("name"));
  }

  /**
   * Asserts <code>content1</code> and <code>content2</code> are equal.
   */
  private void assertContentEquals(String message, Content content1, Content content2) {
    InputStream stream1 = null;
    InputStream stream2 = null;
    try {
      stream1 = new BufferedInputStream(content1.openStream());
      stream2 = new BufferedInputStream(content2.openStream());
      for (int b; (b = stream1.read()) != -1; ) {
        assertEquals(message, b, stream2.read());
      }
      assertEquals(message, -1, stream2.read());
    } catch (IOException ex) {
      fail("Can't access to content");
    } finally {
      try {
        stream1.close();
        stream2.close();
      } catch (IOException ex) {
        fail("Can't close content stream");
      }
    }
  }
}
