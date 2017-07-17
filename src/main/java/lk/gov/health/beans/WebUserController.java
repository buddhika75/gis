package lk.gov.health.beans;

import lk.gov.health.schoolhealth.WebUser;
import lk.gov.health.beans.util.JsfUtil;
import lk.gov.health.beans.util.JsfUtil.PersistAction;
import lk.gov.health.faces.WebUserFacade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
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
import lk.gov.health.schoolhealth.Area;
import lk.gov.health.schoolhealth.AreaType;
import lk.gov.health.schoolhealth.Institution;
import lk.gov.health.schoolhealth.InstitutionType;
import lk.gov.health.schoolhealth.Month;
import lk.gov.health.schoolhealth.PrivilegeType;
import lk.gov.health.schoolhealth.Quarter;

@Named
@SessionScoped
public class WebUserController implements Serializable {

    @EJB
    private lk.gov.health.faces.WebUserFacade ejbFacade;

    @Inject
    AreaController areaController;
    @Inject
    InstitutionController institutionController;

    private List<WebUser> items = null;
    private WebUser selected;

    List<Area> myProvinces;
    List<Area> myDistricts;
    List<Area> myMohAreas;
    List<Area> myEducationalZones;
    List<Area> myPhiAreas;
    List<Area> myAreas;
    List<Institution> mySchools;
    List<PrivilegeType> myPrivilegeTypes;

    private Area loggedPhiArea;
    private Area loggedMohArea;
    private Area loggedRdhsArea;
    private Area loggedPdhsArea;
    private Institution loggedMohOffice;
    private Institution loggedRdhsOffice;
    private WebUser loggedUser;
    private PrivilegeType loggedPrivilegeType;
    private boolean logged;
    private boolean developmentStage = false;

    private String userName;
    private String password;
    private String currentPassword;
    private String confirmPassword;
    private Area phiArea;
    private Area mohArea;
    private Area rdhsArea;
    private Institution mohOffice;
    private Institution rdhsOffice;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String toEditMyDetails() {
        if (loggedUser == null) {
            return "";
        }
        selected = loggedUser;
        return "/webUser/edit_my_details";
    }

    public String toEditMyPassword() {
        if (loggedUser == null) {
            return "";
        }
        selected = loggedUser;
        return "/webUser/edit_my_password";
    }

    public String toAddNewUser() {
        selected = new WebUser();
        selected.setActive(true);
        return "/webUser/add_new_user";
    }

    public String updateMyPassword() {
        if (selected == null) {
            return "";
        }
        if (selected.getId() == null) {
            return "";
        }
        if (!selected.getPassword().equals(currentPassword)) {
            JsfUtil.addErrorMessage("Current Password is Wrong");
            return "";
        }
        if (!password.equals(confirmPassword)) {
            JsfUtil.addErrorMessage("Password and confirm password is NOT maching");
            return "";
        }
        selected.setPassword(password);
        getFacade().edit(selected);
        password = "";
        confirmPassword = "";
        currentPassword = "";
        selected = null;
        JsfUtil.addSuccessMessage("Updated");
        return "/index";
    }

    public String saveNewUser() {
        if (userName == null) {
            JsfUtil.addErrorMessage("Enter a Username");
            return "";
        }
        if (userName.trim().equals("")) {
            JsfUtil.addErrorMessage("Enter a Username");
            return "";
        }
        if (userName.contains(" ")) {
            JsfUtil.addErrorMessage("Username can NOT contain spaces");
            return "";
        }
        if (password == null) {
            JsfUtil.addErrorMessage("Enter a Password");
            return "";
        }
        if (password.contains(" ")) {
            JsfUtil.addErrorMessage("Password can NOT contain spaces");
            return "";
        }
        if (!password.equals(confirmPassword)) {
            JsfUtil.addErrorMessage("Passwords NOT matching");
            return "";
        }
        try {
            selected.setUserName(userName);
            selected.setPassword(password);
            getFacade().create(selected);
        } catch (Exception e) {
            JsfUtil.addErrorMessage("User name already taken. Please select another username");
            return "";
        }
        JsfUtil.addSuccessMessage("User Added");
        userName = "";
        password = "";
        confirmPassword = "";
        selected = null;
        return "/webUser/add_webuser_index";
    }

