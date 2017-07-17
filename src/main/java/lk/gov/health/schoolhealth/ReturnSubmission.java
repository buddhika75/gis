/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.gov.health.schoolhealth;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.Transient;

/**
 *
 * @author User
 */
@Entity
public class ReturnSubmission implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    ReturnFormat returnFormat;
    @Enumerated(EnumType.STRING)
    Quarter quarter;
    @Enumerated(EnumType.STRING)
    Month returnMonth;
    int returnYear;
    @Temporal(javax.persistence.TemporalType.DATE)
    Date fromDate;
    @Temporal(javax.persistence.TemporalType.DATE)
    Date toDate;

    @Temporal(javax.persistence.TemporalType.DATE)
    Date sentDate;
    @ManyToOne
    Area sentArea;

    @Lob
    String sentComments;

    @ManyToOne
    WebUser prepairedBy;
    @ManyToOne
    WebUser sentBy;
    @ManyToOne
    WebUser receivedBy;
    @Temporal(javax.persistence.TemporalType.DATE)
    Date receiveDate;
    @ManyToOne
    Area receiveArea;

    @Lob
    String receiveComments;

    @Enumerated(EnumType.STRING)
    ReturnReceiveCategory returnReceiveCategory;

    @Transient
    String foreGroundColor;

    @Transient
    String backGroundColor;

    /**
     * 
     *  "https://isabelcastillo.com/error-info-messages-css"
     * 
     */
    
    public String getForeGroundColor() {
        switch (returnReceiveCategory) {
            case Not_Received:
                foreGroundColor = "#D8000C"; break;
            case Received_late:
                foreGroundColor = "#9F6000"; break;
            case Received_on_time:
                foreGroundColor = "#4F8A10"; break;
            case Sent_yet_to_receive:
                foreGroundColor = "#00529B"; break;
        }
        return foreGroundColor;
    }

    public String getBackGroundColor() {
        switch (returnReceiveCategory) {
            case Not_Received:
                backGroundColor = "#FFBABA"; break;
            case Received_late:
                backGroundColor = "#FEEFB3"; break;
            case Received_on_time:
                backGroundColor = "#DFF2BF"; break;
            case Sent_yet_to_receive:
                backGroundColor = "#BDE5F8"; break;
        }
        return backGroundColor;
    }

    public ReturnReceiveCategory getReturnReceiveCategory() {
        return returnReceiveCategory;
    }

    public void setReturnReceiveCategory(ReturnReceiveCategory returnReceiveCategory) {
        this.returnReceiveCategory = returnReceiveCategory;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReturnFormat getReturnFormat() {
        return returnFormat;
    }

    public void setReturnFormat(ReturnFormat returnFormat) {
        this.returnFormat = returnFormat;
    }

    public Quarter getQuarter() {
        return quarter;
    }

    public void setQuarter(Quarter quarter) {
        this.quarter = quarter;
    }

    public Month getReturnMonth() {
        return returnMonth;
    }

    public void setReturnMonth(Month returnMonth) {
        this.returnMonth = returnMonth;
    }

    public int getReturnYear() {
        return returnYear;
    }

    public void setReturnYear(int returnYear) {
        this.returnYear = returnYear;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public Area getSentArea() {
        return sentArea;
    }

    public void setSentArea(Area sentArea) {
        this.sentArea = sentArea;
    }

    public WebUser getPrepairedBy() {
        return prepairedBy;
    }

    public void setPrepairedBy(WebUser prepairedBy) {
        this.prepairedBy = prepairedBy;
    }

    public WebUser getSentBy() {
        return sentBy;
    }

    public void setSentBy(WebUser sentBy) {
        this.sentBy = sentBy;
    }

    public WebUser getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(WebUser receivedBy) {
        this.receivedBy = receivedBy;
    }

    public Date getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(Date receiveDate) {
        this.receiveDate = receiveDate;
    }

    public Area getReceiveArea() {
        return receiveArea;
    }

    public void setReceiveArea(Area receiveArea) {
        this.receiveArea = receiveArea;
    }

    public String getSentComments() {
        return sentComments;
    }

    public void setSentComments(String sentComments) {
        this.sentComments = sentComments;
    }

    public String getReceiveComments() {
        return receiveComments;
    }

    public void setReceiveComments(String receiveComments) {
        this.receiveComments = receiveComments;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ReturnSubmission)) {
            return false;
        }
        ReturnSubmission other = (ReturnSubmission) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "lk.gov.health.schoolhealth.ReturnSubmission[ id=" + id + " ]";
    }

}
