   /*
 * SimpleUpdateablePostgisFeatureService.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 4. September 2006, 16:55
 *
 */
package de.cismet.cismap.commons.featureservice;


import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.featureservice.factory.PostgisAction;
import de.cismet.cismap.commons.featureservice.factory.PostgisFeatureFactory;
import javax.swing.ImageIcon;
import org.jdom.CDATA;
import org.jdom.Element;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class SimpleUpdateablePostgisFeatureService extends SimplePostgisFeatureService
{
  protected PostgisAction postgisAction;

  /** Creates a new instance of SimpleUpdateablePostgisFeatureService */
  public SimpleUpdateablePostgisFeatureService(Element element) throws Exception
  {
    super(element);
  }

  public SimpleUpdateablePostgisFeatureService(SimpleUpdateablePostgisFeatureService supfs)
  {
    super(supfs);
    this.postgisAction = supfs.getPostgisAction();
  }

  @Override
  public Element toElement()
  {
    Element element = super.toElement();
    element.setAttribute("updateable", "true");//NOI18N
    Element actionElement = new Element("action");//NOI18N
    actionElement.setAttribute("text", this.postgisAction.getActionText());//NOI18N
    actionElement.setAttribute("icon", this.postgisAction.getIconPath());//NOI18N
    actionElement.addContent(new CDATA(this.postgisAction.getAction()));
    element.addContent(actionElement);
    return element;
  }

  @Override
  public void initFromElement(Element element) throws Exception
  {
    super.initFromElement(element);
    Element actionElement = null;
    this.postgisAction = new PostgisAction();
    try
    {
      actionElement = element.getChild("action");//NOI18N
      this.postgisAction.setAction(actionElement.getText());
    } catch (Exception e)
    {
      logger.warn("No action in updateable Service: " + e.getMessage());//NOI18N
    }
    try
    {
      this.postgisAction.setActionText(actionElement.getAttribute("text").getValue());//NOI18N
      this.postgisAction.setIconPath(actionElement.getAttribute("icon").getValue());//NOI18N
      this.postgisAction.setIcon(new ImageIcon(getClass().getResource(this.postgisAction.getIconPath())));
    } catch (Exception e)
    {
      logger.warn("No actiontext in updateable Service: " + e.getMessage());//NOI18N
      this.postgisAction.setActionText(org.openide.util.NbBundle.getMessage(SimpleUpdateablePostgisFeatureService.class, "SimpleUpdateablePostgisFeatureService.initFromElement.postgisAction.actionText") );
    }
  }

  /**
   * Get the value of postgisAction
   *
   * @return the value of postgisAction
   */
  public PostgisAction getPostgisAction()
  {
    return postgisAction;
  }

  @Override
  public SimpleUpdateablePostgisFeatureService clone()
  {
    return new SimpleUpdateablePostgisFeatureService(this);
  }

  @Override
  protected PostgisFeatureFactory createFeatureFactory() throws Exception
  {
    return new PostgisFeatureFactory(this.getLayerProperties(), this.getConnectionInfo(), this.postgisAction, this);
  }
}