    public String login() {
        makeAllLoggedVariablesNull();
        if (developmentStage) {
            loggedUser = new WebUser();
            loggedUser.setType(PrivilegeType.System_Administrator);
        } else {
            String j;
            Map m = new HashMap();
            j = "select w from WebUser w "
                    + " where upper(w.userName) = :un "
                    + " and w.password=:pw "
                    + " order by w.id desc";
            m.put("un", userName.trim().toUpperCase());
            m.put("pw", password);
            loggedUser = getFacade().findFirstBySQL(j, m);
            if (loggedUser == null) {
                JsfUtil.addErrorMessage("Wrong login details, please retry!");
                return "";
            }
            if (loggedUser.isActive() != true) {
                JsfUtil.addErrorMessage("Your account needs Activation. Please contact system administrators!");
                return "";
            }
        }
        loggedPrivilegeType = loggedUser.getType();
        fillLogginDetails();
        fillPrivilegeTypes();
        logged = true;
        return "";
    }

    public void fillPrivilegeTypes() {
        myPrivilegeTypes = new ArrayList<PrivilegeType>();
        if (developmentStage) {
            addGuestPrivilages();
            addMohPrivilages();
            addRdhsPrivilages();
            addPdhsPrivileges();
            addSuperUserPrivileges();
            addAdminsPrivileges();
            return;
        }

        switch (loggedUser.getType()) {
            //Guest Level
            case Guest:
                addGuestPrivilages();
                return;
            //MOH Level
            case MOH:
            case AMOH:
            case MO:
            case RMO_AMO:
            case SPHNS:
            case SPHI:
            case SPHM:
            case MOH_Staff:
                addGuestPrivilages();
                addMohPrivilages();
                return;
            //District Level
            case CCP_RDHS:
            case MO_School_Health:
            case MO_RDHS:
            case DSPHNS:
            case DSPHI:
            case DSPHM:
            case RDHS_Staff:
                addGuestPrivilages();
                addMohPrivilages();
                addRdhsPrivilages();
                return;
            //Provincial Level
            case CCP_PDHS:
            case MO_PDHS:
            case PSPHNS:
            case PSPHI:
            case PSPHM:
            case PDHS_Staff:
                addGuestPrivilages();
                addMohPrivilages();
                addRdhsPrivilages();
                addPdhsPrivileges();
                return;
            //System Level

            case System_Super_User:
            case Institution_Super_User:
                addGuestPrivilages();
                addMohPrivilages();
                addRdhsPrivilages();
                addPdhsPrivileges();
                addSuperUserPrivileges();
                return;
            case Institution_Administrator:
            case System_Administrator:
                addGuestPrivilages();
                addMohPrivilages();
                addRdhsPrivilages();
                addPdhsPrivileges();
                addSuperUserPrivileges();
                addAdminsPrivileges();
                return;
        }
    }

    public boolean isMohStaff() {
        if (loggedPrivilegeType == null) {
            return false;
        }
        switch (loggedPrivilegeType) {
            case System_Administrator:
            case System_Super_User:
            case MOH:
            case AMOH:
            case MO:
            case RMO_AMO:
            case SPHNS:
            case SPHI:
            case SPHM:
            case MOH_Staff:
                return true;
            default:
                return false;
        }
    }

    public boolean isRdhsStaff() {
        if (loggedPrivilegeType == null) {
            return false;
        }
        switch (loggedPrivilegeType) {
            case System_Administrator:
            case System_Super_User:
            case RDHS:
            case CCP_RDHS:
            case MO_School_Health:
            case MO_RDHS:
            case MO_MCH:
            case Regional_Epidemiologist:
            case DSPHNS:
            case DSPHI:
            case DSPHM:
            case RSPHNO:
            case SPHID:
            case SSDT:
            case MO_Planning_RDHS:
            case RDHS_Staff:
            case Triposha_Limited:
                return true;
            default:
                return false;
        }
    }

