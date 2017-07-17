package lk.gov.health.beans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import lk.gov.health.schoolhealth.Area;
import lk.gov.health.beans.util.JsfUtil;
import lk.gov.health.beans.util.JsfUtil.PersistAction;
import lk.gov.health.faces.AreaFacade;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import lk.gov.health.faces.CoordinateFacade;
import lk.gov.health.schoolhealth.AreaType;
import lk.gov.health.schoolhealth.Coordinate;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Polygon;

@Named
@SessionScoped
public class AreaController implements Serializable {

    @EJB
    private lk.gov.health.faces.AreaFacade ejbFacade;
    @EJB
    CoordinateFacade coordinateFacade;
    private List<Area> items = null;
    private Area selected;

    @Inject
    WebUserController webUserController;

    private MapModel polygonModel;

    public String drawArea() {
        polygonModel = new DefaultMapModel();

        //Polygon
        Polygon polygon = new Polygon();

        String j = "select c from Coordinate c where c.area=:a";
        Map m = new HashMap();
        m.put("a", selected);
        List<Coordinate> cs = coordinateFacade.findBySQL(j, m);
        for (Coordinate c : cs) {
            LatLng coord = new LatLng(c.getLatitude(), c.getLongitude());
            polygon.getPaths().add(coord);
        }

        polygon.setStrokeColor("#FF9900");
        polygon.setFillColor("#FF9900");
        polygon.setStrokeOpacity(0.7);
        polygon.setFillOpacity(0.7);

        polygonModel.addOverlay(polygon);

        return "/area/area_map";
    }

    private UploadedFile file;

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public String saveCoordinates() {
        if (selected == null || selected.getId() == null) {
            JsfUtil.addErrorMessage("Please select an Area");
            return "";
        }
        if (file == null || "".equals(file.getFileName())) {
            return "";
        }
        if (file == null) {
            JsfUtil.addErrorMessage("Please select an CSV File");
            return "";
        }

        String j = "select c from Coordinate c where c.area=:a";
        Map m = new HashMap();
        m.put("a", selected);
        List<Coordinate> cs = coordinateFacade.findBySQL(j, m);
        for (Coordinate c : cs) {
            coordinateFacade.remove(c);
        }

        try {
            String line = "";
            String cvsSplitBy = ",";
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputstream(), "UTF-8"));

