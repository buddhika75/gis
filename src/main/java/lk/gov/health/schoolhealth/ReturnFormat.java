/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.gov.health.schoolhealth;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 *
 * @author User
 */
@Entity
public class ReturnFormat implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    String name;
    String code;
    @Enumerated(EnumType.STRING)
    ReturnTimeFrequency frequency;
    @ManyToOne
    Item category;

    @Deprecated
    Integer senderStartDays;

    Integer sendingDeadlineMonths;
    Integer sendingDeadlineDays;

    AreaType sendingAreaType;

    PrivilegeType prepairedBy;
    PrivilegeType sentBy;

    PrivilegeType receivedBy;
    Integer receivingDeadlineMonths;
    Integer receivingDeadlineDays;

    @Deprecated
    Integer receiveDeadline;

    AreaType receivingAreaType;

    @Transient
    boolean needYear;

    @Transient
    boolean needQuarter;

    @Transient
    boolean needMonth;

    @Transient
    boolean needWeek;

    @Transient
    boolean needFromDate;

    @Transient
    boolean needToDate;

    public boolean isNeedYear() {
        if (frequency == ReturnTimeFrequency.Annual || frequency == ReturnTimeFrequency.Monthly || frequency == ReturnTimeFrequency.Quarterly) {
            needYear = true;
        } else {
            needYear = false;
        }
        return needYear;
    }

    public boolean isNeedQuarter() {
        if (frequency == ReturnTimeFrequency.Quarterly) {
            needQuarter = true;
        } else {
            needQuarter = false;
        }

        return needQuarter;
    }

    public boolean isNeedMonth() {
        if (frequency == ReturnTimeFrequency.Monthly) {
            needMonth = true;
        } else {
            needMonth = false;
        }

        return needMonth;
    }

    public boolean isNeedWeek() {
        if (frequency == ReturnTimeFrequency.Weekely) {
            needWeek = true;
        } else {
            needWeek = false;
        }
        return needWeek;
    }

    public boolean isNeedFromDate() {
        if (frequency == ReturnTimeFrequency.Weekely) {
            needFromDate = true;
        } else {
            needFromDate = false;
        }

        return needFromDate;
    }

    public boolean isNeedToDate() {
        if (frequency == ReturnTimeFrequency.Weekely) {
            needToDate = true;
        } else {
            needToDate = false;
        }

        return needToDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ReturnTimeFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(ReturnTimeFrequency frequency) {
        this.frequency = frequency;
    }

    public Item getCategory() {
        return category;
    }

    public void setCategory(Item category) {
        this.category = category;
    }

    public Integer getSenderStartDays() {
        return senderStartDays;
    }

    public void setSenderStartDays(Integer senderStartDays) {
        this.senderStartDays = senderStartDays;
    }

    public Integer getSendingDeadlineMonths() {
        return sendingDeadlineMonths;
    }

    public void setSendingDeadlineMonths(Integer sendingDeadlineMonths) {
        this.sendingDeadlineMonths = sendingDeadlineMonths;
    }

    public Integer getSendingDeadlineDays() {
        return sendingDeadlineDays;
    }

    public void setSendingDeadlineDays(Integer sendingDeadlineDays) {
        this.sendingDeadlineDays = sendingDeadlineDays;
    }

    public PrivilegeType getPrepairedBy() {
        return prepairedBy;
    }

    public void setPrepairedBy(PrivilegeType prepairedBy) {
        this.prepairedBy = prepairedBy;
    }

    public PrivilegeType getSentBy() {
        return sentBy;
    }

    public void setSentBy(PrivilegeType sentBy) {
        this.sentBy = sentBy;
    }

    public PrivilegeType getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(PrivilegeType receivedBy) {
        this.receivedBy = receivedBy;
    }

    public Integer getReceivingDeadlineMonths() {
        return receivingDeadlineMonths;
    }

    public void setReceivingDeadlineMonths(Integer receivingDeadlineMonths) {
        this.receivingDeadlineMonths = receivingDeadlineMonths;
    }

    public Integer getReceivingDeadlineDays() {
        return receivingDeadlineDays;
    }

    public void setReceivingDeadlineDays(Integer receivingDeadlineDays) {
        this.receivingDeadlineDays = receivingDeadlineDays;
    }

    public Integer getReceiveDeadline() {
        return receiveDeadline;
    }

    public void setReceiveDeadline(Integer receiveDeadline) {
        this.receiveDeadline = receiveDeadline;
    }

    public AreaType getSendingAreaType() {
        return sendingAreaType;
    }

    public void setSendingAreaType(AreaType sendingAreaType) {
        this.sendingAreaType = sendingAreaType;
    }

    public AreaType getReceivingAreaType() {
        return receivingAreaType;
    }

    public void setReceivingAreaType(AreaType receivingAreaType) {
        this.receivingAreaType = receivingAreaType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        if (!(object instanceof ReturnFormat)) {
            return false;
        }
        ReturnFormat other = (ReturnFormat) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "lk.gov.health.schoolhealth.ReturnType[ id=" + id + " ]";
    }

}