    public boolean isAdmin() {
        if (loggedPrivilegeType == null) {
            return false;
        }
        switch (loggedPrivilegeType) {
            case System_Administrator:
            case System_Super_User:
                return true;
            default:
                return false;
        }
    }

    public boolean isPdhsStaff() {
        if (loggedPrivilegeType == null) {
            return false;
        }
        switch (loggedPrivilegeType) {
            case System_Administrator:
            case System_Super_User:
            case PDHS:
            case CCP_PDHS:
            case MO_PDHS:
            case PSPHNS:
            case PSPHI:
            case PSPHM:
            case PDHS_Staff:
                return true;
            default:
                return false;
        }
    }

    public boolean isRdhs() {
        if (loggedPrivilegeType == null) {
            return false;
        } else if (loggedPrivilegeType == PrivilegeType.System_Administrator || loggedPrivilegeType == PrivilegeType.System_Super_User || loggedPrivilegeType == PrivilegeType.RDHS) {
            return true;
        } else {
            return false;
        }
    }

    private void addGuestPrivilages() {
        myPrivilegeTypes.add(PrivilegeType.Guest);
    }

    private void addMohPrivilages() {
        //MOH Level
        myPrivilegeTypes.add(PrivilegeType.MOH);
        myPrivilegeTypes.add(PrivilegeType.AMOH);
        myPrivilegeTypes.add(PrivilegeType.MO);
        myPrivilegeTypes.add(PrivilegeType.RMO_AMO);
        myPrivilegeTypes.add(PrivilegeType.SPHI);
        myPrivilegeTypes.add(PrivilegeType.MOH_Staff);
        return;

    }

    private void addRdhsPrivilages() {
        //District Level
        myPrivilegeTypes.add(PrivilegeType.CCP_RDHS);
        myPrivilegeTypes.add(PrivilegeType.MO_School_Health);
        myPrivilegeTypes.add(PrivilegeType.MO_RDHS);
        myPrivilegeTypes.add(PrivilegeType.DSPHI);
        myPrivilegeTypes.add(PrivilegeType.RDHS_Staff);
        myPrivilegeTypes.add(PrivilegeType.MO_MCH);
        myPrivilegeTypes.add(PrivilegeType.MO_Planning_RDHS);
        myPrivilegeTypes.add(PrivilegeType.MO_School_Health);
        myPrivilegeTypes.add(PrivilegeType.MO_MCH);
        myPrivilegeTypes.add(PrivilegeType.Regional_Epidemiologist);
        myPrivilegeTypes.add(PrivilegeType.RDHS);
    }

    private void addPdhsPrivileges() {
        //Provincial Level
        myPrivilegeTypes.add(PrivilegeType.CCP_PDHS);
        myPrivilegeTypes.add(PrivilegeType.MO_PDHS);
        myPrivilegeTypes.add(PrivilegeType.PSPHI);
        myPrivilegeTypes.add(PrivilegeType.PDHS_Staff);
    }

    private void addSuperUserPrivileges() {
        myPrivilegeTypes.add(PrivilegeType.System_Super_User);
        myPrivilegeTypes.add(PrivilegeType.Institution_Super_User);
    }

    private void addAdminsPrivileges() {
        myPrivilegeTypes.add(PrivilegeType.System_Administrator);
        myPrivilegeTypes.add(PrivilegeType.Institution_Administrator);
    }

    public List<PrivilegeType> getMyPrivilegeTypes() {
        if (developmentStage) {
            myPrivilegeTypes = new ArrayList<PrivilegeType>();
            myPrivilegeTypes.add(PrivilegeType.System_Administrator);
        }
        return myPrivilegeTypes;
    }

    public void setMyPrivilegeTypes(List<PrivilegeType> myPrivilegeTypes) {
        this.myPrivilegeTypes = myPrivilegeTypes;
    }

