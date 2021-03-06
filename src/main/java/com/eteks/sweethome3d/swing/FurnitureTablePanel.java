/*
 * FurniturePanel.java 19 juil. 2018
 *
 * Sweet Home 3D, Copyright (c) 2018 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
package com.eteks.sweethome3d.swing;

import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.FurnitureController;
import com.eteks.sweethome3d.viewcontroller.FurnitureView;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Properties;

/**
 * A panel displaying home furniture table and other information like totals.
 * @author Emmanuel Puybaret
 */
public class FurnitureTablePanel extends JPanel implements FurnitureView, Printable {
  private FurnitureTable      furnitureTable;
  private JLabel              totalPriceLabel;
  private JFormattedTextField totalPriceTextField;
  private JLabel              totalValueAddedTaxLabel;
  private JFormattedTextField totalValueAddedTaxTextField;
  private JLabel              totalPriceValueAddedTaxIncludedLabel;
  private JFormattedTextField totalPriceValueAddedTaxIncludedTextField;

  public FurnitureTablePanel(Home home, UserPreferences preferences,
                        FurnitureController controller) {
    super(new GridBagLayout());
    createComponents(home, preferences, controller);
    layoutComponents();
  }

  /**
   * Creates components displayed by this panel.
   */
  private void createComponents(final Home home,
                                final UserPreferences preferences,
                                FurnitureController controller) {
    this.furnitureTable = createFurnitureTable(home, preferences, controller);

    this.totalPriceLabel = new JLabel(preferences.getLocalizedString(
        FurnitureTablePanel.class, "totalPriceLabel.text"));
    this.totalPriceTextField = createTotalTextField();

    this.totalValueAddedTaxLabel = new JLabel(preferences.getLocalizedString(
        FurnitureTablePanel.class, "totalValueAddedTaxLabel.text"));
    this.totalValueAddedTaxTextField = createTotalTextField();

    // Create price Value Added Tax included label and its spinner bound to DEPTH controller property
    this.totalPriceValueAddedTaxIncludedLabel = new JLabel(preferences.getLocalizedString(
        FurnitureTablePanel.class, "totalPriceValueAddedTaxIncludedLabel.text"));
    this.totalPriceValueAddedTaxIncludedTextField = createTotalTextField();

    updateTotalsVisibility(preferences);
    updateTotals(home, preferences);

    // Add listener to update totals when furniture price changes
    final PropertyChangeListener furnitureChangeListener = ev -> {
      if (HomePieceOfFurniture.Property.PRICE.name().equals(ev.getPropertyName())
          || HomePieceOfFurniture.Property.VALUE_ADDED_TAX_PERCENTAGE.name().equals(ev.getPropertyName())
          || HomePieceOfFurniture.Property.CURRENCY.name().equals(ev.getPropertyName())) {
        updateTotals(home, preferences);
      }
    };
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      piece.addPropertyChangeListener(furnitureChangeListener);
    }
    home.addFurnitureListener(ev -> {
      if (ev.getType() == CollectionEvent.Type.ADD) {
        ev.getItem().addPropertyChangeListener(furnitureChangeListener);
      } else if (ev.getType() == CollectionEvent.Type.DELETE) {
        ev.getItem().removePropertyChangeListener(furnitureChangeListener);
      }
      updateTotals(home, preferences);
    });

