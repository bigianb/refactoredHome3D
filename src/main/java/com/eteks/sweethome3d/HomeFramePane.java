/*
 * HomeFramePane.java 1 sept. 2006
 *
 * Sweet Home 3D, Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
package com.eteks.sweethome3d;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.Timer;

import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.SwingTools;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.HomeView;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * A pane that displays a 
 * {@link com.eteks.sweethome3d.swing.HomePane home pane} in a frame.
 * @author Emmanuel Puybaret
 */
public class HomeFramePane extends JRootPane implements View {
  private static final String FRAME_X_VISUAL_PROPERTY         = "com.eteks.sweethome3d.SweetHome3D.FrameX";
  private static final String FRAME_Y_VISUAL_PROPERTY         = "com.eteks.sweethome3d.SweetHome3D.FrameY";
  private static final String FRAME_WIDTH_VISUAL_PROPERTY     = "com.eteks.sweethome3d.SweetHome3D.FrameWidth";
  private static final String FRAME_HEIGHT_VISUAL_PROPERTY    = "com.eteks.sweethome3d.SweetHome3D.FrameHeight";
  private static final String FRAME_MAXIMIZED_VISUAL_PROPERTY = "com.eteks.sweethome3d.SweetHome3D.FrameMaximized";
  private static final String SCREEN_WIDTH_VISUAL_PROPERTY    = "com.eteks.sweethome3d.SweetHome3D.ScreenWidth";
  private static final String SCREEN_HEIGHT_VISUAL_PROPERTY   = "com.eteks.sweethome3d.SweetHome3D.ScreenHeight";
  
  private final Home                    home;
  private final HomeApplication         application;
  private final ContentManager          contentManager;
  private final HomeFrameController     controller;
  private static int                    newHomeCount;
  private int                           newHomeNumber;
  
  public HomeFramePane(Home home,
                       HomeApplication application,
                       ContentManager contentManager, 
                       HomeFrameController controller) {
    this.home = home;
    this.controller = controller;
    this.application = application;
    this.contentManager = contentManager;
    // If home is unnamed, give it a number
    if (home.getName() == null) {
      this.newHomeNumber = ++newHomeCount;
    }
    // Set controller view as content pane
    HomeView homeView = this.controller.getHomeController().getView();
    setContentPane((JComponent)homeView);
  }