    public void fillLogginDetails() {
        if (developmentStage) {
            myAreas = areaController.getAreas(null, null);
            mySchools = institutionController.getInstitutions(InstitutionType.School, null, null, null);
            return;
        }
        switch (loggedUser.getType()) {
            case SPHNS:
            case SPHI:
            case SPHM:
            case MOH:
            case AMOH:
            case RMO_AMO:
            case MO:
            case MOH_Staff:

                loggedMohArea = loggedUser.getArea();
                loggedRdhsArea = loggedMohArea.getParentArea();
                loggedPdhsArea = loggedRdhsArea.getParentArea();
                myProvinces.add(loggedPdhsArea);
                myDistricts.add(loggedRdhsArea);
                myMohAreas.add(loggedMohArea);
                myPhiAreas = areaController.getAreas(AreaType.PHI, loggedMohArea);
                myAreas = areaController.getAreas(null, loggedMohArea);
                mySchools = institutionController.getInstitutions(InstitutionType.School, null, loggedRdhsArea, null);
                break;
            case MO_RDHS:
            case CCP_RDHS:
            case RDHS_Staff:
            case DSPHNS:
            case DSPHI:
            case DSPHM:
            case MO_School_Health:
            case MO_MCH:
            case MO_Planning_RDHS:
            case RDHS:
            case Regional_Epidemiologist:
                loggedRdhsArea = loggedUser.getArea();
                loggedPdhsArea = loggedRdhsArea.getParentArea();
                myProvinces.add(loggedPdhsArea);
                myDistricts.add(loggedRdhsArea);
                myMohAreas = areaController.getAreas(AreaType.MOH, loggedRdhsArea);
                myPhiAreas = areaController.getAreas(AreaType.PHI, loggedRdhsArea);
                myAreas = areaController.getAreas(null, loggedRdhsArea);
                mySchools = institutionController.getInstitutions(InstitutionType.School, null, loggedRdhsArea, null);
                break;
            case PSPHI:
            case PSPHNS:
            case PSPHM:
            case PDHS_Staff:
            case MO_PDHS:
            case CCP_PDHS:
            case PDHS:
                loggedPdhsArea = loggedUser.getArea();
                myProvinces.add(loggedPdhsArea);
                myDistricts = areaController.getAreas(AreaType.District, loggedRdhsArea);
                myMohAreas = areaController.getAreas(AreaType.MOH, loggedPdhsArea);
                myPhiAreas = areaController.getAreas(AreaType.PHI, loggedPdhsArea);
                myAreas = areaController.getAreas(null, loggedPdhsArea);
                mySchools = institutionController.getInstitutions(InstitutionType.School, null, loggedPdhsArea, null);
                break;
            case Institution_Administrator:
            case Institution_Super_User:
            case System_Administrator:
            case System_Super_User:
                myProvinces = areaController.getAreas(AreaType.Province, null);
                myDistricts = areaController.getAreas(AreaType.District, null);
                myMohAreas = areaController.getAreas(AreaType.MOH, null);
                myPhiAreas = areaController.getAreas(AreaType.PHI, null);
                myAreas = areaController.getAreas(null, null);
                mySchools = institutionController.getInstitutions(InstitutionType.School, null, null, null);
                break;
            case Guest:
                break;
        }

    }

    public boolean isCapableOfAddingPhiAreas() {
        if (developmentStage) {
            return true;
        }
        if (loggedPrivilegeType == null) {
            return false;
        }
        switch (loggedPrivilegeType) {
            case System_Administrator:
            case System_Super_User:
            case Institution_Administrator:
            case Institution_Super_User:
            case MOH:
            case MOH_Staff:
            case AMOH:
            case MO:
            case RMO_AMO:
            case PSPHI:
            case PSPHNS:
            case PSPHM:
            case DSPHNS:
            case DSPHI:
            case DSPHM:
            case SPHI:
            case RDHS_Staff:
            case PDHS_Staff:
            case MO_School_Health:
            case MO_RDHS:
            case MO_PDHS:
            case CCP_PDHS:
            case CCP_RDHS:
            case MO_MCH:
            case MO_Planning_RDHS:
                return true;
            case Guest:
                return false;

        }
        return false;
    }

