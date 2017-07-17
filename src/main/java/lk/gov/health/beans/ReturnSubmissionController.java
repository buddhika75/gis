package lk.gov.health.beans;

import lk.gov.health.schoolhealth.ReturnSubmission;
import lk.gov.health.beans.util.JsfUtil;
import lk.gov.health.beans.util.JsfUtil.PersistAction;
import lk.gov.health.faces.ReturnSubmissionFacade;

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
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import lk.gov.health.faces.CoordinateFacade;
import lk.gov.health.faces.ReturnFormatFacade;
import lk.gov.health.schoolhealth.Area;
import lk.gov.health.schoolhealth.AreaType;
import lk.gov.health.schoolhealth.Coordinate;
import lk.gov.health.schoolhealth.Month;
import lk.gov.health.schoolhealth.Quarter;
import lk.gov.health.schoolhealth.ReturnFormat;
import lk.gov.health.schoolhealth.ReturnFormatLastSubmission;
import lk.gov.health.schoolhealth.ReturnReceiveCategory;
import org.primefaces.event.timeline.TimelineSelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Polygon;
import org.primefaces.model.timeline.TimelineEvent;
import org.primefaces.model.timeline.TimelineModel;

@Named("returnSubmissionController")
@SessionScoped
public class ReturnSubmissionController implements Serializable {

    @Inject
    WebUserController webUserController;
    @EJB
    private lk.gov.health.faces.ReturnSubmissionFacade ejbFacade;
    @EJB
    ReturnFormatFacade returnFormatFacade;
    @EJB
    CoordinateFacade coordinateFacade;
    private List<ReturnSubmission> items = null;
    private List<ReturnSubmission> genItems = null;
    private ReturnSubmission selected;

    ReturnFormat returnFormat;
    int year;
    Month month;
    Quarter quarter;
    Date deadlineDate;
    private Date fromDate;
    private Date toDate;
    List<ReturnFormatLastSubmission> returnFormatLastSubmissions;

    List<Area> mySendingAreas;

    private TimelineModel model;
    private MapModel polygonModel;

    public String listReturnFormatLastSubmissions() {
        returnFormatLastSubmissions = new ArrayList<ReturnFormatLastSubmission>();
        String j;
        Map m = new HashMap();
        j = "select f from ReturnFormat f "
                + " where f.sentBy = :p "
                + " order by f.name";
        m.put("p", webUserController.getLoggedPrivilegeType());
        ReturnFormat f = new ReturnFormat();
        List<ReturnFormat> rfs = returnFormatFacade.findBySQL(j, m);
        for (ReturnFormat rf : rfs) {
            ReturnFormatLastSubmission rfls = new ReturnFormatLastSubmission();
            rfls.setReturnFormat(rf);
            m = new HashMap();
            j = "select r from ReturnSubmission r "
                    + " where r.sentArea=:sa "
                    + " and r.returnFormat=:rf "
                    + " order by r.id desc";
            m.put("rf", rf);
            m.put("sa", webUserController.getLoggedMohArea());
            ReturnSubmission rs = getFacade().findFirstBySQL(j, m);
            rfls.setReturnSubmission(rs);
            returnFormatLastSubmissions.add(rfls);
        }
        return "/returnSubmission/format_data";
    }

    public String toReceiveReturns() {
        items = new ArrayList<ReturnSubmission>();
        return "/returnSubmission/receive_returns";
    }

    public String toCheckSubmissionStatus() {
        items = new ArrayList<ReturnSubmission>();
        return "/returnSubmission/check_submission_status";
    }

    public String listMySubmissions() {
        String j;
        Map m = new HashMap();
        j = "select r from ReturnSubmission r "
                + " where r.sentArea=:sa "
                + " and r.sentDate between :fd and :td";
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("sa", webUserController.getLoggedMohArea());
        items = getFacade().findBySQL(j, m);
        return "";
    }

    public String listPendingRetunrsToReceive() {
        if (returnFormat == null) {
            JsfUtil.addErrorMessage("Select a return format");
            return "";
        }
        String j;
        Map m = new HashMap();
        j = "select r from ReturnSubmission r "
                + " where r.returnFormat=:rf "
                + " and r.receiveArea=:ra "
                + " and r.receiveDate is null";
        m.put("rf", returnFormat);
        m.put("ra", webUserController.getLoggedRdhsArea());
        items = getFacade().findBySQL(j, m);
        return "";
    }

