/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.gov.health.beans;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import lk.gov.health.schoolhealth.AreaType;
import lk.gov.health.schoolhealth.InstitutionType;
import lk.gov.health.schoolhealth.ItemType;
import lk.gov.health.schoolhealth.Month;
import lk.gov.health.schoolhealth.PrivilegeType;
import lk.gov.health.schoolhealth.Quarter;
import lk.gov.health.schoolhealth.ReturnReceiveCategory;
import lk.gov.health.schoolhealth.ReturnTimeFrequency;

/**
 *
 * @author User
 */
@Named(value = "enumController")
@ApplicationScoped
public class EnumController {

    /**
     * Creates a new instance of EnumController
     */
    public EnumController() {
    }

    public Month[] getMonths() {
        return Month.values();
    }

    public Quarter[] getQuarters() {
        return Quarter.values();
    }

    public AreaType[] getAreaTypes() {
        return AreaType.values();
    }
    
    public ReturnReceiveCategory[] getReturnReceiveCategories(){
        return ReturnReceiveCategory.values();
    }

    public PrivilegeType[] getPrivilegeTypes() {
        return PrivilegeType.values();
    }

    public InstitutionType[] getInstitutionTypes() {
        return InstitutionType.values();
    }

    public ItemType[] getItemTypes() {
        return ItemType.values();
    }
    
    public ReturnTimeFrequency[] getReturnTimeFrequencys(){
        return ReturnTimeFrequency.values();
    }

}