    public boolean isCapableOfAddingMohAreas() {
        if (developmentStage) {
            return true;
        }
        if (loggedPrivilegeType == null) {
            return false;
        }
        switch (loggedPrivilegeType) {
            case System_Administrator:
            case System_Super_User:
            case Institution_Administrator:
            case Institution_Super_User:
            case PSPHI:
            case DSPHI:
            case SPHI:
            case SPHM:
            case SPHNS:

            case RDHS_Staff:
            case PDHS_Staff:
            case MO_School_Health:
            case MO_RDHS:
            case MO_PDHS:
            case CCP_PDHS:
            case CCP_RDHS:
                return true;
            case MOH:
            case MOH_Staff:
            case AMOH:
            case MO:
            case RMO_AMO:
            case Guest:
                return false;

        }
        return false;
    }

    public boolean isCapableOfAddingRdhsAreas() {
        if (developmentStage) {
            return true;
        }
        if (loggedPrivilegeType == null) {
            return false;
        }
        switch (loggedPrivilegeType) {
            case System_Administrator:
            case System_Super_User:
            case Institution_Administrator:
            case Institution_Super_User:
            case PSPHI:
            case PDHS_Staff:
            case MO_PDHS:
            case CCP_PDHS:
                return true;
            case CCP_RDHS:
            case MO_School_Health:
            case MO_RDHS:
            case DSPHI:
            case SPHI:
            case RDHS_Staff:
            case MOH:
            case MOH_Staff:
            case AMOH:
            case MO:
            case RMO_AMO:
            case Guest:
                return false;

        }
        return false;
    }

    public boolean isCapableOfAddingProvinces() {
        if (developmentStage) {
            return true;
        }
        if (loggedPrivilegeType == null) {
            return false;
        }
        switch (loggedPrivilegeType) {
            case System_Administrator:
            case System_Super_User:
                return true;
            case Institution_Administrator:
            case Institution_Super_User:
            case PSPHI:
            case PDHS_Staff:
            case MO_PDHS:
            case CCP_PDHS:
            case CCP_RDHS:
            case MO_School_Health:
            case MO_RDHS:
            case DSPHI:
            case SPHI:
            case RDHS_Staff:
            case MOH:
            case MOH_Staff:
            case AMOH:
            case MO:
            case RMO_AMO:
            case Guest:
                return false;

        }
        return false;
    }

    public boolean isCapableOfManagingAnyArea() {
        if (developmentStage) {
            return true;
        }
        if (loggedPrivilegeType == null) {
            return false;
        }
        switch (loggedPrivilegeType) {
            case System_Administrator:
            case System_Super_User:
                return true;
            case Institution_Administrator:
            case Institution_Super_User:
            case PSPHI:
            case PDHS_Staff:
            case MO_PDHS:
            case CCP_PDHS:
            case CCP_RDHS:
            case MO_School_Health:
            case MO_RDHS:
            case DSPHI:
            case SPHI:
            case RDHS_Staff:
            case MOH:
            case MOH_Staff:
            case AMOH:
            case MO:
            case RMO_AMO:
            case Guest:
                return false;

        }
        return false;
    }

    public String logout() {
        makeAllLoggedVariablesNull();
        logged = false;
        return "";
    }

    public void makeAllLoggedVariablesNull() {
        myProvinces = new ArrayList<Area>();
        myDistricts = new ArrayList<Area>();
        myMohAreas = new ArrayList<Area>();
        myPhiAreas = new ArrayList<Area>();
        myEducationalZones = new ArrayList<Area>();
        loggedPhiArea = null;
        loggedMohArea = null;
        loggedRdhsArea = null;
        loggedMohOffice = null;
        loggedRdhsOffice = null;
        loggedPdhsArea = null;
        loggedUser = null;
        loggedPrivilegeType = null;
        logged = false;
    }

    public WebUserController() {
    }

    public AreaController getAreaController() {
        return areaController;
    }

    public void setAreaController(AreaController areaController) {
        this.areaController = areaController;
    }

    public List<Area> getMyProvinces() {
        return myProvinces;
    }

    public void setMyProvinces(List<Area> myProvinces) {
        this.myProvinces = myProvinces;
    }

    public List<Area> getMyDistricts() {
        return myDistricts;
    }

    public void setMyDistricts(List<Area> myDistricts) {
        this.myDistricts = myDistricts;
    }

