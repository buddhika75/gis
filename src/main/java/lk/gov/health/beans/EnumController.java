/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.gov.health.beans;

import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import lk.gov.health.dengue.AreaType;
import lk.gov.health.dengue.InstitutionType;
import lk.gov.health.dengue.ItemType;
import lk.gov.health.dengue.Month;
import lk.gov.health.dengue.PrivilegeType;
import lk.gov.health.dengue.Quarter;

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
    
    
    public PrivilegeType[] getPrivilegeTypes() {
        return PrivilegeType.values();
    }

    public InstitutionType[] getInstitutionTypes() {
        return InstitutionType.values();
    }

    public ItemType[] getItemTypes() {
        return ItemType.values();
    }
    
   

}
