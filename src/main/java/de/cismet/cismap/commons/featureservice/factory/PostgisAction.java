/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.featureservice.factory;

import javax.swing.ImageIcon;

/**
 *
 * @author pascal
 */
public class PostgisAction {

  protected String action;
  protected String actionText;
  protected String iconPath;
  protected ImageIcon icon;

  /**
   * Get the value of icon
   *
   * @return the value of icon
   */
  public ImageIcon getIcon()
  {
    return icon;
  }

  /**
   * Set the value of icon
   *
   * @param icon new value of icon
   */
  public void setIcon(ImageIcon icon)
  {
    this.icon = icon;
  }


  /**
   * Get the value of stringiconPath
   *
   * @return the value of stringiconPath
   */
  public String getIconPath()
  {
    return iconPath;
  }

  /**
   * Set the value of stringiconPath
   *
   * @param stringiconPath new value of stringiconPath
   */
  public void setIconPath(String iconPath)
  {
    this.iconPath = iconPath;
  }


  /**
   * Get the value of actionText
   *
   * @return the value of actionText
   */
  public String getActionText()
  {
    return actionText;
  }

  /**
   * Set the value of actionText
   *
   * @param actionText new value of actionText
   */
  public void setActionText(String actionText)
  {
    this.actionText = actionText;
  }


  /**
   * Get the value of action
   *
   * @return the value of action
   */
  public String getAction()
  {
    return action;
  }

  /**
   * Set the value of action
   *
   * @param action new value of action
   */
  public void setAction(String action)
  {
    this.action = action;
  }

}