    public List<Area> getMyMohAreas() {
        return myMohAreas;
    }

    public void setMyMohAreas(List<Area> myMohAreas) {
        this.myMohAreas = myMohAreas;
    }

    public List<Area> getMyPhiAreas() {
        return myPhiAreas;
    }

    public void setMyPhiAreas(List<Area> myPhiAreas) {
        this.myPhiAreas = myPhiAreas;
    }

    public WebUser getSelected() {
        return selected;
    }

    public void setSelected(WebUser selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private WebUserFacade getFacade() {
        return ejbFacade;
    }

    public List<Area> getMyEducationalZones() {
        return myEducationalZones;
    }

    public void setMyEducationalZones(List<Area> myEducationalZones) {
        this.myEducationalZones = myEducationalZones;
    }

    public WebUser prepareCreate() {
        selected = new WebUser();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("WebUserCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/Bundle").getString("WebUserUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("WebUserDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<WebUser> getItems() {
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

    public List<Institution> getMySchools() {
        return mySchools;
    }

    public void setMySchools(List<Institution> mySchools) {
        this.mySchools = mySchools;
    }

    public List<WebUser> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<WebUser> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public Area getLoggedPhiArea() {
        return loggedPhiArea;
    }

    public void setLoggedPhiArea(Area loggedPhiArea) {
        this.loggedPhiArea = loggedPhiArea;
    }

    public Area getLoggedMohArea() {
        return loggedMohArea;
    }

    public void setLoggedMohArea(Area loggedMohArea) {
        this.loggedMohArea = loggedMohArea;
    }

    public Area getLoggedRdhsArea() {
        return loggedRdhsArea;
    }

    public void setLoggedRdhsArea(Area loggedRdhsArea) {
        this.loggedRdhsArea = loggedRdhsArea;
    }

    public Area getLoggedPdhsArea() {
        return loggedPdhsArea;
    }

    public void setLoggedPdhsArea(Area loggedPdhsArea) {
        this.loggedPdhsArea = loggedPdhsArea;
    }

    public Institution getLoggedMohOffice() {
        return loggedMohOffice;
    }

    public void setLoggedMohOffice(Institution loggedMohOffice) {
        this.loggedMohOffice = loggedMohOffice;
    }

    public Institution getLoggedRdhsOffice() {
        return loggedRdhsOffice;
    }

    public void setLoggedRdhsOffice(Institution loggedRdhsOffice) {
        this.loggedRdhsOffice = loggedRdhsOffice;
    }

    public WebUser getLoggedUser() {
        return loggedUser;
    }

    public void setLoggedUser(WebUser loggedUser) {
        this.loggedUser = loggedUser;
    }

    public PrivilegeType getLoggedPrivilegeType() {
        return loggedPrivilegeType;
    }

    public void setLoggedPrivilegeType(PrivilegeType loggedPrivilegeType) {
        this.loggedPrivilegeType = loggedPrivilegeType;
    }

    public boolean isLogged() {
        if (developmentStage) {
            System.out.println("development Stage ");
            return true;
        }
//        System.out.println("logged = " + logged);
        return logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    public boolean isDevelopmentStage() {
        return developmentStage;
    }

    public void setDevelopmentStage(boolean developmentStage) {
        this.developmentStage = developmentStage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public Area getPhiArea() {
        return phiArea;
    }

    public void setPhiArea(Area phiArea) {
        this.phiArea = phiArea;
    }

    public Area getMohArea() {
        return mohArea;
    }

    public void setMohArea(Area mohArea) {
        this.mohArea = mohArea;
    }

    public Area getRdhsArea() {
        return rdhsArea;
    }

    public void setRdhsArea(Area rdhsArea) {
        this.rdhsArea = rdhsArea;
    }

    public Institution getMohOffice() {
        return mohOffice;
    }

    public void setMohOffice(Institution mohOffice) {
        this.mohOffice = mohOffice;
    }

    public Institution getRdhsOffice() {
        return rdhsOffice;
    }

    public void setRdhsOffice(Institution rdhsOffice) {
        this.rdhsOffice = rdhsOffice;
    }

    public List<Area> getMyAreas() {
        return myAreas;
    }

    public void setMyAreas(List<Area> myAreas) {
        this.myAreas = myAreas;
    }

    public Date getFirstDayOfQuarter() {
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH);
        Quarter q = null;
        switch (month) {
            case Calendar.JANUARY:
            case Calendar.FEBRUARY:
            case Calendar.MARCH:
                q = Quarter.First;
                break;
            case Calendar.APRIL:
            case Calendar.MAY:
            case Calendar.JUNE:
                q = Quarter.Second;
                break;
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.SEPTEMBER:
                q = Quarter.Thired;
                break;
            case Calendar.OCTOBER:
            case Calendar.NOVEMBER:
            case Calendar.DECEMBER:
                q = Quarter.Forth;
        }
        return getFirstDayOfQuarter(q);
    }

    public Date getFirstDayOfQuarter(Quarter quarter) {
        Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR);
        return getFirstDayOfQuarter(y, quarter);
    }

    public Date getFirstDayOfQuarter(int year, Quarter quarter) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        switch (quarter) {
            case First:
                c.set(Calendar.MONTH, Calendar.JANUARY);
                break;
            case Second:
                c.set(Calendar.MONTH, Calendar.APRIL);
                break;
            case Thired:
                c.set(Calendar.MONTH, Calendar.JULY);
                break;
            case Forth:
                c.set(Calendar.MONTH, Calendar.OCTOBER);
                break;
        }
        return getFirstDayOfMonth(c.getTime());
    }

    public Date getLastDayOfQuarter() {
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH);
        Quarter q = null;
        switch (month) {
            case Calendar.JANUARY:
            case Calendar.FEBRUARY:
            case Calendar.MARCH:
                q = Quarter.First;
                break;
            case Calendar.APRIL:
            case Calendar.MAY:
            case Calendar.JUNE:
                q = Quarter.Second;
                break;
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.SEPTEMBER:
                q = Quarter.Thired;
                break;
            case Calendar.OCTOBER:
            case Calendar.NOVEMBER:
            case Calendar.DECEMBER:
                q = Quarter.Forth;
        }
        return getLastDayOfQuarter(q);
    }

    public Date getLastDayOfQuarter(Quarter quarter) {
        Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR);
        return getLastDayOfQuarter(y, quarter);
    }

    public Date getLastDayOfQuarter(int year, Quarter quarter) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        switch (quarter) {
            case First:
                c.set(Calendar.MONTH, Calendar.MARCH);
                break;
            case Second:
                c.set(Calendar.MONTH, Calendar.JUNE);
                break;
            case Thired:
                c.set(Calendar.MONTH, Calendar.SEPTEMBER);
                break;
            case Forth:
                c.set(Calendar.MONTH, Calendar.DECEMBER);
                break;
        }
        return getLastDayOfMonth(c.getTime());
    }