    public String listReceivedReturns() {
        if (returnFormat == null) {
            JsfUtil.addErrorMessage("Select a return format");
            return "";
        }
        String j;
        Map m = new HashMap();
        j = "select r from ReturnSubmission r "
                + " where r.returnFormat=:rf "
                + " and r.receiveArea=:ra "
                + " and r.receiveDate is not null ";

        if (returnFormat.isNeedYear()) {
            j += " and r.returnYear=:ry ";
            m.put("ry", year);
        }

        if (returnFormat.isNeedQuarter()) {
            j += " and r.quarter=:rq ";
            m.put("rq", quarter);
        }

        if (returnFormat.isNeedMonth()) {
            j += " and r.returnMonth=:rm ";
            m.put("rm", month);
        }

        j += " order by r.receiveDate";
        m.put("rf", returnFormat);
        m.put("ra", webUserController.getLoggedRdhsArea());
        items = getFacade().findBySQL(j, m);
        fillMySendingAreas();
        markAreasForReceiving();
        markTimelineForReceiving();
        markMapForReceiving();
        return "";
    }

    private void fillMySendingAreas() {
        mySendingAreas = webUserController.getMyMohAreas();
    }

    private void markAreasForReceiving() {
        genItems = new ArrayList<ReturnSubmission>();
        for (Area a : mySendingAreas) {
            String j;
            Map m = new HashMap();
            j = "select r from ReturnSubmission r "
                    + " where r.returnFormat=:rf "
                    + " and r.receiveArea=:ra "
                    + " and r.sentArea=:sa ";
            if (returnFormat.isNeedYear()) {
                j += " and r.returnYear=:ry ";
                m.put("ry", year);
            }
            if (returnFormat.isNeedQuarter()) {
                j += " and r.quarter=:rq ";
                m.put("rq", quarter);
            }
            if (returnFormat.isNeedMonth()) {
                j += " and r.returnMonth=:rm ";
                m.put("rm", month);
            }
            j += " order by r.receiveDate desc";
            m.put("sa", a);
            m.put("rf", returnFormat);
            m.put("ra", webUserController.getLoggedRdhsArea());
            ReturnSubmission rs = getFacade().findFirstBySQL(j, m);
            if (rs == null) {
                rs = new ReturnSubmission();
                rs.setSentArea(a);
                rs.setReturnReceiveCategory(ReturnReceiveCategory.Not_Received);
            } else if (rs.getReceiveDate() == null) {
                rs.setReturnReceiveCategory(ReturnReceiveCategory.Sent_yet_to_receive);
            } else if (rs.getReceiveDate().after(deadlineDate)) {
                rs.setReturnReceiveCategory(ReturnReceiveCategory.Received_late);
            } else {
                rs.setReturnReceiveCategory(ReturnReceiveCategory.Received_on_time);
            }
            genItems.add(rs);
        }
    }

    private void markTimelineForReceiving() {
        model = new TimelineModel();
        model.add(new TimelineEvent("SUBMISSION DEADLINE", deadlineDate));
        for (ReturnSubmission s : items) {
            if (s != null && s.getSentArea() != null && s.getSentArea().getName() != null && s.getReceiveDate() != null) {
                model.add(new TimelineEvent(s.getSentArea().getName(), s.getReceiveDate()));
            }
        }
    }