            int i = 0;
            while ((line = br.readLine()) != null) {
                String[] country = line.split(cvsSplitBy);

                if (i > 0) {
                    if (country.length > 2) {
                        System.out.println("Coordinates [Longitude= " + country[1] + " , Latitude=" + country[2] + "]");
                        Coordinate c = new Coordinate();
                        c.setArea(selected);

                        String strLon = country[1].replace("\"", "");
                        String strLat = country[2].replace("\"", "");

                        double lon = Double.parseDouble(strLon);
                        System.out.println("lon = " + lon);

                        double lat = Double.parseDouble(strLat);

                        c.setLongitude(lon);
                        c.setLatitude(lat);

                        coordinateFacade.create(c);
                    }
                }
                i++;
            }
            return "";
        } catch (IOException e) {
            System.out.println("e = " + e);
            return "";
        }

    }

    public String saveCentreCoordinates() {
        System.out.println("saveCentreCoordinates = ");
        if (file == null || "".equals(file.getFileName())) {
            return "";
        }
        if (file == null) {
            JsfUtil.addErrorMessage("Please select an CSV File");
            return "";
        }

        try {
            String line = "";
            String cvsSplitBy = ",";
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputstream(), "UTF-8"));

            int i = 0;
            while ((line = br.readLine()) != null) {
                String[] country = line.split(cvsSplitBy);
                System.out.println("i = " + i);
                if (i > 0) {
                    System.out.println("country.length = " + country.length);
                    if (country.length > 3) {
                        System.out.println(country[3] + "Coordinates [Longitude= " + country[1] + " , Latitude=" + country[2] + "]");

                        String areName = country[3].replace("\"", "");
                        String j = "select c from Area c where upper(c.name) like :a order by c.id desc";
                        Map m = new HashMap();
                        m.put("a", areName.toUpperCase() + "%");
                        Area a = getFacade().findFirstBySQL(j, m);

                        if (a == null) {
//                            a = new Area();
//                            a.setName(areName);
//                            a.setType(AreaType.MOH);
//                            getFacade().create(a);
                            break;
                        }

                        String strLon = country[1].replace("\"", "");
                        String strLat = country[2].replace("\"", "");

                        double lon = Double.parseDouble(strLon);
                        System.out.println("lon = " + lon);

                        double lat = Double.parseDouble(strLat);

                        a.setCentreLatitude(lat);
                        a.setCentreLongitude(lon);
                        a.setZoomLavel(12);

                        getFacade().edit(a);
                    }
                }
                i++;
            }
            return "";
        } catch (IOException e) {
            System.out.println("e = " + e);
            return "";
        }

    }

    public String toAddProvince() {
        if (!webUserController.isCapableOfAddingProvinces()) {
            JsfUtil.addErrorMessage("You are not autherized");
            return "";
        }
        selected = new Area();
        selected.setType(AreaType.Province);
        return "/area/add_province";
    }

    public String toAddDistrict() {
        if (!webUserController.isCapableOfAddingRdhsAreas()) {
            JsfUtil.addErrorMessage("You are not autherized");
            return "";
        }
        selected = new Area();
        selected.setType(AreaType.District);
        return "/area/add_district";
    }

    public String toAddMhoArea() {
        if (!webUserController.isCapableOfAddingMohAreas()) {
            JsfUtil.addErrorMessage("You are not autherized");
            return "";
        }
        selected = new Area();
        selected.setType(AreaType.MOH);
        return "/area/add_moh";
    }

    public String toEducationalZones() {
        if (!webUserController.isCapableOfAddingMohAreas()) {
            JsfUtil.addErrorMessage("You are not autherized");
            return "";
        }
        selected = new Area();
        return "/area/add_educational_zones";
    }

    public String toAddPhiArea() {
        if (!webUserController.isCapableOfAddingPhiAreas()) {
            JsfUtil.addErrorMessage("You are not autherized");
            return "";
        }
        selected = new Area();
        selected.setType(AreaType.PHI);
        return "/area/add_phi";
    }

    public String saveNewProvince() {
        if (!webUserController.isCapableOfAddingProvinces()) {
            JsfUtil.addErrorMessage("You are not autherized");
            return "";
        }
        selected.setCreateAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;
        webUserController.fillLogginDetails();
        JsfUtil.addSuccessMessage("New Province Saved");
        return "/area/add_area_index";
    }

    public String saveNewDistrict() {
        if (!webUserController.isCapableOfAddingRdhsAreas()) {
            JsfUtil.addErrorMessage("You are not autherized");
            return "";
        }
        selected.setCreateAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;
        webUserController.fillLogginDetails();
        JsfUtil.addSuccessMessage("New District Saved");
        return "/area/add_area_index";
    }

    public String saveNewMoh() {
        if (!webUserController.isCapableOfAddingMohAreas()) {
            JsfUtil.addErrorMessage("You are not autherized");
            return "";
        }
        selected.setCreateAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;
        webUserController.fillLogginDetails();
        JsfUtil.addSuccessMessage("New MOH Area Saved");
        return "/area/add_area_index";
    }

    public String saveNewEducationalZone() {
        if (!webUserController.isCapableOfAddingMohAreas()) {
            JsfUtil.addErrorMessage("You are not autherized");
            return "";
        }
        selected.setCreateAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;
        webUserController.fillLogginDetails();
        JsfUtil.addSuccessMessage("New Educational Zone Saved");
        return "/area/add_area_index";
    }

    public String saveNewPhi() {
        if (!webUserController.isCapableOfAddingPhiAreas()) {
            JsfUtil.addErrorMessage("You are not autherized");
            return "";
        }
        selected.setCreateAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;
        webUserController.fillLogginDetails();
        JsfUtil.addSuccessMessage("New PHI Area Saved");
        return "/area/add_area_index";
    }

    public List<Area> getAreas(AreaType areaType, Area superArea) {
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.name is not null ";
//        if (areaType == null) {
//            areaType = AreaType.MOH;
//        }
        if (areaType != null) {
            j += " and a.type=:t";
            m.put("t", areaType);
        }
        if (superArea != null) {
//            j += " and (a=:pa or a.parentArea=:pa or a.parentArea.parentArea=:pa or a.parentArea.parentArea.parentArea=:pa  or a.parentArea.parentArea.parentArea.parentArea=:pa) ";
            j += " and a.parentArea=:pa ";
            m.put("pa", superArea);
        }
        j += " order by a.name";
        System.out.println("m = " + m);
        System.out.println("j = " + j);
        List<Area> areas = getFacade().findBySQL(j, m);
        System.out.println("areas = " + areas);
        return areas;
    }

    public AreaController() {
    }

    public Area getSelected() {
        return selected;
    }

    public void setSelected(Area selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private AreaFacade getFacade() {
        return ejbFacade;
    }

    public Area prepareCreate() {
        selected = new Area();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("AreaCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/Bundle").getString("AreaUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("AreaDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Area> getItems() {
        if (items == null) {
            items = getFacade().findAll();
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
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            }
        }
    }

    public List<Area> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Area> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public MapModel getPolygonModel() {
        return polygonModel;
    }

    public void onPolygonSelect(OverlaySelectEvent event) {
        JsfUtil.addSuccessMessage("Selected");
    }

    @FacesConverter(forClass = Area.class)
    public static class AreaControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            AreaController controller = (AreaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "areaController");
            return controller.getFacade().find(getKey(value));
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
            if (object instanceof Area) {
                Area o = (Area) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Area.class.getName()});
                return null;
            }
        }

    }

}