    UserPreferencesChangeListener preferencesListener = new UserPreferencesChangeListener(this);
    preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, preferencesListener);
    preferences.addPropertyChangeListener(UserPreferences.Property.CURRENCY, preferencesListener);
    preferences.addPropertyChangeListener(UserPreferences.Property.VALUE_ADDED_TAX_ENABLED, preferencesListener);
  }

  /**
   * Creates and returns the main furniture table displayed by this component.
   */
  protected FurnitureTable createFurnitureTable(Home home, UserPreferences preferences, FurnitureController controller) {
    return new FurnitureTable(home, preferences, controller);
  }

  private JFormattedTextField createTotalTextField() {
    NumberFormat currencyFormat = DecimalFormat.getCurrencyInstance();
    JFormattedTextField totalTextField = new JFormattedTextField(currencyFormat);
    totalTextField.setEditable(false);
    totalTextField.setFocusable(false);
    return totalTextField;
  }

  /**
   * Preferences property listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.
   */
  public static class UserPreferencesChangeListener implements PropertyChangeListener {
    private final WeakReference<FurnitureTablePanel> furnitureTablePanel;

    public UserPreferencesChangeListener(FurnitureTablePanel furnitureTotalPricePanel) {
      this.furnitureTablePanel = new WeakReference<>(furnitureTotalPricePanel);
    }

    public void propertyChange(PropertyChangeEvent ev) {
      // If furniture table was garbage collected, remove this listener from preferences
      FurnitureTablePanel furnitureTablePanel = this.furnitureTablePanel.get();
      UserPreferences preferences = (UserPreferences)ev.getSource();
      UserPreferences.Property property = UserPreferences.Property.valueOf(ev.getPropertyName());
      if (furnitureTablePanel == null) {
        preferences.removePropertyChangeListener(UserPreferences.Property.LANGUAGE, this);
      } else {
        switch (property) {
          case LANGUAGE :
            furnitureTablePanel.totalPriceLabel.setText(preferences.getLocalizedString(
                FurnitureTablePanel.class, "totalPriceLabel.text"));
            furnitureTablePanel.totalValueAddedTaxLabel.setText(
                preferences.getLocalizedString(FurnitureTablePanel.class, "totalValueAddedTaxLabel.text"));
            furnitureTablePanel.totalPriceValueAddedTaxIncludedLabel.setText(
                preferences.getLocalizedString(FurnitureTablePanel.class, "totalPriceValueAddedTaxIncludedLabel.text"));
            // No break
          case CURRENCY :
          case VALUE_ADDED_TAX_ENABLED :
            furnitureTablePanel.updateTotalsVisibility(preferences);
            break;
        }
      }
    }
  }

  /**
   * Updates visibility of the total text fields.
   */
  private void updateTotalsVisibility(UserPreferences preferences) {
    this.totalPriceLabel.setVisible(preferences.getCurrency() != null);
    this.totalPriceTextField.setVisible(preferences.getCurrency() != null);
    this.totalValueAddedTaxLabel.setVisible(preferences.isValueAddedTaxEnabled());
    this.totalValueAddedTaxTextField.setVisible(preferences.isValueAddedTaxEnabled());
    this.totalPriceValueAddedTaxIncludedLabel.setVisible(preferences.isValueAddedTaxEnabled());
    this.totalPriceValueAddedTaxIncludedTextField.setVisible(preferences.isValueAddedTaxEnabled());
  }

  /**
   * Updates the values shown by total text fields.
   */
  private void updateTotals(Home home, UserPreferences preferences) {
    List<HomePieceOfFurniture> furniture = home.getFurniture();
    if (furniture.size() > 0) {
      BigDecimal totalPrice = new BigDecimal("0");
      BigDecimal totalValueAddedTax = new BigDecimal("0");
      BigDecimal totalPriceValueAddedTaxIncluded = new BigDecimal("0");
      FurnitureFilter furnitureFilter = getFurnitureFilter();
      String currencyCode = null;
      boolean currencySet = false;
      for (HomePieceOfFurniture piece : home.getFurniture()) {
        if (furnitureFilter == null || furnitureFilter.include(home, piece)) {
          BigDecimal price = piece.getPrice();
          if (price != null) {
            if (!currencySet) {
              currencySet = true;
              currencyCode = piece.getCurrency();
            } else if ((currencyCode != null || piece.getCurrency() != null)
                       && (currencyCode == null || !currencyCode.equals(piece.getCurrency()))) {
              // Cancel sum if prices are not in the same currency
              this.totalPriceTextField.setValue(null);
              this.totalValueAddedTaxTextField.setValue(null);
              this.totalPriceValueAddedTaxIncludedTextField.setValue(null);
              return;
            }
            totalPrice = totalPrice.add(price);
            BigDecimal valueAddedTax = piece.getValueAddedTax();
            if (valueAddedTax != null) {
              totalValueAddedTax = totalValueAddedTax.add(valueAddedTax);
            }
            totalPriceValueAddedTaxIncluded = totalPriceValueAddedTaxIncluded.add(piece.getPriceValueAddedTaxIncluded());
          }
        }
      }

      NumberFormat currencyFormat = DecimalFormat.getCurrencyInstance();
      if (currencyCode == null) {
        currencyCode = preferences.getCurrency();
      }
      if (currencyCode != null) {
        try {
          Currency currency = Currency.getInstance(currencyCode);
          currencyFormat.setCurrency(currency);
          currencyFormat.setMaximumFractionDigits(currency.getDefaultFractionDigits());
        } catch (IllegalArgumentException ex) {
          // Ignore currency
        }
      }
      DefaultFormatterFactory formatterFactory = new DefaultFormatterFactory(new NumberFormatter(currencyFormat));
      this.totalPriceTextField.setFormatterFactory(formatterFactory);
      this.totalValueAddedTaxTextField.setFormatterFactory(formatterFactory);
      this.totalPriceValueAddedTaxIncludedTextField.setFormatterFactory(formatterFactory);

      this.totalPriceTextField.setValue(totalPrice);
      this.totalValueAddedTaxTextField.setValue(totalValueAddedTax);
      this.totalPriceValueAddedTaxIncludedTextField.setValue(totalPriceValueAddedTaxIncluded);
    } else {
      this.totalPriceTextField.setValue(null);
      this.totalValueAddedTaxTextField.setValue(null);
      this.totalPriceValueAddedTaxIncludedTextField.setValue(null);
    }
  }

  /**
   * Layouts the components displayed by this panel.
   */
  private void layoutComponents() {
    JScrollPane furnitureScrollPane = SwingTools.createScrollPane(this.furnitureTable);
    furnitureScrollPane.setMinimumSize(new Dimension());
    // Add a mouse listener that gives focus to furniture view when
    // user clicks in its viewport (tables don't spread vertically if their row count is too small)
    final JViewport viewport = furnitureScrollPane.getViewport();
    viewport.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent ev) {
            viewport.getView().requestFocusInWindow();
          }
        });

    // Set default traversal keys of furniture view to ignore tab key within the table
    KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    this.furnitureTable.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
        focusManager.getDefaultFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
    this.furnitureTable.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
        focusManager.getDefaultFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));

    SwingTools.installFocusBorder(this.furnitureTable);
    setFocusTraversalPolicyProvider(false);
    setMinimumSize(new Dimension());

    add(furnitureScrollPane, new GridBagConstraints(
        0, 0, 6, 1, 1, 1, GridBagConstraints.CENTER,
        GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

    int labelAlignment = OperatingSystem.isMacOSX()
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    // First row
    Insets labelInsets = new Insets(2, 0, 0, 5);
    Insets componentInsets = new Insets(2, 0, 0, 10);
    add(this.totalPriceLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(2, 2, 0, 5), 0, 0));
    add(this.totalPriceTextField, new GridBagConstraints(
        1, 1, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(2, 0, 0, 0), 0, 0));
    add(this.totalValueAddedTaxLabel, new GridBagConstraints(
        2, 1, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(2, 10, 0, 5), 0, 0));
    add(this.totalValueAddedTaxTextField, new GridBagConstraints(
        3, 1, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, componentInsets, 0, 0));
    add(this.totalPriceValueAddedTaxIncludedLabel, new GridBagConstraints(
        4, 1, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.totalPriceValueAddedTaxIncludedTextField, new GridBagConstraints(
        5, 1, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
  }

  public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
    return this.furnitureTable.print(g, pageFormat, pageIndex);
  }

  @Override
  public void setTransferHandler(TransferHandler newHandler) {
    this.furnitureTable.setTransferHandler(newHandler);
    ((JViewport)this.furnitureTable.getParent()).setTransferHandler(newHandler);
  }

  @Override
  public void setComponentPopupMenu(JPopupMenu popup) {
    this.furnitureTable.setComponentPopupMenu(popup);
    ((JViewport)this.furnitureTable.getParent()).setComponentPopupMenu(popup);
  }

  /**
   * Returns a copy of the furniture data for transfer purpose.
   */
  public Object createTransferData(DataType dataType) {
    return this.furnitureTable.createTransferData(dataType);
  }

  /**
   * Returns <code>true</code> if the given format is CSV.
   */
  public boolean isFormatTypeSupported(FormatType formatType) {
    return this.furnitureTable.isFormatTypeSupported(formatType);
  }

  /**
   * Writes in the given stream the content of the table at CSV format if this is the requested format.
   */
  public void exportData(OutputStream out, FormatType formatType, Properties settings) throws IOException {
    this.furnitureTable.exportData(out, formatType, settings);
  }

  /**
   * Sets the filter applied to the furniture displayed by this component.
   */
  public void setFurnitureFilter(FurnitureView.FurnitureFilter filter) {
    this.furnitureTable.setFurnitureFilter(filter);
  }

  /**
   * Returns the filter applied to the furniture displayed in this component.
   */
  public FurnitureView.FurnitureFilter getFurnitureFilter() {
    return this.furnitureTable.getFurnitureFilter();
  }
}