    private void markMapForReceiving() {
        polygonModel = new DefaultMapModel();
       
        for (Area a : mySendingAreas) {

            //Polygon
            Polygon polygon = new Polygon();

            String j = "select c from Coordinate c where c.area=:a";
            Map m = new HashMap();
            m.put("a", a);
            List<Coordinate> cs = coordinateFacade.findBySQL(j, m);
            for (Coordinate c : cs) {
                LatLng coord = new LatLng(c.getLatitude(), c.getLongitude());
                polygon.getPaths().add(coord);
            }

            polygon.setStrokeColor("#FF9900");
            

            m = new HashMap();

            j = "select r from ReturnSubmission r "
                    + " where r.returnFormat=:rf "
                    + " and r.receiveArea=:ra "
                    + " and r.sentArea=:sa ";
            if (returnFormat.isNeedYear()) {
                j += " and r.returnYear=:ry ";
                m.put("ry", year);
            }
            if (returnFormat.isNeedQuarter()) {
                j += " and r.quarter=:rq ";
                m.put("rq", quarter);
            }
            if (returnFormat.isNeedMonth()) {
                j += " and r.returnMonth=:rm ";
                m.put("rm", month);
            }
            j += " order by r.receiveDate desc";
            m.put("sa", a);
            m.put("rf", returnFormat);
            m.put("ra", webUserController.getLoggedRdhsArea());
            ReturnSubmission rs = getFacade().findFirstBySQL(j, m);
            if (rs == null) {
                polygon.setFillColor("#D8000C");
            } else if (rs.getReceiveDate() == null) {
                 polygon.setFillColor("#D8000C");
            } else if (rs.getReceiveDate().after(deadlineDate)) {
                 polygon.setFillColor("#9F6000");
            } else {
                 polygon.setFillColor("#4F8A10");
            }
            
            polygon.setStrokeOpacity(1);
            polygon.setFillOpacity(0.9);
            polygon.setData(a.getName());
            polygonModel.addOverlay(polygon);
           
        }
    }

