package lk.gov.health.beans;

import lk.gov.health.schoolhealth.ReturnFormat;
import lk.gov.health.beans.util.JsfUtil;
import lk.gov.health.beans.util.JsfUtil.PersistAction;
import lk.gov.health.faces.ReturnFormatFacade;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

@Named("returnFormatController")
@SessionScoped
public class ReturnFormatController implements Serializable {

    @EJB
    private lk.gov.health.faces.ReturnFormatFacade ejbFacade;
    @Inject
    WebUserController webUserController;
    private List<ReturnFormat> items = null;
    private ReturnFormat selected;

    public ReturnFormatController() {
    }

    public ReturnFormat getSelected() {
        return selected;
    }

    public void setSelected(ReturnFormat selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private ReturnFormatFacade getFacade() {
        return ejbFacade;
    }

    public ReturnFormat prepareCreate() {
        selected = new ReturnFormat();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleReturns").getString("ReturnFormatCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleReturns").getString("ReturnFormatUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleReturns").getString("ReturnFormatDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<ReturnFormat> getItems() {
        if (items == null) {
            String j = "Select f from ReturnFormat f order by f.name";
            items = getFacade().findBySQL(j);
        }
        return items;
    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            setEmbeddableKeys();
            try {
                if (persistAction != PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    getFacade().remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage(msg);
                } else {
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleReturns").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleReturns").getString("PersistenceErrorOccured"));
            }
        }
    }

    public ReturnFormat getReturnFormat(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<ReturnFormat> getItemsAvailableSelectMany() {
        return getItems();
    }

    public List<ReturnFormat> getItemsAvailableSelectOne() {
        return getItems();
    }

    public List<ReturnFormat> getReturnsToReceive() {
        List<ReturnFormat> rfs;
        String j = "Select f from ReturnFormat f "
                + " where f.receivedBy=:mp"
                + " order by f.name";

        Map m = new HashMap();
        m.put("mp", webUserController.getLoggedPrivilegeType());
        rfs = getFacade().findBySQL(j,m);
        return rfs;
    }

    public List<ReturnFormat> getReturnsToSend() {
        List<ReturnFormat> rfs;
        String j = "Select f from ReturnFormat f "
                + " where f.sentBy=:mp"
                + " order by f.name";
        Map m = new HashMap();
        m.put("mp", webUserController.getLoggedPrivilegeType());
        rfs = getFacade().findBySQL(j,m);
        return rfs;
    }

    @FacesConverter(forClass = ReturnFormat.class)
    public static class ReturnFormatControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ReturnFormatController controller = (ReturnFormatController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "returnFormatController");
            return controller.getReturnFormat(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof ReturnFormat) {
                ReturnFormat o = (ReturnFormat) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), ReturnFormat.class.getName()});
                return null;
            }
        }

    }

}