  /**
   * Builds and shows the frame that displays this pane.
   */
  public void displayView() {
    final JFrame homeFrame = new JFrame() {
      {
        // Replace frame rootPane by home controller view
        setRootPane(HomeFramePane.this);
      }
    };
    // Update frame image and title 
    List<Image> frameImages = new ArrayList<Image>(3);
    frameImages.add(new ImageIcon(HomeFramePane.class.getResource("resources/frameIcon.png")).getImage());
    frameImages.add(new ImageIcon(HomeFramePane.class.getResource("resources/frameIcon32x32.png")).getImage());
    if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
      frameImages.add(new ImageIcon(HomeFramePane.class.getResource("resources/frameIcon128x128.png")).getImage());
    }
    try {
      // Call Java 1.6 setIconImages by reflection
      homeFrame.getClass().getMethod("setIconImages", List.class).invoke(homeFrame, frameImages);
    } catch (Exception ex) {
      // Call setIconImage available in previous versions
      homeFrame.setIconImage(frameImages.get(0));
    }
    if (OperatingSystem.isMacOSXLionOrSuperior()) {
      MacOSXConfiguration.installToolBar(this);
    }
    updateFrameTitle(homeFrame, this.home, this.application);
    // Change component orientation
    applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));    
    // Compute frame size and location
    computeFrameBounds(this.home, homeFrame);
    // Enable windows to update their content while window resizing
    getToolkit().setDynamicLayout(true); 
    // The best MVC solution should be to avoid the following statements 
    // but Mac OS X accepts to display the menu bar of a frame in the screen 
    // menu bar only if this menu bar depends directly on its root pane  
    HomeView homeView = this.controller.getHomeController().getView();
    if (homeView instanceof JRootPane) {
      JRootPane homePane = (JRootPane)homeView;
      setJMenuBar(homePane.getJMenuBar());
      homePane.setJMenuBar(null);
    }
    
    // Add listeners to model and frame   
    addListeners(this.home, this.application, this.controller.getHomeController(), homeFrame);
    
    homeFrame.setVisible(true);
    EventQueue.invokeLater(new Runnable() {
        public void run() {
          // Add state listener to frame once it's visible to avoid some undesired events during first showing    
          addWindowStateListener(home, application, controller.getHomeController(), homeFrame);
          // Request the frame to go to front again because closing waiting dialog meanwhile  
          // could put in front the already opened frame  
          homeFrame.toFront();
        }
      });
  }
  
  /**
   * Adds listeners to <code>frame</code> and model objects.
   */
  private void addListeners(final Home home,
                            final HomeApplication application,
                            final HomeController controller,
                            final JFrame frame) {
    // Add a listener that keeps track of window location and size
    final ComponentAdapter componentListener = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent ev) {
          // Store new size only if frame isn't maximized
          if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != JFrame.MAXIMIZED_BOTH) {
            controller.setHomeProperty(FRAME_WIDTH_VISUAL_PROPERTY, String.valueOf(frame.getWidth()));
            controller.setHomeProperty(FRAME_HEIGHT_VISUAL_PROPERTY, String.valueOf(frame.getHeight()));
          }
          Dimension userScreenSize = getUserScreenSize();
          controller.setHomeProperty(SCREEN_WIDTH_VISUAL_PROPERTY, String.valueOf(userScreenSize.width));
          controller.setHomeProperty(SCREEN_HEIGHT_VISUAL_PROPERTY, String.valueOf(userScreenSize.height));
        }
        
        @Override
        public void componentMoved(ComponentEvent ev) {
          // Store new location only if frame isn't maximized
          if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != JFrame.MAXIMIZED_BOTH) {
            controller.setHomeProperty(FRAME_X_VISUAL_PROPERTY, String.valueOf(frame.getX()));
            controller.setHomeProperty(FRAME_Y_VISUAL_PROPERTY, String.valueOf(frame.getY()));
          }
        }
      };
    frame.addComponentListener(componentListener);
    // Control frame closing and activation 
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    final WindowAdapter windowListener = new WindowAdapter () {
        private Component mostRecentFocusOwner;

        @Override
        public void windowClosing(WindowEvent ev) {
          controller.close();
        }
        
        @Override
        public void windowDeactivated(WindowEvent ev) {
          // Java 3D 1.5 bug : windowDeactivated notifications should not be sent to this frame
          // while canvases 3D are created in a child modal dialog like the one managing 
          // ImportedFurnitureWizardStepsPanel. As this makes Swing loose the most recent focus owner
          // let's store it in a field to use it when this frame will be reactivated. 
          Component mostRecentFocusOwner = frame.getMostRecentFocusOwner();          
          if (!(mostRecentFocusOwner instanceof JFrame)
              && mostRecentFocusOwner != null) {
            this.mostRecentFocusOwner = mostRecentFocusOwner;
          }
        }

        @Override
        public void windowActivated(WindowEvent ev) {                    
          // Java 3D 1.5 bug : let's request focus in window for the most recent focus owner when
          // this frame is reactivated
          if (this.mostRecentFocusOwner != null) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                  mostRecentFocusOwner.requestFocusInWindow();
                }
              });
          }
        } 
      };
    frame.addWindowListener(windowListener);    
    // Add a listener to preferences to apply component orientation to frame matching current language
    application.getUserPreferences().addPropertyChangeListener(UserPreferences.Property.LANGUAGE, 
        new LanguageChangeListener(frame, this));
    // Dispose window when a home is deleted 
    application.addHomesListener(new CollectionListener<Home>() {
        public void collectionChanged(CollectionEvent<Home> ev) {
          if (ev.getItem() == home
              && ev.getType() == CollectionEvent.Type.DELETE) {
            application.removeHomesListener(this);
            frame.dispose();
            frame.removeWindowListener(windowListener);
            frame.removeComponentListener(componentListener);
          }
        };
      });
    
    // Update title when the name or the modified state of home changes
    PropertyChangeListener frameTitleChangeListener = new PropertyChangeListener () {
        public void propertyChange(PropertyChangeEvent ev) {
          updateFrameTitle(frame, home, application);
        }
      };
    home.addPropertyChangeListener(Home.Property.NAME, frameTitleChangeListener);
    home.addPropertyChangeListener(Home.Property.MODIFIED, frameTitleChangeListener);
    home.addPropertyChangeListener(Home.Property.RECOVERED, frameTitleChangeListener);
    home.addPropertyChangeListener(Home.Property.REPAIRED, frameTitleChangeListener);
  }
    
  /**
   * Preferences property listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.  
   */
  private static class LanguageChangeListener implements PropertyChangeListener {
    private WeakReference<JFrame>        frame;
    private WeakReference<HomeFramePane> homeFramePane;

    public LanguageChangeListener(JFrame frame, HomeFramePane homeFramePane) {
      this.frame = new WeakReference<JFrame>(frame);
      this.homeFramePane = new WeakReference<HomeFramePane>(homeFramePane);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If frame was garbage collected, remove this listener from preferences
      HomeFramePane homeFramePane = this.homeFramePane.get();
      UserPreferences preferences = (UserPreferences)ev.getSource();
      if (homeFramePane == null) {
        preferences.removePropertyChangeListener(
            UserPreferences.Property.LANGUAGE, this);
      } else {
        this.frame.get().applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        homeFramePane.updateFrameTitle(this.frame.get(), homeFramePane.home, homeFramePane.application);
      }
    }
  }
  
  /**
   * Adds window state listener to <code>frame</code>.
   */
  private void addWindowStateListener(final Home home,
                                      final HomeApplication application,
                                      final HomeController controller,
                                      final JFrame frame) {
    // Control frame closing and activation 
    final WindowStateListener windowStateListener = new WindowStateListener () {
        public void windowStateChanged(WindowEvent ev) {
          controller.setHomeProperty(FRAME_MAXIMIZED_VISUAL_PROPERTY, 
              String.valueOf((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH));
        }
      };
    frame.addWindowStateListener(windowStateListener);    
    // Dispose window when a home is deleted 
    application.addHomesListener(new CollectionListener<Home>() {
        public void collectionChanged(CollectionEvent<Home> ev) {
          if (ev.getItem() == home
              && ev.getType() == CollectionEvent.Type.DELETE) {
            application.removeHomesListener(this);
            frame.removeWindowStateListener(windowStateListener);
          }
        };
      });
  }

  /**
   * Computes <code>frame</code> size and location to fit into screen.
   */
  private void computeFrameBounds(Home home, final JFrame frame) {
    Number x = home.getNumericProperty(FRAME_X_VISUAL_PROPERTY);
    Number y = home.getNumericProperty(FRAME_Y_VISUAL_PROPERTY);
    Number width = home.getNumericProperty(FRAME_WIDTH_VISUAL_PROPERTY);
    Number height = home.getNumericProperty(FRAME_HEIGHT_VISUAL_PROPERTY);
    boolean maximized = Boolean.parseBoolean(home.getProperty(FRAME_MAXIMIZED_VISUAL_PROPERTY));
    Number screenWidth = home.getNumericProperty(SCREEN_WIDTH_VISUAL_PROPERTY);
    Number screenHeight = home.getNumericProperty(SCREEN_HEIGHT_VISUAL_PROPERTY);
    
    Dimension screenSize = getUserScreenSize();
    // If home frame bounds exist and screen resolution didn't reduce 
    if (x != null && y != null 
        && width != null && height != null 
        && screenWidth != null && screenHeight != null
        && screenWidth.intValue() <= screenSize.width
        && screenHeight.intValue() <= screenSize.height) {
      final Rectangle frameBounds = new Rectangle(x.intValue(), y.intValue(), width.intValue(), height.intValue());
      if (maximized) {
        if (OperatingSystem.isMacOSX() 
            && OperatingSystem.isJavaVersionGreaterOrEqual("1.7")) {
          // Display the frame at its maximum size because calling setExtendedState to maximize 
          // the frame moves it to the bottom left at its minimum size  
          Insets insets = frame.getInsets();
          frame.setSize(screenSize.width + insets.left + insets.right, 
              screenSize.height + insets.bottom);
        } else if (OperatingSystem.isLinux()) {
          EventQueue.invokeLater(new Runnable() {
            public void run() {
              // Under Linux, maximize frame once it's displayed
              frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
          });
        } else {
          frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        // Add a listener that will set the normal size when the frame leaves the maximized state
        WindowAdapter windowStateListener = new WindowAdapter() {
            @Override
            public void windowStateChanged(WindowEvent ev) {
              if ((ev.getOldState() == JFrame.MAXIMIZED_BOTH 
                    || (OperatingSystem.isMacOSX() 
                        && OperatingSystem.isJavaVersionGreaterOrEqual("1.7")
                        && ev.getOldState() == JFrame.NORMAL))
                  && ev.getNewState() == JFrame.NORMAL) {
                if (OperatingSystem.isMacOSXLionOrSuperior()) {
                  // Set back frame size later once frame reduce animation is finished 
                  new Timer(20, new ActionListener() {
                      public void actionPerformed(ActionEvent ev) {
                        if (frame.getHeight() < 40) {
                          ((Timer)ev.getSource()).stop();
                          frame.setBounds(frameBounds);
                        }
                      }
                    }).start();
                } else {
                  frame.setBounds(frameBounds);
                }
                frame.removeWindowStateListener(this);
              }
            }
            
            @Override
            public void windowClosed(WindowEvent ev) {
              frame.removeWindowListener(this);
              frame.removeWindowStateListener(this);
            }
          };
        frame.addWindowStateListener(windowStateListener);
        frame.addWindowListener(windowStateListener);
      } else {
        // Reuse home bounds
        frame.setBounds(frameBounds);
        frame.setLocationByPlatform(!SwingTools.isRectangleVisibleAtScreen(frameBounds));
      }
    } else {      
      frame.setLocationByPlatform(true);
      frame.pack();
      frame.setSize(Math.min(screenSize.width * 4 / 5, frame.getWidth()), 
              Math.min(screenSize.height * 4 / 5, frame.getHeight()));
      if (OperatingSystem.isMacOSX() 
          && OperatingSystem.isJavaVersionBetween("1.7", "1.9")) {
        // JFrame#setLocationByPlatform does nothing under Java 7/8
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Frame applicationFrame : Frame.getFrames()) {
          if (applicationFrame.isShowing() 
              && applicationFrame.getBackground().getAlpha() != 0) {
            minX = Math.min(minX, applicationFrame.getX());
            minY = Math.min(minY, applicationFrame.getY());
            maxX = Math.max(maxX, applicationFrame.getX());
            maxY = Math.max(maxY, applicationFrame.getY());
          }
        }
        
        if (minX == Integer.MAX_VALUE || minX >= 23) {
          x = 0;
        } else {
          x = maxX + 23;
        }
        if (minY == Integer.MAX_VALUE || minY >= 23) {
          y = 0;
        } else {
          y = maxY + 23;
        }
        frame.setLocation(x.intValue(), y.intValue());
      }
    }
  }

  /**
   * Returns the screen size available to user. 
   */
  private Dimension getUserScreenSize() {
    Dimension screenSize = getToolkit().getScreenSize();
    Insets screenInsets = getToolkit().getScreenInsets(getGraphicsConfiguration());
    screenSize.width -= screenInsets.left + screenInsets.right;
    screenSize.height -= screenInsets.top + screenInsets.bottom;
    return screenSize;
  }
  
  /**
   * Updates <code>frame</code> title from <code>home</code> and <code>application</code> name.
   */
  private void updateFrameTitle(JFrame frame, 
                                Home home,
                                HomeApplication application) {
    String homeName = home.getName();
    String homeDisplayedName;
    if (homeName == null) {
      homeDisplayedName = application.getUserPreferences().getLocalizedString(HomeFramePane.class, "untitled"); 
      if (newHomeNumber > 1) {
        homeDisplayedName += " " + newHomeNumber;
      }
    } else {
      homeDisplayedName = this.contentManager.getPresentationName(
          homeName, ContentManager.ContentType.SWEET_HOME_3D);
    }
    
    if (home.isRecovered()) {
      homeDisplayedName += " " + application.getUserPreferences().getLocalizedString(HomeFramePane.class, "recovered");
    } 
    if (home.isRepaired()) {
      homeDisplayedName += " " + application.getUserPreferences().getLocalizedString(HomeFramePane.class, "repaired");
    }
    
    String title = homeDisplayedName;
    if (OperatingSystem.isMacOSX()) {
      // Use black indicator in close icon for a modified home 
      Boolean homeModified = Boolean.valueOf(home.isModified() || home.isRecovered());
      // Set Mac OS X 10.4 property for backward compatibility
      putClientProperty("windowModified", homeModified);
      
      if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
        putClientProperty("Window.documentModified", homeModified);
        
        if (homeName != null) {        
          File homeFile = new File(homeName);
          if (homeFile.exists()) {
            // Update the home icon in window title bar for home files
            putClientProperty("Window.documentFile", homeFile);
          }
        }
      }

      if (!frame.isVisible() 
          && OperatingSystem.isMacOSXLionOrSuperior()) {
        try {
          // Call Mac OS X specific FullScreenUtilities.setWindowCanFullScreen(homeFrame, true) by reflection 
          Class.forName("com.apple.eawt.FullScreenUtilities").
              getMethod("setWindowCanFullScreen", new Class<?> [] {Window.class, boolean.class}).
              invoke(null, frame, true);
        } catch (Exception ex) {
          // Full screen mode is not supported
        }
      }
    } else {
      title += " - " + application.getName(); 
      if (home.isModified() || home.isRecovered()) {
        title = "* " + title;
      }
    }
    frame.setTitle(title);
  }
}
