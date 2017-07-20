/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.gov.health.dengue;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;

/**
 *
 * @author User
 */
@Entity
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    Long pid;
    
    private String serialNo;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date SendDate;
    @ManyToOne
    private Institution hospital;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date addmitedDate;
    private String bht;
    private String patientsName;
    private int age;
    @Enumerated(EnumType.STRING)
    private Sex gender;
    @ManyToOne
    Area district;
    @ManyToOne
    Area moh;
    @ManyToOne
    private Area phiArea;
    @ManyToOne
    private Area gnDivision;
    private String address;
    private String tel;
    private boolean fooging;

    
    

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
        if (!(object instanceof Notification)) {
            return false;
        }
        Notification other = (Notification) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "lk.gov.health.dengue.Notification[ id=" + id + " ]";
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public Date getSendDate() {
        return SendDate;
    }

    public void setSendDate(Date SendDate) {
        this.SendDate = SendDate;
    }

    public Institution getHospital() {
        return hospital;
    }

    public void setHospital(Institution hospital) {
        this.hospital = hospital;
    }

    public Date getAddmitedDate() {
        return addmitedDate;
    }

    public void setAddmitedDate(Date addmitedDate) {
        this.addmitedDate = addmitedDate;
    }

    public String getBht() {
        return bht;
    }

    public void setBht(String bht) {
        this.bht = bht;
    }

    public String getPatientsName() {
        return patientsName;
    }

    public void setPatientsName(String patientsName) {
        this.patientsName = patientsName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Sex getGender() {
        return gender;
    }

    public void setGender(Sex gender) {
        this.gender = gender;
    }

    public Area getPhiArea() {
        return phiArea;
    }

    public void setPhiArea(Area phiArea) {
        this.phiArea = phiArea;
    }

    public Area getGnDivision() {
        return gnDivision;
    }

    public void setGnDivision(Area gnDivision) {
        this.gnDivision = gnDivision;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public boolean isFooging() {
        return fooging;
    }

    public void setFooging(boolean fooging) {
        this.fooging = fooging;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public Area getDistrict() {
        return district;
    }

    public void setDistrict(Area district) {
        this.district = district;
    }

    public Area getMoh() {
        return moh;
    }

    public void setMoh(Area moh) {
        this.moh = moh;
    }
    
    
    
}
