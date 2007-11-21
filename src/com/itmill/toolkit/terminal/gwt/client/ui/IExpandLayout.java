package com.itmill.toolkit.terminal.gwt.client.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.itmill.toolkit.terminal.gwt.client.ApplicationConnection;
import com.itmill.toolkit.terminal.gwt.client.Caption;
import com.itmill.toolkit.terminal.gwt.client.Container;
import com.itmill.toolkit.terminal.gwt.client.ContainerResizedListener;
import com.itmill.toolkit.terminal.gwt.client.Paintable;
import com.itmill.toolkit.terminal.gwt.client.StyleConstants;
import com.itmill.toolkit.terminal.gwt.client.UIDL;
import com.itmill.toolkit.terminal.gwt.client.Util;

/**
 * @author IT Mill Ltd
 */
public class IExpandLayout extends ComplexPanel implements
        ContainerResizedListener, Container {

    public static final String CLASSNAME = "i-expandlayout";
    public static final int ORIENTATION_HORIZONTAL = 1;

    public static final int ORIENTATION_VERTICAL = 0;

    /**
     * Contains reference to Element where Paintables are wrapped.
     */
    protected Element childContainer;

    protected ApplicationConnection client;

    protected HashMap componentToCaption = new HashMap();

    /*
     * Elements that provides the Layout interface implementation.
     */
    protected Element element;
    private Widget expandedWidget;

    private UIDL expandedWidgetUidl;

    int orientationMode = ORIENTATION_VERTICAL;

    protected int topMargin = -1;
    private String width;
    private String height;
    private Element me;
    private Element breakElement;
    private int bottomMargin = -1;
    private boolean hasComponentSpacing;
    private int spacingSize = -1;

    public IExpandLayout() {
        this(IExpandLayout.ORIENTATION_VERTICAL);
    }

    public IExpandLayout(int orientation) {
        orientationMode = orientation;
        constructDOM();
        setStyleName(CLASSNAME);
    }

    public void add(Widget w) {
        WidgetWrapper wrapper = createWidgetWrappper();
        DOM.appendChild(childContainer, wrapper.getElement());
        super.add(w, wrapper.getContainerElement());
    }

    protected void constructDOM() {
        element = DOM.createDiv();
        DOM.setStyleAttribute(element, "overflow", "hidden");

        if (orientationMode == ORIENTATION_HORIZONTAL) {
            me = DOM.createDiv();
            if (Util.isIE()) {
                DOM.setStyleAttribute(me, "zoom", "1");
                DOM.setStyleAttribute(me, "overflow", "hidden");
            }
            childContainer = DOM.createDiv();
            if (Util.isIE()) {
                DOM.setStyleAttribute(childContainer, "zoom", "1");
                DOM.setStyleAttribute(childContainer, "overflow", "hidden");
            }
            DOM.setStyleAttribute(childContainer, "height", "100%");
            breakElement = DOM.createDiv();
            DOM.setStyleAttribute(breakElement, "overflow", "hidden");
            DOM.setStyleAttribute(breakElement, "height", "0px");
            DOM.setStyleAttribute(breakElement, "clear", "both");
            DOM.appendChild(me, childContainer);
            DOM.appendChild(me, breakElement);
            DOM.appendChild(element, me);
        } else {
            childContainer = DOM.createDiv();
            DOM.appendChild(element, childContainer);
            me = childContainer;
        }
        setElement(element);
    }

    protected WidgetWrapper createWidgetWrappper() {
        switch (orientationMode) {
        case ORIENTATION_HORIZONTAL:
            return new HorizontalWidgetWrapper();
        default:
            return new VerticalWidgetWrapper();
        }
    }

    /**
     * Returns given widgets WidgetWrapper
     * 
     * @param child
     * @return
     */
    public WidgetWrapper getWidgetWrapperFor(Widget child) {
        Element containerElement = DOM.getParent(child.getElement());
        switch (orientationMode) {
        case ORIENTATION_HORIZONTAL:
            return new HorizontalWidgetWrapper(containerElement);
        default:
            return new VerticalWidgetWrapper(containerElement);
        }
    }

    abstract class WidgetWrapper extends UIObject {
        /**
         * @return element that contains Widget
         */
        public Element getContainerElement() {
            return getElement();
        }

        abstract void setExpandedSize(int pixels);

        abstract void setAlignment(String verticalAlignment,
                String horizontalAlignment);

        abstract void setSpacingEnabled(boolean b);
    }

    class VerticalWidgetWrapper extends WidgetWrapper {

        public VerticalWidgetWrapper(Element div) {
            setElement(div);
        }

        public VerticalWidgetWrapper() {
            setElement(DOM.createDiv());
            // this is mostly needed for IE, could be isolated
            DOM.setStyleAttribute(getContainerElement(), "overflow", "hidden");
        }

        void setExpandedSize(int pixels) {
            int spaceForMarginsAndSpacings = getOffsetHeight()
                    - DOM.getElementPropertyInt(getElement(), "clientHeight");
            int fixedInnerSize = pixels - spaceForMarginsAndSpacings;
            if (fixedInnerSize < 0) {
                fixedInnerSize = 0;
            }
            setHeight(fixedInnerSize + "px");
        }

        void setAlignment(String verticalAlignment, String horizontalAlignment) {
            DOM.setStyleAttribute(getElement(), "textAlign",
                    horizontalAlignment);
            // ignoring vertical alignment
        }

        void setSpacingEnabled(boolean b) {
            setStyleName(getElement(), CLASSNAME + "-vspacing", b);
        }
    }

    class HorizontalWidgetWrapper extends WidgetWrapper {

        Element td;
        String valign = "top";

        public HorizontalWidgetWrapper(Element element) {
            if (DOM.getElementProperty(element, "nodeName").equals("TD")) {
                td = element;
                setElement(DOM.getParent(DOM.getParent(DOM.getParent(DOM
                        .getParent(td)))));
            } else {
                setElement(element);
            }
        }

        public HorizontalWidgetWrapper() {
            setElement(DOM.createDiv());
            DOM.setStyleAttribute(getElement(), "cssFloat", "left");
            if (Util.isIE()) {
                DOM.setStyleAttribute(getElement(), "styleFloat", "left");
            }
            DOM.setStyleAttribute(getElement(), "height", "100%");
        }

        void setExpandedSize(int pixels) {
            setWidth(pixels + "px");
            DOM.setStyleAttribute(getElement(), "overflow", "hidden");
        }

        void setAlignment(String verticalAlignment, String horizontalAlignment) {
            DOM.setStyleAttribute(getElement(), "verticalAlign",
                    verticalAlignment);
            if (!valign.equals(verticalAlignment)) {
                if (verticalAlignment.equals("top")) {
                    // remove table, move content to div

                } else {
                    if (td == null) {
                        // build one cell table
                        Element table = DOM.createTable();
                        Element tBody = DOM.createTBody();
                        Element tr = DOM.createTR();
                        td = DOM.createTD();
                        DOM.appendChild(table, tBody);
                        DOM.appendChild(tBody, tr);
                        DOM.appendChild(tr, td);
                        DOM.setElementProperty(table, "className", CLASSNAME
                                + "-valign");
                        DOM.setElementProperty(tr, "className", CLASSNAME
                                + "-valign");
                        DOM.setElementProperty(td, "className", CLASSNAME
                                + "-valign");
                        // move possible content to cell
                        Element content = DOM.getFirstChild(getElement());
                        if (content != null) {
                            DOM.removeChild(getElement(), content);
                            DOM.appendChild(td, content);
                        }
                        DOM.appendChild(getElement(), table);
                    }
                    // set alignment
                    DOM.setStyleAttribute(td, "verticalAlign",
                            verticalAlignment);
                }
                valign = verticalAlignment;
            }
        }

        public Element getContainerElementu() {
            if (td == null) {
                return super.getContainerElement();
            } else {
                return td;
            }
        }

        void setSpacingEnabled(boolean b) {
            setStyleName(getElement(), CLASSNAME + "-hspacing", b);
        }
    }

    protected ArrayList getPaintables() {
        ArrayList al = new ArrayList();
        Iterator it = iterator();
        while (it.hasNext()) {
            Widget w = (Widget) it.next();
            if (w instanceof Paintable) {
                al.add(w);
            }
        }
        return al;
    }

    public Widget getWidget(int index) {
        return getChildren().get(index);
    }

    public int getWidgetCount() {
        return getChildren().size();
    }

    public int getWidgetIndex(Widget child) {
        return getChildren().indexOf(child);
    }

    protected void handleAlignments(UIDL uidl) {
        // Component alignments as a comma separated list.
        // See com.itmill.toolkit.terminal.gwt.client.ui.AlignmentInfo.java for
        // possible values.
        int[] alignments = uidl.getIntArrayAttribute("alignments");
        int alignmentIndex = 0;
        // Set alignment attributes
        Iterator it = getPaintables().iterator();
        boolean first = true;
        while (it.hasNext()) {
            // Calculate alignment info
            AlignmentInfo ai = new AlignmentInfo(alignments[alignmentIndex++]);
            WidgetWrapper wr = getWidgetWrapperFor((Widget) it.next());
            wr.setAlignment(ai.getVerticalAlignment(), ai
                    .getHorizontalAlignment());
            if (first) {
                wr.setSpacingEnabled(false);
                first = false;
            } else {
                wr.setSpacingEnabled(hasComponentSpacing);
            }

        }
    }

    protected void handleMargins(UIDL uidl) {
        MarginInfo margins = new MarginInfo(uidl.getIntAttribute("margins"));
        setStyleName(me, CLASSNAME + "-" + StyleConstants.LAYOUT_MARGIN_TOP,
                margins.hasTop());
        setStyleName(me, StyleConstants.LAYOUT_MARGIN_RIGHT, margins.hasRight());
        setStyleName(me, CLASSNAME + "-" + StyleConstants.LAYOUT_MARGIN_BOTTOM,
                margins.hasBottom());
        setStyleName(me, StyleConstants.LAYOUT_MARGIN_LEFT, margins.hasLeft());
    }

    public boolean hasChildComponent(Widget component) {
        return getWidgetIndex(component) >= 0;
    }

    public void iLayout() {
        if (orientationMode == ORIENTATION_HORIZONTAL) {
            int pixels = getOffsetHeight() - getTopMargin() - getBottomMargin();
            if (pixels < 0) {
                pixels = 0;
            }
            DOM.setStyleAttribute(me, "height", pixels + "px");
        }

        if (expandedWidget == null) {
            return;
        }

        int availableSpace = getAvailableSpace();

        int usedSpace = getUsedSpace();

        int spaceForExpandedWidget = availableSpace - usedSpace;

        if (spaceForExpandedWidget < 0) {
            // TODO fire warning for developer
            spaceForExpandedWidget = 0;
        }

        WidgetWrapper wr = getWidgetWrapperFor(expandedWidget);
        wr.setExpandedSize(spaceForExpandedWidget);

        // TODO save previous size and only propagate if really changed
        Util.runDescendentsLayout(this);
    }

    private int getTopMargin() {
        if (topMargin < 0) {
            topMargin = DOM.getElementPropertyInt(childContainer, "offsetTop");
        }
        return topMargin;
    }

    private int getBottomMargin() {
        if (bottomMargin < 0) {
            bottomMargin = DOM.getElementPropertyInt(me, "offsetHeight")
                    - DOM.getElementPropertyInt(breakElement, "offsetTop");
        }
        return bottomMargin;
    }

    private int getUsedSpace() {
        int total = 0;
        int widgetCount = getWidgetCount();
        Iterator it = iterator();
        while (it.hasNext()) {
            Widget w = (Widget) it.next();
            if (w != expandedWidget) {
                switch (orientationMode) {
                case ORIENTATION_VERTICAL:
                    total += DOM.getElementPropertyInt(DOM.getParent(w
                            .getElement()), "offsetHeight");
                    break;
                default:
                    total += DOM.getElementPropertyInt(DOM.getParent(w
                            .getElement()), "offsetWidth");
                    break;
                }
            }
        }
        total += getSpacingSize() * (widgetCount - 1);
        return total;
    }

    private int getSpacingSize() {
        if (hasComponentSpacing) {
            if (spacingSize < 0) {
                Element temp = DOM.createDiv();
                WidgetWrapper wr = createWidgetWrappper();
                wr.setSpacingEnabled(true);
                DOM.appendChild(temp, wr.getElement());
                DOM.setStyleAttribute(temp, "position", "absolute");
                DOM.setStyleAttribute(temp, "top", "0");
                DOM.setStyleAttribute(temp, "visibility", "hidden");
                DOM.appendChild(RootPanel.getBodyElement(), temp);
                if (orientationMode == ORIENTATION_HORIZONTAL) {
                    spacingSize = DOM.getElementPropertyInt(wr.getElement(),
                            "offsetLeft");
                } else {
                    spacingSize = DOM.getElementPropertyInt(wr.getElement(),
                            "offsetTop");
                }
                DOM.removeChild(RootPanel.getBodyElement(), temp);
            }
            return spacingSize;
        } else {
            return 0;
        }
    }

    private int getAvailableSpace() {
        int size;
        switch (orientationMode) {
        case ORIENTATION_VERTICAL:
            size = getOffsetHeight();

            int marginTop = DOM.getElementPropertyInt(DOM.getFirstChild(me),
                    "offsetTop")
                    - DOM.getElementPropertyInt(element, "offsetTop");

            Element lastElement = DOM.getChild(me, (DOM.getChildCount(me) - 1));
            int marginBottom = DOM.getElementPropertyInt(me, "offsetHeight")
                    + DOM.getElementPropertyInt(me, "offsetTop")
                    - (DOM.getElementPropertyInt(lastElement, "offsetTop") + DOM
                            .getElementPropertyInt(lastElement, "offsetHeight"));
            size -= (marginTop + marginBottom); // FIXME expects same size
            // top/bottom margin
            break;
        default:
            size = DOM.getElementPropertyInt(childContainer, "offsetWidth");
            break;
        }
        return size;
    }

    protected void insert(Widget w, int beforeIndex) {
        if (w instanceof Caption) {
            Caption c = (Caption) w;
            // captions go into same container element as their
            // owners
            Element container = DOM.getParent(((UIObject) c.getOwner())
                    .getElement());
            Element captionContainer = DOM.createDiv();
            DOM.insertChild(container, captionContainer, 0);
            insert(w, captionContainer, beforeIndex, false);
        } else {
            WidgetWrapper wrapper = createWidgetWrappper();
            DOM.insertChild(childContainer, wrapper.getElement(), beforeIndex);
            insert(w, wrapper.getContainerElement(), beforeIndex, false);
        }
    }

    public boolean remove(int index) {
        return remove(getWidget(index));
    }

    public boolean remove(Widget w) {
        WidgetWrapper ww = getWidgetWrapperFor(w);
        boolean removed = super.remove(w);
        if (removed) {
            if (!(w instanceof Caption)) {
                DOM.removeChild(childContainer, ww.getElement());
            }
            return true;
        }
        return false;
    }

    public void removeCaption(Widget w) {
        Caption c = (Caption) componentToCaption.get(w);
        if (c != null) {
            this.remove(c);
            componentToCaption.remove(w);
        }
    }

    public boolean removePaintable(Paintable p) {
        Caption c = (Caption) componentToCaption.get(p);
        if (c != null) {
            componentToCaption.remove(c);
            remove(c);
        }
        client.unregisterPaintable(p);
        return remove((Widget) p);
    }

    public void replaceChildComponent(Widget from, Widget to) {
        client.unregisterPaintable((Paintable) from);
        Caption c = (Caption) componentToCaption.get(from);
        if (c != null) {
            remove(c);
            componentToCaption.remove(c);
        }
        int index = getWidgetIndex(from);
        if (index >= 0) {
            remove(index);
            insert(to, index);
        }
    }

    public void updateCaption(Paintable component, UIDL uidl) {

        Caption c = (Caption) componentToCaption.get(component);

        if (Caption.isNeeded(uidl)) {
            if (c == null) {
                int index = getWidgetIndex((Widget) component);
                c = new Caption(component, client);
                insert(c, index);
                componentToCaption.put(component, c);
            }
            c.updateCaption(uidl);
        } else {
            if (c != null) {
                remove(c);
                componentToCaption.remove(component);
            }
        }
    }

    public void setWidth(String newWidth) {
        if (newWidth.equals(width)) {
            return;
        }
        width = newWidth;
        super.setWidth(width);
    }

    public void setHeight(String newHeight) {
        if (newHeight.equals(height)) {
            return;
        }
        height = newHeight;
        super.setHeight(height);
        if (orientationMode == ORIENTATION_HORIZONTAL) {
            iLayout();
        }
    }

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {

        this.client = client;

        // Ensure correct implementation
        if (client.updateComponent(this, uidl, false)) {
            return;
        }

        // Modify layout margins
        handleMargins(uidl);

        setWidth(uidl.hasAttribute("width") ? uidl.getStringAttribute("width")
                : "");

        setHeight(uidl.hasAttribute("height") ? uidl
                .getStringAttribute("height") : "");

        hasComponentSpacing = uidl.getBooleanAttribute("spacing");

        ArrayList uidlWidgets = new ArrayList();
        for (Iterator it = uidl.getChildIterator(); it.hasNext();) {
            UIDL cellUidl = (UIDL) it.next();
            Widget child = client.getWidget(cellUidl.getChildUIDL(0));
            uidlWidgets.add(child);
            if (cellUidl.hasAttribute("expanded")) {
                expandedWidget = child;
                expandedWidgetUidl = cellUidl.getChildUIDL(0);
            }
        }

        ArrayList oldWidgets = getPaintables();

        Iterator oldIt = oldWidgets.iterator();
        Iterator newIt = uidlWidgets.iterator();
        Iterator newUidl = uidl.getChildIterator();

        Widget oldChild = null;
        while (newIt.hasNext()) {
            Widget child = (Widget) newIt.next();
            UIDL childUidl = ((UIDL) newUidl.next()).getChildUIDL(0);
            if (oldChild == null && oldIt.hasNext()) {
                // search for next old Paintable which still exists in layout
                // and delete others
                while (oldIt.hasNext()) {
                    oldChild = (Widget) oldIt.next();
                    // now oldChild is an instance of Paintable
                    if (uidlWidgets.contains(oldChild)) {
                        break;
                    } else {
                        removePaintable((Paintable) oldChild);
                        oldChild = null;
                    }
                }
            }
            if (oldChild == null) {
                // we are adding components to layout
                add(child);
            } else if (child == oldChild) {
                // child already attached and updated
                oldChild = null;
            } else if (hasChildComponent(child)) {
                // current child has been moved, re-insert before current
                // oldChild
                // TODO this might be optimized by moving only container element
                // to correct position
                removeCaption(child);
                int index = getWidgetIndex(oldChild);
                if (componentToCaption.containsKey(oldChild)) {
                    index--;
                }
                remove(child);
                insert(child, index);
            } else {
                // insert new child before old one
                int index = getWidgetIndex(oldChild);
                insert(child, index);
            }
            if (child != expandedWidget) {
                ((Paintable) child).updateFromUIDL(childUidl, client);
            }
        }
        // remove possibly remaining old Paintable object which were not updated
        while (oldIt.hasNext()) {
            oldChild = (Widget) oldIt.next();
            Paintable p = (Paintable) oldChild;
            if (!uidlWidgets.contains(p)) {
                removePaintable(p);
            }
        }

        if (uidlWidgets.size() == 0) {
            return;
        }

        // Set component alignments
        handleAlignments(uidl);

        iLayout();

        /*
         * Expanded widget is updated after layout function so it has its
         * container fixed at the moment of updateFromUIDL.
         */
        ((Paintable) expandedWidget).updateFromUIDL(expandedWidgetUidl, client);

    }
}