    public Date getFirstDayOfYear() {
        return getFirstDayOfYear(new Date());
    }

    public Date getFirstDayOfYear(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.MONTH, Calendar.JANUARY);
        return getFirstDayOfMonth(c.getTime());
    }

    public Date getLastDayOfYear() {
        return getLastDayOfYear(new Date());
    }

    public Date getLastDayOfYear(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.MONTH, Calendar.DECEMBER);
        return getLastDayOfMonth(c.getTime());
    }

    public Date getLastDayOfYear(int year) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, Calendar.DECEMBER);
        return getLastDayOfMonth(c.getTime());
    }

    public Date getFirstDayOfMonth() {
        return getFirstDayOfMonth(new Date());
    }

    public Date getFirstDayOfMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
        c.set(Calendar.HOUR_OF_DAY, c.getActualMinimum(Calendar.HOUR_OF_DAY));
        c.set(Calendar.MINUTE, c.getActualMinimum(Calendar.MINUTE));
        c.set(Calendar.SECOND, c.getActualMinimum(Calendar.SECOND));
        return c.getTime();
    }

    public Date getLastDayOfMonth() {
        return getLastDayOfMonth(new Date());
    }

    public Date getLastDayOfMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        c.set(Calendar.HOUR_OF_DAY, c.getActualMaximum(Calendar.HOUR_OF_DAY));
        c.set(Calendar.MINUTE, c.getActualMaximum(Calendar.MINUTE));
        c.set(Calendar.SECOND, c.getActualMaximum(Calendar.SECOND));
        return c.getTime();
    }

    public Date getLastDayOfMonth(int year, Month month) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, getIntMonth(month));
        return getLastDayOfMonth(c.getTime());
    }

    public Date getFirstDayOfMonth(int year, Month month) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, getIntMonth(month));
        return getFirstDayOfMonth(c.getTime());
    }

    public Month getLastMonth() {
        return getLastMonth(new Date());
    }

    public Month getLastMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, -1);
        int month = c.get(Calendar.MONTH);
        return getMonthFromInt(month);
    }

    public List<Integer> getRecentYears() {
        List<Integer> ys = new ArrayList<Integer>();
        ys.add(getThisYear());
        ys.add(getThisYear() - 1);
        ys.add(getThisYear() - 2);
        ys.add(getThisYear() - 3);
        return ys;
    }

    public int getThisYear() {
        return getThisYear(new Date());
    }

    public int getThisYear(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int year = c.get(Calendar.YEAR);
        return year;
    }

    public int getLastYear() {
        return getLastYear(new Date());
    }

    public int getLastYear(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.YEAR, -1);
        int year = c.get(Calendar.YEAR);
        return year;
    }

    public Quarter getLastQuarterFromDate(Date date) {
        Quarter q = getQuarterFromDate(date);
        switch (q) {
            case First:
                return Quarter.Forth;
            case Second:
                return Quarter.First;
            case Thired:
                return Quarter.Second;
            case Forth:
                return Quarter.Thired;
        }
        return null;
    }

    public Quarter getQuarterFromDate(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, -1);
        int month = c.get(Calendar.MONTH);
        switch (month) {
            case Calendar.JANUARY:
            case Calendar.FEBRUARY:
            case Calendar.MARCH:
                return Quarter.First;
            case Calendar.APRIL:
            case Calendar.MAY:
            case Calendar.JUNE:
                return Quarter.Second;
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.SEPTEMBER:
                return Quarter.Thired;
            case Calendar.OCTOBER:
            case Calendar.NOVEMBER:
            case Calendar.DECEMBER:
                return Quarter.Forth;

        }
        return null;
    }

    public Integer getIntMonth(Month month) {
        switch (month) {
            case January:
                return Calendar.JANUARY;
            case February:
                return Calendar.FEBRUARY;
            case March:
                return Calendar.MARCH;
            case April:
                return Calendar.APRIL;
            case May:
                return Calendar.MAY;
            case June:
                return Calendar.JUNE;
            case July:
                return Calendar.JULY;
            case August:
                return Calendar.AUGUST;
            case September:
                return Calendar.SEPTEMBER;
            case October:
                return Calendar.OCTOBER;
            case November:
                return Calendar.NOVEMBER;
            case December:
                return Calendar.DECEMBER;
        }
        return null;
    }

    public Month getMonthFromInt(int month) {
        switch (month) {
            case Calendar.JANUARY:
                return Month.January;
            case Calendar.FEBRUARY:
                return Month.February;
            case Calendar.MARCH:
                return Month.March;
            case Calendar.APRIL:
                return Month.April;
            case Calendar.MAY:
                return Month.May;
            case Calendar.JUNE:
                return Month.June;
            case Calendar.JULY:
                return Month.July;
            case Calendar.AUGUST:
                return Month.August;
            case Calendar.SEPTEMBER:
                return Month.September;
            case Calendar.OCTOBER:
                return Month.October;
            case Calendar.NOVEMBER:
                return Month.November;
            case Calendar.DECEMBER:
                return Month.December;
        }
        return null;
    }

    @FacesConverter(forClass = WebUser.class)
    public static class WebUserControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            WebUserController controller = (WebUserController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "webUserController");
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
            if (object instanceof WebUser) {
                WebUser o = (WebUser) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), WebUser.class.getName()});
                return null;
            }
        }

    }

}
