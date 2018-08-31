/*
 * ViewFactory.java 28 oct. 2008
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

import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A factory that specifies how to create the views displayed in Sweet Home 3D. 
 * @author Emmanuel Puybaret
 */
public interface ViewFactory {
  /**
   * Returns a new view that displays furniture <code>catalog</code>.
   */
  View createFurnitureCatalogView(FurnitureCatalog catalog,
                                  UserPreferences preferences,
                                  FurnitureCatalogController furnitureCatalogController);
  
  /**
   * Returns a new view that displays <code>home</code> furniture list.
   */
  View createFurnitureView(Home home, UserPreferences preferences,
                           FurnitureController furnitureController);

  /**
   * Returns a new view that displays <code>home</code> on a plan.
   */
  PlanView createPlanView(Home home, UserPreferences preferences,
                          PlanController planController);

  /**
   * Returns a new view that displays <code>home</code> in 3D.
   */
  View createView3D(Home home, UserPreferences preferences,
                    HomeController3D homeController3D);

  /**
   * Returns a new view that displays <code>home</code> and its sub views.
   */
  HomeView createHomeView(Home home, UserPreferences preferences,
                          HomeController homeController);

  /**
   * Returns a new view that displays a wizard. 
   */
  DialogView createWizardView(UserPreferences preferences,
                              WizardController wizardController);

  /**
   * Returns a new view that displays the different steps that helps the user to choose a background image. 
   */
  View createBackgroundImageWizardStepsView(
          BackgroundImage backgroundImage,
          UserPreferences preferences,
          BackgroundImageWizardController backgroundImageWizardController);

  /**
   * Returns a new view that displays the different steps that helps the user to import furniture. 
   */
  ImportedFurnitureWizardStepsView createImportedFurnitureWizardStepsView(
          CatalogPieceOfFurniture piece,
          String modelName, boolean importHomePiece,
          UserPreferences preferences,
          ImportedFurnitureWizardController importedFurnitureWizardController);

  /**
   * Returns a new view that displays the different steps that helps the user to import a texture. 
   */
  View createImportedTextureWizardStepsView(
          CatalogTexture texture, String textureName,
          UserPreferences preferences,
          ImportedTextureWizardController importedTextureWizardController);

  /**
   * Returns a new view that displays message for a threaded task.
   */
  ThreadedTaskView createThreadedTaskView(String taskMessage,
                                          UserPreferences userPreferences,
                                          ThreadedTaskController threadedTaskController);

  /**
   * Returns a new view that edits user preferences.
   */
  DialogView createUserPreferencesView(
          UserPreferences preferences,
          UserPreferencesController userPreferencesController);
  
  /**
   * Returns a new view that edits level values.
   */
  DialogView createLevelView(UserPreferences preferences, LevelController levelController);

  /**
   * Returns a new view that edits furniture values.
   */
  DialogView createHomeFurnitureView(UserPreferences preferences,
                                     HomeFurnitureController homeFurnitureController);

  /**
   * Returns a new view that edits wall values.
   */
  DialogView createWallView(UserPreferences preferences,
                            WallController wallController);

  /**
   * Returns a new view that edits room values.
   */
  DialogView createRoomView(UserPreferences preferences,
                            RoomController roomController);
  
  /**
   * Returns a new view that edits polyline values.
   * @since 5.0
   */
  DialogView createPolylineView(UserPreferences preferences,
                                PolylineController polylineController);

  /**
   * Returns a new view that edits label values.
   */
  DialogView createLabelView(boolean modification,
                             UserPreferences preferences,
                             LabelController labelController);

  /**
   * Returns a new view that edits compass values.
   */
  DialogView createCompassView(UserPreferences preferences,
                               CompassController compassController);
  
  /**
   * Returns a new view that edits observer camera values.
   */
  DialogView createObserverCameraView(UserPreferences preferences,
                                      ObserverCameraController home3DAttributesController);
  
  /**
   * Returns a new view that edits 3D attributes.
   */
  DialogView createHome3DAttributesView(UserPreferences preferences,
                                        Home3DAttributesController home3DAttributesController);

  /**
   * Returns a new view that edits the texture of its controller.  
   */
  TextureChoiceView createTextureChoiceView(UserPreferences preferences,
                                            TextureChoiceController textureChoiceController);

  /**
   * Returns a new view that edits the baseboard of its controller.  
   */
  View createBaseboardChoiceView(UserPreferences preferences,
                                 BaseboardChoiceController baseboardChoiceController);

  /**
   * Returns a new view that edits the materials of its controller.  
   */
  View createModelMaterialsView(UserPreferences preferences,
                                ModelMaterialsController modelMaterialsController);

  /**
   * Creates a new view that edits page setup.
   */
  DialogView createPageSetupView(UserPreferences preferences,
                                 PageSetupController pageSetupController);

  /**
   * Returns a new view that displays home print preview. 
   */
  DialogView createPrintPreviewView(Home home,
                                    UserPreferences preferences,
                                    HomeController homeController,
                                    PrintPreviewController printPreviewController);

  /**
   * Returns a new view able to compute a photo realistic image of a home. 
   */
  DialogView createPhotoView(Home home, UserPreferences preferences,
                             PhotoController photoController);

  /**
   * Returns a new view able to compute a photos of a home from its stored points of view. 
   */
  DialogView createPhotosView(Home home, UserPreferences preferences,
                              PhotosController photosController);

  /**
   * Returns a new view able to compute a 3D video of a home. 
   */
  DialogView createVideoView(Home home, UserPreferences preferences,
                             VideoController videoController);

  /**
   * Returns a new view that displays Sweet Home 3D help.
   */
  HelpView createHelpView(UserPreferences preferences,
                          HelpController helpController);
}
