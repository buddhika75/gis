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

/**
 *
 * @author User
 */
@Entity
public class Institution implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Enumerated(EnumType.STRING)
    InstitutionType type;
    String name;
    String code;
    @Lob
    String address;
    String telephone;
    String email;
    String fax;
    @ManyToOne
    Institution parentInstitution;
    @ManyToOne
    Area area;
    @ManyToOne
    Area educationalZone;
    @ManyToOne
    Area phiArea;
    @ManyToOne
    WebUser creater;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date createAt;

    
    int male_students;
    int female_students;

    int year_one_male_students;
    int year_four_male_students;
    int year_seven_male_students;
    int year_ten_male_students;
    int year_other_male_students;

    int year_one_female_students;
    int year_four_female_students;
    int year_seven_female_students;
    int year_ten_female_students;
    int year_other_female_students;

    boolean no_latrines;
    boolean inadequate_toilet_facilities;
    boolean no_drinking_water_supply;

    public Area getEducationalZone() {
        return educationalZone;
    }

    public void setEducationalZone(Area educationalZone) {
        this.educationalZone = educationalZone;
    }

    public Area getPhiArea() {
        return phiArea;
    }

    public void setPhiArea(Area phiArea) {
        this.phiArea = phiArea;
    }

    
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InstitutionType getType() {
        return type;
    }

    public void setType(InstitutionType type) {
        this.type = type;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public Institution getParentInstitution() {
        return parentInstitution;
    }

    public void setParentInstitution(Institution parentInstitution) {
        this.parentInstitution = parentInstitution;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public WebUser getCreater() {
        return creater;
    }

    public void setCreater(WebUser creater) {
        this.creater = creater;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public int getMale_students() {
        return male_students;
    }

    public void setMale_students(int male_students) {
        this.male_students = male_students;
    }

    public int getFemale_students() {
        return female_students;
    }

    public void setFemale_students(int female_students) {
        this.female_students = female_students;
    }

    public int getYear_one_male_students() {
        return year_one_male_students;
    }

    public void setYear_one_male_students(int year_one_male_students) {
        this.year_one_male_students = year_one_male_students;
    }

    public int getYear_four_male_students() {
        return year_four_male_students;
    }

    public void setYear_four_male_students(int year_four_male_students) {
        this.year_four_male_students = year_four_male_students;
    }

    public int getYear_seven_male_students() {
        return year_seven_male_students;
    }

    public void setYear_seven_male_students(int year_seven_male_students) {
        this.year_seven_male_students = year_seven_male_students;
    }

    public int getYear_ten_male_students() {
        return year_ten_male_students;
    }

    public void setYear_ten_male_students(int year_ten_male_students) {
        this.year_ten_male_students = year_ten_male_students;
    }

    public int getYear_other_male_students() {
        return year_other_male_students;
    }

    public void setYear_other_male_students(int year_other_male_students) {
        this.year_other_male_students = year_other_male_students;
    }

    public int getYear_one_female_students() {
        return year_one_female_students;
    }

    public void setYear_one_female_students(int year_one_female_students) {
        this.year_one_female_students = year_one_female_students;
    }

    public int getYear_four_female_students() {
        return year_four_female_students;
    }

    public void setYear_four_female_students(int year_four_female_students) {
        this.year_four_female_students = year_four_female_students;
    }

    public int getYear_seven_female_students() {
        return year_seven_female_students;
    }

    public void setYear_seven_female_students(int year_seven_female_students) {
        this.year_seven_female_students = year_seven_female_students;
    }

    public int getYear_ten_female_students() {
        return year_ten_female_students;
    }

    public void setYear_ten_female_students(int year_ten_female_students) {
        this.year_ten_female_students = year_ten_female_students;
    }

    public int getYear_other_female_students() {
        return year_other_female_students;
    }

    public void setYear_other_female_students(int year_other_female_students) {
        this.year_other_female_students = year_other_female_students;
    }

    public boolean isNo_latrines() {
        return no_latrines;
    }

    public void setNo_latrines(boolean no_latrines) {
        this.no_latrines = no_latrines;
    }

    public boolean isInadequate_toilet_facilities() {
        return inadequate_toilet_facilities;
    }

    public void setInadequate_toilet_facilities(boolean inadequate_toilet_facilities) {
        this.inadequate_toilet_facilities = inadequate_toilet_facilities;
    }

    public boolean isNo_drinking_water_supply() {
        return no_drinking_water_supply;
    }

    public void setNo_drinking_water_supply(boolean no_drinking_water_supply) {
        this.no_drinking_water_supply = no_drinking_water_supply;
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
        if (!(object instanceof Institution)) {
            return false;
        }
        Institution other = (Institution) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "lk.gov.health.schoolhealth.Institution[ id=" + id + " ]";
    }

}