    public String markAsReceived() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select a return");
            return "";
        }
        selected.setReceiveArea(webUserController.getLoggedRdhsArea());
        selected.setReceiveDate(new Date());
        selected.setReceivedBy(webUserController.getLoggedUser());
        getFacade().edit(selected);
        listPendingRetunrsToReceive();
        JsfUtil.addSuccessMessage("Received");
        return "";
    }

    public String toSubmitNewReport() {
        selected = new ReturnSubmission();
        return "/returnSubmission/submit_new_report";
    }

    public String toSubmitNewReportFull() {
        selected = new ReturnSubmission();
        return "/returnSubmission/submit_new_report_full";
    }

    public String toCheckReturns() {
        items = new ArrayList<ReturnSubmission>();
        return "/returnSubmission/check_returns";
    }
    
    public String toCheckReturnsAll() {
        items = new ArrayList<ReturnSubmission>();
        return "/returnSubmission/check_returns_all";
    }

    public String toSubmitNewReportAfterSelectionAsAdmin() {
        if (returnFormat == null) {
            JsfUtil.addErrorMessage("Select a Return");
            return "";
        }
        selected.setReturnFormat(returnFormat);
        selected.setSentDate(new Date());

        if (null == selected.getReturnFormat().getFrequency()) {
            JsfUtil.addErrorMessage("Still Under Development");
            return "";

        } else {
            switch (returnFormat.getFrequency()) {
                case Annual:
                    selected.setReturnYear(webUserController.getLastYear());
                    break;
                case Quarterly:
                    selected.setQuarter(webUserController.getLastQuarterFromDate(new Date()));
                    if (selected.getQuarter() == Quarter.Forth) {
                        selected.setReturnYear(webUserController.getLastYear());
                    } else {
                        selected.setReturnYear(webUserController.getThisYear());
                    }
                    break;
                case Monthly:
                    selected.setReturnMonth(webUserController.getLastMonth(new Date()));
                    if (selected.getReturnMonth() == Month.December) {
                        selected.setReturnYear(webUserController.getLastYear());
                    } else {
                        selected.setReturnYear(webUserController.getThisYear());
                    }
                    break;
                case Weekely:
                    JsfUtil.addErrorMessage("Still Under Development");
                    return "";
                default:
                    JsfUtil.addErrorMessage("Still Under Development");
                    return "";

            }
        }
        return "/returnSubmission/submit_new_report_selected_full";
    }

    public String toSubmitNewReportAfterSelection() {
        if (returnFormat == null) {
            JsfUtil.addErrorMessage("Select a Return");
            return "";
        }
        selected.setReturnFormat(returnFormat);
        selected.setSentDate(new Date());

        if (selected.getReturnFormat().getSendingAreaType() == null || selected.getReturnFormat().getSendingAreaType() == AreaType.MOH) {
            selected.setSentArea(webUserController.getLoggedMohArea());
        }
        if (selected.getReturnFormat().getReceivingAreaType() == null || selected.getReturnFormat().getReceivingAreaType() == AreaType.District) {
            selected.setReceiveArea(webUserController.getLoggedRdhsArea());
        }

        selected.setReceiveArea(webUserController.getLoggedRdhsArea());

        if (null == selected.getReturnFormat().getFrequency()) {
            JsfUtil.addErrorMessage("Still Under Development");
            return "";

        } else {
            switch (returnFormat.getFrequency()) {
                case Annual:
                    selected.setReturnYear(webUserController.getLastYear());
                    break;
                case Quarterly:
                    selected.setQuarter(webUserController.getLastQuarterFromDate(new Date()));
                    if (selected.getQuarter() == Quarter.Forth) {
                        selected.setReturnYear(webUserController.getLastYear());
                    } else {
                        selected.setReturnYear(webUserController.getThisYear());
                    }
                    break;
                case Monthly:
                    selected.setReturnMonth(webUserController.getLastMonth(new Date()));
                    if (selected.getReturnMonth() == Month.December) {
                        selected.setReturnYear(webUserController.getLastYear());
                    } else {
                        selected.setReturnYear(webUserController.getThisYear());
                    }
                    break;
                case Weekely:
                    JsfUtil.addErrorMessage("Still Under Development");
                    return "";
                default:
                    JsfUtil.addErrorMessage("Still Under Development");
                    return "";

            }
        }
        return "/returnSubmission/submit_new_report_selected";
    }

    public void calculateDeadline() {
        Date lastDayOfPeriod = new Date();
        switch (returnFormat.getFrequency()) {
            case Annual:
                year = webUserController.getLastYear();
                lastDayOfPeriod = webUserController.getLastDayOfYear(year);
                break;
            case Quarterly:
                quarter = webUserController.getLastQuarterFromDate(new Date());
                if (quarter == Quarter.Forth) {
                    year = webUserController.getLastYear();
                } else {
                    year = webUserController.getThisYear();
                }
                lastDayOfPeriod = webUserController.getLastDayOfQuarter(year, quarter);
                break;
            case Monthly:
                month = webUserController.getLastMonth(new Date());
                if (month == Month.December) {
                    year = webUserController.getLastYear();
                } else {
                    year = webUserController.getThisYear();
                }
                lastDayOfPeriod = webUserController.getLastDayOfMonth(year, month);
                break;
            case Weekely:
                JsfUtil.addErrorMessage("Still Under Development");
                return;
            default:
                JsfUtil.addErrorMessage("Still Under Development");
                return;

        }
        Calendar c = Calendar.getInstance();
        c.setTime(lastDayOfPeriod);
        if (returnFormat.getReceivingDeadlineMonths() != null) {
            c.add(Calendar.MONTH, returnFormat.getReceivingDeadlineMonths());
        }
        if (returnFormat.getReceivingDeadlineDays() != null) {
            c.add(Calendar.DATE, returnFormat.getReceivingDeadlineDays());
        }
        deadlineDate = c.getTime();
    }

    public String toCheckReturnAfterSelection() {
        if (returnFormat == null) {
            JsfUtil.addErrorMessage("Select a Return");
            return "";
        }

        if (null == returnFormat.getFrequency()) {
            JsfUtil.addErrorMessage("Still Under Development");
            return "";
        } else {
            switch (returnFormat.getFrequency()) {
                case Annual:
                    year = webUserController.getLastYear();
                    break;
                case Quarterly:
                    quarter = webUserController.getLastQuarterFromDate(new Date());
                    if (quarter == Quarter.Forth) {
                        year = webUserController.getLastYear();
                    } else {
                        year = webUserController.getThisYear();
                    }
                    break;
                case Monthly:
                    month = webUserController.getLastMonth(new Date());
                    if (month == Month.December) {
                        year = webUserController.getLastYear();
                    } else {
                        year = webUserController.getThisYear();
                    }
                    break;
                case Weekely:
                    JsfUtil.addErrorMessage("Still Under Development");
                    return "";
                default:
                    JsfUtil.addErrorMessage("Still Under Development");
                    return "";

            }
        }
        calculateDeadline();
        return "/returnSubmission/check_returns_selected";
    }

    public String submitReturn() {

        if (selected.getId() == null) {
            selected.setSentBy(webUserController.getLoggedUser());
            getFacade().create(selected);
            JsfUtil.addSuccessMessage("Submitted");
        } else {
            getFacade().edit(selected);
            JsfUtil.addSuccessMessage("Submission Updated");
        }
        return "/index";
    }

    public String submitReturnAsAdmin() {
        if (selected.getId() == null) {
            getFacade().create(selected);
            JsfUtil.addSuccessMessage("Created");
        } else {
            getFacade().edit(selected);
            JsfUtil.addSuccessMessage("Updated");
        }
        return "/index";
    }

    public String makeReceiveDateFromSentDate() {
        List<ReturnSubmission> ss = getFacade().findAll();
        for (ReturnSubmission s : ss) {
            s.setReceiveDate(s.getSentDate());
            getFacade().edit(s);
        }
        return "/index";
    }

    public ReturnSubmissionController() {
    }

    public ReturnSubmission getSelected() {
        return selected;
    }

    public void setSelected(ReturnSubmission selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private ReturnSubmissionFacade getFacade() {
        return ejbFacade;
    }

    public ReturnSubmission prepareCreate() {
        selected = new ReturnSubmission();
        initializeEmbeddableKey();
        return selected;
    }

    public String deleteSubmission() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to delete");
            return "";
        }
        getFacade().remove(selected);
        listReceivedReturns();
        JsfUtil.addSuccessMessage("Deleted");
        return "";
    }

    public String editSubmission() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to delete");
            return "";
        }
        return "/returnSubmission/submit_new_report_selected_full";
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleReturns").getString("ReturnSubmissionCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleReturns").getString("ReturnSubmissionUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleReturns").getString("ReturnSubmissionDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<ReturnSubmission> getItems() {
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
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleReturns").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleReturns").getString("PersistenceErrorOccured"));
            }
        }
    }

    public ReturnSubmission getReturnSubmission(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<ReturnSubmission> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<ReturnSubmission> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public ReturnFormat getReturnFormat() {
        return returnFormat;
    }

    public void setReturnFormat(ReturnFormat returnFormat) {
        this.returnFormat = returnFormat;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public Quarter getQuarter() {
        return quarter;
    }

    public void setQuarter(Quarter quarter) {
        this.quarter = quarter;
    }

    public Date getDeadlineDate() {
        if (deadlineDate == null) {
            deadlineDate = new Date();
        }
        return deadlineDate;
    }

    public void setDeadlineDate(Date deadlineDate) {
        this.deadlineDate = deadlineDate;
    }

    public List<ReturnSubmission> getGenItems() {
        return genItems;
    }

    public void setGenItems(List<ReturnSubmission> genItems) {
        this.genItems = genItems;
    }

    public List<Area> getMySendingAreas() {
        return mySendingAreas;
    }

    public void setMySendingAreas(List<Area> mySendingAreas) {
        this.mySendingAreas = mySendingAreas;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public TimelineModel getModel() {
        return model;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = webUserController.getFirstDayOfQuarter();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = webUserController.getLastDayOfQuarter();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public List<ReturnFormatLastSubmission> getReturnFormatLastSubmissions() {
        return returnFormatLastSubmissions;
    }

    public void setReturnFormatLastSubmissions(List<ReturnFormatLastSubmission> returnFormatLastSubmissions) {
        this.returnFormatLastSubmissions = returnFormatLastSubmissions;
    }

    public MapModel getPolygonModel() {
        return polygonModel;
    }

    public void setPolygonModel(MapModel polygonModel) {
        this.polygonModel = polygonModel;
    }

    @FacesConverter(forClass = ReturnSubmission.class)
    public static class ReturnSubmissionControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ReturnSubmissionController controller = (ReturnSubmissionController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "returnSubmissionController");
            return controller.getReturnSubmission(getKey(value));
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
            if (object instanceof ReturnSubmission) {
                ReturnSubmission o = (ReturnSubmission) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), ReturnSubmission.class.getName()});
                return null;
            }
        }

    }

}
